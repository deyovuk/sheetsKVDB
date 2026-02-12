package com.example.sheetkv.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.sheetkv.exception.BadRequestException;

@ConfigurationProperties(prefix = "sheet")
public class SheetProperties {
    private static final Pattern ID_PATTERN = Pattern.compile("/spreadsheets/d/([a-zA-Z0-9-_]+)");

    private String publicUrl;
    private String apiKey;
    private String serviceAccountJsonPath;
    private String spreadsheetId;

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
        this.spreadsheetId = parseSpreadsheetId(publicUrl);
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getServiceAccountJsonPath() {
        return serviceAccountJsonPath;
    }

    public void setServiceAccountJsonPath(String serviceAccountJsonPath) {
        this.serviceAccountJsonPath = serviceAccountJsonPath;
    }

    public String getSpreadsheetId() {
        return spreadsheetId;
    }

    public static String parseSpreadsheetId(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) {
            return null;
        }
        Matcher matcher = ID_PATTERN.matcher(publicUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        if (publicUrl.matches("^[a-zA-Z0-9-_]+$")) {
            return publicUrl;
        }
        throw new BadRequestException("Invalid sheet.publicUrl; unable to parse spreadsheet id");
    }
}
