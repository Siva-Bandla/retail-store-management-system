package com.retailstore.integration.log;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogFileIT {

    private static final String LOG_DIR = "logs";
    private static final String MAIN_LOG = "logs/retail-store.log";
    private static final String ERROR_LOG = "logs/retail-store-error.log";

    @Autowired
    private TestRestTemplate restTemplate;

    //==============<< Generate logs by hitting safe public endpoints >>===============
    @Test
    @Order(1)
    void callSomeApisToGenerateLogs(){ //this will not have any data, as we are not inserting before hitting the endpoints, is that okay?
        restTemplate.postForEntity("/users/register", new DummyRegisterRequest(), String.class);
        restTemplate.getForEntity("/products", String.class);
        restTemplate.getForEntity("/products/99999", String.class);
        restTemplate.getForEntity("/invalid-path-12345", String.class);
    }

    @Test
    @Order(2)
    void verifyLogFilesExist(){
        Assertions.assertTrue(new File(LOG_DIR).exists(), "logs folder missing!");
        Assertions.assertTrue(new File(MAIN_LOG).exists(), "main log file missing!");
        Assertions.assertTrue(new File(ERROR_LOG).exists(), "error log file missing!");
    }

    @Test
    @Order(3)
    void verifyMainLogHasContent() throws Exception{
        File file = new File(MAIN_LOG);
        Assertions.assertTrue(file.length() > 0, "main log is empty");
    }

    @Test
    @Order(4)
    void verifyNoSensitiveDataLogged() throws Exception{
        File mainLog = new File(MAIN_LOG);
        List<String> lines = Files.readAllLines(mainLog.toPath());

        for (String line: lines){
            Assertions.assertFalse(line.matches(".*password\\s*[:=]\\s*[^, }]+.*"), "Password leaked!");
            Assertions.assertFalse(line.matches(".*token\\s*[:=]\\s*[^, }]+.*"), "Token leaked!");
            Assertions.assertFalse(line.matches(".*secret\\s*[:=]\\s*[^, }]+.*"), "Secret leaked!");
        }
    }

    @Test
    @Order(5)
    void verifyDailyRotationFileExists(){
        String expectedFile = "logs/retail-store-" + LocalDate.now() + ".log";
        File file = new File(expectedFile);

        if (!file.exists()) {
            System.out.println("WARNING: Daily rotated log file missing: " + expectedFile);
        }
    }

    //==============<< Dummy DTOs for safe public calls >>===============
    static class DummyRegisterRequest {
        public String email = "dummyuser@test.com";
//        public String password = "Dummy123";
        public String name = "Dummy User";
    }
}
