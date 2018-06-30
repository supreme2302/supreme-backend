package com.supreme.spa.backend.vue.services;

import com.supreme.spa.backend.vue.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
@Transactional
public class UserService {
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
        }
    }

    public List<User> getListOfUsers(int page) {
        int limit = 10;
        int offset = (page - 1) * limit;
        String sql = "SELECT * FROM users WHERE onpage = TRUE ORDER BY username OFFSET ? ROWS LIMIT ?";
        try {
            return jdbc.query(sql, new Object[]{offset, limit}, userMapper);
        } catch (EmptyResultDataAccessException error) {
            return null;
        }
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

