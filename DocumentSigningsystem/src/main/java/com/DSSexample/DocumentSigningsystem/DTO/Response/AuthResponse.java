package com.DSSexample.DocumentSigningsystem.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String userId;
    private String fullName;
    private String email;
    private String role;
}