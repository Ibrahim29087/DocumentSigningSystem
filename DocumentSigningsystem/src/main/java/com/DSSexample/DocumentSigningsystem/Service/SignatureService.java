package com.DSSexample.DocumentSigningsystem.Service;

import com.DSSexample.DocumentSigningsystem.DTO.Request.SignDocumentRequest;
import com.DSSexample.DocumentSigningsystem.DTO.Response.SignatureEventResponse;
import com.DSSexample.DocumentSigningsystem.Entity.Document;
import com.DSSexample.DocumentSigningsystem.Entity.Document.DocumentStatus;
import com.DSSexample.DocumentSigningsystem.Entity.SignatureEvent;
import com.DSSexample.DocumentSigningsystem.Entity.SignatureRequest;
import com.DSSexample.DocumentSigningsystem.Entity.SignatureRequest.RequestStatus;
import com.DSSexample.DocumentSigningsystem.Entity.Signer;
import com.DSSexample.DocumentSigningsystem.Entity.Signer.SignerStatus;
import com.DSSexample.DocumentSigningsystem.Entity.User;
import com.DSSexample.DocumentSigningsystem.Repository.DocumentRepository;
import com.DSSexample.DocumentSigningsystem.Repository.SignatureEventRepository;
import com.DSSexample.DocumentSigningsystem.Repository.SignatureRequestRepository;
import com.DSSexample.DocumentSigningsystem.Repository.SignerRepository;
import com.DSSexample.DocumentSigningsystem.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final SignerRepository signerRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final SignatureRequestRepository signatureRequestRepository;
    private final SignatureEventRepository signatureEventRepository;

    // Sign a document
    public SignatureEventResponse signDocument(SignDocumentRequest request, String signerEmail) {

        // Fetch signer user
        User signerUser = userRepository.findByEmail(signerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch signer record
        Signer signer = signerRepository.findById(request.getSignerId())
                .orElseThrow(() -> new RuntimeException("Signer record not found"));

        // Check signer matches logged in user
        if (!signer.getUser().getId().equals(signerUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        // Check signer status
        if (!signer.getStatus().equals(SignerStatus.PENDING)) {
            throw new RuntimeException("Document already signed or not eligible");
        }

        // Fetch document
        Document document = signer.getSignatureRequest().getDocument();

        // Verify signature using RSA public key
        boolean isValid = verifySignature(
                document.getFileHash(),
                request.getSignatureHash(),
                signerUser.getPublicKey());

        if (!isValid) {
            throw new RuntimeException("Invalid signature — verification failed");
        }

        // Update signer status
        signer.setStatus(SignerStatus.SIGNED);
        signer.setSignedAt(LocalDateTime.now());
        signerRepository.save(signer);

        // Log signature event — append only
        SignatureEvent event = SignatureEvent.builder()
                .document(document)
                .user(signerUser)
                .signatureRequest(signer.getSignatureRequest())
                .eventType("SIGNED")
                .signatureHash(request.getSignatureHash())
                .ipAddress(request.getIpAddress())
                .remarks(request.getRemarks())
                .build();

        signatureEventRepository.save(event);

        // Check if all signers have signed
        checkAndCompleteRequest(signer.getSignatureRequest(), document);

        return mapToResponse(event);
    }

    public List<Map<String, Object>> getPendingSignaturesForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Signer> pendingSigners = signerRepository
                .findByUserIdAndStatus(user.getId(), Signer.SignerStatus.PENDING);

        return pendingSigners.stream().map(signer -> {
            Map<String, Object> result = new HashMap<>();
            result.put("signerId", signer.getId());
            result.put("documentId", signer.getSignatureRequest().getDocument().getId());
            result.put("documentTitle", signer.getSignatureRequest().getDocument().getTitle());
            result.put("documentFileName", signer.getSignatureRequest().getDocument().getFileName());
            result.put("documentFileHash", signer.getSignatureRequest().getDocument().getFileHash());
            result.put("requestedBy", signer.getSignatureRequest().getCreatedBy().getFullName());
            result.put("createdAt", signer.getCreatedAt());
            return result;
        }).collect(Collectors.toList());
    }

    // Decline signing
    public SignatureEventResponse declineDocument(String signerId, String signerEmail, String remarks) {

        User signerUser = userRepository.findByEmail(signerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Signer signer = signerRepository.findById(signerId)
                .orElseThrow(() -> new RuntimeException("Signer record not found"));

        if (!signer.getUser().getId().equals(signerUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (!signer.getStatus().equals(SignerStatus.PENDING)) {
            throw new RuntimeException("Already responded to this request");
        }

        // Update signer status
        signer.setStatus(SignerStatus.DECLINED);
        signerRepository.save(signer);

        // Log decline event
        SignatureEvent event = SignatureEvent.builder()
                .document(signer.getSignatureRequest().getDocument())
                .user(signerUser)
                .signatureRequest(signer.getSignatureRequest())
                .eventType("DECLINED")
                .remarks(remarks)
                .build();

        signatureEventRepository.save(event);

        checkAndCompleteRequest(signer.getSignatureRequest(), signer.getSignatureRequest().getDocument());


        return mapToResponse(event);
    }

    // Get full audit log for a document
    public List<SignatureEventResponse> getAuditLog(String documentId) {
        return signatureEventRepository
                .findByDocumentIdOrderByCreatedAtAsc(documentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // RSA signature verification
    private boolean verifySignature(String fileHash, String signatureHash, String publicKeyStr) {
        try {
            // Decode public key from Base64
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // Verify signature
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(fileHash.getBytes());

            byte[] signatureBytes = Base64.getDecoder().decode(signatureHash);
            return signature.verify(signatureBytes);

        } catch (Exception e) {
            throw new RuntimeException("Signature verification error: " + e.getMessage());
        }
    }

    // Check if all signers signed — update statuses accordingly
    private void checkAndCompleteRequest(SignatureRequest signatureRequest, Document document) {

        List<Signer> allSigners = signerRepository
                .findBySignatureRequestId(signatureRequest.getId());

        boolean allSigned = allSigners.stream()
                .allMatch(s -> s.getStatus().equals(SignerStatus.SIGNED));

        boolean anyDeclined = allSigners.stream()
                .anyMatch(s -> s.getStatus().equals(SignerStatus.DECLINED));

        if (allSigned) {
            signatureRequest.setStatus(RequestStatus.COMPLETED);
            document.setStatus(DocumentStatus.COMPLETED);
        } else if (anyDeclined) {
            signatureRequest.setStatus(RequestStatus.CANCELLED);
            document.setStatus(DocumentStatus.DRAFT);
        } else {
            signatureRequest.setStatus(RequestStatus.IN_PROGRESS);
            document.setStatus(DocumentStatus.IN_PROGRESS);
        }

        signatureRequestRepository.save(signatureRequest);
        documentRepository.save(document);
    }

    // DTO mapping
    private SignatureEventResponse mapToResponse(SignatureEvent event) {
        return SignatureEventResponse.builder()
                .id(event.getId())
                .documentId(event.getDocument().getId())
                .documentTitle(event.getDocument().getTitle())
                .userId(event.getUser().getId())
                .userFullName(event.getUser().getFullName())
                .eventType(event.getEventType())
                .signatureHash(event.getSignatureHash())
                .ipAddress(event.getIpAddress())
                .remarks(event.getRemarks())
                .createdAt(event.getCreatedAt())
                .build();
    }

    public Map<String, Object> verifyDocument(String documentId, MultipartFile file) {
        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            // Compute hash of uploaded file
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(file.getBytes());
            String uploadedFileHash = HexFormat.of().formatHex(hashBytes);

            // Compare with stored hash
            boolean isIntact = uploadedFileHash.equals(document.getFileHash());

            // Get audit events
            List<SignatureEvent> events = signatureEventRepository
                    .findByDocumentIdOrderByCreatedAtAsc(documentId);

            List<Map<String, Object>> eventList = events.stream().map(event -> {
                Map<String, Object> e = new HashMap<>();
                e.put("eventType", event.getEventType());
                e.put("signedBy", event.getUser().getFullName());
                e.put("signatureHash", event.getSignatureHash());
                e.put("ipAddress", event.getIpAddress());
                e.put("remarks", event.getRemarks());
                e.put("createdAt", event.getCreatedAt());
                return e;
            }).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("documentTitle", document.getTitle());
            result.put("documentFileName", document.getFileName());
            result.put("storedHash", document.getFileHash());
            result.put("uploadedHash", uploadedFileHash);
            result.put("isIntact", isIntact);
            result.put("tampered", !isIntact);
            result.put("signingEvents", eventList);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Verification failed: " + e.getMessage());
        }
    }

}