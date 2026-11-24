package com.hzy.aillm.config;

import org.springframework.boot.tomcat.TomcatWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 内嵌 Tomcat 连接器配置
 */
@Configuration
public class TomcatConfig {

    /**
     * Tomcat 11 默认最多解析 Accept-Language 头中的 10 个 locale，
     * 超出会抛出 IllegalArgumentException 导致请求失败。
     * 这里放宽上限至 100（设为负数则完全不限制，但会失去防 DoS 保护）。
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatWebServerFactory> tomcatLocaleCustomizer() {
        return factory -> factory.addConnectorCustomizers(
                connector -> connector.setMaxLocaleCount(100));
    }
}