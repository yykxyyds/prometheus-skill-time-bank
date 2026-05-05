package com.prometheus.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 双盲评价实体
 * 注意：review 表无 update_time，故不继承 BaseEntity
 */
@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;

    /** 评价人ID */
    private Long reviewerId;

    /** 被评人ID */
    private Long targetId;

    /** 综合评分 1-5 */
    private Integer score;

    /** 按时 1-5 */
    private Integer punctualityScore;

    /** 沟通 1-5 */
    private Integer communicationScore;

    /** 专业 1-5 */
    private Integer professionalScore;

    /** 态度 1-5 */
    private Integer attitudeScore;

    /** 评价内容 */
    private String comment;

    /** 双盲标记：0不可见 1可见 */
    @com.baomidou.mybatisplus.annotation.TableField("is_visible")
    private Integer isVisible;

    /** 可见时间（创建后7天） */
    private LocalDateTime visibleTime;

    private LocalDateTime createTime;
}
