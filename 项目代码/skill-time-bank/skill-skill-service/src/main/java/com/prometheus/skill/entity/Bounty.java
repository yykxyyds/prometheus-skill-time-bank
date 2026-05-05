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

    /** 截止时间 */
    private LocalDateTime deadline;

    /** 状态：1=已发布 2=已接单 3=已完成 4=已过期 */
    private Integer status;

    /** 中标申请人ID */
    private Long applicantId;
}
