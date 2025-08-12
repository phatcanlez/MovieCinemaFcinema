package com.example.projectwebmovie.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching; // Vẫn cần để Spring nhận diện @Cacheable

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching // Đảm bảo bạn có @EnableCaching ở đây hoặc trên ProjectWebMovieApplication
public class CaffeineCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Định nghĩa cấu hình cho "accountsCache"
        Caffeine<Object, Object> defaultCaffeineBuilder = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES) // TTTL 30 phút
                .maximumSize(5000); // Tối đa 5000 entries
        cacheManager.setCaffeine(defaultCaffeineBuilder);

        // Đăng ký tên các cache bạn sẽ sử dụng
        cacheManager.setCacheNames(java.util.Set.of("accountsCache", "accountCountsCache", "employeesCache"));

        // Bạn cũng có thể định nghĩa các cấu hình riêng cho từng cache nếu muốn
        // Ví dụ:
        // cacheManager.registerCustomCache("shortLivedCache", Caffeine.newBuilder()
        //      .expireAfterWrite(5, TimeUnit.MINUTES)
        //      .maximumSize(100).build());

        return cacheManager;
    }
}