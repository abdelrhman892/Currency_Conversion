package com.bm.graduationproject.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CachingConfig {

    @Value("${expire_after_duration}")
    private long expireAfterDuration;

    @Value("${expire_after_time_unit}")
    private String expireAfterTimeUnit;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(expireAfterDuration, TimeUnit.valueOf(expireAfterTimeUnit)) // Cache entries expire after 1 hour
                .maximumSize(100));
        return cacheManager;
    }
}

