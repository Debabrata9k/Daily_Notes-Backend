package com.daily.note.save.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    @SuppressWarnings("removal")
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );
    }

    // ✅ ADD THIS
    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.url")
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();

        configs.put("notes",
                cacheConfiguration().entryTtl(Duration.ofMinutes(5)));

        configs.put("notes_search",
                cacheConfiguration().entryTtl(Duration.ofMinutes(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration())
                .withInitialCacheConfigurations(configs)
                .build();
    }
}