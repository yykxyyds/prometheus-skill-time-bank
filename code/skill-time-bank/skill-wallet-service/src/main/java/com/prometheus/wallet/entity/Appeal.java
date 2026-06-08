package com.prometheus.wallet.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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

    /** 证据图片URL列表，JSON数组，如 ["/uploads/images/a.jpg","/uploads/images/b.jpg"] */
    private String evidenceImages;

    /** 1-待处理 2-处理中 3-已处理 */
    private Integer status;

    private String result;

    private Long handledBy;

    /** 申诉人角色 — BUYER / SELLER，非数据库字段，由 getAppeals() 填充 */
    @TableField(exist = false)
    private String appellantRole;

    /** 申诉人用户名，非数据库字段 */
    @TableField(exist = false)
    private String appellantName;

    /** 关联悬赏标题（bounty 订单时填充），非数据库字段 */
    @TableField(exist = false)
    private String bountyTitle;
}
