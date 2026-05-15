package com.prometheus.skill.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.prometheus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 技能
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("skill")
public class Skill extends BaseEntity {

    /** 发布者ID */
    private Long userId;

    /** 技能分类ID */
    private Long categoryId;

    /** 技能标题 */
    private String title;

    /** 技能描述 */
    private String description;

    /** 价格（时间币/小时） */
    private Integer price;

    /** 可用时间（如"周末全天"） */
    private String availableTime;

    /** 封面图片URL */
    private String coverImage;

    /** 状态：0=下架 1=上架 2=待审核 3=已拒绝 */
    private Integer status;

    /** 浏览量 */
    private Integer viewCount;

    /** 订单数 */
    private Integer orderCount;

    /** 分类名称（联表查询，非数据库字段） */
    @TableField(exist = false)
    private String categoryName;

    /** 发布者用户名（联表查询，非数据库字段） */
    @TableField(exist = false)
    private String userName;

    /** 发布者头像（联表查询，非数据库字段） */
    @TableField(exist = false)
    private String userAvatar;
}
