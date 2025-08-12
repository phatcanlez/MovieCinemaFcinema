package com.example.projectwebmovie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching; // <-- THÊM DÒNG NÀY
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching // <-- THÊM ANNOTATION NÀY
@EnableAsync
@EnableScheduling
public class ProjectWebMovieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjectWebMovieApplication.class, args);
    }

}