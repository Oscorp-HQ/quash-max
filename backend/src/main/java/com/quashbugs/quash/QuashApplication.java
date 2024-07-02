package com.quashbugs.quash;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableMongoRepositories
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableAsync
@SecurityScheme(
        name = "jwtAuth", // Use a suitable name
        type = SecuritySchemeType.HTTP,
        scheme = "bearer", // This indicates JWT bearer token
        bearerFormat = ""
)
public class QuashApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuashApplication.class, args);
    }

}

