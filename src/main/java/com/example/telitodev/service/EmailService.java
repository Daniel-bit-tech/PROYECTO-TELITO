package com.example.telitodev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@telitodev.com}")
    private String fromEmail;

    @Value("${app.name:TelitoDev}")
    private String appName;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    /**
     * Probar conectividad SMTP (para debug)
     */
    public boolean probarConectividad() {
        try {
            System.out.println("=== PROBANDO CONECTIVIDAD SMTP ===");
            System.out.println("From Email: " + fromEmail);
            System.out.println("App Name: " + appName);
            
            // Crear un mensaje de prueba simple
            MimeMessage testMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(testMessage, true, "UTF-8");
            
            helper.setFrom("TelitoDev Portal <" + fromEmail + ">");
            helper.setTo(fromEmail); // Enviar a nosotros mismos como prueba
            helper.setSubject("Test de conectividad SMTP");
            helper.setText("Test de conectividad exitoso", false);
            
            System.out.println("✅ Configuración SMTP: OK");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error de configuración SMTP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Enviar token de confirmación por email
     */
    public boolean enviarTokenConfirmacion(String email, String nombre, String token, 
                                         LocalDateTime fechaExpiracion) {
        try {
            System.out.println("=== ENVIANDO EMAIL DE CONFIRMACIÓN ===");
            System.out.println("Destinatario: " + email);
            System.out.println("Nombre: " + nombre);
            System.out.println("Token: " + token);
            System.out.println("Expira: " + fechaExpiracion);
            System.out.println("From Email: " + fromEmail);
            System.out.println("App Name: " + appName);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("TelitoDev Portal <" + fromEmail + ">");
            helper.setTo(email);
            helper.setSubject("🔐 Confirma tu cuenta en " + appName);

            String htmlContent = construirEmailConfirmacion(nombre, token, fechaExpiracion, email);
            
            // LOG DEL CONTENIDO DEL EMAIL PARA DEBUG
            System.out.println("=== CONTENIDO DEL EMAIL ===");
            System.out.println("Subject: " + helper.getMimeMessage().getSubject());
            System.out.println("From: " + helper.getMimeMessage().getFrom()[0].toString());
            System.out.println("To: " + helper.getMimeMessage().getAllRecipients()[0].toString());
            System.out.println("Content Length: " + htmlContent.length() + " caracteres");
            
            helper.setText(htmlContent, true);

            System.out.println("📧 Enviando email...");
            mailSender.send(message);
            
            System.out.println("✅ Email enviado exitosamente a: " + email);
            return true;

        } catch (Exception e) {
            System.err.println("❌ ERROR DETALLADO ENVIANDO EMAIL:");
            System.err.println("   Email destino: " + email);
            System.err.println("   Error: " + e.getClass().getSimpleName());
            System.err.println("   Mensaje: " + e.getMessage());
            System.err.println("   Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "Sin causa específica"));
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Enviar email de bienvenida tras confirmación exitosa
     */
    public boolean enviarEmailBienvenida(String email, String nombre, String dni, String rol) {
        try {
            System.out.println("=== ENVIANDO EMAIL DE BIENVENIDA ===");
            System.out.println("Destinatario: " + email);
            System.out.println("Nombre: " + nombre);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("TelitoDev Portal <" + fromEmail + ">");
            helper.setTo(email);
            helper.setSubject("🎉 ¡Bienvenido a " + appName + "!");

            String htmlContent = construirEmailBienvenida(nombre, dni, rol);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            
            System.out.println("✅ Email de bienvenida enviado a: " + email);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error enviando email de bienvenida a " + email + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Construir contenido HTML del email de confirmación
     */
    private String construirEmailConfirmacion(String nombre, String token, LocalDateTime fechaExpiracion, String email) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fechaExpiracionStr = fechaExpiracion.format(formatter);

        String htmlTemplate = """
            <!DOCTYPE html>
            <html lang=\"es\">
            <head>
                <meta charset=\"UTF-8\">
                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">
                <title>Confirma tu cuenta</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f8f9fa; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; font-weight: 300; }
                    .content { padding: 40px 30px; }
                    .token-container { background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 25px 0; text-align: center; }
                    .token { font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; margin: 10px 0; }
                    .expiration { color: #dc3545; font-weight: 500; margin-top: 15px; }
                    .instructions { background-color: #e3f2fd; padding: 20px; border-radius: 8px; margin: 25px 0; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }
                    .button { display: inline-block; background-color: #667eea; color: white; padding: 12px 25px; text-decoration: none; border-radius: 6px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class=\"container\">
                    <div class=\"header\">
                        <h1>🔐 Confirma tu cuenta</h1>
                        <p>APP_NAME_PLACEHOLDER</p>
                    </div>
                    
                    <div class=\"content\">
                        <h2>¡Hola NOMBRE_PLACEHOLDER!</h2>
                        <p>Se ha creado una cuenta para ti en <strong>APP_NAME_PLACEHOLDER</strong>. Para activar tu cuenta, necesitas confirmar tu dirección de email usando el siguiente código:</p>
                        
                        <div class=\"token-container\">
                            <p style=\"margin: 0; font-size: 18px; color: #495057;\">Tu código de confirmación es:</p>
                            <div class=\"token\">TOKEN_PLACEHOLDER</div>
                            <div class=\"expiration\">⏰ Expira el FECHA_PLACEHOLDER</div>
                        </div>
                        <div style=\"text-align:center; margin: 30px 0;\">
                            <a class=\"button\" href=\"APP_URL_PLACEHOLDER/confirmar-cuenta?email=EMAIL_PLACEHOLDER&token=TOKEN_PLACEHOLDER\" target=\"_blank\">Confirmar mi cuenta</a>
                        </div>
                        <div class=\"instructions\">
                            <h3>📋 Instrucciones:</h3>
                            <ol>
                                <li><strong>Haz clic en el botón "Confirmar mi cuenta"</strong> para ir directamente al formulario</li>
                                <li>Se abrirá una página con tus datos pre-cargados</li>
                                <li>Ingresa tu código de 6 dígitos y establece tu contraseña</li>
                                <li>¡Listo! Podrás acceder al sistema con tu email y contraseña</li>
                            </ol>
                            <p style="background-color: #fff3cd; padding: 10px; border-radius: 5px; border-left: 4px solid #ffc107;">
                                💡 <strong>Tip:</strong> Si el botón no funciona, ve manualmente a <strong>APP_URL_PLACEHOLDER/confirmar-cuenta</strong> e ingresa tu email y código.
                            </p>
                        </div>
                        
                        <p><strong>⚠️ Importante:</strong></p>
                        <ul>
                            <li>Este código es válido por <strong>3 minutos</strong> (modo prueba)</li>
                            <li>Solo puedes usar este código <strong>una vez</strong></li>
                            <li>Si no confirmaste tu cuenta en este tiempo, deberás solicitar una nueva cuenta</li>
                        </ul>
                        
                        <p>Si no solicitaste esta cuenta, puedes ignorar este email de forma segura.</p>
                    </div>
                    
                    <div class=\"footer\">
                        <p>© 2025 APP_NAME_PLACEHOLDER - Sistema de gestión de proyectos</p>
                        <p>Este es un email automático, por favor no respondas a este mensaje.</p>
                    </div>
                </div>
            </body>
            </html>
            """;

    return htmlTemplate
        .replace("APP_NAME_PLACEHOLDER", appName)
        .replace("NOMBRE_PLACEHOLDER", nombre)
        .replace("TOKEN_PLACEHOLDER", token)
        .replace("FECHA_PLACEHOLDER", fechaExpiracionStr)
        .replace("APP_URL_PLACEHOLDER", appUrl)
        .replace("EMAIL_PLACEHOLDER", email);
    }

    /**
     * Construir contenido HTML del email de bienvenida
     */
    private String construirEmailBienvenida(String nombre, String dni, String rol) {
        String htmlTemplate = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>¡Bienvenido!</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f8f9fa; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #28a745 0%, #20c997 100%); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; font-weight: 300; }
                    .content { padding: 40px 30px; }
                    .info-box { background-color: #f8f9fa; border-left: 4px solid #28a745; padding: 20px; margin: 25px 0; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 ¡Cuenta activada!</h1>
                        <p>¡Bienvenido a APP_NAME_PLACEHOLDER!</p>
                    </div>
                    
                    <div class="content">
                        <h2>¡Hola NOMBRE_PLACEHOLDER!</h2>
                        <p>¡Felicitaciones! Tu cuenta ha sido confirmada exitosamente y ya puedes acceder al sistema.</p>
                        
                        <div class="info-box">
                            <h3>📋 Información de tu cuenta:</h3>
                            <ul>
                                <li><strong>Usuario:</strong> DNI_PLACEHOLDER</li>
                                <li><strong>Rol:</strong> ROL_PLACEHOLDER</li>
                                <li><strong>Estado:</strong> ✅ Activa</li>
                            </ul>
                        </div>
                        
                        <p><strong>🔑 Para acceder al sistema:</strong></p>
                        <ol>
                            <li>Ve a la página de login: <a href="APP_URL_PLACEHOLDER/login">APP_URL_PLACEHOLDER/login</a></li>
                            <li>Usa tu email como usuario</li>
                            <li>Ingresa la contraseña que estableciste</li>
                        </ol>
                        
                        <p>Si tienes alguna pregunta o necesitas ayuda, contacta al administrador del sistema.</p>
                        
                        <p>¡Esperamos que tengas una excelente experiencia!</p>
                    </div>
                    
                    <div class="footer">
                        <p>© 2025 APP_NAME_PLACEHOLDER - Sistema de gestión de proyectos</p>
                        <p>Este es un email automático, por favor no respondas a este mensaje.</p>
                    </div>
                </div>
            </body>
            </html>
            """;

        return htmlTemplate
                .replace("APP_NAME_PLACEHOLDER", appName)
                .replace("NOMBRE_PLACEHOLDER", nombre)
                .replace("DNI_PLACEHOLDER", dni)
                .replace("ROL_PLACEHOLDER", rol)
                .replace("APP_URL_PLACEHOLDER", appUrl);
    }

    /**
     * Método de prueba para verificar configuración de email
     */
    public boolean probarConexion() {
        try {
            System.out.println("=== PROBANDO CONFIGURACIÓN EMAIL ===");
            System.out.println("From Email: " + fromEmail);
            System.out.println("App Name: " + appName);
            
            // Crear mensaje de prueba
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(fromEmail); // Enviar a uno mismo para probar
            message.setSubject("Test de configuración - " + appName);
            message.setText("Si recibes este mensaje, la configuración de email está funcionando correctamente.");
            
            mailSender.send(message);
            System.out.println("✅ Test de email exitoso");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error en test de email: " + e.getMessage());
            return false;
        }
    }
}