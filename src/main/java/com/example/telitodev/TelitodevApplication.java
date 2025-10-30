package com.example.telitodev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.autoconfigure.session.JdbcSessionDataSourceScriptDatabaseInitializer;

@SpringBootApplication(exclude = { SessionAutoConfiguration.class })
public class TelitodevApplication {

    private static final Logger logger = LoggerFactory.getLogger(TelitodevApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TelitodevApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("🚀 TELITODEV Application is ready!");
        logger.info("📊 Database connection configured for MySQL on localhost:3306/db_telito");
        logger.info("🔗 JPA entities are auto-mapped to database tables");
    }
}
