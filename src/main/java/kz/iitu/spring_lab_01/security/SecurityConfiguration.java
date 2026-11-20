package kz.iitu.spring_lab_01.security;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * Lab 12: the REST API (/api/**) is used by programs, not by the browser pages.
     * It is stateless and authenticates every request with an Authorization: Basic header.
     * The entry point answers 401 WITHOUT a WWW-Authenticate challenge, so a browser never
     * shows a login prompt and never caches the credentials: there is no ambient credential
     * (no session cookie, no cached Basic header) that a forged cross-site request could reuse,
     * which is why CSRF protection is disabled for this chain only.
     */
    @Bean
    @Order(1)
    SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")
                .requestMatchers("/api/reports/**").hasRole("ADMIN")
                .anyRequest().denyAll())
            .httpBasic(basic -> basic.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /** The browser pages: form login, session cookie, CSRF tokens (lab 10). */
    @Bean
    @Order(2)
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 1. public: home page, login page, static resources, error page
                .requestMatchers("/", "/login", "/css/**", "/error").permitAll()
                // 2. reading the catalogue: any logged-in user
                .requestMatchers(HttpMethod.GET, "/books/**").hasAnyRole("USER", "ADMIN")
                // 3. changing the catalogue (create, edit, delete): administrators only
                .requestMatchers("/books/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 4. everything nobody thought about is closed
                .anyRequest().denyAll())
            .formLogin(form -> form.permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/?logout"));
        return http.build();
    }

    /** Answers "is this the right password?". Declared explicitly, no {id} prefixes needed. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Variant 10: publishes AuthenticationSuccessEvent / AbstractAuthenticationFailureEvent. */
    @Bean
    AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }
}
