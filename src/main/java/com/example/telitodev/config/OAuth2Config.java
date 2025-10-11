package com.example.telitodev.config;

import com.example.telitodev.service.OAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Configuración específica para OAuth2
 * FORZA el uso de nuestro OAuth2UserService personalizado
 */
@Configuration
public class OAuth2Config {

    @Autowired
    private OAuth2UserService oAuth2UserService;

    @Bean("oauth2UserServiceBean")
    @Primary
    public org.springframework.security.oauth2.client.userinfo.OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserServiceBean() {
        System.out.println("🔥 OAUTH2CONFIG - CREANDO BEAN FORZADO");
        System.out.println("   - Servicio: " + oAuth2UserService.getClass().getName());
        System.out.println("   - Bean Name: oauth2UserServiceBean");
        System.out.println("   - Es Primary: true");
        return oAuth2UserService;
    }
}