package com.example.telitodev.config;
import com.example.telitodev.service.UsuarioDetailService;
import com.example.telitodev.filter.UsuarioActivoFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.Collection;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioDetailService usuarioDetailService;

    @Autowired
    private UsuarioActivoFilter usuarioActivoFilter;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Públicas
                        .requestMatchers("/", "/login/**", "/registro", "/apis", "/tabler/**", "/forgot-password", "/register").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**", "/static/**").permitAll()
                        .requestMatchers("/error", "/acceso-denegado").permitAll()

                        .requestMatchers("/cat").permitAll()
                        .requestMatchers("/api/apis/**").authenticated()

                        //  la página y la API del Sandbox
                        .requestMatchers("/sandbox", "/qa/api/sandbox/**").hasAnyRole("DEV", "QA", "SUPERADMIN")


                        // API endpoints - requieren autenticación pero sin CSRF
                        .requestMatchers("/api/onboarding/**").authenticated()

                        // De rol
                        // Endpoints de impersonación - accesibles durante impersonación
                        .requestMatchers("/admin/gestion-usuarios/stop-impersonation").hasAnyRole("SUPERADMIN", "QA", "DEV", "PO")
                        .requestMatchers("/admin/gestion-usuarios/impersonation-status").hasAnyRole("SUPERADMIN", "QA", "DEV", "PO")
                        .requestMatchers("/admin/**").hasRole("SUPERADMIN")
                        .requestMatchers("/dev/**").hasAnyRole("DEV", "SUPERADMIN")
                        .requestMatchers("/qa/**").hasAnyRole("QA", "SUPERADMIN")
                        .requestMatchers("/po/**").hasAnyRole("PO", "SUPERADMIN")
                        .requestMatchers("/qa-dev/**").hasAnyRole("QA", "DEV")

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
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("remember-me")
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(86400)
                        .userDetailsService(usuarioDetailService))      //.tokenRepository(persistentTokenRepository(dataSource)) para cookies persistentes
//                .exceptionHandling(exception -> exception
//                        .accessDeniedPage("/acceso-denegado")
//                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/qa/**","/api/onboarding/**")
                )
                // Control de sesiones concurrentes y seguridad de sesión
                .sessionManagement(session -> session
                        .maximumSessions(2) // Máximo 2 sesiones por usuario admin
                        .maxSessionsPreventsLogin(false) // Permitir login, expulsar sesión más antigua
                        .sessionRegistry(sessionRegistry)
                        .expiredUrl("/login?expired=true")
                        .and()
                        .sessionFixation().none() // Prevenir session fixation attacks
                        .invalidSessionUrl("/login?invalid=true")
                )
                // Agregar filtro personalizado para verificar usuarios activos en tiempo real
                .addFilterBefore(usuarioActivoFilter, UsernamePasswordAuthenticationFilter.class);

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
                            System.out.println("Redirigiendo DEV a /dev/home");
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    /**
     * Bean requerido para el manejo de eventos de sesión HTTP
     * Necesario para el funcionamiento correcto del SessionRegistry
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }





}