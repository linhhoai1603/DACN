package com.example.documentmanagementbackend.service.impl;

import com.example.documentmanagementbackend.dto.response.ImageKitUploadResponse;
import com.example.documentmanagementbackend.exception.BadRequestException;
import com.example.documentmanagementbackend.service.ImageKitService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ImageKitServiceImpl implements ImageKitService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${imagekit.private_key}")
    private String privateKey;

    @Override
    public ImageKitUploadResponse uploadFile(MultipartFile file, String folder) {
        try {
            if (privateKey == null || privateKey.isBlank()) {
                throw new BadRequestException("IMAGEKIT_PRIVATE_KEY is not configured");
            }

            String endpoint = "https://upload.imagekit.io/api/v1/files/upload";
            String base64File = Base64.getEncoder().encodeToString(file.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            String auth = Base64.getEncoder().encodeToString((privateKey + ":").getBytes(StandardCharsets.UTF_8));
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + auth);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", base64File);
            body.add("fileName", file.getOriginalFilename());
            body.add("folder", folder);
            body.add("useUniqueFileName", "true");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, requestEntity, String.class);
            JsonNode responseJson = objectMapper.readTree(response.getBody());

            return new ImageKitUploadResponse(
                    responseJson.path("fileId").asText(),
                    responseJson.path("url").asText(),
                    responseJson.path("name").asText(file.getOriginalFilename()),
                    responseJson.path("size").asLong(file.getSize())
            );
        } catch (Exception ex) {
            throw new BadRequestException("ImageKit upload failed: " + ex.getMessage());
        }
    }
}
