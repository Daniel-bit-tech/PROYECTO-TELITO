package com.example.telitodev.config;
import com.example.telitodev.service.UsuarioDetailService;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.util.Collection;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioDetailService usuarioDetailService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Públicas
                        .requestMatchers("/", "/login/**", "/registro", "/apis", "/tabler/**", "/forgot-password", "/register").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**", "/static/**").permitAll()
                        .requestMatchers("/error", "/acceso-denegado").permitAll()

                        .requestMatchers("/cat").permitAll()

                        // De rol
                        .requestMatchers("/admin/**").hasRole("SADMIN")
                        .requestMatchers("/dev/**").hasAnyRole("DEV", "SADMIN")
                        .requestMatchers("/qa/**").hasAnyRole("QA", "SADMIN")
                        .requestMatchers("/po/**").hasAnyRole("PO", "SADMIN")


                        // Otras rutas
                        .anyRequest().authenticated() // Requiere autenticación pero cualquier rol
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
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/acceso-denegado")
                );

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
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

                for (GrantedAuthority authority : authorities) {
                    String role = authority.getAuthority();

                    switch (authority.getAuthority()) {
                        case "ROLE_SADMIN":
                            return "/admin/home";
                        case "ROLE_DEV":
                            return "/dev/home";
                        case "ROLE_QA":
                            return "/qa/home";
                        case "ROLE_PO":
//                            return "/po/home";
                            return "/po/Dashboard";
                        default:
                            return "/login";
                    }
                }
                return "/login";
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

}