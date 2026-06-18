package com.DSSexample.DocumentSigningsystem.DTO.Response;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignatureEventResponse {

    private String id;
    private String documentId;
    private String documentTitle;
    private String userId;
    private String userFullName;
    private String eventType;
    private String signatureHash;
    private String ipAddress;
    private String remarks;
    private LocalDateTime createdAt;
}