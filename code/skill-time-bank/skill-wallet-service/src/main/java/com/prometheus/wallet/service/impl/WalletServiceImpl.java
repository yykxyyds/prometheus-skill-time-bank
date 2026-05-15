package com.prometheus.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.user.entity.User;
import com.prometheus.user.mapper.UserMapper;
import com.prometheus.wallet.entity.TimeTransaction;
import com.prometheus.wallet.mapper.TimeTransactionMapper;
import com.prometheus.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final UserMapper userMapper;
    private final TimeTransactionMapper timeTransactionMapper;

    @Override
    public Map<String, Object> getBalanceInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 累计收入
        QueryWrapper<TimeTransaction> earnedWrapper = new QueryWrapper<>();
        earnedWrapper.select("COALESCE(SUM(amount), 0) as total")
                .eq("user_id", userId)
                .eq("type", "INCOME");
        Map<String, Object> earnedMap = timeTransactionMapper.selectMaps(earnedWrapper).get(0);
        int totalEarned = ((Number) earnedMap.get("total")).intValue();

        // 累计支出
        QueryWrapper<TimeTransaction> spentWrapper = new QueryWrapper<>();
        spentWrapper.select("COALESCE(SUM(amount), 0) as total")
                .eq("user_id", userId)
                .eq("type", "EXPENSE");
        Map<String, Object> spentMap = timeTransactionMapper.selectMaps(spentWrapper).get(0);
        int totalSpent = ((Number) spentMap.get("total")).intValue();

        Map<String, Object> result = new HashMap<>();
        result.put("balance", user.getBalance());
        result.put("frozenBalance", user.getFrozenBalance());
        result.put("totalEarned", totalEarned);
        result.put("totalSpent", totalSpent);
        return result;
    }

    @Override
    public Page<TimeTransaction> getTransactions(Long userId, int pageNum, int size) {
        Page<TimeTransaction> page = new Page<>(pageNum, size);
        LambdaQueryWrapper<TimeTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeTransaction::getUserId, userId)
                .orderByDesc(TimeTransaction::getCreateTime);
        return timeTransactionMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void addTransaction(Long userId, Long orderId, String type,
                               int amount, int balanceAfter, String remark) {
        TimeTransaction tx = new TimeTransaction();
        tx.setUserId(userId);
        tx.setOrderId(orderId);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setBalanceAfter(balanceAfter);
        tx.setRemark(remark);
        tx.setCreateTime(LocalDateTime.now());
        timeTransactionMapper.insert(tx);
        log.info("记录流水: userId={}, type={}, amount={}, balanceAfter={}", userId, type, amount, balanceAfter);
    }
}
