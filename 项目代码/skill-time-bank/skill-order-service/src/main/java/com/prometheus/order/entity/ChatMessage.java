package com.prometheus.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.prometheus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message")
public class ChatMessage extends BaseEntity {
    private Long orderId;
    private Long senderId;
    private String content;
    private String messageType;
    private Integer isRead;
}
