package com.prometheus.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时间币交易流水实体
 * 注意：time_transaction 表无 update_time，故不继承 BaseEntity
 */
@Data
@TableName("time_transaction")
public class TimeTransaction {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long orderId;

    /** INCOME / EXPENSE / FREEZE / UNFREEZE / GIFT */
    private String type;

    private Integer amount;

    private Integer balanceAfter;

    private String remark;

    private LocalDateTime createTime;
}
