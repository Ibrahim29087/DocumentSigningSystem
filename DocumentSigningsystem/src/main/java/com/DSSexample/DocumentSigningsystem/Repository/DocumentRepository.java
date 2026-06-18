package com.DSSexample.DocumentSigningsystem.Repository;

import com.DSSexample.DocumentSigningsystem.Entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public  interface DocumentRepository extends JpaRepository<Document,String> {

    List<Document> findByOwnerId(String ownerId);

    List<Document> findByOwnerIdAndStatus(String ownerId, Document.DocumentStatus status);

    boolean existsByIdAndOwnerId(String id, String ownerId);

}
