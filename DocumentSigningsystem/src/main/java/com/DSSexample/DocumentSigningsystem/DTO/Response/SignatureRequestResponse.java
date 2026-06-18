package com.DSSexample.DocumentSigningsystem.DTO.Response;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignatureRequestResponse {

    private String id;
    private String documentId;
    private String documentTitle;
    private String createdById;
    private String createdByName;
    private String status;
    private Integer signingOrder;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private List<SignerResponse> signers;
}