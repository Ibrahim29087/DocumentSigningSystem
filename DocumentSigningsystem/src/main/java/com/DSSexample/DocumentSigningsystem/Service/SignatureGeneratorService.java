package com.DSSexample.DocumentSigningsystem.Service;

import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class SignatureGeneratorService {

    // Sign file hash with private key — for testing purposes
    public Map<String, String> signFileHash(String fileHash, String privateKeyBase64) {
        try {
            // Decode private key
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Sign the file hash
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(fileHash.getBytes());
            byte[] signatureBytes = signature.sign();

            // Encode signature to Base64
            String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);

            Map<String, String> result = new HashMap<>();
            result.put("signatureHash", signatureBase64);
            result.put("fileHash", fileHash);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Signing failed: " + e.getMessage());
        }
    }
}