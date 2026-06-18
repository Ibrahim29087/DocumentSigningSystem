package com.DSSexample.DocumentSigningsystem.Entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Signer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_request_id", nullable = false)
    private SignatureRequest signatureRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SignerStatus status;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = SignerStatus.PENDING;
    }

    public enum SignerStatus {
        PENDING, SIGNED, DECLINED, EXPIRED
    }
}