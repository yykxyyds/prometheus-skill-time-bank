package com.prometheus.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.prometheus.order.entity.SkillOrder;
import com.prometheus.order.mapper.SkillOrderMapper;
import com.prometheus.wallet.entity.Review;
import com.prometheus.wallet.entity.UserSkillTag;
import com.prometheus.wallet.mapper.ReviewMapper;
import com.prometheus.wallet.mapper.UserSkillTagMapper;
import com.prometheus.wallet.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final UserSkillTagMapper userSkillTagMapper;
    private final SkillOrderMapper skillOrderMapper;

    @Override
    @Transactional
    public void createReview(Long orderId, Long reviewerId, Long targetId, Review review) {
        // 1. 填充/覆盖 review 字段
        review.setOrderId(orderId);
        review.setReviewerId(reviewerId);
        review.setTargetId(targetId);
        review.setIsVisible(0);
        LocalDateTime now = LocalDateTime.now();
        review.setCreateTime(now);
        review.setVisibleTime(now.plusDays(7));
        reviewMapper.insert(review);

        // 2. 双方互评检测：同一订单双方都评价后，评价立即可见
        Long reviewCount = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>().eq(Review::getOrderId, orderId));
        if (reviewCount >= 2) {
            reviewMapper.update(null, new LambdaUpdateWrapper<Review>()
                    .eq(Review::getOrderId, orderId)
                    .set(Review::getIsVisible, 1));
            log.info("订单 {} 双方均已评价，评价立即可见", orderId);
        }

        // 3. 更新被评人的四个技能标签维度（加权平均）
        updateSkillTag(targetId, "按时", review.getPunctualityScore());
        updateSkillTag(targetId, "沟通", review.getCommunicationScore());
        updateSkillTag(targetId, "专业", review.getProfessionalScore());
        updateSkillTag(targetId, "态度", review.getAttitudeScore());

        log.info("评价创建成功: orderId={}, reviewer={}, target={}, score={}", orderId, reviewerId, targetId, review.getScore());
    }

    /**
     * 更新单个技能标签的加权平均分
     */
    private void updateSkillTag(Long userId, String tagName, Integer newScore) {
        UserSkillTag tag = userSkillTagMapper.selectOne(
                new LambdaQueryWrapper<UserSkillTag>()
                        .eq(UserSkillTag::getUserId, userId)
                        .eq(UserSkillTag::getTagName, tagName));

        LocalDateTime now = LocalDateTime.now();
        if (tag == null) {
            // 首次评价，创建记录
            tag = new UserSkillTag();
            tag.setUserId(userId);
            tag.setTagName(tagName);
            tag.setScore(BigDecimal.valueOf(newScore));
            tag.setReviewCount(1);
            tag.setCreateTime(now);
            tag.setUpdateTime(now);
            userSkillTagMapper.insert(tag);
        } else {
            // 加权平均：(旧分 * 旧次数 + 新分) / (旧次数 + 1)
            BigDecimal currentTotal = tag.getScore()
                    .multiply(BigDecimal.valueOf(tag.getReviewCount()));
            BigDecimal newAvg = currentTotal.add(BigDecimal.valueOf(newScore))
                    .divide(BigDecimal.valueOf(tag.getReviewCount() + 1), 1, RoundingMode.HALF_UP);
            tag.setScore(newAvg);
            tag.setReviewCount(tag.getReviewCount() + 1);
            tag.setUpdateTime(now);
            userSkillTagMapper.updateById(tag);
        }
    }

    @Override
    public List<Review> getVisibleReviews(Long userId) {
        return reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getTargetId, userId)
                        .eq(Review::getIsVisible, 1)
                        .orderByDesc(Review::getCreateTime));
    }

    @Override
    public Map<String, Object> getReputationData(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 综合平均分 & 评价总数
        QueryWrapper<Review> avgWrapper = new QueryWrapper<>();
        avgWrapper.select("COALESCE(AVG(score), 0) as avg_score", "COUNT(1) as review_count")
                .eq("target_id", userId);
        Map<String, Object> statMap = reviewMapper.selectMaps(avgWrapper).get(0);

        BigDecimal avgScore = statMap.get("avg_score") != null
                ? new BigDecimal(statMap.get("avg_score").toString()) : BigDecimal.ZERO;
        long reviewCount = ((Number) statMap.get("review_count")).longValue();

        result.put("avgScore", avgScore.setScale(1, RoundingMode.HALF_UP));
        result.put("reviewCount", reviewCount);

        // 默认使用综合平均分（无评价时兜底）
        result.put("buyerScore", avgScore.setScale(1, RoundingMode.HALF_UP));
        result.put("sellerScore", avgScore.setScale(1, RoundingMode.HALF_UP));

        // 买方/卖方评分：通过关联订单区分用户在交易中的角色
        if (reviewCount > 0) {
            List<Review> reviews = reviewMapper.selectList(
                    new LambdaQueryWrapper<Review>().eq(Review::getTargetId, userId));
            Set<Long> orderIds = reviews.stream()
                    .map(Review::getOrderId)
                    .collect(Collectors.toSet());
            List<SkillOrder> orders = skillOrderMapper.selectBatchIds(orderIds);
            Map<Long, SkillOrder> orderMap = orders.stream()
                    .collect(Collectors.toMap(SkillOrder::getId, o -> o));

            BigDecimal buyerTotal = BigDecimal.ZERO;
            int buyerCount = 0;
            BigDecimal sellerTotal = BigDecimal.ZERO;
            int sellerCount = 0;

            for (Review r : reviews) {
                SkillOrder order = orderMap.get(r.getOrderId());
                if (order == null) {
                    continue;
                }
                // target 是买方 → 被卖方评价 → 计入 buyerScore
                if (order.getBuyerId().equals(userId)) {
                    buyerTotal = buyerTotal.add(BigDecimal.valueOf(r.getScore()));
                    buyerCount++;
                }
                // target 是卖方 → 被买方评价 → 计入 sellerScore
                if (order.getSellerId().equals(userId)) {
                    sellerTotal = sellerTotal.add(BigDecimal.valueOf(r.getScore()));
                    sellerCount++;
                }
            }

            if (buyerCount > 0) {
                result.put("buyerScore", buyerTotal.divide(
                        BigDecimal.valueOf(buyerCount), 1, RoundingMode.HALF_UP));
            }
            if (sellerCount > 0) {
                result.put("sellerScore", sellerTotal.divide(
                        BigDecimal.valueOf(sellerCount), 1, RoundingMode.HALF_UP));
            }
        }

        // 雷达图数据：四个维度的技能标签
        List<UserSkillTag> tags = userSkillTagMapper.selectList(
                new LambdaQueryWrapper<UserSkillTag>().eq(UserSkillTag::getUserId, userId));
        List<Map<String, Object>> radarData = tags.stream().map(tag -> {
            Map<String, Object> item = new HashMap<>();
            item.put("tag", tag.getTagName());
            item.put("score", tag.getScore());
            return item;
        }).collect(Collectors.toList());
        result.put("radarData", radarData);

        return result;
    }

    @Override
    public void checkAndRevealReviews() {
        long updated = reviewMapper.update(null, new LambdaUpdateWrapper<Review>()
                .eq(Review::getIsVisible, 0)
                .le(Review::getVisibleTime, LocalDateTime.now())
                .set(Review::getIsVisible, 1));
        if (updated > 0) {
            log.info("定时解盲: {} 条评价已设为可见", updated);
        }
    }
}
