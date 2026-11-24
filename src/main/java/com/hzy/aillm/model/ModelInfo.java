package com.hzy.aillm.model;

import java.util.List;

/**
 * 对外暴露的模型元信息，便于前端/调用方选择模型。
 */
public record ModelInfo(String id,
                        String provider,
                        String model,
                        String baseUrl,
                        boolean defaultModel,
                        List<String> fallback) {
}
