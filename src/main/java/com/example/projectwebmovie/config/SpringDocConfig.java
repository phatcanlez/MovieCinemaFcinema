package com.example.projectwebmovie.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Movie Theater API")
                        .version("1.0")
                        .description("API documentation for the Movie Theater Web Application."));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public") // Tên nhóm API
                .packagesToScan("com.example.projectwebmovie.controller") // Thiết lập package quét rõ ràng
                .pathsToMatch("/**") // Tất cả các đường dẫn trong các package đã quét
                .build();
    }
}