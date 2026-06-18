package com.DSSexample.DocumentSigningsystem.Service;

import com.DSSexample.DocumentSigningsystem.DTO.Response.DocumentResponse;
import com.DSSexample.DocumentSigningsystem.Entity.Document;
import com.DSSexample.DocumentSigningsystem.Entity.Document.DocumentStatus;
import com.DSSexample.DocumentSigningsystem.Entity.User;
import com.DSSexample.DocumentSigningsystem.Repository.DocumentRepository;
import com.DSSexample.DocumentSigningsystem.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    // Upload new document
    public DocumentResponse uploadDocument(
            String title,
            String fileName,
            String filePath,
            String fileHash,
            String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Document document = Document.builder()
                .title(title)
                .fileName(fileName)
                .filePath(filePath)
                .fileHash(fileHash)
                .owner(owner)
                .status(DocumentStatus.DRAFT)
                .build();

        documentRepository.save(document);

        return mapToResponse(document);
    }

    // Get all documents for logged in user
    public List<DocumentResponse> getMyDocuments(String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return documentRepository.findByOwnerId(owner.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get single document by ID
    public DocumentResponse getDocumentById(String documentId, String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(document);
    }

    // Update document status
    public DocumentResponse updateStatus(String documentId, DocumentStatus newStatus, String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Access denied");
        }

        document.setStatus(newStatus);
        documentRepository.save(document);

        return mapToResponse(document);
    }

    // Delete document
    public void deleteDocument(String documentId, String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (!document.getStatus().equals(DocumentStatus.DRAFT)) {
            throw new RuntimeException("Cannot delete document that is not in DRAFT state");
        }

        documentRepository.delete(document);
    }

    // DTO mapping
    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .fileName(document.getFileName())
                .filePath(document.getFilePath())
                .fileHash(document.getFileHash())
                .status(document.getStatus().name())
                .ownerId(document.getOwner().getId())
                .ownerName(document.getOwner().getFullName())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .expiresAt(document.getExpiresAt())
                .build();
    }
}