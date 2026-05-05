package com.prometheus.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prometheus.common.BusinessException;
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

    private static final int STATUS_PENDING = 1;
    private static final int STATUS_IN_PROGRESS = 2;
    private static final int STATUS_WAIT_COMPLETE = 3;
    private static final int STATUS_COMPLETED = 4;
    private static final int STATUS_CANCELLED = 5;

    @Override
    @Transactional
    public SkillOrder createOrder(Long buyerId, Long sellerId, Long skillId, Integer amount) {
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
        SkillOrder order = skillOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("只有卖方可以确认订单");
        }
        if (order.getStatus() != STATUS_PENDING) {
            throw new BusinessException("订单状态不正确，当前状态：" + getStatusDesc(order.getStatus()));
        }

        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer == null) {
            throw new BusinessException("买方用户不存在");
        }
        int buyerBalance = buyer.getBalance() == null ? 0 : buyer.getBalance();
        if (buyerBalance < order.getAmount()) {
            throw new BusinessException("买方时间币余额不足");
        }

        buyer.setBalance(buyerBalance - order.getAmount());
        int frozen = buyer.getFrozenBalance() == null ? 0 : buyer.getFrozenBalance();
        buyer.setFrozenBalance(frozen + order.getAmount());
        buyer.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(buyer);

        order.setStatus(STATUS_IN_PROGRESS);
        order.setFrozenAmount(order.getAmount());
        order.setUpdateTime(LocalDateTime.now());
        skillOrderMapper.updateById(order);

        log.info("卖方确认订单, orderId={}, sellerId={}", orderId, sellerId);
    }

    @Override
    @Transactional
    public void buyerConfirmComplete(Long orderId, Long userId) {
        SkillOrder order = skillOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("只有买方可以确认完成");
        }
        if (order.getStatus() != STATUS_IN_PROGRESS) {
            throw new BusinessException("订单状态不正确，当前状态：" + getStatusDesc(order.getStatus()));
        }

        order.setBuyerConfirm(1);
        order.setStatus(STATUS_WAIT_COMPLETE);
        order.setUpdateTime(LocalDateTime.now());
        skillOrderMapper.updateById(order);

        log.info("买方确认完成, orderId={}, userId={}", orderId, userId);

        if (order.getSellerConfirm() != null && order.getSellerConfirm() == 1) {
            doCompleteOrder(orderId);
        }
    }

    @Override
    @Transactional
    public void sellerConfirmComplete(Long orderId, Long userId) {
        SkillOrder order = skillOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException("只有卖方可以确认完成");
        }
        if (order.getStatus() != STATUS_IN_PROGRESS) {
            throw new BusinessException("订单状态不正确，当前状态：" + getStatusDesc(order.getStatus()));
        }

        order.setSellerConfirm(1);
        order.setStatus(STATUS_WAIT_COMPLETE);
        order.setUpdateTime(LocalDateTime.now());
        skillOrderMapper.updateById(order);

        log.info("卖方确认完成, orderId={}, userId={}", orderId, userId);

        if (order.getBuyerConfirm() != null && order.getBuyerConfirm() == 1) {
            doCompleteOrder(orderId);
        }
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId) {
        doCompleteOrder(orderId);
    }

    private void doCompleteOrder(Long orderId) {
        SkillOrder order = skillOrderMapper.selectById(orderId);
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

        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            int frozen = (buyer.getFrozenBalance() == null ? 0 : buyer.getFrozenBalance()) - order.getAmount();
            buyer.setFrozenBalance(Math.max(frozen, 0));
            buyer.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(buyer);
        }

        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            int balance = (seller.getBalance() == null ? 0 : seller.getBalance()) + order.getAmount();
            seller.setBalance(balance);
            seller.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(seller);
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
        SkillOrder order = skillOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("只有买卖双方可以取消订单");
        }
        if (order.getStatus() != STATUS_PENDING) {
            throw new BusinessException("仅在待确认状态可取消订单，当前状态：" + getStatusDesc(order.getStatus()));
        }

        order.setStatus(STATUS_CANCELLED);
        order.setUpdateTime(LocalDateTime.now());
        skillOrderMapper.updateById(order);

        log.info("订单已取消, orderId={}, userId={}", orderId, userId);
    }

    @Override
    public SkillOrder getOrderDetail(Long orderId) {
        SkillOrder order = skillOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    @Override
    public List<SkillOrder> getBuyerOrders(Long buyerId) {
        LambdaQueryWrapper<SkillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillOrder::getBuyerId, buyerId)
               .orderByDesc(SkillOrder::getCreateTime);
        return skillOrderMapper.selectList(wrapper);
    }

    @Override
    public List<SkillOrder> getSellerOrders(Long sellerId) {
        LambdaQueryWrapper<SkillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillOrder::getSellerId, sellerId)
               .orderByDesc(SkillOrder::getCreateTime);
        return skillOrderMapper.selectList(wrapper);
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
