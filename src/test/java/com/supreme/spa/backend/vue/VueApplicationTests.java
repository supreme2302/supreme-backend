package com.supreme.spa.backend.vue;
import com.google.gson.Gson;
import com.supreme.spa.backend.vue.models.Auth;
import com.supreme.spa.backend.vue.models.Comment;
import com.supreme.spa.backend.vue.models.User;
import com.supreme.spa.backend.vue.resource.LocalStorage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.Cookie;
import java.nio.charset.Charset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
public class VueApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private Gson gson = new Gson();

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));


    @Autowired
    private WebApplicationContext wac;


    @Before
    public void setUp() throws Exception {

//        mockSession = new MockHttpSession(wac.getServletContext(), UUID.randomUUID().toString());
    }


    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void registrationOkTest() throws Exception {
        Auth auth = new Auth();
        auth.setEmail("test@s.ru");
        auth.setUsername("test");
        auth.setPassword("123");
        auth.setConfirmPassword("123");
        String authJSON = gson.toJson(auth);
        this.mockMvc.perform(post("/users/create")
                .contentType(contentType)
                .content(authJSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void registrationFailedConflictTest() throws Exception {
        Auth auth = new Auth();
        auth.setEmail("exist@e.ru");
        auth.setUsername("exist");
        auth.setPassword("123");
        auth.setConfirmPassword("123");
        String authJSON = gson.toJson(auth);
        this.mockMvc.perform(post("/users/create")
                .contentType(contentType)
                .content(authJSON))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void signInOkTest() throws Exception {
        Auth auth = new Auth();
        auth.setEmail("exist@e.ru");
        auth.setPassword("123");
        this.mockMvc.perform(post("/users/auth")
                .contentType(contentType)
                .content(gson.toJson(auth)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void signInWrongCredentialsTest() throws Exception {
        Auth auth = new Auth();
        auth.setEmail("exist@e.ru");
        auth.setPassword("1234");
        this.mockMvc.perform(post("/users/auth")
                .contentType(contentType)
                .content(gson.toJson(auth)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void signInNotFoundUserTest() throws Exception {
        Auth auth = new Auth();
        auth.setEmail("notFoundUser@e.ru");
        auth.setPassword("123");
        this.mockMvc.perform(post("/users/auth")
                .contentType(contentType)
                .content(gson.toJson(auth)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void logoutWithoutSessionTest() throws Exception {
        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.post("/users/logout").content("");
        this.mockMvc.perform(builder).andExpect(status().isUnauthorized());
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void logoutTestOk() throws Exception {
        Auth auth = new Auth();
        auth.setEmail("exist@e.ru");
        auth.setPassword("123");
        Cookie[] allCookies = this.mockMvc.perform(post("/users/auth")
                .contentType(contentType)
                .content(gson.toJson(auth))).andReturn().getResponse().getCookies();
        this.mockMvc.perform(post("/users/logout")
                .contentType(contentType)
                .cookie(allCookies)
        )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void infoWithAccessTest() throws Exception {
        this.mockMvc.perform(get("/users/info")
                .contentType(contentType)
        )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void infoWithEmptyProfileOkTest() throws Exception {
        Auth auth = new Auth();
        auth.setEmail("exist2@e.ru");
        auth.setUsername("existOneMore");
        auth.setPassword("123");
        auth.setConfirmPassword("123");
        Cookie[] allCookies = this.mockMvc.perform(post("/users/create")
                .contentType(contentType)
                .content(gson.toJson(auth))).andReturn().getResponse().getCookies();
        this.mockMvc.perform(get("/users/info")
                .contentType(contentType)
                .cookie(allCookies)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(
                        LocalStorage.emptyProfile
                ));
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void infoWithFullProfileOkTest() throws Exception {
        Auth auth = new Auth();
        auth.setEmail("exist3@e.ru");
        auth.setUsername("userWithProfileAndSkillsAndGenres");
        auth.setPassword("123");
        auth.setConfirmPassword("123");
        Cookie[] allCookies = this.mockMvc.perform(post("/users/auth")
                .contentType(contentType)
                .content(gson.toJson(auth))).andReturn().getResponse().getCookies();
        this.mockMvc.perform(get("/users/info")
                .contentType(contentType)
                .cookie(allCookies)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(
                        LocalStorage.fullProfile
                ));
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void changeOkTest() throws Exception {
        Auth auth = new Auth();
        auth.setEmail("exist3@e.ru");
        auth.setPassword("123");
        Cookie[] allCookies = this.mockMvc.perform(post("/users/auth")
                .contentType(contentType)
                .content(gson.toJson(auth))).andReturn().getResponse().getCookies();
        User user = new User();
        user.setAbout("i am user");
        user.setPhone("0000000000");
        String[] skills = {"guitar", "keyboards"};
        String[] genres = {"pop", "metal"};
        user.setSkills(skills);
        user.setGenres(genres);
        user.setId(2);
        this.mockMvc.perform(post("/users/change")
                .content(gson.toJson(user))
                .contentType(contentType)
                .cookie(allCookies)
        )
                .andDo(print())
                .andExpect(status().isOk());
        this.mockMvc.perform(get("/users/info")
                .contentType(contentType)
                .cookie(allCookies)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(
                        LocalStorage.changedProfile
                ));
    }


    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void userCardTestOk() throws Exception {
        Auth auth = new Auth();
        auth.setEmail("exist3@e.ru");
        auth.setUsername("userWithProfileAndSkillsAndGenres");
        auth.setPassword("123");
        auth.setConfirmPassword("123");
        Cookie[] allCookies = this.mockMvc.perform(post("/users/auth")
                .contentType(contentType)
                .content(gson.toJson(auth))).andReturn().getResponse().getCookies();
        this.mockMvc.perform(get("/users/usercard/2")
                .cookie(allCookies)
                .contentType(contentType)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(LocalStorage.fullProfile));
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void addCommentTestOk() throws Exception {
        Auth auth = new Auth();
        auth.setEmail("exist3@e.ru");
        auth.setUsername("userWithProfileAndSkillsAndGenres");
        auth.setPassword("123");
        auth.setConfirmPassword("123");
        Cookie[] allCookies = this.mockMvc.perform(post("/users/auth")
                .contentType(contentType)
                .content(gson.toJson(auth))).andReturn().getResponse().getCookies();
        Comment comment = new Comment();
        comment.setToUserId(1);
        comment.setFromUsername("userWithProfileAndSkillsAndGenres");
        comment.setFromEmail("exist3@e.ru");
        comment.setRating(5);
        comment.setCommentVal("Not bad");
        this.mockMvc.perform(post("/users/add-comment")
                .cookie(allCookies)
                .contentType(contentType)
                .content(gson.toJson(comment))
        )
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void addCommentByWrongUserTestForbidden() throws Exception {
        Auth auth = new Auth();
        auth.setEmail("exist3@e.ru");
        auth.setUsername("userWithProfileAndSkillsAndGenres");
        auth.setPassword("123");
        auth.setConfirmPassword("123");
        Cookie[] allCookies = this.mockMvc.perform(post("/users/auth")
                .contentType(contentType)
                .content(gson.toJson(auth))).andReturn().getResponse().getCookies();
        Comment comment = new Comment();
        comment.setToUserId(1);
        comment.setFromEmail("wrongUser@e.ru");
        this.mockMvc.perform(post("/users/add-comment")
                .cookie(allCookies)
                .contentType(contentType)
                .content(gson.toJson(comment))
        )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void getCommentTestOk() throws Exception {
        for (int i = 0; i < 2; ++i) {
            Auth auth = new Auth();
            auth.setEmail("userForComment" + String.valueOf(i) + "@e.ru");
            auth.setUsername("userForComment" + String.valueOf(i));
            auth.setPassword("123");
            auth.setConfirmPassword("123");
            Cookie[] allCookies = this.mockMvc.perform(post("/users/create")
                    .contentType(contentType)
                    .content(gson.toJson(auth))).andReturn().getResponse().getCookies();
            Comment comment = new Comment();
            comment.setToUserId(1);
            comment.setFromUsername("userForComment" + String.valueOf(i));
            comment.setFromEmail("userForComment" + String.valueOf(i) + "@e.ru");
            comment.setRating(4 + i);
            comment.setCommentVal("Not bad - " + String.valueOf(i));
            this.mockMvc.perform(post("/users/add-comment")
                    .cookie(allCookies)
                    .contentType(contentType)
                    .content(gson.toJson(comment))
            );
        }
        this.mockMvc.perform(get("/users/get-comments/1")
                .contentType(contentType)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(LocalStorage.commentList));
    }
}
