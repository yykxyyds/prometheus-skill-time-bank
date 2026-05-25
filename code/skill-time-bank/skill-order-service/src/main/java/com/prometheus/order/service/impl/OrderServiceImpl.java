package com.prometheus.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.prometheus.common.BusinessException;
import com.prometheus.common.TransactionRecorder;
import com.prometheus.order.entity.SkillOrder;
import com.prometheus.order.mapper.SkillOrderMapper;
import com.prometheus.order.service.OrderService;
import com.prometheus.user.entity.User;
import com.prometheus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final SkillOrderMapper skillOrderMapper;
    private final UserMapper userMapper;
    private final TransactionRecorder transactionRecorder;

    private static final int STATUS_PENDING = 1;
    private static final int STATUS_IN_PROGRESS = 2;
    private static final int STATUS_WAIT_COMPLETE = 3;
    private static final int STATUS_COMPLETED = 4;
    private static final int STATUS_CANCELLED = 5;

    @Override
    @Transactional
    public SkillOrder createOrder(Long buyerId, Long sellerId, Long skillId, Integer amount,
                                  String contactPhone, String appointmentTime, String appointmentLocation, String plan) {
        if (buyerId.equals(sellerId)) {
            throw new BusinessException("不能给自己下单");
        }
        if (amount == null || amount <= 0) {
            throw new BusinessException("时间币金额必须大于0");
        }

        SkillOrder order = new SkillOrder();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setSkillId(skillId);
        order.setAmount(amount);
        order.setFrozenAmount(0);
        order.setContactPhone(contactPhone);
        order.setAppointmentTime(appointmentTime);
        order.setAppointmentLocation(appointmentLocation);
        order.setPlan(plan);
        order.setStatus(STATUS_PENDING);
        order.setBuyerConfirm(0);
        order.setSellerConfirm(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        skillOrderMapper.insert(order);
        log.info("订单创建成功, orderNo={}, buyerId={}, sellerId={}", order.getOrderNo(), buyerId, sellerId);
        return order;
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId, Long sellerId) {
        SkillOrder order = skillOrderMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("只有卖方可以确认订单");
        }
        if (order.getStatus() != STATUS_PENDING) {
            throw new BusinessException("订单状态不正确，当前状态：" + getStatusDesc(order.getStatus()));
        }

        User buyer = userMapper.selectByIdForUpdate(order.getBuyerId());
        if (buyer == null) {
            throw new BusinessException("买方用户不存在");
        }
        int buyerBalance = buyer.getBalance() == null ? 0 : buyer.getBalance();
        if (buyerBalance < order.getAmount()) {
            throw new BusinessException("买方时间币余额不足");
        }

        int balanceAfter = buyerBalance - order.getAmount();
        buyer.setBalance(balanceAfter);
        int frozen = buyer.getFrozenBalance() == null ? 0 : buyer.getFrozenBalance();
        buyer.setFrozenBalance(frozen + order.getAmount());
        buyer.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(buyer);

        transactionRecorder.record(order.getBuyerId(), orderId, "FREEZE",
                order.getAmount(), balanceAfter,
                "订单「" + order.getOrderNo() + "」冻结时间币");

        order.setStatus(STATUS_IN_PROGRESS);
        order.setFrozenAmount(order.getAmount());
        order.setUpdateTime(LocalDateTime.now());
        skillOrderMapper.updateById(order);

        log.info("卖方确认订单, orderId={}, sellerId={}", orderId, sellerId);
    }

    @Override
    @Transactional
    public void buyerConfirmComplete(Long orderId, Long userId) {
        // SELECT FOR UPDATE — 悲观锁防止买卖双方同时确认时的竞态
        SkillOrder order = skillOrderMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("只有买方可以确认完成");
        }
        if (order.getStatus() != STATUS_IN_PROGRESS && order.getStatus() != STATUS_WAIT_COMPLETE) {
            throw new BusinessException("订单状态不正确，当前状态：" + getStatusDesc(order.getStatus()));
        }
        if (order.getBuyerConfirm() != null && order.getBuyerConfirm() == 1) {
            throw new BusinessException("您已确认完成，请勿重复操作");
        }

        // 买方确认，检查卖方是否已确认（锁保护下，无需重读）
        if (order.getSellerConfirm() != null && order.getSellerConfirm() == 1) {
            // 双方都已确认 → 直接完成
            order.setBuyerConfirm(1);
            order.setUpdateTime(LocalDateTime.now());
            skillOrderMapper.updateById(order);
            doCompleteOrder(orderId);
        } else {
            // 仅买方确认 → 设置 WAIT_COMPLETE
            skillOrderMapper.update(null, new LambdaUpdateWrapper<SkillOrder>()
                    .eq(SkillOrder::getId, orderId)
                    .set(SkillOrder::getBuyerConfirm, 1)
                    .set(SkillOrder::getStatus, STATUS_WAIT_COMPLETE)
                    .set(SkillOrder::getUpdateTime, LocalDateTime.now()));
        }

        log.info("买方确认完成, orderId={}, userId={}", orderId, userId);
    }

    @Override
    @Transactional
    public void sellerConfirmComplete(Long orderId, Long userId) {
        // SELECT FOR UPDATE — 悲观锁防止买卖双方同时确认时的竞态
        SkillOrder order = skillOrderMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException("只有卖方可以确认完成");
        }
        if (order.getStatus() != STATUS_IN_PROGRESS && order.getStatus() != STATUS_WAIT_COMPLETE) {
            throw new BusinessException("订单状态不正确，当前状态：" + getStatusDesc(order.getStatus()));
        }
        if (order.getSellerConfirm() != null && order.getSellerConfirm() == 1) {
            throw new BusinessException("您已确认完成，请勿重复操作");
        }

        // 卖方确认，检查买方是否已确认（锁保护下，无需重读）
        if (order.getBuyerConfirm() != null && order.getBuyerConfirm() == 1) {
            // 双方都已确认 → 直接完成
            order.setSellerConfirm(1);
            order.setUpdateTime(LocalDateTime.now());
            skillOrderMapper.updateById(order);
            doCompleteOrder(orderId);
        } else {
            // 仅卖方确认 → 设置 WAIT_COMPLETE
            skillOrderMapper.update(null, new LambdaUpdateWrapper<SkillOrder>()
                    .eq(SkillOrder::getId, orderId)
                    .set(SkillOrder::getSellerConfirm, 1)
                    .set(SkillOrder::getStatus, STATUS_WAIT_COMPLETE)
                    .set(SkillOrder::getUpdateTime, LocalDateTime.now()));
        }

        log.info("卖方确认完成, orderId={}, userId={}", orderId, userId);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId) {
        doCompleteOrder(orderId);
    }

    private void doCompleteOrder(Long orderId) {
        SkillOrder order = skillOrderMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != STATUS_WAIT_COMPLETE) {
            throw new BusinessException("订单状态不正确，当前状态：" + getStatusDesc(order.getStatus()));
        }
        int buyerConfirm = order.getBuyerConfirm() == null ? 0 : order.getBuyerConfirm();
        int sellerConfirm = order.getSellerConfirm() == null ? 0 : order.getSellerConfirm();
        if (buyerConfirm != 1 || sellerConfirm != 1) {
            throw new BusinessException("双方都确认完成后才能完成订单");
        }

        User buyer = userMapper.selectByIdForUpdate(order.getBuyerId());
        if (buyer != null) {
            int frozen = (buyer.getFrozenBalance() == null ? 0 : buyer.getFrozenBalance()) - order.getAmount();
            buyer.setFrozenBalance(Math.max(frozen, 0));
            buyer.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(buyer);

            transactionRecorder.record(order.getBuyerId(), orderId, "EXPENSE",
                    order.getAmount(), buyer.getBalance() == null ? 0 : buyer.getBalance(),
                    "订单完成支出");
        }

        User seller = userMapper.selectByIdForUpdate(order.getSellerId());
        if (seller != null) {
            int balance = (seller.getBalance() == null ? 0 : seller.getBalance()) + order.getAmount();
            seller.setBalance(balance);
            seller.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(seller);

            transactionRecorder.record(order.getSellerId(), orderId, "INCOME",
                    order.getAmount(), balance,
                    "订单完成收入");
        }

        order.setStatus(STATUS_COMPLETED);
        order.setFrozenAmount(0);
        order.setCompletedTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        skillOrderMapper.updateById(order);

        log.info("订单完成, orderId={}", orderId);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        SkillOrder order = skillOrderMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("只有买卖双方可以取消订单");
        }

        if (order.getStatus() == STATUS_PENDING) {
            // 待确认状态：直接取消，无需退款
            order.setStatus(STATUS_CANCELLED);
            order.setUpdateTime(LocalDateTime.now());
            skillOrderMapper.updateById(order);
        } else if (order.getStatus() == STATUS_IN_PROGRESS) {
            User buyer = userMapper.selectByIdForUpdate(order.getBuyerId());
            if (buyer != null) {
                int frozenAmount = order.getFrozenAmount();
                int newBalance = (buyer.getBalance() == null ? 0 : buyer.getBalance()) + frozenAmount;
                buyer.setBalance(newBalance);
                buyer.setFrozenBalance(Math.max((buyer.getFrozenBalance() == null ? 0 : buyer.getFrozenBalance()) - frozenAmount, 0));
                buyer.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(buyer);

                transactionRecorder.record(order.getBuyerId(), orderId, "UNFREEZE",
                        frozenAmount, newBalance,
                        "订单取消解冻");
            }
            order.setStatus(STATUS_CANCELLED);
            order.setFrozenAmount(0);
            order.setUpdateTime(LocalDateTime.now());
            skillOrderMapper.updateById(order);
        } else {
            throw new BusinessException("当前状态不可取消，当前状态：" + getStatusDesc(order.getStatus()));
        }

        log.info("订单已取消, orderId={}, userId={}", orderId, userId);
    }

    @Override
    public SkillOrder getOrderDetail(Long orderId) {
        SkillOrder order = skillOrderMapper.selectByIdWithDetails(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    @Override
    public List<SkillOrder> getBuyerOrders(Long buyerId) {
        return skillOrderMapper.selectListByBuyerId(buyerId);
    }

    @Override
    public List<SkillOrder> getSellerOrders(Long sellerId) {
        return skillOrderMapper.selectListBySellerId(sellerId);
    }

    private String getStatusDesc(int status) {
        return switch (status) {
            case STATUS_PENDING -> "待确认";
            case STATUS_IN_PROGRESS -> "进行中";
            case STATUS_WAIT_COMPLETE -> "待确认完成";
            case STATUS_COMPLETED -> "已完成";
            case STATUS_CANCELLED -> "已取消";
            default -> "未知";
        };
    }
}
