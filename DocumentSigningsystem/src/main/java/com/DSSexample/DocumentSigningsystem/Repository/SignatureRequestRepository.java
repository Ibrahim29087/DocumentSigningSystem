package com.DSSexample.DocumentSigningsystem.Repository;

import com.DSSexample.DocumentSigningsystem.Entity.SignatureRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignatureRequestRepository extends JpaRepository<SignatureRequest, String> {

    List<SignatureRequest> findByDocumentId(String documentId);

    List<SignatureRequest> findByCreatedById(String userId);

    List<SignatureRequest> findByDocumentIdAndStatus(String documentId, SignatureRequest.RequestStatus status);
}