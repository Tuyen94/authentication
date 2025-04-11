package tuyenbd.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class AuthenticationApplication {

        public static void main(String[] args) {
                log.info("Starting Authentication Application...");
                SpringApplication.run(AuthenticationApplication.class, args);
                log.info("Authentication Application started successfully");
        }

}

