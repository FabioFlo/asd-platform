package it.asd.bffadmin;

import it.asd.bffadmin.config.SatelliteRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(SatelliteRegistry.class)
public class BffAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffAdminApplication.class, args);
    }
}
