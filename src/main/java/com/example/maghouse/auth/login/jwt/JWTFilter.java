package com.example.maghouse.auth.login.jwt;

import com.example.maghouse.auth.registration.token.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getServletPath().contains("/auth/**")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String header = request.getHeader("Authorization");
        if(header == null ||!header.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = header.substring(7);
        final String userEmail;
        try {
            userEmail = jwtService.extractUserEmail(jwt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null ){
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            var isValidToken = tokenRepository.findByToken(jwt)
                    .map(t -> !t.isExpired() && !t.isRevoked())
                    .orElse(false);
            try {
                if(jwtService.isValidToken(jwt, userDetails) && isValidToken) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("User authenticated: " + userEmail);


                    userDetails.getAuthorities().forEach(grantedAuthority -> {
                        System.out.println("Role: " + grantedAuthority.getAuthority());
                    });


                    boolean isAdmin = userDetails.getAuthorities().stream()
                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                    boolean isUser = userDetails.getAuthorities().stream()
                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));

                    if (isAdmin) {
                        System.out.println("Logged in user is an Admin");
                    } else if (isUser) {
                        System.out.println("Logged in user is a User");
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        filterChain.doFilter(request, response);
    }
}
