package com.supreme.spa.backend.vue.controller;


import com.supreme.spa.backend.vue.models.*;
import com.supreme.spa.backend.vue.services.MediaService;
import com.supreme.spa.backend.vue.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@EnableJdbcHttpSession
@CrossOrigin(origins = {"http://localhost:8080", "https://supreme-spa.firebaseapp.com"}, allowCredentials = "true")
public class UserController {

    private final UserService userService;
    private final MediaService mediaService;
    private final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserController(UserService userService,
                   MediaService mediaService) {
        this.userService = userService;
        this.mediaService = mediaService;
    }

    @PostMapping(path = "/create")
    public ResponseEntity createUser(@RequestBody Auth auth,
                                     HttpSession session) {
        Object sessionAttribute = session.getAttribute("user");
        if (sessionAttribute != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.ALREADY_AUTHENTICATED));
        }
        if (auth.getEmail() == null || auth.getUsername() == null
                || auth.getPassword() == null || auth.getConfirmPassword() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.WRONG_CREDENTIALS));
        }

        if (!auth.checkConfirm(auth.getPassword(), auth.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.WRONG_CREDENTIALS));
        }
        auth.saltHash();
        try {
            userService.createUser(auth);
        } catch (DuplicateKeyException error) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new Message(UserStatus.NOT_UNIQUE_USERNAME_OR_EMAIL));
        }
        sessionAuth(session, auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Message(UserStatus.SUCCESSFULLY_REGISTERED));
    }

    @PostMapping(path = "/auth")
    public ResponseEntity signIn(@RequestBody Auth auth,
                                 HttpSession session) {
        Object sessionAttribute = session.getAttribute("user");
        if (sessionAttribute != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.ALREADY_AUTHENTICATED));
        }
        if (auth.getEmail() == null || auth.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.WRONG_CREDENTIALS));
        }
        String existingUserPassword = userService.getAuthForCheck(auth.getEmail());
        if (existingUserPassword == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Message(UserStatus.NOT_FOUND));
        }

        if (!auth.checkPassword(existingUserPassword)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Message(UserStatus.WRONG_CREDENTIALS));
        }
        sessionAuth(session, auth);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new Message(UserStatus.SUCCESSFULLY_AUTHED));
    }

    @PostMapping(path = "/logout")
    public ResponseEntity logout(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
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
        User user = userService.getUser(userEmail);
        if (user == null) {
            Auth authUser = userService.getAuthByEmail(userEmail);
            return ResponseEntity.ok(authUser);
        }
        List<String> skills = userService.getSkillsByUserEmail(userEmail);
        List<String> genres = userService.getGenresByUserEmail(userEmail);
        if (skills.size() > 0) {
            String[] arraySkills = skills.toArray(new String[skills.size()]);
            user.setSkills(arraySkills);
        }
        if (genres.size() > 0) {
            String[] arrayGenres = genres.toArray(new String[genres.size()]);
            user.setGenres(arrayGenres);
        }
        try {
            String link = mediaService.getLink(user.getId());
            user.setImage(link);
        } catch (EmptyResultDataAccessException ignored) {}

        return ResponseEntity.ok(user);
    }

    @PostMapping(path = "/change")
    public ResponseEntity change(@RequestBody User user, HttpSession session) {
        Object sessionAttribute = session.getAttribute("user");
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
    public ResponseEntity listOfUsersWithParams(@PathVariable("page") int page,
                                                @RequestParam(value = "skill", required = false) ArrayList<String> skills,
                                                @RequestParam(value = "genre", required = false) ArrayList<String> genres) {
        List<TotalUserData> users;
        if (null == skills && null == genres) {
            users = userService.getListOfUsers(page);
        } else if (null != skills && null == genres) {
            users = userService.getUsersBySkills(page, skills);
        } else if (null == skills && null != genres) {
            users = userService.getUsersByGenres(page, genres);
        } else {
            users = userService.getUsersBySkillsAndGenres(page, skills, genres);
        }


        Map<String, Object> hashResp = new HashMap<>();
        hashResp.put("page", page + 1);
        hashResp.put("users", users);
        return ResponseEntity.ok(hashResp);
    }

    @GetMapping(path = "/usercard/{id}")
    public ResponseEntity userCard(@PathVariable("id") int id) {
        User existsUser = userService.getUserById(id);
        if (existsUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Message(UserStatus.NOT_FOUND));
        }

        List<String> skills = userService.getSkillsByUserEmail(existsUser.getEmail());
        List<String> genres = userService.getGenresByUserEmail(existsUser.getEmail());
        if (skills.size() > 0) {
            String[] arraySkills = skills.toArray(new String[skills.size()]);
            existsUser.setSkills(arraySkills);
        }
        if (genres.size() > 0) {
            String[] arrayGenres = genres.toArray(new String[genres.size()]);
            existsUser.setGenres(arrayGenres);
        }
        try {
            String link = mediaService.getLink(existsUser.getId());
            existsUser.setImage(link);
        } catch (EmptyResultDataAccessException ignored) {}
        return ResponseEntity.ok(existsUser);
    }

    private void sessionAuth(HttpSession session, Auth auth) {
        session.setAttribute("user", auth.getEmail());
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

    @GetMapping(path = "/skills")
    public ResponseEntity getAllSkills() {
        return ResponseEntity.ok(userService.getAllSkills());
    }

    @GetMapping(path = "/genres")
    public ResponseEntity getAllGenres() {
        return ResponseEntity.ok(userService.getAllGenres());
    }

    @PostMapping(path = "/add-comment")
    public ResponseEntity addComment(@RequestBody Comment comment, HttpSession session) {
        Object userSession = session.getAttribute("user");
        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(UserStatus.ACCESS_ERROR);
        }
        if (!userSession.toString().equals(comment.getFromEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(UserStatus.ACCESS_ERROR);
        }
        userService.addComment(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserStatus.SUCCESSFULLY_CREATED);
    }

    @GetMapping(path = "/get-comments/{userId}")
    public ResponseEntity getComments(@PathVariable(name = "userId") int userId) {
        List<Comment> commentList = userService.getCommentsByUserId(userId);
        return ResponseEntity.ok(commentList);
    }

    @GetMapping(path = "/messages/{userId}")
    public List<ChatMessage> getMessages(@PathVariable(name = "userId") int userId) {
        return userService.getMessagesById(userId);
    }
}
