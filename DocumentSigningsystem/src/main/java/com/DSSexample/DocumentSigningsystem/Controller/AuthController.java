package com.DSSexample.DocumentSigningsystem.Controller;

import com.DSSexample.DocumentSigningsystem.DTO.Request.LoginRequest;
import com.DSSexample.DocumentSigningsystem.DTO.Request.RegisterRequest;
import com.DSSexample.DocumentSigningsystem.DTO.Response.AuthResponse;
import com.DSSexample.DocumentSigningsystem.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}