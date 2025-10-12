package com.example.telitodev.config;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

/**
 * Implementación mock de JavaMailSender para desarrollo/testing
 * Simula el envío de emails sin realmente enviarlos
 */
public class MockJavaMailSender implements JavaMailSender {

    @Override
    public MimeMessage createMimeMessage() {
        Session session = Session.getDefaultInstance(new Properties());
        return new MimeMessage(session);
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        return createMimeMessage();
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        System.out.println("📧 [MOCK EMAIL] Email simulado enviado:");
        try {
            System.out.println("   - Para: " + (mimeMessage.getAllRecipients() != null ? 
                mimeMessage.getAllRecipients()[0] : "N/A"));
            System.out.println("   - Asunto: " + mimeMessage.getSubject());
            System.out.println("   - Estado: ✅ Simulado exitosamente");
        } catch (Exception e) {
            System.out.println("   - Error obteniendo detalles del mensaje: " + e.getMessage());
        }
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        for (MimeMessage message : mimeMessages) {
            send(message);
        }
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        MimeMessage message = createMimeMessage();
        try {
            mimeMessagePreparator.prepare(message);
            send(message);
        } catch (Exception e) {
            System.err.println("❌ [MOCK EMAIL] Error preparando mensaje: " + e.getMessage());
        }
    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
        for (MimeMessagePreparator preparator : mimeMessagePreparators) {
            send(preparator);
        }
    }

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        System.out.println("📧 [MOCK EMAIL] Email simple simulado enviado:");
        System.out.println("   - Para: " + (simpleMessage.getTo() != null ? 
            simpleMessage.getTo()[0] : "N/A"));
        System.out.println("   - Asunto: " + simpleMessage.getSubject());
        System.out.println("   - Texto: " + simpleMessage.getText());
        System.out.println("   - Estado: ✅ Simulado exitosamente");
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        for (SimpleMailMessage message : simpleMessages) {
            send(message);
        }
    }
}