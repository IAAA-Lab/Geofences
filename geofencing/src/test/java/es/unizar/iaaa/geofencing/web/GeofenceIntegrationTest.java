package es.unizar.iaaa.geofencing.web;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;

import es.unizar.iaaa.geofencing.Application;
import es.unizar.iaaa.geofencing.config.DatabaseFillerOnStartup;
import es.unizar.iaaa.geofencing.model.Geofence;
import es.unizar.iaaa.geofencing.security.model.JwtAuthenticationRequest;
import es.unizar.iaaa.geofencing.view.View;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest("server.port:0")
public class GeofenceIntegrationTest {

    @Value("${local.server.port}")
    int port;

    @Autowired
    private DatabaseFillerOnStartup databaseFillerOnStartup;




    // {"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[],[30.44867367928756,34.1015625],[54.16243396806779,71.015625],[48.45835188280866,23.203125]]]},"user":null,"rules":[]}

    private static final Geofence GEOFENCE1 = new Geofence(null, "Feature", new HashMap<>(),
            new GeometryFactory().createPolygon(new Coordinate[]{new Coordinate(48.45835188280866,23.203125),
                    new Coordinate(30.44867367928756,34.1015625),
                    new Coordinate(54.16243396806779,71.015625),
                    new Coordinate(48.45835188280866,23.203125)}),  null, new HashSet<>());

    @Autowired
    private Jackson2ObjectMapperBuilder jacksonBuilder;

    @Before
    public void setup() {
        databaseFillerOnStartup.cleanup();
        databaseFillerOnStartup.populate();
    }

    @After
    public void cleanup() {
        databaseFillerOnStartup.cleanup();
    }

    @Test
    public void createGeofenceWithAuthorization() {
        JwtAuthenticationRequest jwtAuthenticationRequest = new JwtAuthenticationRequest("admin", "admin");
        RestTemplate client = new RestTemplate();
        ResponseEntity<String> response = client.postForEntity("http://localhost:{port}/api/users/auth", jwtAuthenticationRequest, String.class, port);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        client.getInterceptors().add(new LoggingRequestInterceptor());

        String body = response.getBody();
        HttpHeaders entityHeaders = new HttpHeaders();
        entityHeaders.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        entityHeaders.add("Authorization", body.substring(10, body.length()-2));
        MappingJacksonValue jacksonValue = new MappingJacksonValue(GEOFENCE1);
        jacksonValue.setSerializationView(View.GeofenceCompleteView.class);



        client.getMessageConverters().add(0, new MappingJackson2HttpMessageConverter(jacksonBuilder.build()));
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, entityHeaders);

        ResponseEntity<Geofence> response2 = client.postForEntity("http://localhost:{port}/api/geofences", entity, Geofence.class, port);
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        assertNotNull(response2.getBody().getId());
    }

    @Test
    public void deleteUser() {
        JwtAuthenticationRequest jwtAuthenticationRequest = new JwtAuthenticationRequest("admin", "admin");
        RestTemplate client = new RestTemplate();
        ResponseEntity<String> response = client.postForEntity("http://localhost:{port}/api/users/auth", jwtAuthenticationRequest, String.class, port);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        client.getInterceptors().add(new LoggingRequestInterceptor());

        String body = response.getBody();
        HttpHeaders entityHeaders = new HttpHeaders();
        entityHeaders.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        entityHeaders.add("Authorization", body.substring(10, body.length()-2));

        HttpEntity<Object> request = new HttpEntity<>(entityHeaders);

        ResponseEntity<String> response2 = client.exchange("http://localhost:{port}/api/users/admin", HttpMethod.DELETE, request, String.class, port);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }


}

class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingRequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
                                        final ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);

        response = log(request, body, response);

        return response;
    }

    private ClientHttpResponse log(final HttpRequest request, final byte[] body, final ClientHttpResponse response) throws IOException {
        final ClientHttpResponse responseCopy = new BufferingClientHttpResponseWrapper(response);
        logger.info("Method: ", request.getMethod().toString());
        logger.info("URI: ", request.getURI().toString());
        logger.info("Request Body: " + new String(body));
        logger.info("Response body: " + IOUtils.toString(responseCopy.getBody(), Charset.forName("UTF-8")));
        return responseCopy;
    }

}

class BufferingClientHttpResponseWrapper implements ClientHttpResponse {

    private final ClientHttpResponse response;

    private byte[] body;


    BufferingClientHttpResponseWrapper(ClientHttpResponse response) {
        this.response = response;
    }


    public HttpStatus getStatusCode() throws IOException {
        return this.response.getStatusCode();
    }

    public int getRawStatusCode() throws IOException {
        return this.response.getRawStatusCode();
    }

    public String getStatusText() throws IOException {
        return this.response.getStatusText();
    }

    public HttpHeaders getHeaders() {
        return this.response.getHeaders();
    }

    public InputStream getBody() throws IOException {
        if (this.body == null) {
            this.body = StreamUtils.copyToByteArray(this.response.getBody());
        }
        return new ByteArrayInputStream(this.body);
    }

    public void close() {
        this.response.close();
    }

}