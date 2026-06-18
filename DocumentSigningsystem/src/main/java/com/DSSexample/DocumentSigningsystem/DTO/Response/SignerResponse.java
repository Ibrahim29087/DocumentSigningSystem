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
public class SignerResponse {

    private String id;
    private String userId;
    private String userFullName;
    private String userEmail;
    private String status;
    private LocalDateTime signedAt;
    private LocalDateTime createdAt;
}