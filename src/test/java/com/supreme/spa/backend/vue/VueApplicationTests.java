package com.supreme.spa.backend.vue;

import com.google.gson.Gson;
import com.supreme.spa.backend.vue.models.Auth;
import com.supreme.spa.backend.vue.services.UserService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
public class VueApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final HttpHeaders REQUEST_HEADERS = new HttpHeaders();

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private Gson gson = new Gson();

    public VueApplicationTests() {

        final List<String> origin = Collections.singletonList("http://localhost:5002");
        REQUEST_HEADERS.put(HttpHeaders.ORIGIN, origin);
        final List<String> contentType = Collections.singletonList("application/json");
        REQUEST_HEADERS.put(HttpHeaders.CONTENT_TYPE, contentType);
    }


    @Test
    public void registration() throws Exception {

        ResultMatcher ok = status().isOk();
        Auth testAuth = new Auth();
        testAuth.setUsername("testAuthUser");
        testAuth.setEmail("test@t.ruru");
        testAuth.setPassword("123456789");
        testAuth.setConfirmPassword("123456789");

        String testAuthJson = gson.toJson(testAuth);
        this.mockMvc.perform(MockMvcRequestBuilders.post("/users/create")
                .contentType(contentType)
                .content(testAuthJson))
                .andExpect(status().isCreated());


    }

    @Test
    public void testMock() {
        Auth tAuth = new Auth();
        tAuth.setEmail("a@test.test");
        tAuth.setUsername("tessssssssssst");
        tAuth.setPassword("12345678");
        tAuth.setConfirmPassword("12345678");
//        when(userService.testAuth(tAuth)).thenReturn(tAuth);
        final HttpEntity<Auth> requestEntity = new HttpEntity<>(tAuth, REQUEST_HEADERS);
        final ResponseEntity<String> response = restTemplate.postForEntity("/users/create", requestEntity, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }


}
