package com.DSSexample.DocumentSigningsystem.Controller;

import com.DSSexample.DocumentSigningsystem.DTO.Response.DocumentResponse;
import com.DSSexample.DocumentSigningsystem.Entity.Document.DocumentStatus;
import com.DSSexample.DocumentSigningsystem.Service.DocumentService;
import com.DSSexample.DocumentSigningsystem.Service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Store file and compute hash
        String filePath = fileStorageService.storeFile(file);
        String fileHash = fileStorageService.computeFileHash(file);

        return ResponseEntity.ok(
                documentService.uploadDocument(
                        title,
                        file.getOriginalFilename(),
                        filePath,
                        fileHash,
                        userDetails.getUsername()
                )
        );
    }

        @GetMapping("/{id}/download")
        public ResponseEntity<Resource> download(
                @PathVariable String id,
                @AuthenticationPrincipal UserDetails userDetails) {

        // This also enforces ownership — throws "Access denied" if not the owner
        DocumentResponse doc = documentService.getDocumentById(id, userDetails.getUsername());

        Resource resource = fileStorageService.loadFileAsResource(doc.getFilePath());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
        }

    @GetMapping("/my")
    public ResponseEntity<List<DocumentResponse>> getMyDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                documentService.getMyDocuments(userDetails.getUsername())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getById(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                documentService.getDocumentById(id, userDetails.getUsername())
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DocumentResponse> updateStatus(
            @PathVariable String id,
            @RequestParam DocumentStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                documentService.updateStatus(id, status, userDetails.getUsername())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        documentService.deleteDocument(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}