package com.supreme.spa.backend.vue;
import com.google.gson.Gson;
import com.supreme.spa.backend.vue.controller.UserController;
import com.supreme.spa.backend.vue.models.Auth;
import com.supreme.spa.backend.vue.models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
public class VueApplicationTests {

    @Autowired
    private UserController userController;

    @MockBean
    private PasswordEncoder passwordEncoder;

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
    public void logoutWithoutSessionTest() throws Exception {
        //todo httpSession
        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.post("/users/logout").content("");
        this.mockMvc.perform(builder).andExpect(status().isUnauthorized());
    }

    @Test
    public void infoOkTest() throws Exception {
        //todo httpSession
        Auth auth = new Auth();
        auth.setEmail("exist@e.ru");
        auth.setPassword("123");
        Cookie[] allCookies = this.mockMvc.perform(post("/users/auth")
                .contentType(contentType)
                .content(gson.toJson(auth))).andReturn().getResponse().getCookies();
        this.mockMvc.perform(get("/users/info")
                .contentType(contentType)
                .cookie(allCookies)
        )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Sql(value = {"/test-set-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void changeOkTest() throws Exception {
        User user = new User();
//        user.set
//        this.mockMvc.perform(post("/users/auth")
//                .contentType(contentType)
//                .content(gson.toJson(auth)))
//                .andDo(print())
//                .andExpect(status().isNotFound());
    }
}
