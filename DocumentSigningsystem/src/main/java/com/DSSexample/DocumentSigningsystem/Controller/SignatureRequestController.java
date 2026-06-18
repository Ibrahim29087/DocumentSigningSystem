package com.DSSexample.DocumentSigningsystem.Controller;

import com.DSSexample.DocumentSigningsystem.DTO.Request.SignatureRequestDto;
import com.DSSexample.DocumentSigningsystem.DTO.Response.SignatureRequestResponse;
import com.DSSexample.DocumentSigningsystem.Service.SignatureRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/signature-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SignatureRequestController {

    private final SignatureRequestService signatureRequestService;

    @PostMapping
    public ResponseEntity<SignatureRequestResponse> createRequest(
            @Valid @RequestBody SignatureRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                signatureRequestService.createRequest(request, userDetails.getUsername())
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<SignatureRequestResponse>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                signatureRequestService.getMyRequests(userDetails.getUsername())
        );
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<SignatureRequestResponse>> getByDocument(
            @PathVariable String documentId) {
        return ResponseEntity.ok(
                signatureRequestService.getRequestsByDocument(documentId)
        );
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<SignatureRequestResponse> cancelRequest(
            @PathVariable String requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                signatureRequestService.cancelRequest(requestId, userDetails.getUsername())
        );
    }
}