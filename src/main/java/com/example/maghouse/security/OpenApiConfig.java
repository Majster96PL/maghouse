package com.example.maghouse.security;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Maghouse API")
                        .version("1.0.0")
                        .description("Documentation Maghouse API")
                        .termsOfService("http://swagger.io/terms")
                        .license(new io.swagger.v3.oas.models.info.License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
