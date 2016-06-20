package es.unizar.iaaa.geofencing.web;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;

import es.unizar.iaaa.geofencing.Application;
import es.unizar.iaaa.geofencing.model.Geofence;
import es.unizar.iaaa.geofencing.model.User;
import es.unizar.iaaa.geofencing.repository.GeofenceRepository;
import es.unizar.iaaa.geofencing.repository.UserRepository;
import es.unizar.iaaa.geofencing.security.model.JwtAuthenticationRequest;
import es.unizar.iaaa.geofencing.view.View;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest("server.port:0")
@ActiveProfiles("test")
public class SecurityTest {

    @Value("${local.server.port}")
    int port;

    @Autowired
    private GeofenceRepository geofenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String PASSWORD = "password";

    private static final User USER1 = new User(null, "example.gmail.com", PASSWORD, "First", "Last", Date.valueOf("1992-08-07"),
            "356938035643809", new HashSet<>(), true, "ROLE_USER", Date.valueOf("2016-05-19"), new HashSet<>(), new HashSet<>());

    private static final Geofence GEOFENCE1 = new Geofence(null, "Feature", new HashMap<>(),
            new GeometryFactory().createPoint(new Coordinate(1, 2)), USER1, new HashSet<>());

    @Autowired
    private Jackson2ObjectMapperBuilder jacksonBuilder;

    @Before
    public void setup() {
        userRepository.deleteAll();
        USER1.setPassword(passwordEncoder.encode(PASSWORD));
        User currentUser = userRepository.save(USER1);
    }

    @After
    public void cleanup() {
        geofenceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void createGeofenceWithAuthorization() {
        JwtAuthenticationRequest jwtAuthenticationRequest = new JwtAuthenticationRequest("example.gmail.com", PASSWORD);
        RestTemplate client = new RestTemplate();
        ResponseEntity<String> response = client.postForEntity("http://localhost:{port}/api/users/auth", jwtAuthenticationRequest, String.class, port);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        String body = response.getBody();
        HttpHeaders entityHeaders = new HttpHeaders();
        entityHeaders.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        entityHeaders.add("Authorization", body.substring(10, body.length()-2));
        MappingJacksonValue jacksonValue = new MappingJacksonValue(GEOFENCE1);
        jacksonValue.setSerializationView(View.GeofenceBaseView.class);


        client.getMessageConverters().add(0, new MappingJackson2HttpMessageConverter(jacksonBuilder.build()));
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, entityHeaders);

        ResponseEntity<Geofence> response2 = client.postForEntity("http://localhost:{port}/api/geofences", entity, Geofence.class, port);
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        assertNotNull(response2.getBody().getId());
    }

    @Test(expected=ResourceAccessException.class)
    public void createGeofenceWithoutAuthorizationFails() {

        HttpHeaders entityHeaders = new HttpHeaders();
        entityHeaders.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        MappingJacksonValue jacksonValue = new MappingJacksonValue(GEOFENCE1);
        jacksonValue.setSerializationView(View.GeofenceBaseView.class);

        RestTemplate client = new RestTemplate();
        client.getMessageConverters().add(0, new MappingJackson2HttpMessageConverter(jacksonBuilder.build()));
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, entityHeaders);
        client.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });

        client.postForEntity("http://localhost:{port}/api/geofences", entity, Geofence.class, port);
    }
}