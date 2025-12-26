package com.robsartin.graphs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Security configuration for OAuth2 login with Google.
 * Requires authentication for all endpoints except public resources.
 * Includes form-based login for development when OAuth2 is not configured.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Value("${app.dev-user.enabled:true}")
    private boolean devUserEnabled;

    @Value("${app.dev-user.username:dev}")
    private String devUsername;

    @Value("${app.dev-user.password:dev}")
    private String devPassword;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Public resources - no authentication required
                .requestMatchers("/", "/index.html", "/error").permitAll()
                .requestMatchers("/css/**", "/js/**", "/assets/**", "/favicon.ico").permitAll()
                .requestMatchers("/oauth2/**", "/login/**", "/api/csrf").permitAll()
                // Actuator health endpoint for monitoring
                .requestMatchers("/actuator/health").permitAll()
                // H2 console for development
                .requestMatchers("/h2-console/**").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/")
                .successHandler(oauth2AuthenticationSuccessHandler())
                .failureHandler((request, response, exception) -> {
                    logger.error("OAuth2 authentication failed: {}", exception.getMessage());
                    response.sendRedirect("/?error=authentication_failed");
                })
            )
            .formLogin(form -> form
                .loginPage("/")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    logger.info("User authenticated via form login: {}", authentication.getName());
                    response.sendRedirect("/edit_graph");
                })
                .failureHandler((request, response, exception) -> {
                    logger.error("Form authentication failed: {}", exception.getMessage());
                    response.sendRedirect("/?error=invalid_credentials");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) -> {
                    logger.info("User logged out successfully");
                    response.sendRedirect("/");
                })
            )
            // Required for H2 console
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            );

        logger.info("Security filter chain configured successfully");
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                String name = oauth2User.getAttribute("name");
                // Log user info without sensitive data
                logger.info("User authenticated successfully: name={}, email={}",
                    name, maskEmail(email));
            } else {
                logger.info("User authenticated successfully");
            }
            response.sendRedirect("/edit_graph");
        };
    }

    /**
     * Masks email for logging purposes to avoid exposing full email addresses.
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "[unknown]";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***@" + (atIndex >= 0 ? email.substring(atIndex + 1) : "***");
        }
        return email.charAt(0) + "***@" + email.substring(atIndex + 1);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        if (!devUserEnabled) {
            logger.info("Development user disabled");
            return new InMemoryUserDetailsManager();
        }
        logger.info("Development user enabled: {}", devUsername);
        var user = User.builder()
                .username(devUsername)
                .password(passwordEncoder.encode(devPassword))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
