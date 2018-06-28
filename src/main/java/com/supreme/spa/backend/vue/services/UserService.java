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

    public User getUser(User user) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try {
            return jdbc.queryForObject(sql, userMapper, user.getEmail());
        } catch (EmptyResultDataAccessException error) {
            return null;
        }
    }
    public int updateUserData(User user) {
        String sql = "UPDATE users SET phone = ?, about = ? WHERE LOWER(email) = LOWER(?)";
        try {
            jdbc.update(sql, user.getPhone(), user.getAbout(), user.getEmail());
            return 200;
        } catch (EmptyResultDataAccessException error) {
            return 404;
        }

    }

    private static final class UserMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet, int i) throws SQLException {
        final User user = new User();
        user.setEmail(resultSet.getString("email"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        return user;
    }
}
}
