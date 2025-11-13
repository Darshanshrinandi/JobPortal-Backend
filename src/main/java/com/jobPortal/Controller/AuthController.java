package com.jobPortal.Controller;

import com.jobPortal.Jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/jobPortal")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing or invalid Authorization header"));
        }

        String oldToken = authHeader.substring(7);

        if (!jwtService.validateToken(oldToken)) {
            return ResponseEntity.status(401).body(Map.of("error", "Token expired or invalid"));
        }

        String newToken = jwtService.refreshToken(oldToken);
        return ResponseEntity.ok(Map.of("token", newToken));
    }
}
