package com.supreme.spa.backend.vue.services;

import com.supreme.spa.backend.vue.models.*;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    public static final String PATH_AVATARS_FOLDER = Paths.get("uploads")
            .toAbsolutePath().toString() + '/';

    private JdbcTemplate jdbc;
    private static final UserMapper userMapper = new UserMapper();
    private static final AuthMapper authMapper = new AuthMapper();
    private static final MessageMapper messageMapper = new MessageMapper();
    private static final TotalUserDataMapper totalUserDataMapper = new TotalUserDataMapper();
    private static final CommentMapper commentMapper = new CommentMapper();

    @Autowired
    public UserService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void createUser(Auth auth) {
        String sql = "INSERT INTO auth (username, email, password) VALUES (?, ?, ?) RETURNING id";
        String sqlProfile = "INSERT INTO profile(user_id) VALUES (?)";
        String sqlCommentCounter = "INSERT INTO comment_counter(user_id) VALUES (?)";
        Integer id = jdbc.queryForObject(sql, Integer.class, auth.getUsername(), auth.getEmail(), auth.getPassword());
        jdbc.update(sqlProfile, id);
        jdbc.update(sqlCommentCounter, id);
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

    @Transactional
    public int updateUserData(User user) {
        String sqlForUpdateProfile = "UPDATE profile SET phone = ?, about = ?, onpage = ? WHERE user_id = ?";
        try {
            jdbc.update(sqlForUpdateProfile, user.getPhone(), user.getAbout(), user.getOnpage(), user.getId());
            updateUserSkills(user.getId(), user.getSkills());
            updateUserGenres(user.getId(), user.getGenres());
            return 200;
        } catch (EmptyResultDataAccessException error) {
            return 404;
        } catch (DuplicateKeyException error) {
            return 409;
        }
    }

    @Transactional
    void updateUserSkills(int userId, String[] skills) {
        Integer profileId = getProfileIdByUserId(userId);
        String deleteSql = "DELETE FROM profile_skill WHERE profile_id = ?";
        jdbc.update(deleteSql, profileId);
        for (String name : skills) {
            Integer skillId = getSkillIdByName(name);
            if (skillId == null) {
                String sql = "INSERT INTO skill(skill_name) VALUES (?) RETURNING id";
                skillId = jdbc.queryForObject(sql, Integer.class, name);
            }
            String sql = "INSERT INTO profile_skill(profile_id, skill_id) VALUES (?, ?)";
            jdbc.update(sql, profileId, skillId);
        }
    }

    @Transactional
    void updateUserGenres(int userId, String[] genres) {
        Integer profileId = getProfileIdByUserId(userId);
        String deleteSql = "DELETE FROM profile_genre WHERE profile_id = ?";
        jdbc.update(deleteSql, profileId);
        for (String name: genres) {
            Integer genreId = getGenreByName(name);
            if (null == genreId) {
                String sql = "INSERT INTO genre(genre_name) VALUES (?) RETURNING id";
                genreId = jdbc.queryForObject(sql, Integer.class, name);
            }
            String sql = "INSERT INTO profile_genre(profile_id, genre_id) VALUES (?, ?)";
            jdbc.update(sql, profileId, genreId);
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

    private Integer getGenreByName(String name) {
        String sql = "SELECT id FROM genre WHERE genre_name = ?";
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

    public List<TotalUserData> getListOfUsers(int page) {
        int limit = 6;
        int offset = (page - 1) * limit;
        String sql = "SELECT auth.id, email, username, about FROM auth "
                + "JOIN profile p on auth.id = p.user_id "
                + "WHERE p.onpage = TRUE "
                + "ORDER BY username OFFSET ? ROWS LIMIT ?";
        try {
            return jdbc.query(sql, new Object[]{offset, limit}, totalUserDataMapper);
        } catch (EmptyResultDataAccessException error) {
            return null;
        }
    }

    public List<TotalUserData> getUsersBySkills(int page, ArrayList<String> skills) {
        int limit = 6;
        int offset = (page - 1) * limit;
        StringBuilder sqlBuilder = new StringBuilder();
        List<Object> list = new ArrayList<>();
        sqlBuilder.append("select distinct auth.id, fromUsername, email, about from auth\n" +
                "join profile p on auth.id = p.user_id\n" +
                "join profile_skill skill on p.id = skill.profile_id\n" +
                "join skill s on skill.skill_id = s.id ");

        sqlBuilder.append(" where ");
        for (int i = 0; i < skills.size(); ++i) {
            sqlBuilder.append((i != 0) ? " or " : "").append(" s.skill_name::citext = ?::citext ");
            list.add(skills.get(i).toLowerCase());
        }
        sqlBuilder.append(" and p.onpage = true ");
        sqlBuilder.append("order by fromUsername offset ? rows limit ?");
        list.add(offset);
        list.add(limit);
        return jdbc.query(sqlBuilder.toString(), list.toArray(), totalUserDataMapper);
    }

    public List<TotalUserData> getUsersByGenres(int page, ArrayList<String> genres) {
        int limit = 6;
        int offset = (page - 1) * limit;
        StringBuilder sqlBuilder = new StringBuilder();
        List<Object> list = new ArrayList<>();
        sqlBuilder.append("select distinct auth.id, fromUsername, email, about from auth\n" +
                "join profile p on auth.id = p.user_id\n" +
                "join profile_genre pg on p.id = pg.profile_id\n" +
                "join genre g on pg.genre_id = g.id");

        sqlBuilder.append(" where ");
        for (int i = 0; i < genres.size(); ++i) {
            sqlBuilder.append((i != 0) ? " or " : "").append(" g.genre_name::citext = ?::citext ");
            list.add(genres.get(i).toLowerCase());
        }
        sqlBuilder.append(" and p.onpage = true ");
        sqlBuilder.append("order by fromUsername offset ? rows limit ?");
        list.add(offset);
        list.add(limit);
        return jdbc.query(sqlBuilder.toString(), list.toArray(), totalUserDataMapper);
    }

    public List<TotalUserData> getUsersBySkillsAndGenres(int page, ArrayList<String> skills, ArrayList<String> genres) {
        int limit = 6;
        int offset = (page - 1) * limit;
        StringBuilder sqlBuilder = new StringBuilder();
        List<Object> list = new ArrayList<>();
        sqlBuilder.append("select distinct auth.id, fromUsername, email, about from auth\n" +
                "join profile p on auth.id = p.user_id\n" +
                "join profile_skill skill on p.id = skill.profile_id\n" +
                "join skill s on skill.skill_id = s.id\n" +
                "join profile_genre pg on p.id = pg.profile_id\n" +
                "join genre g on pg.genre_id = g.id");

        sqlBuilder.append(" where ");
        for (int i = 0; i < skills.size(); ++i) {
            sqlBuilder.append((i != 0) ? " or " : "").append(" s.skill_name::citext = ?::citext ");
            list.add(skills.get(i).toLowerCase());
        }
        sqlBuilder.append(" and ");
        for (int i = 0; i < genres.size(); ++i) {
            sqlBuilder.append((i != 0) ? " or " : "").append(" g.genre_name::citext = ?::citext ");
            list.add(genres.get(i).toLowerCase());
        }
        sqlBuilder.append(" and p.onpage = true ");
        sqlBuilder.append("order by fromUsername offset ? rows limit ?");
        list.add(offset);
        list.add(limit);
        return jdbc.query(sqlBuilder.toString(), list.toArray(), totalUserDataMapper);
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
     * @param user is fromUsername
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

    public List<String> getAllSkills() {
        String sql = "SELECT * FROM skill";
        return jdbc.query(sql, (resultSet, i) -> resultSet.getString("skill_name"));
    }

    public List<String> getAllGenres() {
        String sql = "SELECT * FROM genre";
        return jdbc.query(sql, (resultSet, i) -> resultSet.getString("genre_name"));
    }

    public List<String> getGenresByUserEmail(String userEmail) {
        String sql = "select genre_name from profile\n" +
                "join profile_genre pg on profile.id = pg.profile_id\n" +
                "join genre g on pg.genre_id = g.id\n" +
                "join auth a on profile.user_id = a.id\n" +
                "where email = ?";
        return jdbc.query(sql, ((resultSet, i) -> resultSet.getString("genre_name")), userEmail);
    }

    @Transactional
    public void addComment(Comment comment) {
        String sqlForInsertIntoComment = "INSERT INTO "
                + "comment(to_user_id, from_username, from_email, comment_val, rating) VALUES (?, ?, ?, ?, ?)";
        String sqlForInsertIntoCommentsCounter = "UPDATE comment_counter SET counter = counter + 1, "
                + "sum_rating = sum_rating + ? WHERE user_id = ?";
        String sqlForUpdateProfile = "UPDATE profile SET rating = "
                + "(SELECT sum_rating / counter  FROM comment_counter WHERE comment_counter.user_id = ?) "
                + "WHERE user_id = ?";
        jdbc.update(sqlForInsertIntoComment, comment.getToUserId(), comment.getFromUsername(),
                comment.getFromEmail(), comment.getCommentVal(), comment.getRating());
        jdbc.update(sqlForInsertIntoCommentsCounter, comment.getRating(), comment.getToUserId());
        jdbc.update(sqlForUpdateProfile, comment.getToUserId(), comment.getToUserId());
    }

    public List<Comment> getCommentsByUserId(int userId) {
        String sql = "SELECT * from comment where to_user_id = ?";
        return jdbc.query(sql, commentMapper, userId);
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
            auth.setUsername(resultSet.getString("fromUsername"));
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

    private static final class TotalUserDataMapper implements RowMapper<TotalUserData> {
        @Override
        public TotalUserData mapRow(ResultSet resultSet, int i) throws SQLException {
            TotalUserData totalUserData = new TotalUserData();
            totalUserData.setId(resultSet.getInt("id"));
            totalUserData.setEmail(resultSet.getString("email"));
            totalUserData.setUsername(resultSet.getString("fromUsername"));
            totalUserData.setAbout(resultSet.getString("about"));
            return totalUserData;
        }
    }

    private static final class CommentMapper implements RowMapper<Comment> {
        @Override
        public Comment mapRow(ResultSet resultSet, int i) throws SQLException {
            Comment comment = new Comment();
            comment.setId(resultSet.getInt("id"));
            comment.setToUserId(resultSet.getInt("to_user_id"));
            comment.setFromUsername(resultSet.getString("from_username"));
            comment.setFromEmail(resultSet.getString("from_email"));
            comment.setCommentVal(resultSet.getString("comment_val"));
            comment.setRating(resultSet.getInt("rating"));
            return comment;
        }
    }
}

