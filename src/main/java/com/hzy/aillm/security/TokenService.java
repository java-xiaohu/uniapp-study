package com.hzy.aillm.security;

import com.hzy.aillm.config.JwtProperties;
import com.hzy.aillm.entity.SysUser;
import com.hzy.aillm.mapper.SysUserMapper;
import com.hzy.aillm.model.TokenResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 双令牌管理：JWT 访问令牌（jti 白名单存 Redis）+ 不透明刷新令牌（仅存 Redis）
 * <p>白名单使访问令牌可被主动撤销（登出/踢人），刷新令牌每次使用后轮换。
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String ACCESS_KEY_PREFIX = "auth:access:";
    private static final String REFRESH_KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redis;
    private final JwtTokenProvider jwtProvider;
    private final JwtProperties properties;
    private final SysUserMapper userMapper;

    /** 登录成功后签发双令牌 */
    public TokenResult issueTokens(Long userId, String username) {
        String jti = UUID.randomUUID().toString();
        String accessToken = jwtProvider.issue(userId, username, jti);
        redis.opsForValue().set(ACCESS_KEY_PREFIX + jti, String.valueOf(userId), properties.getAccessTtl());

        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(REFRESH_KEY_PREFIX + refreshToken, String.valueOf(userId), properties.getRefreshTtl());

        return new TokenResult(accessToken, refreshToken, properties.getAccessTtl().toSeconds());
    }

    /** 访问令牌 jti 是否在白名单内（未被登出撤销） */
    public boolean isAccessActive(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(ACCESS_KEY_PREFIX + jti));
    }

    /** 刷新令牌换新双令牌，旧刷新令牌立即作废（轮换） */
    public TokenResult refresh(String refreshToken) {
        String key = REFRESH_KEY_PREFIX + refreshToken;
        String userId = redis.opsForValue().get(key);
        if (userId == null) {
            throw new BadCredentialsException("刷新令牌无效或已过期");
        }
        redis.delete(key);

        SysUser user = userMapper.selectById(Long.valueOf(userId));
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BadCredentialsException("账号不存在或已被禁用");
        }
        return issueTokens(user.getId(), user.getUsername());
    }

    /** 登出：同时撤销当前访问令牌与刷新令牌 */
    public void revoke(String jti, String refreshToken) {
        if (jti != null) {
            redis.delete(ACCESS_KEY_PREFIX + jti);
        }
        if (refreshToken != null) {
            redis.delete(REFRESH_KEY_PREFIX + refreshToken);
        }
    }
}