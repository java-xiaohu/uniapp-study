package com.hzy.aillm;

import com.hzy.aillm.model.TokenResult;
import com.hzy.aillm.security.JwtTokenProvider;
import com.hzy.aillm.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Redis 集成测试：验证本机 Redis 连接（密码认证）与 JWT 令牌白名单链路。
 * <p>依赖本机已启动的 Redis（application.yaml 中 spring.data.redis 配置），不依赖 MySQL。
 */
@SpringBootTest
class RedisIntegrationTest {

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtTokenProvider jwtProvider;

    /** 基础连通性：PING + 读写删 + TTL */
    @Test
    void redisConnectionAndCrud() {
        try (RedisConnection connection = redis.getRequiredConnectionFactory().getConnection()) {
            assertEquals("PONG", connection.ping());
        }

        String key = "test:redis:" + UUID.randomUUID();
        try {
            redis.opsForValue().set(key, "hello-redis", Duration.ofSeconds(60));
            assertEquals("hello-redis", redis.opsForValue().get(key));

            Long ttl = redis.getExpire(key);
            assertNotNull(ttl);
            assertTrue(ttl > 0 && ttl <= 60, "TTL 应在 (0, 60] 秒内，实际=" + ttl);
        } finally {
            redis.delete(key);
        }
        assertFalse(Boolean.TRUE.equals(redis.hasKey(key)), "删除后 key 不应存在");
    }

    /** 令牌链路：签发后 jti 进入白名单，登出撤销后白名单失效 */
    @Test
    void tokenWhitelistIssueAndRevoke() {
        TokenResult tokens = tokenService.issueTokens(999999L, "redis-test-user");
        assertNotNull(tokens.accessToken());
        assertNotNull(tokens.refreshToken());
        assertTrue(tokens.expiresIn() > 0);

        String jti = jwtProvider.parse(tokens.accessToken()).getId();
        assertTrue(tokenService.isAccessActive(jti), "签发后访问令牌 jti 应在 Redis 白名单内");

        tokenService.revoke(jti, tokens.refreshToken());
        assertFalse(tokenService.isAccessActive(jti), "登出后 jti 应从白名单移除");
        // 刷新令牌已撤销，再刷新应被拒绝
        assertThrows(BadCredentialsException.class, () -> tokenService.refresh(tokens.refreshToken()));
    }

    /** 无效刷新令牌应被拒绝 */
    @Test
    void refreshWithInvalidTokenRejected() {
        assertThrows(BadCredentialsException.class,
                () -> tokenService.refresh("invalid-refresh-token-" + UUID.randomUUID()));
    }
}