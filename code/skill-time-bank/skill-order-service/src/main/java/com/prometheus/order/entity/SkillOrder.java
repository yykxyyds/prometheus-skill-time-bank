package com.prometheus.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.prometheus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("skill_order")
public class SkillOrder extends BaseEntity {
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long skillId;
    private Long bountyId;
    private Integer amount;
    private Integer frozenAmount;
    private String contactPhone;
    private String appointmentTime;
    private String appointmentLocation;
    private String plan;
    private Integer status;
    private Integer buyerConfirm;
    private Integer sellerConfirm;
    private LocalDateTime completedTime;

    /** 以下为联表查询字段，非数据库列 */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String buyerName;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String buyerAvatar;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String sellerName;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String sellerAvatar;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String skillName;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String bountyTitle;
}
