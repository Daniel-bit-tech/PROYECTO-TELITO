package com.example.telitodev.config;

import com.example.telitodev.service.OAuth2UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler personalizado que intercepta DESPUÉS de la autenticación OAuth2
 * para forzar la ejecución de nuestro OAuth2UserService
 */
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private OAuth2UserService oAuth2UserService;
    
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            
            try {
                // Obtener el ClientRegistration
                String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
                ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);
                
                if (clientRegistration != null) {
                    // Procesar usuario OAuth2 con datos existentes del token
                    OAuth2User currentOAuth2User = oauth2Token.getPrincipal();
                    OAuth2User processedUser = oAuth2UserService.processOAuth2UserFromToken(currentOAuth2User);
                    
                    // Crear nuevo token con autoridades actualizadas
                    OAuth2AuthenticationToken newToken = new OAuth2AuthenticationToken(
                        processedUser, 
                        processedUser.getAuthorities(), 
                        oauth2Token.getAuthorizedClientRegistrationId()
                    );
                    
                    // Actualizar Security Context
                    SecurityContextHolder.getContext().setAuthentication(newToken);
                }
                
            } catch (Exception e) {
                System.err.println("❌ ERROR EN SUCCESS HANDLER: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Redirigir al dashboard
        response.sendRedirect("/dashboard");
    }
}