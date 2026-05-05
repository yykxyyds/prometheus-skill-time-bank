package com.prometheus.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告实体（id 为 AUTO 自增，不继承 BaseEntity 以独立控制 id 生成策略）
 */
@Data
@TableName("announcement")
public class Announcement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Long publisherId;

    @TableField("is_top")
    private Integer isTop;

    private Integer viewCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
