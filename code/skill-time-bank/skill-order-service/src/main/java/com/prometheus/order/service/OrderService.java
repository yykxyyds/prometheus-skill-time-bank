package com.prometheus.order.service;

import com.prometheus.order.entity.SkillOrder;

import java.util.List;

public interface OrderService {
    SkillOrder createOrder(Long buyerId, Long sellerId, Long skillId, Integer amount,
                           String contactPhone, String appointmentTime, String appointmentLocation, String plan);
    void confirmOrder(Long orderId, Long sellerId);
    void buyerConfirmComplete(Long orderId, Long userId);
    void sellerConfirmComplete(Long orderId, Long userId);
    void completeOrder(Long orderId);
    void cancelOrder(Long orderId, Long userId);
    SkillOrder getOrderDetail(Long orderId);
    List<SkillOrder> getBuyerOrders(Long buyerId);
    List<SkillOrder> getSellerOrders(Long sellerId);
}
