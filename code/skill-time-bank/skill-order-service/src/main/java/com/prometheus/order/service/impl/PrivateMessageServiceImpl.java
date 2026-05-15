package com.prometheus.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.prometheus.common.BusinessException;
import com.prometheus.order.entity.PrivateMessage;
import com.prometheus.order.mapper.PrivateMessageMapper;
import com.prometheus.order.service.PrivateMessageService;
import com.prometheus.user.entity.User;
import com.prometheus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private final PrivateMessageMapper privateMessageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public PrivateMessage sendMessage(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) {
            throw new BusinessException("不能给自己发消息");
        }
        User receiver = userMapper.selectById(receiverId);
        if (receiver == null) {
            throw new BusinessException("接收用户不存在");
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException("消息内容不能为空");
        }
        if (content.length() > 1000) {
            throw new BusinessException("消息内容不能超过1000字");
        }

        PrivateMessage msg = new PrivateMessage();
        msg.setId(IdUtil.getSnowflakeNextId());
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        msg.setUpdateTime(LocalDateTime.now());
        privateMessageMapper.insert(msg);
        log.info("私信发送: senderId={}, receiverId={}", senderId, receiverId);
        return msg;
    }

    @Override
    public List<PrivateMessage> getMessages(Long userId, Long otherUserId) {
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(PrivateMessage::getSenderId, userId)
                        .eq(PrivateMessage::getReceiverId, otherUserId))
                .or(w -> w.eq(PrivateMessage::getSenderId, otherUserId)
                        .eq(PrivateMessage::getReceiverId, userId))
                .orderByAsc(PrivateMessage::getCreateTime);
        return privateMessageMapper.selectList(wrapper);
    }

    @Override
    public List<Map<String, Object>> getConversations(Long userId) {
        // 查询所有与当前用户相关的私信（作为发送者或接收者）
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessage::getReceiverId, userId)
                .or()
                .eq(PrivateMessage::getSenderId, userId)
                .orderByDesc(PrivateMessage::getCreateTime);
        List<PrivateMessage> allMessages = privateMessageMapper.selectList(wrapper);

        // 按对方用户ID分组，取每组最新一条
        Map<Long, List<PrivateMessage>> grouped = allMessages.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<Map<String, Object>> conversations = new ArrayList<>();
        for (Map.Entry<Long, List<PrivateMessage>> entry : grouped.entrySet()) {
            Long otherId = entry.getKey();
            List<PrivateMessage> msgs = entry.getValue();
            PrivateMessage lastMsg = msgs.get(0); // 第一条就是最新的（按时间降序）

            // 未读数
            long unread = msgs.stream()
                    .filter(m -> m.getReceiverId().equals(userId) && m.getIsRead() == 0)
                    .count();

            // 对方用户信息
            User other = userMapper.selectById(otherId);
            if (other == null) continue;

            Map<String, Object> conv = new HashMap<>();
            conv.put("otherUserId", otherId);
            conv.put("otherUsername", other.getUsername());
            conv.put("otherAvatar", other.getAvatar());
            conv.put("lastContent", lastMsg.getContent());
            conv.put("lastTime", lastMsg.getCreateTime());
            conv.put("unreadCount", unread);
            conversations.add(conv);
        }

        // 按最后消息时间降序排列
        conversations.sort((a, b) -> {
            LocalDateTime ta = (LocalDateTime) a.get("lastTime");
            LocalDateTime tb = (LocalDateTime) b.get("lastTime");
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });

        return conversations;
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<PrivateMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessage::getReceiverId, userId)
                .eq(PrivateMessage::getIsRead, 0);
        return privateMessageMapper.selectCount(wrapper);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long otherUserId) {
        LambdaUpdateWrapper<PrivateMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PrivateMessage::getReceiverId, userId)
                .eq(PrivateMessage::getSenderId, otherUserId)
                .eq(PrivateMessage::getIsRead, 0)
                .set(PrivateMessage::getIsRead, 1);
        privateMessageMapper.update(null, updateWrapper);
    }
}
