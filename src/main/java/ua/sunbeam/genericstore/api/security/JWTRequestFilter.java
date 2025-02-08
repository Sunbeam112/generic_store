package ua.sunbeam.genericstore.api.security;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ua.sunbeam.genericstore.model.DAO.UserRepository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.service.UserDetailsService;

import java.io.IOException;
import java.util.Optional;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {
    private final JWTUtils jwtUtils;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    public JWTRequestFilter(JWTUtils jwtUtils, UserRepository userRepository, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tokenHeader = request.getHeader("Authorization");
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            if (token.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Token is empty");
            } else {
                try {
                    String emailFromToken = jwtUtils.getEmailFromToken(token);
                    Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(emailFromToken);
                    if (opUser.isPresent()) {
                        LocalUser user = opUser.get();
                        UserDetails userDetails = userDetailsService.loadUserByUsername(emailFromToken);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                emailFromToken, userDetails.getPassword(), userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        if (SecurityContextHolder.getContext().getAuthentication() == null) {
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                } catch (JWTDecodeException ignored) {

                } catch (JWTVerificationException ex) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT Token");
                }
            }
        }
        filterChain.doFilter(request, response);

    }
}
