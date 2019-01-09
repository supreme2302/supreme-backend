package com.supreme.spa.backend.vue.services;

import com.supreme.spa.backend.vue.models.Auth;
import com.supreme.spa.backend.vue.models.ChatMessage;
import com.supreme.spa.backend.vue.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
@Transactional
public class UserService {
    public static final String PATH_AVATARS_FOLDER = Paths.get("uploads")
            .toAbsolutePath().toString() + '/';

    private JdbcTemplate jdbc;
    private static final UserMapper userMapper = new UserMapper();
    private static final AuthMapper authMapper = new AuthMapper();
    private static final MessageMapper messageMapper = new MessageMapper();

    @Autowired
    public UserService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void createUser(Auth auth) {
        String sql = "INSERT INTO auth (username, email, password) VALUES (?, ?, ?) RETURNING id";
        String sqlProfile = "INSERT INTO profile(user_id) VALUES (?)";
        Integer id = jdbc.queryForObject(sql, Integer.class, auth.getUsername(), auth.getEmail(), auth.getPassword());
        jdbc.update(sqlProfile, id);
    }

    public String getAuthForCheck(String email) {
        String sql = "SELECT password FROM auth WHERE LOWER(email) = LOWER(?)";
        try {
            return jdbc.queryForObject(sql, String.class, email);
        } catch (EmptyResultDataAccessException error) {
            return null;
        }
    }

    public Auth getAuthByEmail(String email) {
        String sql = "SELECT * FROM auth WHERE LOWER(email) = LOWER(?)";
        try {
            return jdbc.queryForObject(sql, authMapper, email);
        } catch (EmptyResultDataAccessException error) {
            return null;
        }
    }

    public User getUser(String email) {
        String sql = "select auth.id, auth.email, auth.username, profile.phone, profile.about, profile.onpage from auth\n" +
                "join profile on auth.id = profile.user_id\n" +
                "where email = ?";
        try {
            return jdbc.queryForObject(sql, userMapper, email);
        } catch (EmptyResultDataAccessException error) {
            return null;
        }
    }

    public User getUserById(int id) {
        String sql = "select auth.id, auth.email, auth.username, profile.phone, profile.about, profile.onpage from auth\n" +
                "join profile on auth.id = profile.user_id\n" +
                "where auth.id = ?";
        try {
            return jdbc.queryForObject(sql, userMapper, id);
        } catch (EmptyResultDataAccessException error) {
            return null;
        }
    }


    public int updateUserData(User user) {
        String sqlForUpdateProfile = "UPDATE profile SET phone = ?, about = ?, onpage = ? WHERE user_id = ?";
        try {
            jdbc.update(sqlForUpdateProfile, user.getPhone(), user.getAbout(), user.getOnpage(), user.getId());
            updateUserSkills(user.getId(), user.getSkills());
            return 200;
        } catch (EmptyResultDataAccessException error) {
            return 404;
        } catch (DuplicateKeyException error) {
            return 409;
        }
    }

    private void updateUserSkills(int userId, String[] skills) {
        for (String name: skills) {
            Integer skillId = getSkillIdByName(name);
            Integer profileId = getProfileIdByUserId(userId);
            if (skillId == null) {
                String sql = "INSERT INTO skill(skill_name) VALUES (?) RETURNING id";
                skillId = jdbc.queryForObject(sql, Integer.class, name);
            }
            String sql = "INSERT INTO profile_skill(profile_id, skill_id) VALUES (?, ?)";
            jdbc.update(sql, profileId, skillId);
        }
    }

    private Integer getSkillIdByName(String name) {
        String sql = "SELECT id FROM skill WHERE skill_name = ?";
        try {
            return jdbc.queryForObject(sql, Integer.class, name);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Integer getProfileIdByUserId(int id) {
        String sql = "SELECT id FROM profile WHERE user_id = ?";
        try {
            return jdbc.queryForObject(sql, Integer.class, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<String> getSkillsByUserEmail(String email) {
        String sql = "select skill_name from profile\n" +
                "join profile_skill ps on profile.id = ps.profile_id\n" +
                "join skill s on ps.skill_id = s.id\n" +
                "join auth a on profile.user_id = a.id\n" +
                "where email = ?";
        return jdbc.query(sql, ((resultSet, i) -> resultSet.getString("skill_name")), email);
    }

    public List<Auth> getListOfUsers(int page) {
        int limit = 15;
        int offset = (page - 1) * limit;
        String sql = "SELECT auth.id, email, username FROM auth "
                + "JOIN profile p on auth.id = p.user_id "
                + "WHERE p.onpage = TRUE "
                + "ORDER BY username OFFSET ? ROWS LIMIT ?";
        try {
            return jdbc.query(sql, new Object[]{offset, limit}, authMapper);
        } catch (EmptyResultDataAccessException error) {
            return null;
        }
    }

    /**
     * Function to save image on PC and db.
     *
     * @param file image to save
     * @param user user to avatar
     * @throws IOException if there is error(Handled in controller)
     */
    public void store(MultipartFile file, String user) throws IOException {
        File tosave = new File(PATH_AVATARS_FOLDER + user + "a.jpg");
        file.transferTo(tosave);
        String sql = "UPDATE profile SET avatar = ? FROM auth WHERE profile.user_id = auth.id AND auth.email = ?";
        jdbc.update(sql, user + "a.jpg", user);
    }

    /**
     * load.
     *
     * @param user is username
     * @return avatar
     */

    public BufferedImage loadAvatar(String user) throws IOException {
        String image = jdbc.queryForObject(
                "SELECT avatar FROM profile JOIN auth a on profile.user_id = a.id WHERE a.email = ?",
                String.class, user
        );
        BufferedImage avatar = ImageIO.read(new File(PATH_AVATARS_FOLDER + image));
        return avatar;
    }

    public void addMessage(ChatMessage message) {
        String sql = "INSERT INTO message(content, recipient, sender, message_date) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, message.getContent(), message.getRecipient(), message.getSender(), message.getDate());
    }

    public List<ChatMessage> getMessagesByEmails(String sender, String recipient) {
        String sql = "SELECT * FROM message WHERE (sender = ? AND recipient = ?) "
                + "OR (recipient = ? and sender = ?) ORDER BY message_date";
        return jdbc.query(sql, messageMapper, sender, recipient, sender, recipient);
    }

    private static final class UserMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            final User user = new User();
            user.setId(resultSet.getInt("id"));
            user.setEmail(resultSet.getString("email"));
            user.setUsername(resultSet.getString("username"));
            user.setPhone(resultSet.getString("phone"));
            user.setAbout(resultSet.getString("about"));
            user.setOnpage(resultSet.getBoolean("onpage"));
            return user;
        }
    }

    private static final class AuthMapper implements RowMapper<Auth> {
        @Override
        public Auth mapRow(ResultSet resultSet, int i) throws SQLException {
            Auth auth = new Auth();
            auth.setId(resultSet.getInt("id"));
            auth.setEmail(resultSet.getString("email"));
            auth.setUsername(resultSet.getString("username"));
            return auth;
        }
    }

    private static final class MessageMapper implements RowMapper<ChatMessage> {
        @Override
        public ChatMessage mapRow(ResultSet resultSet, int i) throws SQLException {
            ChatMessage message = new ChatMessage();
            message.setId(resultSet.getInt("id"));
            message.setRecipient(resultSet.getString("recipient"));
            message.setSender(resultSet.getString("sender"));
            message.setContent(resultSet.getString("content"));
            message.setDate(resultSet.getTimestamp("message_date"));
            return message;
        }
    }
}

