package com.supreme.spa.backend.vue.controller;


import com.supreme.spa.backend.vue.models.Message;
import com.supreme.spa.backend.vue.models.User;
import com.supreme.spa.backend.vue.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/users")
@EnableJdbcHttpSession
@CrossOrigin(origins = {"http://localhost:8080"}, allowCredentials = "true")
public class UserController {
    private final UserService userService;

    @Autowired
    UserController(UserService userService) {
        this.userService = userService;
    }

    private enum UserStatus {
        SUCCESSFULLY_REGISTERED,
        SUCCESSFULLY_AUTHED,
        SUCCESSFULLY_LOGGED_OUT,
        SUCCESSFULLY_CHANGED,
        ACCESS_ERROR,
        WRONG_CREDENTIALS,
        NOT_UNIQUE_USERNAME_OR_EMAIL,
        ALREADY_AUTHENTICATED,
        UNEXPECTED_ERROR,
        NOT_FOUND,
        MAGIC
    }

    @PostMapping(path="/create")
    public ResponseEntity createUser(@RequestBody User user,
                                     HttpSession session) {
        Object sessionAttribute = session.getAttribute("user");
        if (sessionAttribute != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.ALREADY_AUTHENTICATED));
        }
        if ((!user.checkConfirm(user.getPassword(), user.getConfirmPassword()))
                || user.getEmail() == null || user.getUsername() == null
                || user.getPassword() == null || user.getConfirmPassword() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.WRONG_CREDENTIALS));
        }
        user.saltHash();
        try {
            userService.createUser(user);
        } catch (DuplicateKeyException error) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new Message(UserStatus.NOT_UNIQUE_USERNAME_OR_EMAIL));
        }
        sessionAuth(session, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Message(UserStatus.SUCCESSFULLY_REGISTERED));
    }
    @PostMapping(path = "/auth")
    public ResponseEntity signIn(@RequestBody User user,
                                 HttpSession session) {
        Object sessionAttribute = session.getAttribute("user");
        if (sessionAttribute != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.ALREADY_AUTHENTICATED));
        }
        User existsUser = userService.getUser(user);
        if (existsUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Message(UserStatus.NOT_FOUND));
        }
        if (!existsUser.checkPassword(user.getPassword())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.WRONG_CREDENTIALS));
        }
        sessionAuth(session, user);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new Message(UserStatus.SUCCESSFULLY_AUTHED));
    }

    @PostMapping(path = "/logout")
    public ResponseEntity logout(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.ACCESS_ERROR));
        }
        session.invalidate();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new Message(UserStatus.SUCCESSFULLY_LOGGED_OUT));
    }

    @PostMapping(path = "/change")
    public ResponseEntity change(@RequestBody User user,
                                 HttpSession session) {
        Object sessionAttribute = session.getAttribute("user");
        user.setEmail((String)sessionAttribute);
        if (sessionAttribute == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.ACCESS_ERROR));
        }

        int result = userService.updateUserData(user);
        if (result == 404) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.MAGIC));
        }
        return ResponseEntity.ok(new Message(UserStatus.SUCCESSFULLY_CHANGED));

    }

    private void sessionAuth(HttpSession session, User user) {
        session.setAttribute("user", user.getEmail());
        session.setMaxInactiveInterval(60 * 60);
    }
}
