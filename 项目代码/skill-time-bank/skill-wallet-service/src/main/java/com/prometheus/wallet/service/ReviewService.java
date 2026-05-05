package com.prometheus.wallet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.wallet.entity.Review;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    /**
     * 创建双盲评价
     */
    void createReview(Long orderId, Long reviewerId, Long targetId, Review review);

    /**
     * 获取某用户可见的评价（is_visible=1）
     */
    List<Review> getVisibleReviews(Long userId);

    /**
     * 获取信誉雷达图数据
     * @return {avgScore, reviewCount, buyerScore, sellerScore, radarData: [{tag, score}]}
     */
    Map<String, Object> getReputationData(Long userId);

    /**
     * 检查到期评价并设为可见（定时任务或手动调用）
     */
    void checkAndRevealReviews();
}
