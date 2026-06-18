package com.DSSexample.DocumentSigningsystem.Controller;

import com.DSSexample.DocumentSigningsystem.DTO.Request.SignDocumentRequest;
import com.DSSexample.DocumentSigningsystem.DTO.Response.SignatureEventResponse;
import com.DSSexample.DocumentSigningsystem.Service.SignatureGeneratorService;
import com.DSSexample.DocumentSigningsystem.Service.SignatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/signatures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SignatureController {

    private final SignatureService signatureService;

    @PostMapping("/sign")
    public ResponseEntity<SignatureEventResponse> sign(
            @Valid @RequestBody SignDocumentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                signatureService.signDocument(request, userDetails.getUsername())
        );
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingSignatures(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                signatureService.getPendingSignaturesForUser(userDetails.getUsername())
        );
    }

    @PostMapping("/decline/{signerId}")
    public ResponseEntity<SignatureEventResponse> decline(
            @PathVariable String signerId,
            @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                signatureService.declineDocument(signerId, userDetails.getUsername(), remarks)
        );
    }

    private final SignatureGeneratorService signatureGeneratorService;

    // Test endpoint — generate signature hash using private key
    @PostMapping("/generate-signature")
    public ResponseEntity<Map<String, String>> generateSignature(
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(
                signatureGeneratorService.signFileHash(
                        request.get("fileHash"),
                        request.get("privateKey")
                )
        );
    }

    @GetMapping("/audit/{documentId}")
    public ResponseEntity<List<SignatureEventResponse>> getAuditLog(
            @PathVariable String documentId) {
        return ResponseEntity.ok(
                signatureService.getAuditLog(documentId)
        );
    }

    @PostMapping("/verify/{documentId}")
    public ResponseEntity<Map<String, Object>> verifyDocument(
            @PathVariable String documentId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(
                signatureService.verifyDocument(documentId, file)
        );
    }
}