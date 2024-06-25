package com.example.user_service.auth.login.jwt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

@Component
public class SecretPropertiesReader {

    private final Properties properties = new Properties();

    public SecretPropertiesReader() {
        try {
            ClassPathResource classPathResource = new ClassPathResource("properties.secret");
            properties.load(classPathResource.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readSecretKey() {
        String secretKey = properties.getProperty("security.jwt.secret-key");
        return Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public long readJwtExpiration() {
        String jwtExpiration = properties.getProperty("security.jwt.expiration");
        return Long.parseLong(jwtExpiration);
    }

    public long readRefreshExpiration() {
        String refreshExpiration = properties.getProperty("security.jwt.refresh-expiration");
        return Long.parseLong(refreshExpiration);
    }
}
