package com.prometheus.skill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.prometheus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 需求悬赏
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bounty")
public class Bounty extends BaseEntity {

    /** 发布者ID */
    private Long userId;

    /** 悬赏标题 */
    private String title;

    /** 悬赏描述 */
    private String description;

    /** 悬赏金额（时间币） */
    private Integer reward;

    /** 技能分类ID */
    private Long categoryId;

    /** 截止时间 */
    private LocalDateTime deadline;

    /** 状态：0=待审核 1=已发布 2=已接单 3=已完成 4=已拒绝 */
    private Integer status;

    /** 中标申请人ID */
    private Long applicantId;

    /** 分类名称（联表查询，非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String categoryName;

    /** 发布者用户名（联表查询，非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String userName;

    /** 发布者头像（联表查询，非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String userAvatar;

    /** 申请人用户名（联表查询，非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String applicantName;

    /** 申请人头像（联表查询，非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String applicantAvatar;
}
