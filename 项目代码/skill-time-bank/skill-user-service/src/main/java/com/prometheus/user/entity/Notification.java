package com.prometheus.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.prometheus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    private Long userId;

    private String type;

    private String title;

    private String content;

    private Long targetId;

    private Integer isRead;
}
