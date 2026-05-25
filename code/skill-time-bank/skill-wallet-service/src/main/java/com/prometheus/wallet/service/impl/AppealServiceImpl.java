package com.prometheus.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.order.entity.SkillOrder;
import com.prometheus.order.mapper.SkillOrderMapper;
import com.prometheus.user.entity.User;
import com.prometheus.user.mapper.UserMapper;
import com.prometheus.wallet.entity.Appeal;
import com.prometheus.wallet.entity.TimeTransaction;
import com.prometheus.wallet.mapper.AppealMapper;
import com.prometheus.wallet.mapper.TimeTransactionMapper;
import com.prometheus.wallet.service.AppealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppealServiceImpl implements AppealService {

    private final AppealMapper appealMapper;
    private final SkillOrderMapper skillOrderMapper;
    private final UserMapper userMapper;
    private final TimeTransactionMapper timeTransactionMapper;

    @Override
    @Transactional
    public void createAppeal(Appeal appeal) {
        appeal.setStatus(1); // 待处理
        LocalDateTime now = LocalDateTime.now();
        appeal.setCreateTime(now);
        appeal.setUpdateTime(now);
        appealMapper.insert(appeal);
        log.info("申诉提交成功: id={}, orderId={}, userId={}", appeal.getId(), appeal.getOrderId(), appeal.getUserId());
    }

    @Override
    public Page<Appeal> getAppeals(int pageNum, int size, Integer status) {
        Page<Appeal> page = new Page<>(pageNum, size);
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Appeal::getStatus, status);
        }
        wrapper.orderByDesc(Appeal::getCreateTime);
        Page<Appeal> result = appealMapper.selectPage(page, wrapper);

        // 填充 appellantRole（BUYER/SELLER）和 appellantName
        for (Appeal a : result.getRecords()) {
            if (a.getOrderId() != null) {
                SkillOrder order = skillOrderMapper.selectById(a.getOrderId());
                if (order != null) {
                    a.setAppellantRole(a.getUserId().equals(order.getBuyerId()) ? "BUYER" : "SELLER");
                }
            }
            User user = userMapper.selectById(a.getUserId());
            if (user != null) {
                a.setAppellantName(user.getUsername());
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void handleAppeal(Long id, String result, Long adminId, String decision) {
        Appeal appeal = appealMapper.selectById(id);
        if (appeal == null) {
            throw new BusinessException(404, "申诉不存在");
        }
        if (appeal.getStatus() == 3) {
            throw new BusinessException(400, "该申诉已处理");
        }

        Long orderId = appeal.getOrderId();
        if (orderId != null && "ACCEPT_REFUND".equals(decision)) {
            // 支持买家：退款并取消订单
            SkillOrder order = skillOrderMapper.selectById(orderId);
            if (order != null && order.getStatus() == 2) { // STATUS_IN_PROGRESS
                refundBuyer(order);
                cancelOrder(order);
                log.info("申诉处理（退款取消）：订单已取消并退款, orderId={}", orderId);
            }
        } else if (orderId != null && "ACCEPT_COMPLETE".equals(decision)) {
            // 支持卖家：强制完成并付款
            SkillOrder order = skillOrderMapper.selectById(orderId);
            if (order != null && (order.getStatus() == 2 || order.getStatus() == 3)) {
                forceComplete(order);
                log.info("申诉处理（强制完成）：订单已完成并付款, orderId={}", orderId);
            }
        } else {
            // REJECT 或其他：仅记录处理结果
            log.info("申诉处理（驳回）：仅记录结果, id={}", id);
        }

        appeal.setStatus(3); // 已处理
        appeal.setResult(result);
        appeal.setHandledBy(adminId);
        appeal.setUpdateTime(LocalDateTime.now());
        appealMapper.updateById(appeal);
        log.info("申诉处理完成: id={}, adminId={}", id, adminId);
    }

    private void refundBuyer(SkillOrder order) {
        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer == null) return;
        int frozenAmount = order.getFrozenAmount();
        int newBalance = (buyer.getBalance() == null ? 0 : buyer.getBalance()) + frozenAmount;
        buyer.setBalance(newBalance);
        buyer.setFrozenBalance(Math.max(
                (buyer.getFrozenBalance() == null ? 0 : buyer.getFrozenBalance()) - frozenAmount, 0));
        buyer.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(buyer);

        TimeTransaction tx = new TimeTransaction();
        tx.setUserId(buyer.getId());
        tx.setOrderId(order.getId());
        tx.setType("UNFREEZE");
        tx.setAmount(frozenAmount);
        tx.setBalanceAfter(newBalance);
        tx.setRemark("申诉退款解冻");
        tx.setCreateTime(LocalDateTime.now());
        timeTransactionMapper.insert(tx);
    }

    private void forceComplete(SkillOrder order) {
        LocalDateTime now = LocalDateTime.now();
        int amount = order.getAmount();

        // 解冻买方冻结余额
        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            int frozen = (buyer.getFrozenBalance() == null ? 0 : buyer.getFrozenBalance()) - amount;
            buyer.setFrozenBalance(Math.max(frozen, 0));
            buyer.setUpdateTime(now);
            userMapper.updateById(buyer);

            // 买方支出流水
            TimeTransaction expenseTx = new TimeTransaction();
            expenseTx.setUserId(buyer.getId());
            expenseTx.setOrderId(order.getId());
            expenseTx.setType("EXPENSE");
            expenseTx.setAmount(amount);
            expenseTx.setBalanceAfter(buyer.getBalance() == null ? 0 : buyer.getBalance());
            expenseTx.setRemark("申诉强制完成支出");
            expenseTx.setCreateTime(now);
            timeTransactionMapper.insert(expenseTx);
        }

        // 卖方收到时间币
        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            int newBalance = (seller.getBalance() == null ? 0 : seller.getBalance()) + amount;
            seller.setBalance(newBalance);
            seller.setUpdateTime(now);
            userMapper.updateById(seller);

            TimeTransaction incomeTx = new TimeTransaction();
            incomeTx.setUserId(seller.getId());
            incomeTx.setOrderId(order.getId());
            incomeTx.setType("INCOME");
            incomeTx.setAmount(amount);
            incomeTx.setBalanceAfter(newBalance);
            incomeTx.setRemark("申诉强制完成收入");
            incomeTx.setCreateTime(now);
            timeTransactionMapper.insert(incomeTx);
        }

        // 更新订单状态
        order.setStatus(4); // STATUS_COMPLETED
        order.setFrozenAmount(0);
        order.setCompletedTime(now);
        order.setUpdateTime(now);
        skillOrderMapper.updateById(order);
    }

    private void cancelOrder(SkillOrder order) {
        order.setStatus(5); // STATUS_CANCELLED
        order.setFrozenAmount(0);
        order.setUpdateTime(LocalDateTime.now());
        skillOrderMapper.updateById(order);
    }
}
