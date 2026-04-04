package com.example.documentmanagementbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadVersionRequest {

    @NotBlank(message = "commitMessage is required")
    private String commitMessage;
}
