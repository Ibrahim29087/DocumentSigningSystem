package com.DSSexample.DocumentSigningsystem.Service;

import com.DSSexample.DocumentSigningsystem.Entity.User;
import com.DSSexample.DocumentSigningsystem.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Generate RSA key pair and store public key
    public Map<String, String> generateAndStoreKeyPair(String email) {
        try {
            // Generate RSA-2048 key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Encode keys to Base64
            String publicKeyBase64 = Base64.getEncoder()
                    .encodeToString(keyPair.getPublic().getEncoded());
            String privateKeyBase64 = Base64.getEncoder()
                    .encodeToString(keyPair.getPrivate().getEncoded());

            // Store public key in DB
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setPublicKey(publicKeyBase64);
            userRepository.save(user);

            // Return both keys — private key only shown once, never stored
            Map<String, String> keys = new HashMap<>();
            keys.put("publicKey", publicKeyBase64);
            keys.put("privateKey", privateKeyBase64);
            keys.put("message", "Save your private key safely — it will never be shown again");

            return keys;

        } catch (Exception e) {
            throw new RuntimeException("Key generation failed: " + e.getMessage());
        }
    }

    // Update public key manually
    public void updatePublicKey(String email, String publicKey) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPublicKey(publicKey);
        userRepository.save(user);
    }
}