package it.asd.compliance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"it.asd.compliance", "it.asd.common"})
@EnableScheduling
public class ComplianceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ComplianceServiceApplication.class, args);
    }
}
