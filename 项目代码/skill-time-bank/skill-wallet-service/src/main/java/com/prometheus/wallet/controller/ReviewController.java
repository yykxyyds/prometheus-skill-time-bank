package com.prometheus.wallet.controller;

import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import com.prometheus.wallet.entity.Review;
import com.prometheus.wallet.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @RequireAuth
    public Result<Void> createReview(HttpServletRequest request, @RequestBody Review review) {
        Long userId = (Long) request.getAttribute("userId");
        reviewService.createReview(review.getOrderId(), userId, review.getTargetId(), review);
        return Result.success();
    }

    @GetMapping("/user/{userId}")
    public Result<List<Review>> getUserReviews(@PathVariable Long userId) {
        return Result.success(reviewService.getVisibleReviews(userId));
    }

    @GetMapping("/reputation/{userId}")
    public Result<Map<String, Object>> getReputation(@PathVariable Long userId) {
        return Result.success(reviewService.getReputationData(userId));
    }
}
