package com.DSSexample.DocumentSigningsystem.Repository;

import com.DSSexample.DocumentSigningsystem.Entity.Signer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface  SignerRepository extends JpaRepository<Signer,String> {
    List<Signer> findBySignatureRequestId(String signatureRequestId);

    List<Signer> findByUserId(String userId);

    List<Signer> findBySignatureRequestIdAndStatus(String signatureRequestId, Signer.SignerStatus status);

    List<Signer> findByUserIdAndStatus(String userId, Signer.SignerStatus status);

    boolean existsBySignatureRequestIdAndUserId(String signatureRequestId, String userId);
}
