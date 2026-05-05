package com.prometheus.order.controller;

import com.prometheus.common.Result;
import com.prometheus.order.entity.SkillOrder;
import com.prometheus.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @PostMapping
    public Result<SkillOrder> createOrder(@RequestBody Map<String, Object> body,
                                           HttpServletRequest request) {
        Long buyerId = getUserId(request);
        Long sellerId = Long.valueOf(body.get("sellerId").toString());
        Long skillId = Long.valueOf(body.get("skillId").toString());
        Integer amount = Integer.valueOf(body.get("amount").toString());
        return Result.success(orderService.createOrder(buyerId, sellerId, skillId, amount));
    }

    @PutMapping("/{id}/confirm")
    public Result<Void> confirmOrder(@PathVariable Long id, HttpServletRequest request) {
        Long sellerId = getUserId(request);
        orderService.confirmOrder(id, sellerId);
        return Result.success();
    }

    @PutMapping("/{id}/buyer-complete")
    public Result<Void> buyerConfirmComplete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        orderService.buyerConfirmComplete(id, userId);
        return Result.success();
    }

    @PutMapping("/{id}/seller-complete")
    public Result<Void> sellerConfirmComplete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        orderService.sellerConfirmComplete(id, userId);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        orderService.cancelOrder(id, userId);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SkillOrder> getOrderDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    @GetMapping("/buyer")
    public Result<List<SkillOrder>> getBuyerOrders(HttpServletRequest request) {
        Long buyerId = getUserId(request);
        return Result.success(orderService.getBuyerOrders(buyerId));
    }

    @GetMapping("/seller")
    public Result<List<SkillOrder>> getSellerOrders(HttpServletRequest request) {
        Long sellerId = getUserId(request);
        return Result.success(orderService.getSellerOrders(sellerId));
    }
}
