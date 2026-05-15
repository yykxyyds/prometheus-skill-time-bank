package com.prometheus.wallet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.wallet.entity.TimeTransaction;

import java.util.Map;

public interface WalletService {

    /**
     * 获取用户余额信息
     * @return {balance, frozenBalance, totalEarned, totalSpent}
     */
    Map<String, Object> getBalanceInfo(Long userId);

    /**
     * 分页查询交易流水
     */
    Page<TimeTransaction> getTransactions(Long userId, int page, int size);

    /**
     * 记录交易流水
     */
    void addTransaction(Long userId, Long orderId, String type, int amount, int balanceAfter, String remark);
}
