package ua.sunbeam.genericstore.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ua.sunbeam.genericstore.service.UserDetailsService;

import java.util.List;

@Configuration
@EnableWebMvc
@EnableWebSecurity
public class WebSecurityConfig {

    private final JWTRequestFilter jwtRequestFilter;
    private final UserDetailsService userDetailsService;

    public WebSecurityConfig(JWTRequestFilter jwtRequestFilter, UserDetailsService userDetailsService) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String[] allowedToAllPaths = {"/product/all", "/product/name=**", "/product/category=**",
                "/product/category/all", "/product/id=**", "/product/importFromCSV**", "/product/importFromCSVAndSave**", "/product/export-csv**", "/auth/v1/register", "/auth/v1/login", "/auth/v1/verify",
                "/auth/v1/reset_password**", "/auth/v1/forgot_password**"};

        String[] allowedToAdminPaths = {"/product/add", "/product/delete/**", "/product/update/**", "/admin/order/create**", "/admin/order/**"};
        String[] allowedToUserPaths = {"/auth/v1/me"};

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(allowedToUserPaths).hasRole("USER")
                        .requestMatchers(allowedToAdminPaths).hasRole("ADMIN")
                        .requestMatchers(allowedToAllPaths).permitAll()
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer
                        .authenticationEntryPoint(authenticationEntryPoint()) // Use the custom entry point
                        .accessDeniedHandler(accessDeniedHandler()) // Use the custom access denied handler
                )
                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Custom Authentication Entry Point for unauthenticated users
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            // Log the attempt for debugging (optional)
            System.out.println("Unauthenticated access attempt to " + request.getRequestURI() + ". Redirecting to login.");
            // Redirect to your frontend login page
            response.sendRedirect("http://localhost:3000/login");
        };
    }

    // Custom Access Denied Handler for authenticated but unauthorized users
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            // Log the attempt for debugging (optional)
            System.out.println("Unauthorized access attempt by authenticated user to " + request.getRequestURI() + ". Redirecting to login.");
            // Redirect to your frontend login page (or a specific "access denied" page)
            // It's often good practice to redirect to login if the user's current role isn't enough,
            // as they might need to log in as a different user or understand they don't have permission.
            response.sendRedirect("http://localhost:3000/login");
        };
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //Make the below setting as * to allow connection from any host
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}