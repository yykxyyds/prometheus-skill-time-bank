package com.prometheus.wallet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.prometheus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户技能标签（信誉雷达图维度）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_skill_tag")
public class UserSkillTag extends BaseEntity {

    private Long userId;

    /** 标签名：按时/沟通/专业/态度 */
    private String tagName;

    /** 加权平均分 DECIMAL(2,1)，默认5.0 */
    private BigDecimal score;

    /** 评价次数 */
    private Integer reviewCount;
}
