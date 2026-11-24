package com.hzy.aillm.security;

import com.hzy.aillm.config.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * JWT 签发与解析（HS256，Nimbus 实现）
 */
@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final JwtEncoder encoder;
    private final NimbusJwtDecoder decoder;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        SecretKey key = new SecretKeySpec(
                properties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /** 签发访问令牌：sub=userId，附带 username 与 jti（jti 用于 Redis 白名单） */
    public String issue(Long userId, String username, String jti) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(userId))
                .id(jti)
                .claim("username", username)
                .issuedAt(now)
                .expiresAt(now.plus(properties.getAccessTtl()))
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    /** 解析并校验签名与有效期，失败抛 JwtException */
    public Jwt parse(String token) throws JwtException {
        return decoder.decode(token);
    }
}