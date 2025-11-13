package com.jobPortal.Jwt;

import com.jobPortal.Model.Company;
import com.jobPortal.Model.User;
import com.jobPortal.Security.CompanyDetailsImpl;
import com.jobPortal.Security.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        System.out.println("Request Path: " + path);

        // Public endpoints skip authentication
        if (path.startsWith("/jobPortal/public/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // Check cookie first
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwtToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Fallback: check header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtService.extractEmail(token);
            Long id = jwtService.extractId(token);
            String type = jwtService.extractType(token);
            String role = jwtService.extractRole(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                Object principal;

                if ("COMPANY".equalsIgnoreCase(type)) {
                    Company company = new Company();
                    company.setCompanyId(id);
                    company.setEmail(email);
                    principal = new CompanyDetailsImpl(company);
                } else {
                    User user = new User();
                    user.setUserId(id);
                    user.setEmail(email);
                    principal = new UserDetailsImpl(user);
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                principal, null,
                                Collections.singleton(() -> "ROLE_" + role.toUpperCase())
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            System.out.println("❌ JWT validation error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
