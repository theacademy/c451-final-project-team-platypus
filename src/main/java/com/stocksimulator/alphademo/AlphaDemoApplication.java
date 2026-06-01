package com.stocksimulator.alphademo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {
    HibernateJpaAutoConfiguration.class
})
public class AlphaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlphaDemoApplication.class, args);
    }
}