package com.DSSexample.DocumentSigningsystem.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignDocumentRequest {

    @NotBlank(message = "Signer ID is required")
    private String signerId;

    @NotBlank(message = "Signature hash is required")
    private String signatureHash;

    private String remarks;

    private String ipAddress;
}