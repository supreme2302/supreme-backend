package com.supreme.spa.backend.vue.services;

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

    @Autowired
    public UserService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void createUser(User user) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        jdbc.update(sql, user.getUsername(), user.getEmail(), user.getPassword());
    }

    public User getUserForCheck(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try {
            return jdbc.queryForObject(sql, (resultSet, i) -> new User(
                    resultSet.getString("password")
            ), email);
        } catch (EmptyResultDataAccessException error) {
            return null;
        }
    }

    public User getUser(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try {
            return jdbc.queryForObject(sql, userMapper, email);
        } catch (EmptyResultDataAccessException error) {
            return null;
        }
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbc.queryForObject(sql, userMapper, id);
        } catch (EmptyResultDataAccessException error) {
            return null;
        }
    }


    public int updateUserData(User user) {
        String sql = "UPDATE users SET phone = ?, skills = ?, about = ?, onpage = ? WHERE LOWER(email) = LOWER(?)";
        try {
            jdbc.update(sql, user.getPhone(), user.getSkills(), user.getAbout(), user.getOnpage(), user.getEmail());
            return 200;
        } catch (EmptyResultDataAccessException error) {
            return 404;
        } catch (DuplicateKeyException error) {
            return 409;
        }
    }

    public List<User> getListOfUsers(int page) {
        int limit = 15;
        int offset = (page - 1) * limit;
        String sql = "SELECT * FROM users WHERE onpage = TRUE ORDER BY username OFFSET ? ROWS LIMIT ?";
        try {
            return jdbc.query(sql, new Object[]{offset, limit}, userMapper);
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
        String sql = "UPDATE \"users\" SET avatar=? WHERE email=(?)::citext;";
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
                "SELECT avatar FROM \"users\" "
                        + "WHERE email = ? LIMIT 1;",
                String.class, user
        );
        BufferedImage avatar = ImageIO.read(new File(PATH_AVATARS_FOLDER + image));
        return avatar;
    }

    private static final class UserMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            final User user = new User();
            user.setId(resultSet.getInt("id"));
            user.setEmail(resultSet.getString("email"));
            user.setUsername(resultSet.getString("username"));
            user.setPhone(resultSet.getString("phone"));
            user.setSkills(resultSet.getString("skills"));
            user.setAbout(resultSet.getString("about"));
            user.setOnpage(resultSet.getBoolean("onpage"));
            return user;
        }
    }
}

