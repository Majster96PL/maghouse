package com.example.user_service.auth.login.jwt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

@Component
public class SecretPropiertiesReader {

    private final ClassPathResource classPathResource = new ClassPathResource("properties.secret");
    private final Properties properties = new Properties();


    public String readSecretKey() throws Exception {
        properties.load(classPathResource.getInputStream());
        String secretKey = properties.getProperty("security.jwt.secret-key");
        return Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String readJwtExpiration() throws Exception {
        properties.load(classPathResource.getInputStream());
        return properties.getProperty("secret.jwt.expiration");
    }

    public String readRefreshExpiration() throws Exception {
        properties.load(classPathResource.getInputStream());
        return properties.getProperty("secret.jwt.refresh-expiration");
    }
}
