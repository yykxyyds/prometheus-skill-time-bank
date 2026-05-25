package com.prometheus.wallet.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Long id;
    private Long orderId;
    private Long reviewerId;
    private Long targetId;
    private Integer score;
    private Integer punctualityScore;
    private Integer communicationScore;
    private Integer professionalScore;
    private Integer attitudeScore;
    private String comment;
    private LocalDateTime createTime;

    private String reviewerName;
    private String reviewerAvatar;
    private String reviewerRole;

    private String orderContext;
}
