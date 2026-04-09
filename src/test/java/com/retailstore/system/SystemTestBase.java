package com.retailstore.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("system-tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SystemTestBase {

    @LocalServerPort
    protected int port;

    protected String baseUrl;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    protected String adminToken;
    protected String customerToken;

    @BeforeEach
    void setUp(){
        baseUrl = "http://localhost:" + port + "/retail-store";
    }

    protected HttpHeaders authHeaders(String token){
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isBlank()) {
            headers.setBearerAuth(token);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
