package com.DSSexample.DocumentSigningsystem.Repository;

import com.DSSexample.DocumentSigningsystem.Entity.SignatureEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignatureEventRepository extends JpaRepository<SignatureEvent, String> {

    List<SignatureEvent> findByDocumentIdOrderByCreatedAtAsc(String documentId);

    List<SignatureEvent> findByUserId(String userId);

    List<SignatureEvent> findBySignatureRequestId(String signatureRequestId);
}