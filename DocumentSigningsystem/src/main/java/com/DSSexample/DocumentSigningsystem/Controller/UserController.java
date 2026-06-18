package com.DSSexample.DocumentSigningsystem.Controller;

import com.DSSexample.DocumentSigningsystem.Repository.UserRepository;
import com.DSSexample.DocumentSigningsystem.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/find")
    public ResponseEntity<Map<String, String>> findUserByEmail(
            @RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    Map<String, String> result = new HashMap<>();
                    result.put("userId", user.getId());
                    result.put("fullName", user.getFullName());
                    result.put("email", user.getEmail());
                    return ResponseEntity.ok(result);
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Generate RSA key pair
    @PostMapping("/generate-keys")
    public ResponseEntity<Map<String, String>> generateKeys(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                userService.generateAndStoreKeyPair(userDetails.getUsername())
        );
    }

    // Manually update public key
    @PutMapping("/public-key")
    public ResponseEntity<String> updatePublicKey(
            @RequestBody String publicKey,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.updatePublicKey(userDetails.getUsername(), publicKey);
        return ResponseEntity.ok("Public key updated successfully");
    }
}