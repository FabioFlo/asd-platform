package it.asd.competition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"it.asd.competition", "it.asd.common"})
public class CompetitionServiceApplication {
    static void main(String[] args) {
        SpringApplication.run(CompetitionServiceApplication.class, args);
    }
}
