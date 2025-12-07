package com.homesweet.homesweetback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class HomesweetBackApplication {
    public static void main(String[] args) {
        SpringApplication.run(HomesweetBackApplication.class, args);
    }

}