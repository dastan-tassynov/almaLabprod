package kg.almalab.meddocs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedDocsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedDocsApplication.class, args);
    }
}
