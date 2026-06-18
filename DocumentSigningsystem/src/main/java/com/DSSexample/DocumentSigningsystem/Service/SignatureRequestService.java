package com.DSSexample.DocumentSigningsystem.Service;

import com.DSSexample.DocumentSigningsystem.DTO.Request.SignatureRequestDto;
import com.DSSexample.DocumentSigningsystem.DTO.Response.SignatureRequestResponse;
import com.DSSexample.DocumentSigningsystem.DTO.Response.SignerResponse;
import com.DSSexample.DocumentSigningsystem.Entity.Document;
import com.DSSexample.DocumentSigningsystem.Entity.Document.DocumentStatus;
import com.DSSexample.DocumentSigningsystem.Entity.SignatureRequest;
import com.DSSexample.DocumentSigningsystem.Entity.SignatureRequest.RequestStatus;
import com.DSSexample.DocumentSigningsystem.Entity.Signer;
import com.DSSexample.DocumentSigningsystem.Entity.Signer.SignerStatus;
import com.DSSexample.DocumentSigningsystem.Entity.User;
import com.DSSexample.DocumentSigningsystem.Repository.DocumentRepository;
import com.DSSexample.DocumentSigningsystem.Repository.SignatureRequestRepository;
import com.DSSexample.DocumentSigningsystem.Repository.SignerRepository;
import com.DSSexample.DocumentSigningsystem.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SignatureRequestService {

    private final SignatureRequestRepository signatureRequestRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final SignerRepository signerRepository;

    // Create new signature request
    public SignatureRequestResponse createRequest(SignatureRequestDto request, String creatorEmail) {

        // Fetch creator
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch document
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Check ownership
        if (!document.getOwner().getId().equals(creator.getId())) {
            throw new RuntimeException("Access denied");
        }

        // Only DRAFT documents can have signature requests created
        if (!document.getStatus().equals(DocumentStatus.DRAFT)) {
            throw new RuntimeException("Document must be in DRAFT state to create signature request");
        }

        // Build signature request
        SignatureRequest signatureRequest = SignatureRequest.builder()
                .document(document)
                .createdBy(creator)
                .signingOrder(request.getSigningOrder())
                .expiresAt(request.getExpiresAt())
                .build();

        signatureRequestRepository.save(signatureRequest);

        // Add signers
        for (String signerUserId : request.getSignerUserIds()) {
            User signerUser = userRepository.findById(signerUserId)
                    .orElseThrow(() -> new RuntimeException("Signer user not found: " + signerUserId));

            Signer signer = Signer.builder()
                    .signatureRequest(signatureRequest)
                    .user(signerUser)
                    .status(SignerStatus.PENDING)
                    .build();

            signerRepository.save(signer);
        }

        // Update document status to PENDING
        document.setStatus(DocumentStatus.PENDING);
        documentRepository.save(document);

        return mapToResponse(signatureRequest);
    }

    // Get all signature requests for a document
    public List<SignatureRequestResponse> getRequestsByDocument(String documentId) {
        return signatureRequestRepository.findByDocumentId(documentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get all signature requests created by logged in user
    public List<SignatureRequestResponse> getMyRequests(String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return signatureRequestRepository.findByCreatedById(creator.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Cancel a signature request
    public SignatureRequestResponse cancelRequest(String requestId, String creatorEmail) {

        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SignatureRequest signatureRequest = signatureRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Signature request not found"));

        // Check ownership
        if (!signatureRequest.getCreatedBy().getId().equals(creator.getId())) {
            throw new RuntimeException("Access denied");
        }

        // Only PENDING requests can be cancelled
        if (!signatureRequest.getStatus().equals(RequestStatus.PENDING)) {
            throw new RuntimeException("Only pending requests can be cancelled");
        }

        signatureRequest.setStatus(RequestStatus.CANCELLED);
        signatureRequestRepository.save(signatureRequest);

        // Update document status back to DRAFT
        Document document = signatureRequest.getDocument();
        document.setStatus(DocumentStatus.DRAFT);
        documentRepository.save(document);

        return mapToResponse(signatureRequest);
    }

    // DTO mapping
    private SignatureRequestResponse mapToResponse(SignatureRequest signatureRequest) {

        List<SignerResponse> signerResponses = signerRepository
                .findBySignatureRequestId(signatureRequest.getId())
                .stream()
                .map(this::mapSignerToResponse)
                .collect(Collectors.toList());

        return SignatureRequestResponse.builder()
                .id(signatureRequest.getId())
                .documentId(signatureRequest.getDocument().getId())
                .documentTitle(signatureRequest.getDocument().getTitle())
                .createdById(signatureRequest.getCreatedBy().getId())
                .createdByName(signatureRequest.getCreatedBy().getFullName())
                .status(signatureRequest.getStatus().name())
                .signingOrder(signatureRequest.getSigningOrder())
                .createdAt(signatureRequest.getCreatedAt())
                .expiresAt(signatureRequest.getExpiresAt())
                .signers(signerResponses)
                .build();
    }

    // Signer DTO mapping
    private SignerResponse mapSignerToResponse(Signer signer) {
        return SignerResponse.builder()
                .id(signer.getId())
                .userId(signer.getUser().getId())
                .userFullName(signer.getUser().getFullName())
                .userEmail(signer.getUser().getEmail())
                .status(signer.getStatus().name())
                .signedAt(signer.getSignedAt())
                .createdAt(signer.getCreatedAt())
                .build();
    }
}