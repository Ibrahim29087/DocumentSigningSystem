package com.DSSexample.DocumentSigningsystem.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SignatureRequestDto {

    @NotBlank(message = "Document ID is required")
    private String documentId;

    @NotEmpty(message = "At least one signer is required")
    private List<String> signerUserIds;

    private Integer signingOrder;

    private LocalDateTime expiresAt;
}