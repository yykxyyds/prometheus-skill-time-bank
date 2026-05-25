package com.prometheus.wallet.service.impl;

import com.prometheus.common.TransactionRecorder;
import com.prometheus.wallet.entity.TimeTransaction;
import com.prometheus.wallet.mapper.TimeTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionRecorderImpl implements TransactionRecorder {

    private final TimeTransactionMapper timeTransactionMapper;

    @Override
    public void record(Long userId, Long orderId, String type, int amount, int balanceAfter, String remark) {
        TimeTransaction tx = new TimeTransaction();
        tx.setUserId(userId);
        tx.setOrderId(orderId);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setBalanceAfter(balanceAfter);
        tx.setRemark(remark);
        tx.setCreateTime(LocalDateTime.now());
        timeTransactionMapper.insert(tx);
        log.debug("记录交易流水: userId={}, orderId={}, type={}, amount={}, balanceAfter={}",
                userId, orderId, type, amount, balanceAfter);
    }
}
