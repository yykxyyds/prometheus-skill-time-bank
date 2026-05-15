package com.prometheus.wallet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.prometheus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 申诉实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("appeal")
public class Appeal extends BaseEntity {

    private Long orderId;

    private Long userId;

    private String reason;

    private String evidence;

    /** 1-待处理 2-处理中 3-已处理 */
    private Integer status;

    private String result;

    private Long handledBy;
}
