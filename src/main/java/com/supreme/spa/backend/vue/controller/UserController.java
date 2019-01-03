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
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@EnableJdbcHttpSession
@CrossOrigin(origins = {"http://localhost:8080", "https://supreme-spa.firebaseapp.com"}, allowCredentials = "true")
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
        NOT_UNIQUE_PHONE,
        ALREADY_AUTHENTICATED,
        UNEXPECTED_ERROR,
        NOT_FOUND,
        MAGIC
    }

    @PostMapping(path = "/create")
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
        User existsUser = userService.getUserForCheck(user.getEmail());
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

    @GetMapping(path = "/info")
    public ResponseEntity userInfo(HttpSession session) {
        Object sessionAttr = session.getAttribute("user");
        String userEmail = (String) sessionAttr;
        if (sessionAttr == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Message(UserStatus.ACCESS_ERROR));
        }
        return ResponseEntity.ok(userService.getUser(userEmail));
    }

    @PostMapping(path = "/change")
    public ResponseEntity change(@RequestBody User user, HttpSession session) {
        Object sessionAttribute = session.getAttribute("user");
        user.setEmail((String) sessionAttribute);
        if (sessionAttribute == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.ACCESS_ERROR));
        }

        int result = userService.updateUserData(user);
        if (result == 404) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.MAGIC));
        }
        if (result == 409) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new Message(UserStatus.NOT_UNIQUE_PHONE));
        }
        return ResponseEntity.ok(new Message(UserStatus.SUCCESSFULLY_CHANGED));

    }

    @GetMapping(path = "/list/{page}")
    public ResponseEntity listOfUsers(@PathVariable("page") int page) {
        List<User> users = userService.getListOfUsers(page);
        if (users == null || users.size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(UserStatus.NOT_FOUND));
        }
        Map<String, Object> hashResp = new HashMap<>();
        hashResp.put("page", page + 1);
        hashResp.put("users", users);
        return ResponseEntity.status(HttpStatus.OK)
                .body(hashResp);
    }

    @GetMapping(path = "/usercard/{id}")
    public ResponseEntity userCard(@PathVariable("id") int id) {
        User existsUser = userService.getUserById(id);
        if (existsUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Message(UserStatus.NOT_FOUND));
        }
        return ResponseEntity.ok(existsUser);
    }

    private void sessionAuth(HttpSession session, User user) {
        session.setAttribute("user", user.getEmail());
        session.setMaxInactiveInterval(60 * 60);
    }

    /**
     * Function to change avatar.
     *
     * @param file    file of avatar to change
     * @param session session to use
     * @return response
     */
    @PostMapping("/chava")
    public ResponseEntity changeAva(@RequestParam("image") MultipartFile file,
                                    HttpSession session) {
        System.out.println("chava");
        if (session.getAttribute("user") == null) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new Message(UserStatus.NOT_FOUND));
        }
        try {
            userService.store(file, session.getAttribute("user").toString());
        } catch (IOException except) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new Message(UserStatus.UNEXPECTED_ERROR));
        }
        return ResponseEntity.status(HttpStatus.OK).body(
                new Message(UserStatus.SUCCESSFULLY_CHANGED));
    }

    /**
     * Function to get avatar.
     *
     * @param session session to check
     * @return response(image)
     */
    @GetMapping("/gava/{email}")
    public ResponseEntity getAva(HttpSession session,
                                 @PathVariable(name = "email") String email) {
        System.out.println("gava");
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        final BufferedImage file;
        try {
//            Object userSession = session.getAttribute("user");
//            if (userSession == null) {
//                BufferedImage avatar = ImageIO.read(new File(PATH_AVATARS_FOLDER + image));
//            }
            file = userService.loadAvatar(email);
            ImageIO.write(file, "png", bao);
        } catch (IOException exc) {
            exc.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                    new Message(UserStatus.UNEXPECTED_ERROR));
        }

        return ResponseEntity.ok(bao.toByteArray());
    }
}

