package com.example.telitodev.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;


@Configuration
public class WireMockConfig {

    private static final int MOCK_PORT = 8083;


    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer wireMockServer() {
        WireMockServer wireMockServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig()
                        .port(MOCK_PORT)
                        .usingFilesUnderClasspath("wiremock")
//                        .usingFilesUnderDirectory("/home/ec2-user/wiremock")
        );
        System.out.println("Wiremock funciona bien papu , en el puerto: " + MOCK_PORT);

        return wireMockServer;
    }

}