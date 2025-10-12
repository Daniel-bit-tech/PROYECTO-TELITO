package com.example.telitodev.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    /**
     * Bean de JavaMailSender que se crea solo si las propiedades de email están configuradas
     */
    @Bean
    @ConditionalOnProperty(
        value = "spring.mail.enabled", 
        havingValue = "true", 
        matchIfMissing = false
    )
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // Configuración real para Gmail
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("telitodevportal@gmail.com");
        mailSender.setPassword("dqppurwycuclkclh");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");
        
        // Configuraciones SSL específicas para evitar problemas de certificados
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.checkserveridentity", "false");
        props.put("mail.smtp.socketFactory.fallback", "false");

        return mailSender;
    }

    /**
     * Bean de respaldo cuando el email no está habilitado
     */
    @Bean
    @ConditionalOnProperty(
        value = "spring.mail.enabled", 
        havingValue = "false", 
        matchIfMissing = true
    )
    public JavaMailSender mockJavaMailSender() {
        return new MockJavaMailSender();
    }
}