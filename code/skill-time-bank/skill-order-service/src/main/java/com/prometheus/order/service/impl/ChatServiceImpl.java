package com.prometheus.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.prometheus.common.BusinessException;
import com.prometheus.order.entity.ChatMessage;
import com.prometheus.order.entity.SkillOrder;
import com.prometheus.order.mapper.ChatMessageMapper;
import com.prometheus.order.mapper.SkillOrderMapper;
import com.prometheus.order.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final SkillOrderMapper skillOrderMapper;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<ChatMessage> getMessages(Long orderId, Long userId) {
        SkillOrder order = skillOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权查看该订单聊天记录");
        }

        LambdaUpdateWrapper<ChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatMessage::getOrderId, orderId)
                     .ne(ChatMessage::getSenderId, userId)
                     .eq(ChatMessage::getIsRead, 0)
                     .set(ChatMessage::getIsRead, 1);
        chatMessageMapper.update(null, updateWrapper);

        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getOrderId, orderId)
                    .orderByAsc(ChatMessage::getCreateTime);
        return chatMessageMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(Long orderId, Long senderId, String content) {
        SkillOrder order = skillOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(senderId) && !order.getSellerId().equals(senderId)) {
            throw new BusinessException("无权在该订单发送消息");
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException("消息内容不能为空");
        }
        if (content.length() > 1000) {
            throw new BusinessException("消息内容不能超过1000字");
        }

        ChatMessage message = new ChatMessage();
        message.setOrderId(orderId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setMessageType("TEXT");
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());

        chatMessageMapper.insert(message);
        log.info("聊天消息发送, orderId={}, senderId={}", orderId, senderId);
        return message;
    }
}
