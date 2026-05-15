package com.prometheus.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.prometheus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("private_message")
public class PrivateMessage extends BaseEntity {
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer isRead;
}
