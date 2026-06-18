package com.DSSexample.DocumentSigningsystem.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentUploadRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "File name is required")
    private String fileName;
}