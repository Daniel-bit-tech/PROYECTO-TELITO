package com.example.telitodev.config;
import com.example.telitodev.service.UsuarioDetailService;
import com.example.telitodev.filter.UsuarioActivoFilter;
import com.example.telitodev.filter.ImpersonationAuthorizationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.util.Collection;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UsuarioDetailService usuarioDetailService;

    @Autowired
    private UsuarioActivoFilter usuarioActivoFilter;

    @Autowired
    @Qualifier("oauth2UserServiceBean")
    private org.springframework.security.oauth2.client.userinfo.OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserServiceBean;

    @Autowired
    private ImpersonationAuthorizationFilter impersonationAuthorizationFilter;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {

        http
                .authorizeHttpRequests(authz -> authz
                        // Públicas
                        .requestMatchers("/", "/login/**", "/registro", "/apis", "/tabler/**", "/forgot-password", "/register", "/confirmar-cuenta", "/confirmar-cuenta/**", "/oauth2-test", "/oauth2-debug").permitAll()
                        .requestMatchers("/oauth2/reset", "/oauth2/debug", "/oauth2-reset").permitAll()  // ⚡ ENDPOINTS OAUTH2 DEBUG
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**", "/static/**").permitAll()
                        .requestMatchers("/error", "/acceso-denegado").permitAll()
                        .requestMatchers("/api/reniec/**").permitAll()  // API RENIEC para autocompletar DNI

                        .requestMatchers("/cat","/playground.html").permitAll()


                        .requestMatchers("/api/apis/**").authenticated()
                        .requestMatchers("/api/onboarding/**").authenticated()
                        .requestMatchers("/api/validate-session").authenticated()
                        .requestMatchers("/api/session-debug/**").authenticated()


                        //  la página y la API del Sandbox
                        .requestMatchers("/dev/sandbox", "/api/sandbox/**").hasAnyRole("DEV", "QA", "SUPERADMIN")
                        // API endpoints - requieren autenticación pero sin CSRF



                        // Endpoints de impersonación - reglas específicas
                        .requestMatchers("/admin/gestion-usuarios/stop-impersonation").hasAnyRole("SUPERADMIN", "QA", "DEV", "PO")
                        .requestMatchers("/admin/gestion-usuarios/impersonation-status").hasAnyRole("SUPERADMIN", "QA", "DEV", "PO")
                        .requestMatchers("/admin/gestion-usuarios/**").hasRole("SUPERADMIN")
                        
                        // Portales - reglas de seguridad tradicionales (el filtro maneja la impersonación)
                        .requestMatchers("/admin/**").hasRole("SUPERADMIN")
                        .requestMatchers("/dev/**").hasAnyRole("DEV", "DEVELOPER", "SUPERADMIN")
                        .requestMatchers("/qa/**").hasAnyRole("QA", "SUPERADMIN")
                        .requestMatchers("/po/**").hasAnyRole("PO", "SUPERADMIN")
                        .requestMatchers("/qa-dev/**").hasAnyRole("QA", "DEV", "DEVELOPER")

                        // Otras rutas
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestRepository(authorizationRequestRepository())
                        )
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserServiceBean))
                        .successHandler(oauth2AuthenticationSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            // Log error y redirigir
                            exception.printStackTrace();
                            response.sendRedirect("/login?error=oauth2&detail=" + exception.getClass().getSimpleName());
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("remember-me-telito-key-2025")
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(604800) // 7 días
                        .userDetailsService(usuarioDetailService))
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/acceso-denegado")
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/qa/**",
                                "/api/onboarding/**",
                                "/po/registrarFeedbackEnBacklog",
                                "/po/roadmap/**",
                                "/api/sandbox/**"
                        )
                )
                // Control de sesiones concurrentes y seguridad de sesión
                .sessionManagement(session -> session
                        .sessionFixation().changeSessionId() // Cambiar ID de sesión después del login
                        .invalidSessionUrl("/login?invalid=true")
                        .maximumSessions(5)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry)
                        .expiredUrl("/login?expired=true")
                )
                // Agregar filtros personalizados
                .addFilterBefore(usuarioActivoFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(impersonationAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //    @Bean
//    public AuthenticationProvider authenticationManager(UsuarioDetailService usuarioDetailService) {
//        DaoAuthenticationProvider authProv = new DaoAuthenticationProvider();
//        authProv.setUserDetailsService(usuarioDetailService);
//        authProv.setPasswordEncoder(passwordEncoder());
//        return authProv;
//    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(usuarioDetailService).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
                System.out.println("=== AuthenticationSuccessHandler ejecutado ===");
                System.out.println("Usuario: " + authentication.getName());

                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                System.out.println("Autoridades encontradas: " + authorities);

                for (GrantedAuthority authority : authorities) {
                    String role = authority.getAuthority();
                    System.out.println("Procesando rol: " + role);

                    switch (authority.getAuthority()) {
                        case "ROLE_SUPERADMIN":
                            System.out.println("Redirigiendo SUPERADMIN a /admin/home");
                            return "/admin/home";
                        case "ROLE_DEV":
                        case "ROLE_DEVELOPER":
                            System.out.println("Redirigiendo DEVELOPER a /dev/home");
                            return "/dev/home";
                        case "ROLE_QA":
                            System.out.println("Redirigiendo QA a /qa/home");
                            return "/qa/home";
                        case "ROLE_PO":
                            System.out.println("Redirigiendo PO a /po/home");
                            return "/po/home";

                        default:
                            System.out.println("Rol no reconocido: " + role);
                            break;
                    }
                }
                System.out.println("Ningún rol válido encontrado, redirigiendo a /login");
                return "/login";
            }
        };
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    /**
     * Bean explícito para OAuth2UserService personalizado
     * Forzamos que Spring use nuestro servicio en lugar del DefaultOAuth2UserService
     * Usamos la instancia ya existente con dependencias inyectadas
     */
    /*
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService() {
        System.out.println("🔧 CONFIGURANDO BEAN OAUTH2USERSERVICE - Bean creado con: " + oAuth2UserService.getClass().getName());

        // Crear un proxy/wrapper para detectar cuándo se invoca
        return new OAuth2UserService<OAuth2UserRequest, OAuth2User>() {
            @Override
            public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                System.out.println("🎯 WRAPPER OAUTH2USERSERVICE - MÉTODO loadUser() INVOCADO!");
                System.out.println("   - Request: " + userRequest.getClientRegistration().getRegistrationId());
                return oAuth2UserService.loadUser(userRequest);
            }
        };
    }
    */

    /**
     * Bean requerido para el manejo de eventos de sesión HTTP
     * Necesario para el funcionamiento correcto del SessionRegistry
     */
    @Bean
    public org.springframework.security.web.session.HttpSessionEventPublisher httpSessionEventPublisher() {
        return new org.springframework.security.web.session.HttpSessionEventPublisher();
    }

    /**
     * Bean para manejar las solicitudes de autorización OAuth2
     * Esto es necesario para resolver el error "authorization_request_not_found"
     * Usa HttpSession con configuración específica para OAuth2
     */
    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }
//    @Bean
//    public RestTemplate restTemplate(RestTemplateBuilder builder) {
//        return builder.build();
//    }
    @Bean
    public RestTemplate restTemplate() throws Exception {

        javax.net.ssl.SSLContext sslContext = org.apache.hc.core5.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();

        org.apache.hc.client5.http.ssl.NoopHostnameVerifier hostnameVerifier = org.apache.hc.client5.http.ssl.NoopHostnameVerifier.INSTANCE;
        org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory sslsf =
                new org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory(sslContext, hostnameVerifier);

        org.apache.hc.client5.http.impl.classic.CloseableHttpClient httpClient = org.apache.hc.client5.http.impl.classic.HttpClients.custom()
                .setConnectionManager(org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(sslsf)
                        .build())
                .build();


        org.springframework.http.client.HttpComponentsClientHttpRequestFactory requestFactory =
                new org.springframework.http.client.HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(requestFactory);
    }

}