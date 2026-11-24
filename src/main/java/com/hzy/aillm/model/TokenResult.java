package com.hzy.aillm.model;

/**
 * 登录/刷新成功后返回的双令牌
 *
 * @param accessToken  JWT 访问令牌，请求时放入 Authorization: Bearer 头
 * @param refreshToken 不透明刷新令牌，仅用于换取新令牌
 * @param expiresIn    访问令牌有效期（秒）
 */
public record TokenResult(String accessToken, String refreshToken, long expiresIn) {
}