package com.retailstore.system;

import org.junit.jupiter.api.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogVerificationSystemTest extends SystemTestBase{

    private static final String MAIN_LOG = "logs/retail-store.log";

    @Test
    @Order(1)
    void runFullWorkflowToGenerateLogs(){
        testRestTemplate.postForEntity(baseUrl + "/users/register", new DummyRegisterRequest(), String.class);
        testRestTemplate.getForEntity(baseUrl + "/products", String.class);
        testRestTemplate.getForEntity(baseUrl + "/products/99999", String.class);
        testRestTemplate.getForEntity(baseUrl + "/this-endpoint-does-not-exist", String.class);
    }

    @Test
    @Order(2)
    void verifyLogGrowthAfterWorkFlow(){
        File logFile = new File(MAIN_LOG);

        Assertions.assertTrue(logFile.exists(), "Main log file missing");
        Assertions.assertTrue(logFile.length() > 0, "Main log file is empty after workflow");
    }

    @Test
    @Order(3)
    void verifyNoSensitiveDataInSystemTestLogs() throws Exception{
        File logFile = new File(MAIN_LOG);
        Assertions.assertTrue(logFile.exists(), "Main log missing before sensitive-data verification!");

        List<String> lines = Files.readAllLines(new File(MAIN_LOG).toPath());

        for (String line: lines){
            Assertions.assertFalse(line.matches(".*password\\s*[:=]\\s*[^, }]+.*"), "Password leaked!");
            Assertions.assertFalse(line.matches(".*token\\s*[:=]\\s*[^, }]+.*"), "Token leaked!");
            Assertions.assertFalse(line.matches(".*secret\\s*[:=]\\s*[^, }]+.*"), "Secret leaked!");
        }
    }

    static class DummyRegisterRequest{
        public String email = "dummy@test.com";
        public String password = "Password123";
        public String name = "Dummy";
    }
}
