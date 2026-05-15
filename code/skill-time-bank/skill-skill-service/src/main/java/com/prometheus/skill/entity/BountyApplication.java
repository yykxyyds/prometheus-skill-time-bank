package com.prometheus.skill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.prometheus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 悬赏申请
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bounty_application")
public class BountyApplication extends BaseEntity {

    /** 悬赏ID */
    private Long bountyId;

    /** 申请人ID */
    private Long applicantId;

    /** 申请留言 */
    private String message;

    /** 状态：1=待确认 2=已接受 3=已拒绝 */
    private Integer status;
}
