package com.hzy.aillm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * JWT 认证配置
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HS256 签名密钥，至少 32 字节，生产环境用 JWT_SECRET 环境变量覆盖 */
    private String secret;

    /** 访问令牌有效期 */
    private Duration accessTtl = Duration.ofMinutes(30);

    /** 刷新令牌有效期 */
    private Duration refreshTtl = Duration.ofDays(7);
}