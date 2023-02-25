package org.olac.reservation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${security.debug}")
    private boolean securityDebug;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OlacProperties properties) throws Exception {
        http
                .csrf(CsrfConfigurer::disable)
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin.html")
                        .permitAll())
                .logout(withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));

        if (properties.isDisableSecurity()) {
            http.authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll());
        } else {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/css/**", "/js/**", "/img/**", "/lib/**", "/favicon.ico").permitAll()
                    .requestMatchers(HttpMethod.DELETE).hasRole("ADMIN")
                    .requestMatchers("/admin.html", "/api/admin/**", "/admin/**").hasAnyRole("ADMIN")
                    .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/login").anonymous()
                    .anyRequest().permitAll());
        }
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.debug(securityDebug);
    }

}
