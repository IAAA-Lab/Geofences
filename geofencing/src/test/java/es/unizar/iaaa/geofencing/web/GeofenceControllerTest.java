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
import java.util.*;

import es.unizar.iaaa.geofencing.Application;
import es.unizar.iaaa.geofencing.model.Geofence;
import es.unizar.iaaa.geofencing.model.User;
import es.unizar.iaaa.geofencing.repository.GeofenceRepository;
import es.unizar.iaaa.geofencing.repository.UserRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
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
public class GeofenceControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private GeofenceRepository geofenceRepository;

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

    private static final Geofence GEOFENCE1 = new Geofence(null, "Feature", null,
            new GeometryFactory().createPoint(new Coordinate(41.618618, -0.847992)), USER1, new HashSet<>());

    private final int[] COORDINATES = {1, 2};
    private final int COUNT = 10;
    private final int RADIUS = 3;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(SecurityMockMvcConfigurers.springSecurity()).build();
        String hashedPassword = passwordEncoder.encode(PASSWORD);
        USER1.setPassword(hashedPassword);
        User currentUser = userRepository.save(USER1);
        USER1.setPassword(PASSWORD);
        currentUser.setPassword(PASSWORD);
        Map<String, String> properties = new HashMap<>();
        properties.put("name", "Prueba");
        GEOFENCE1.setProperties(properties);
        GEOFENCE1.setUser(currentUser);
    }

    @After
    public void cleanup() {
        geofenceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void createGeofence() throws Exception {
        this.mockMvc.perform(post("/api/geofences")
                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                .content(objectMapper.writeValueAsString(GEOFENCE1))
                .with(user(USER1.getNick())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.type").value(GEOFENCE1.getType()))
                .andExpect(jsonPath("$.properties.name").value(GEOFENCE1.getProperties().get("name")))
                .andExpect(jsonPath("$.geometry.type").value(GEOFENCE1.getGeometry().getGeometryType()))
                .andExpect(jsonPath("$.user.id").value(GEOFENCE1.getUser().getId().intValue()))
                .andExpect(jsonPath("$.rules").isEmpty());
        assertEquals(1, geofenceRepository.count());
    }

    @Test
    public void getGeofences() throws Exception {
        Geofence auxGeofence = GEOFENCE1;
        for (int i = 0; i < COUNT; i++) {
            auxGeofence.setId(null);
            auxGeofence.setGeometry(new GeometryFactory().createPoint(new Coordinate(COORDINATES[0]+i, COORDINATES[1]+i)));
            auxGeofence = geofenceRepository.save(auxGeofence);
        }
        this.mockMvc.perform(get("/api/geofences")
                .with(user(USER1.getNick())))
                .andDo(print())
                .andExpect(status().isOk());
        assertEquals(COUNT, geofenceRepository.count());
    }

    @Test
    public void getGeofencesWithLimitAuthenticated() throws Exception {
        Geofence auxGeofence = GEOFENCE1;
        for (int i = 0; i < COUNT; i++) {
            auxGeofence.setId(null);
            auxGeofence.setGeometry(new GeometryFactory().createPoint(new Coordinate(COORDINATES[0]+i, COORDINATES[1]+i)));
            auxGeofence = geofenceRepository.save(auxGeofence);
        }
        int LIMIT = 2;
        this.mockMvc.perform(get("/api/geofences/area")
                .param("limit", String.valueOf(LIMIT))
                .param("latitude", String.valueOf(COORDINATES[0]))
                .param("longitude", String.valueOf(COORDINATES[1]))
                .param("radius", String.valueOf(RADIUS))
                .with(httpBasic(USER1.getNick(), USER1.getPassword())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(LIMIT)));
        assertEquals(COUNT, geofenceRepository.count());
    }

    @Test
    public void getGeofencesWithLimitNotAuthenticated() throws Exception {
        Geofence auxGeofence = GEOFENCE1;
        for (int i = 0; i < COUNT; i++) {
            auxGeofence.setId(null);
            auxGeofence.setGeometry(new GeometryFactory().createPoint(new Coordinate(COORDINATES[0]+i, COORDINATES[1]+i)));
            auxGeofence = geofenceRepository.save(auxGeofence);
        }
        int LIMIT = 2;
        this.mockMvc.perform(get("/api/geofences/area")
                .param("limit", String.valueOf(LIMIT))
                .param("latitude", String.valueOf(COORDINATES[0]))
                .param("longitude", String.valueOf(COORDINATES[1]))
                .param("radius", String.valueOf(RADIUS)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(LIMIT)));
        assertEquals(COUNT, geofenceRepository.count());
    }

    @Test
    public void getGeofencesWithoutLimitAuthenticated() throws Exception {
        Geofence auxGeofence = GEOFENCE1;
        for (int i = 0; i < COUNT; i++) {
            auxGeofence.setId(null);
            auxGeofence.setGeometry(new GeometryFactory().createPoint(new Coordinate(COORDINATES[0]+i, COORDINATES[1]+i)));
            auxGeofence = geofenceRepository.save(auxGeofence);
        }
        this.mockMvc.perform(get("/api/geofences/area")
                .param("latitude", String.valueOf(COORDINATES[0]))
                .param("longitude", String.valueOf(COORDINATES[1]))
                .param("radius", String.valueOf(RADIUS))
                .with(httpBasic(USER1.getNick(), USER1.getPassword())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(RADIUS)));
        assertEquals(COUNT, geofenceRepository.count());
    }

    @Test
    public void getGeofencesWithoutLimitNotAuthenticated() throws Exception {
        Geofence auxGeofence = GEOFENCE1;
        for (int i = 0; i < COUNT; i++) {
            auxGeofence.setId(null);
            auxGeofence.setGeometry(new GeometryFactory().createPoint(new Coordinate(COORDINATES[0]+i, COORDINATES[1]+i)));
            auxGeofence = geofenceRepository.save(auxGeofence);
        }
        this.mockMvc.perform(get("/api/geofences/area")
                .param("latitude", String.valueOf(COORDINATES[0]))
                .param("longitude", String.valueOf(COORDINATES[1]))
                .param("radius", String.valueOf(RADIUS)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(RADIUS)));
        assertEquals(COUNT, geofenceRepository.count());
    }

    @Test
    public void modifyGeofence() throws Exception {
        Geofence geofence = geofenceRepository.save(GEOFENCE1);
        geofence.getProperties().put("name", "Proof");
        String expectedValue = geofence.getProperties().get("name");
        this.mockMvc.perform(put("/api/geofences/"+geofence.getId())
                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                .content(objectMapper.writeValueAsString(geofence))
                .with(user(USER1.getNick())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.id").value(geofence.getId().intValue()))
                .andExpect(jsonPath("$.type").value(geofence.getType()))
                .andExpect(jsonPath("$.properties.name").value(geofence.getProperties().get("name")))
                .andExpect(jsonPath("$.geometry.type").value(geofence.getGeometry().getGeometryType()))
                .andExpect(jsonPath("$.user.id").value(geofence.getUser().getId().intValue()))
                .andExpect(jsonPath("$.rules").isEmpty());
        Geofence geofenceNew = geofenceRepository.findOne(geofence.getId());
        assertEquals(expectedValue, geofenceNew.getProperties().get("name"));
    }

    @Test
    public void deleteGeofence() throws Exception {
        Geofence geofence = geofenceRepository.save(GEOFENCE1);
        this.mockMvc.perform(delete("/api/geofences/"+geofence.getId())
                .with(user(USER1.getNick())))
                .andExpect(status().isOk());
        assertNull(userRepository.findOne(geofence.getId()));
    }

    @Test
    public void getGeofenceAuthenticated() throws Exception {
        Geofence geofence = geofenceRepository.save(GEOFENCE1);
        this.mockMvc.perform(get("/api/geofences/"+geofence.getId())
                .with(user(USER1.getNick())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.id").value(geofence.getId().intValue()))
                .andExpect(jsonPath("$.type").value(geofence.getType()))
                .andExpect(jsonPath("$.properties.name").value(geofence.getProperties().get("name")))
                .andExpect(jsonPath("$.geometry.type").value(geofence.getGeometry().getGeometryType()))
                .andExpect(jsonPath("$.user.id").value(geofence.getUser().getId().intValue()))
                .andExpect(jsonPath("$.rules").isEmpty());
    }

    @Test
    public void getGeofenceNotAuthenticated() throws Exception {
        Geofence geofence = geofenceRepository.save(GEOFENCE1);
        this.mockMvc.perform(get("/api/geofences/"+geofence.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json; charset=UTF-8"))
                .andExpect(jsonPath("$.id").value(geofence.getId().intValue()))
                .andExpect(jsonPath("$.type").value(geofence.getType()))
                .andExpect(jsonPath("$.properties.name").value(geofence.getProperties().get("name")))
                .andExpect(jsonPath("$.geometry.type").value(geofence.getGeometry().getGeometryType()));
    }
}

