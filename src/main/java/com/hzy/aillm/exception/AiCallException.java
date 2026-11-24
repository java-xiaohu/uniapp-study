package com.hzy.aillm.exception;

/**
 * 所有候选模型都调用失败时抛出。
 */
public class AiCallException extends RuntimeException {

    public AiCallException(String message) {
        super(message);
    }

    public AiCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
