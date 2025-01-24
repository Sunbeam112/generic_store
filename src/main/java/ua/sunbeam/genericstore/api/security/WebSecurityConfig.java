package ua.sunbeam.genericstore.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private JWTRequestFilter jwtRequestFilter;

    public WebSecurityConfig(JWTRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().disable();
        http.addFilterBefore(jwtRequestFilter, AuthenticationFilter.class);
        http.authorizeHttpRequests()
                .requestMatchers("/product/all", "/all-products.html" +
                        "/", "/auth/v1/register", "/auth/v1/login", "/auth/v1/verify").permitAll()
                .anyRequest()
                .permitAll();
        return http.build();
    }
}
