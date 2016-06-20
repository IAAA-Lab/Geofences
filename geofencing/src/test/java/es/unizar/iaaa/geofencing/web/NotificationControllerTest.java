package es.unizar.iaaa.geofencing.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

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
import es.unizar.iaaa.geofencing.model.Geofence;
import es.unizar.iaaa.geofencing.model.Notification;
import es.unizar.iaaa.geofencing.model.Rule;
import es.unizar.iaaa.geofencing.model.User;
import es.unizar.iaaa.geofencing.repository.GeofenceRepository;
import es.unizar.iaaa.geofencing.repository.NotificationRepository;
import es.unizar.iaaa.geofencing.repository.RuleRepository;
import es.unizar.iaaa.geofencing.repository.UserRepository;

import static es.unizar.iaaa.geofencing.model.RuleType.INSIDE;
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
public class NotificationControllerTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private RuleRepository ruleRepository;
    @Autowired
    private GeofenceRepository geofenceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private MockMvc mockMvc;

    private User USER1;
    private Notification NOTIFICATION1;


    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(SecurityMockMvcConfigurers.springSecurity()).build();
        String PASSWORD = "password";
        USER1 = new User(null, "example.gmail.com", PASSWORD, "First", "Last", Date.valueOf("1992-08-07"), "356938035643809",
                new HashSet<>(), true, "ROLE_USER", Date.valueOf("2016-05-19"), new HashSet<>(), new HashSet<>());

        String hashedPassword = passwordEncoder.encode(PASSWORD);
        USER1.setPassword(hashedPassword);
        userRepository.save(USER1);
        USER1.setPassword(PASSWORD);

        Geofence GEOFENCE1 = new Geofence(null, "Feature", null,
                new GeometryFactory().createPoint(new Coordinate(1, 2)), USER1, new HashSet<>());

        GEOFENCE1 = geofenceRepository.save(GEOFENCE1);

        Rule RULE1 = new Rule(null, true, INSIDE, 10, "You are inside", new HashSet<>(), new HashSet<>(), GEOFENCE1);

        RULE1 = ruleRepository.save(RULE1);

        NOTIFICATION1 = new Notification(null, RULE1, USER1, "No leído", Date.valueOf("2016-01-19"));
    }

    @After
    public void cleanup() {
        notificationRepository.deleteAll();
        ruleRepository.deleteAll();
        geofenceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void createNotification() throws Exception {
        this.mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                .content(objectMapper.writeValueAsString(NOTIFICATION1))
                .with(user(USER1.getNick())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.rule.id").value(NOTIFICATION1.getRule().getId().intValue()))
                .andExpect(jsonPath("$.rule.message").value(NOTIFICATION1.getRule().getMessage()))
                .andExpect(jsonPath("$.user.id").value(NOTIFICATION1.getUser().getId().intValue()))
                .andExpect(jsonPath("$.status").value(NOTIFICATION1.getStatus()))
                .andExpect(jsonPath("$.date").value(NOTIFICATION1.getDate().toString()));
        assertEquals(1, notificationRepository.count());
    }

    @Test
    public void getNotifications() throws Exception {
        Notification auxNotification = NOTIFICATION1;
        int COUNT = 10;
        for (int i = COUNT *2; i > COUNT; i--) {
            auxNotification.setId(null);
            auxNotification.setDate(Date.valueOf("2016-01-" + i));
            auxNotification = notificationRepository.save(auxNotification);
        }
        this.mockMvc.perform(get("/api/notifications")
                .with(user(USER1.getNick())))
                .andDo(print())
                .andExpect(status().isOk());
        assertEquals(COUNT, notificationRepository.count());
    }

    @Test
    public void modifyNotification() throws Exception {
        Notification notification = notificationRepository.save(NOTIFICATION1);
        notification.setStatus("Leído");
        String expectedValue = notification.getStatus();
        this.mockMvc.perform(put("/api/notifications/"+notification.getId())
                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                .content(objectMapper.writeValueAsString(notification))
                .with(user(USER1.getNick())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.id").value(notification.getId().intValue()))
                .andExpect(jsonPath("$.rule.id").value(notification.getRule().getId().intValue()))
                .andExpect(jsonPath("$.rule.message").value(notification.getRule().getMessage()))
                .andExpect(jsonPath("$.user.id").value(notification.getUser().getId().intValue()))
                .andExpect(jsonPath("$.status").value(notification.getStatus()))
                .andExpect(jsonPath("$.date").value(notification.getDate().toString()));
        Notification notificationNew = notificationRepository.findOne(notification.getId());
        assertEquals(expectedValue, notificationNew.getStatus());
    }

    @Test
    public void deleteNotification() throws Exception {
        Notification notification = notificationRepository.save(NOTIFICATION1);
        this.mockMvc.perform(delete("/api/notifications/"+notification.getId())
                .with(user(USER1.getNick())))
                .andExpect(status().isOk());
        assertNull(notificationRepository.findOne(notification.getId()));
    }

    @Test
    public void getNotificationAuthenticated() throws Exception {
        Notification notification = notificationRepository.save(NOTIFICATION1);
        this.mockMvc.perform(get("/api/notifications/"+notification.getId())
                .with(user(USER1.getNick())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.id").value(notification.getId().intValue()))
                .andExpect(jsonPath("$.rule.id").value(notification.getRule().getId().intValue()))
                .andExpect(jsonPath("$.rule.message").value(notification.getRule().getMessage()))
                .andExpect(jsonPath("$.user.id").value(notification.getUser().getId().intValue()))
                .andExpect(jsonPath("$.status").value(notification.getStatus()))
                .andExpect(jsonPath("$.date").value(notification.getDate().toString()));
    }

    @Test
    public void getNotificationNotAuthenticated() throws Exception {
        Notification notification = notificationRepository.save(NOTIFICATION1);
        this.mockMvc.perform(get("/api/notifications/"+notification.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.id").value(notification.getId().intValue()))
                .andExpect(jsonPath("$.rule.id").value(notification.getRule().getId().intValue()))
                .andExpect(jsonPath("$.status").value(notification.getStatus()));
    }
}