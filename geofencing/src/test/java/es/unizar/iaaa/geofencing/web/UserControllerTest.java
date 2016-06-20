package es.unizar.iaaa.geofencing.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Date;
import java.util.HashSet;

import es.unizar.iaaa.geofencing.Application;
import es.unizar.iaaa.geofencing.model.User;
import es.unizar.iaaa.geofencing.repository.UserRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes={Application.class, WebConfigForTest.class})
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private MockMvc mockMvc;

    private static final String PASSWORD = "password";

    private static final User USER1 = new User(null, "example.gmail.com", PASSWORD, "First", "Last", Date.valueOf("1992-08-07"),
            "356938035643809", new HashSet<>(), true, "ROLE_USER", Date.valueOf("2016-05-19"), new HashSet<>(), new HashSet<>());


    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }


    @After
    public void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    public void createUser() throws Exception {
        this.mockMvc.perform(post("/api/users")
                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                .content(objectMapper.writeValueAsString(USER1)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nick").value(USER1.getNick()))
                .andExpect(jsonPath("$.password").value(password(PASSWORD)))
                .andExpect(jsonPath("$.firstName").value(USER1.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(USER1.getLastName()))
                .andExpect(jsonPath("$.birthday").value(USER1.getBirthday().toString()))
                .andExpect(jsonPath("$.imei").value(USER1.getImei()))
                .andExpect(jsonPath("$.geofences").isEmpty())
                .andExpect(jsonPath("$.enabled").value(USER1.getEnabled()))
                .andExpect(jsonPath("$.role").value(USER1.getRole()))
                .andExpect(jsonPath("$.lastPasswordResetDate").isNumber())
                .andExpect(jsonPath("$.notifications").isEmpty())
                .andExpect(jsonPath("$.geofencesRegistry").isEmpty());
        assertEquals(1, userRepository.count());
    }

    @Test
    public void modifyUser() throws Exception {
        String hashedPassword = passwordEncoder.encode(USER1.getPassword());
        USER1.setPassword(hashedPassword);
        User usuario = userRepository.save(USER1);
        USER1.setPassword(PASSWORD);
        usuario.setPassword(PASSWORD);
        usuario.setBirthday(Date.valueOf("1994-08-07"));
        this.mockMvc.perform(put("/api/users/"+usuario.getNick())
                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                .content(objectMapper.writeValueAsString(usuario))
                .with(user(USER1.getNick())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.id").value(usuario.getId().intValue()))
                .andExpect(jsonPath("$.nick").value(usuario.getNick()))
                .andExpect(jsonPath("$.password").value(password(PASSWORD)))
                .andExpect(jsonPath("$.firstName").value(usuario.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(usuario.getLastName()))
                .andExpect(jsonPath("$.birthday").value(usuario.getBirthday().toString()))
                .andExpect(jsonPath("$.imei").value(usuario.getImei()))
                .andExpect(jsonPath("$.geofences").isEmpty())
                .andExpect(jsonPath("$.enabled").value(usuario.getEnabled()))
                .andExpect(jsonPath("$.role").value(usuario.getRole()))
                .andExpect(jsonPath("$.lastPasswordResetDate").isNumber())
                .andExpect(jsonPath("$.notifications").isEmpty())
                .andExpect(jsonPath("$.geofencesRegistry").isEmpty());
        User usuarioNew = userRepository.findOne(usuario.getId());
        assertEquals(usuario.getBirthday(), usuarioNew.getBirthday());
    }

    @Test
    public void deleteUser() throws Exception {
        String hashedPassword = passwordEncoder.encode(PASSWORD);
        USER1.setPassword(hashedPassword);
        User usuario = userRepository.save(USER1);
        USER1.setPassword(PASSWORD);
        usuario.setPassword(PASSWORD);
        this.mockMvc.perform(delete("/api/users/"+usuario.getNick())
                .with(user(USER1.getNick())))
                .andExpect(status().isOk());
        assertNull(userRepository.findOne(usuario.getId()));
    }

    @Test
    public void getUserAuthenticated() throws Exception {
        String hashedPassword = passwordEncoder.encode(PASSWORD);
        USER1.setPassword(hashedPassword);
        User usuario = userRepository.save(USER1);
        USER1.setPassword(PASSWORD);
        usuario.setPassword(PASSWORD);
        this.mockMvc.perform(get("/api/users/"+usuario.getNick())
                .with(user(USER1.getNick())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.id").value(usuario.getId().intValue()))
                .andExpect(jsonPath("$.nick").value(usuario.getNick()))
                .andExpect(jsonPath("$.password").value(hashedPassword))
                .andExpect(jsonPath("$.firstName").value(usuario.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(usuario.getLastName()))
                .andExpect(jsonPath("$.birthday").value(usuario.getBirthday().toString()))
                .andExpect(jsonPath("$.imei").value(usuario.getImei()))
                .andExpect(jsonPath("$.geofences").isEmpty())
                .andExpect(jsonPath("$.enabled").value(usuario.getEnabled()))
                .andExpect(jsonPath("$.role").value(usuario.getRole()))
                .andExpect(jsonPath("$.lastPasswordResetDate").isNumber())
                .andExpect(jsonPath("$.notifications").isEmpty())
                .andExpect(jsonPath("$.geofencesRegistry").isEmpty());
    }

    @Test
    public void getUserNotAuthenticated() throws Exception {
        String hashedPassword = passwordEncoder.encode(PASSWORD);
        USER1.setPassword(hashedPassword);
        User usuario = userRepository.save(USER1);
        USER1.setPassword(PASSWORD);
        usuario.setPassword(PASSWORD);
        this.mockMvc.perform(get("/api/users/"+usuario.getNick()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.id").value(usuario.getId().intValue()))
                .andExpect(jsonPath("$.nick").value(usuario.getNick()))
                .andExpect(jsonPath("$.enabled").value(usuario.getEnabled()))
                .andExpect(jsonPath("$.role").value(usuario.getRole()));
    }

    public PasswordMatcher password(String password) {
        return new PasswordMatcher(password);
    }

    class PasswordMatcher extends BaseMatcher<String> {

        private String password;

        public PasswordMatcher(String password) {
            this.password = password;
        }

        @Override
        public boolean matches(Object item) {
            return passwordEncoder.matches(password, item.toString());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("encoded value of ");
            description.appendValue(password);
        }
    }
}