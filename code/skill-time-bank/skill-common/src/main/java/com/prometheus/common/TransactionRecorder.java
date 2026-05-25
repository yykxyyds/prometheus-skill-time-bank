package com.prometheus.common;

/**
 * 交易流水记录接口 — 由 skill-wallet-service 实现，供其他模块调用
 * 避免 skill-order-service 对 skill-wallet-service 的循环依赖
 */
public interface TransactionRecorder {
    void record(Long userId, Long orderId, String type, int amount, int balanceAfter, String remark);
}
