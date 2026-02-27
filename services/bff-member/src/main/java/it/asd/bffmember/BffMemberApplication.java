package it.asd.bffmember;

import it.asd.bffmember.config.SatelliteRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(SatelliteRegistry.class)
public class BffMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffMemberApplication.class, args);
    }
}
