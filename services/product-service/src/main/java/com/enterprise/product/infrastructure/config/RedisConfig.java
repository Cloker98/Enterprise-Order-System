package com.enterprise.product.infrastructure.config;

import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis cache configuration.
 *
 * <p>Configures Redis as cache provider with:
 * <ul>
 *   <li>TTL: 5 minutes (300 seconds)</li>
 *   <li>Serialization: JSON (GenericJackson2JsonRedisSerializer)</li>
 *   <li>Key prefix: products::</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class RedisConfig {

  private static final Duration TTL = Duration.ofMinutes(5);

  /**
   * Configures Redis cache manager.
   *
   * @param connectionFactory the Redis connection factory (auto-injected)
   * @return configured CacheManager
   */
  @Bean
  public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(TTL)
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new StringRedisSerializer()
            )
        )
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer()
            )
        )
        .disableCachingNullValues();

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(config)
        .build();
  }
}
