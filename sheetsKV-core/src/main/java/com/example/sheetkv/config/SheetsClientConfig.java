package com.example.sheetkv.config;

import java.io.FileInputStream;
import java.io.InputStream;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.example.sheetkv.exception.BadRequestException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsRequestInitializer;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Configuration
@EnableConfigurationProperties(SheetProperties.class)
public class SheetsClientConfig {

    @Bean
    public Sheets sheetsClient(SheetProperties properties) throws Exception {
        var transport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = JacksonFactory.getDefaultInstance();
        if (properties.getServiceAccountJsonPath() != null && !properties.getServiceAccountJsonPath().isBlank()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    openServiceAccountStream(properties.getServiceAccountJsonPath()))
                    .createScoped(SheetsScopes.SPREADSHEETS);
            return new Sheets.Builder(transport, jsonFactory, new HttpCredentialsAdapter(credentials))
                    .setApplicationName("sheetkv")
                    .build();
        }

        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BadRequestException("Missing sheet.apiKey or sheet.serviceAccountJsonPath");
        }

        return new Sheets.Builder(transport, jsonFactory, request -> {
        })
                .setGoogleClientRequestInitializer(new SheetsRequestInitializer(properties.getApiKey()))
                .setApplicationName("sheetkv")
                .build();
    }

    private InputStream openServiceAccountStream(String path) throws Exception {
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            return new ClassPathResource(resourcePath).getInputStream();
        }
        return new FileInputStream(path);
    }
}
