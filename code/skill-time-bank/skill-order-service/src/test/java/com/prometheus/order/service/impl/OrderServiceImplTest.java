package com.prometheus.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prometheus.common.BusinessException;
import com.prometheus.order.entity.SkillOrder;
import com.prometheus.order.mapper.SkillOrderMapper;
import com.prometheus.user.entity.User;
import com.prometheus.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private SkillOrderMapper skillOrderMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    // status constants mirroring OrderServiceImpl
    private static final int STATUS_PENDING = 1;
    private static final int STATUS_IN_PROGRESS = 2;
    private static final int STATUS_WAIT_COMPLETE = 3;
    private static final int STATUS_COMPLETED = 4;
    private static final int STATUS_CANCELLED = 5;

    private static final Long BUYER_ID = 10L;
    private static final Long SELLER_ID = 20L;
    private static final Long SKILL_ID = 100L;
    private static final Long ORDER_ID = 1000L;

    // ==================== createOrder ====================

    @Test
    void testCreateOrder_Success() {
        SkillOrder result = orderService.createOrder(BUYER_ID, SELLER_ID, SKILL_ID, 50,
                "13800001111", "明天下午3点", "图书馆", "需要辅导Java");

        assertNotNull(result);
        assertEquals(BUYER_ID, result.getBuyerId());
        assertEquals(SELLER_ID, result.getSellerId());
        assertEquals(SKILL_ID, result.getSkillId());
        assertEquals(50, result.getAmount());
        assertEquals(0, result.getFrozenAmount());
        assertEquals(STATUS_PENDING, result.getStatus());
        assertEquals(0, result.getBuyerConfirm());
        assertEquals(0, result.getSellerConfirm());
        assertEquals("13800001111", result.getContactPhone());
        assertNotNull(result.getOrderNo());
        verify(skillOrderMapper).insert(result);
    }

    @Test
    void testCreateOrder_SelfOrder_Throws() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.createOrder(BUYER_ID, BUYER_ID, SKILL_ID, 50,
                        "138", "time", "place", "plan"));
        assertTrue(ex.getMessage().contains("不能给自己下单"));
        verify(skillOrderMapper, never()).insert(any());
    }

    @Test
    void testCreateOrder_ZeroAmount_Throws() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.createOrder(BUYER_ID, SELLER_ID, SKILL_ID, 0,
                        "138", "time", "place", "plan"));
        assertTrue(ex.getMessage().contains("必须大于0"));
    }

    @Test
    void testCreateOrder_NullAmount_Throws() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.createOrder(BUYER_ID, SELLER_ID, SKILL_ID, null,
                        "138", "time", "place", "plan"));
        assertTrue(ex.getMessage().contains("必须大于0"));
    }

    @Test
    void testCreateOrder_NegativeAmount_Throws() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.createOrder(BUYER_ID, SELLER_ID, SKILL_ID, -10,
                        "138", "time", "place", "plan"));
        assertTrue(ex.getMessage().contains("必须大于0"));
    }

    // ==================== confirmOrder ====================

    @Test
    void testConfirmOrder_Success() {
        SkillOrder pendingOrder = buildOrder(STATUS_PENDING);
        User buyer = buildUser(BUYER_ID, 100, 0);

        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(pendingOrder);
        when(userMapper.selectById(BUYER_ID)).thenReturn(buyer);

        orderService.confirmOrder(ORDER_ID, SELLER_ID);

        // verify buyer balance deducted and frozen
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(userCaptor.capture());
        User updatedBuyer = userCaptor.getValue();
        assertEquals(50, updatedBuyer.getBalance());   // 100 - 50
        assertEquals(50, updatedBuyer.getFrozenBalance()); // 0 + 50

        // verify order status changed
        ArgumentCaptor<SkillOrder> orderCaptor = ArgumentCaptor.forClass(SkillOrder.class);
        verify(skillOrderMapper).updateById(orderCaptor.capture());
        SkillOrder updatedOrder = orderCaptor.getValue();
        assertEquals(STATUS_IN_PROGRESS, updatedOrder.getStatus());
        assertEquals(50, updatedOrder.getFrozenAmount());
    }

    @Test
    void testConfirmOrder_NotFound_Throws() {
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.confirmOrder(ORDER_ID, SELLER_ID));
        assertTrue(ex.getMessage().contains("订单不存在"));
    }

    @Test
    void testConfirmOrder_NotSeller_Throws() {
        SkillOrder order = buildOrder(STATUS_PENDING);
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.confirmOrder(ORDER_ID, 999L)); // wrong seller
        assertTrue(ex.getMessage().contains("只有卖方"));
    }

    @Test
    void testConfirmOrder_WrongStatus_Throws() {
        SkillOrder order = buildOrder(STATUS_IN_PROGRESS); // not PENDING
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.confirmOrder(ORDER_ID, SELLER_ID));
        assertTrue(ex.getMessage().contains("状态不正确"));
    }

    @Test
    void testConfirmOrder_BuyerNotFound_Throws() {
        SkillOrder order = buildOrder(STATUS_PENDING);
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);
        when(userMapper.selectById(BUYER_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.confirmOrder(ORDER_ID, SELLER_ID));
        assertTrue(ex.getMessage().contains("买方用户不存在"));
    }

    @Test
    void testConfirmOrder_InsufficientBalance_Throws() {
        SkillOrder order = buildOrder(STATUS_PENDING);
        User buyer = buildUser(BUYER_ID, 30, 0); // balance(30) < amount(50)
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);
        when(userMapper.selectById(BUYER_ID)).thenReturn(buyer);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.confirmOrder(ORDER_ID, SELLER_ID));
        assertTrue(ex.getMessage().contains("余额不足"));
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void testConfirmOrder_BuyerNullBalance_TreatedAsZero() {
        SkillOrder order = buildOrder(STATUS_PENDING);
        User buyer = buildUser(BUYER_ID, null, 0); // null balance → treated as 0
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);
        when(userMapper.selectById(BUYER_ID)).thenReturn(buyer);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.confirmOrder(ORDER_ID, SELLER_ID));
        assertTrue(ex.getMessage().contains("余额不足"));
    }

    // ==================== buyerConfirmComplete ====================

    @Test
    void testBuyerConfirmComplete_Success_WaitForSeller() {
        SkillOrder order = buildOrder(STATUS_IN_PROGRESS);
        order.setBuyerConfirm(0);
        order.setSellerConfirm(0);
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        orderService.buyerConfirmComplete(ORDER_ID, BUYER_ID);

        verify(skillOrderMapper, times(2)).updateById(order);
        assertEquals(1, order.getBuyerConfirm());
        assertEquals(STATUS_WAIT_COMPLETE, order.getStatus());
    }

    @Test
    void testBuyerConfirmComplete_BothConfirmed_TriggersDoComplete() {
        // Seller already confirmed → order is WAIT_COMPLETE with sellerConfirm=1
        SkillOrder orderAfterSellerConfirm = buildOrder(STATUS_WAIT_COMPLETE);
        orderAfterSellerConfirm.setBuyerConfirm(0);
        orderAfterSellerConfirm.setSellerConfirm(1);

        // When doCompleteOrder re-queries
        SkillOrder orderForDoComplete = buildOrder(STATUS_WAIT_COMPLETE);
        orderForDoComplete.setBuyerConfirm(1);
        orderForDoComplete.setSellerConfirm(1);

        User buyer = buildUser(BUYER_ID, 900, 50);  // frozenBalance=50
        User seller = buildUser(SELLER_ID, 0, 0);

        when(skillOrderMapper.selectById(ORDER_ID))
                .thenReturn(orderAfterSellerConfirm)  // first call in buyerConfirmComplete
                .thenReturn(orderForDoComplete);       // second call in doCompleteOrder
        when(userMapper.selectById(BUYER_ID)).thenReturn(buyer);
        when(userMapper.selectById(SELLER_ID)).thenReturn(seller);

        orderService.buyerConfirmComplete(ORDER_ID, BUYER_ID);

        // buyer: frozen 50 - 50 = 0
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(2)).updateById(userCaptor.capture());
        List<User> updatedUsers = userCaptor.getAllValues();
        assertEquals(0, updatedUsers.get(0).getFrozenBalance());   // buyer frozen released
        assertEquals(50, updatedUsers.get(1).getBalance());         // seller gets 50

        // order: status COMPLETED, frozenAmount cleared
        ArgumentCaptor<SkillOrder> orderCaptor = ArgumentCaptor.forClass(SkillOrder.class);
        verify(skillOrderMapper, atLeastOnce()).updateById(orderCaptor.capture());
        SkillOrder lastUpdate = orderCaptor.getValue();
        assertEquals(STATUS_COMPLETED, lastUpdate.getStatus());
        assertEquals(0, lastUpdate.getFrozenAmount());
    }

    @Test
    void testBuyerConfirmComplete_NotBuyer_Throws() {
        SkillOrder order = buildOrder(STATUS_IN_PROGRESS);
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.buyerConfirmComplete(ORDER_ID, 999L)); // not buyer
        assertTrue(ex.getMessage().contains("只有买方"));
    }

    @Test
    void testBuyerConfirmComplete_WrongStatus_Throws() {
        SkillOrder order = buildOrder(STATUS_PENDING); // only IN_PROGRESS or WAIT_COMPLETE allowed
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.buyerConfirmComplete(ORDER_ID, BUYER_ID));
        assertTrue(ex.getMessage().contains("状态不正确"));
    }

    @Test
    void testBuyerConfirmComplete_Duplicate_Throws() {
        SkillOrder order = buildOrder(STATUS_IN_PROGRESS);
        order.setBuyerConfirm(1); // already confirmed
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.buyerConfirmComplete(ORDER_ID, BUYER_ID));
        assertTrue(ex.getMessage().contains("请勿重复操作"));
    }

    @Test
    void testBuyerConfirmComplete_OrderNotFound_Throws() {
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.buyerConfirmComplete(ORDER_ID, BUYER_ID));
        assertTrue(ex.getMessage().contains("订单不存在"));
    }

    // ==================== sellerConfirmComplete ====================

    @Test
    void testSellerConfirmComplete_Success_WaitForBuyer() {
        SkillOrder order = buildOrder(STATUS_IN_PROGRESS);
        order.setBuyerConfirm(0);
        order.setSellerConfirm(0);
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        orderService.sellerConfirmComplete(ORDER_ID, SELLER_ID);

        verify(skillOrderMapper, times(2)).updateById(order);
        assertEquals(1, order.getSellerConfirm());
        assertEquals(STATUS_WAIT_COMPLETE, order.getStatus());
    }

    @Test
    void testSellerConfirmComplete_NotSeller_Throws() {
        SkillOrder order = buildOrder(STATUS_IN_PROGRESS);
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.sellerConfirmComplete(ORDER_ID, BUYER_ID)); // buyer is not seller
        assertTrue(ex.getMessage().contains("只有卖方"));
    }

    @Test
    void testSellerConfirmComplete_Duplicate_Throws() {
        SkillOrder order = buildOrder(STATUS_IN_PROGRESS);
        order.setSellerConfirm(1);
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.sellerConfirmComplete(ORDER_ID, SELLER_ID));
        assertTrue(ex.getMessage().contains("请勿重复操作"));
    }

    // ==================== cancelOrder ====================

    @Test
    void testCancelOrder_Success_ByBuyer() {
        SkillOrder order = buildOrder(STATUS_PENDING);
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        orderService.cancelOrder(ORDER_ID, BUYER_ID);

        verify(skillOrderMapper).updateById(order);
        assertEquals(STATUS_CANCELLED, order.getStatus());
    }

    @Test
    void testCancelOrder_Success_BySeller() {
        SkillOrder order = buildOrder(STATUS_PENDING);
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        orderService.cancelOrder(ORDER_ID, SELLER_ID);

        verify(skillOrderMapper).updateById(order);
        assertEquals(STATUS_CANCELLED, order.getStatus());
    }

    @Test
    void testCancelOrder_NotFound_Throws() {
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.cancelOrder(ORDER_ID, BUYER_ID));
        assertTrue(ex.getMessage().contains("订单不存在"));
    }

    @Test
    void testCancelOrder_NotBuyerOrSeller_Throws() {
        SkillOrder order = buildOrder(STATUS_PENDING);
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.cancelOrder(ORDER_ID, 999L));
        assertTrue(ex.getMessage().contains("只有买卖双方"));
    }

    @Test
    void testCancelOrder_WrongStatus_Throws() {
        SkillOrder order = buildOrder(STATUS_IN_PROGRESS); // not PENDING
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.cancelOrder(ORDER_ID, SELLER_ID));
        assertTrue(ex.getMessage().contains("仅在待确认状态"));
    }

    // ==================== getOrderDetail ====================

    @Test
    void testGetOrderDetail_Success() {
        SkillOrder order = buildOrder(STATUS_IN_PROGRESS);
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        SkillOrder result = orderService.getOrderDetail(ORDER_ID);
        assertNotNull(result);
        assertEquals(ORDER_ID, result.getId());
        assertEquals(STATUS_IN_PROGRESS, result.getStatus());
    }

    @Test
    void testGetOrderDetail_NotFound_Throws() {
        when(skillOrderMapper.selectById(ORDER_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.getOrderDetail(ORDER_ID));
        assertTrue(ex.getMessage().contains("订单不存在"));
    }

    // ==================== getBuyerOrders / getSellerOrders ====================

    @Test
    void testGetBuyerOrders_Success() {
        SkillOrder o1 = buildOrder(STATUS_IN_PROGRESS);
        SkillOrder o2 = buildOrder(STATUS_COMPLETED);
        when(skillOrderMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(o1, o2));

        List<SkillOrder> result = orderService.getBuyerOrders(BUYER_ID);
        assertEquals(2, result.size());
    }

    @Test
    void testGetSellerOrders_Success() {
        when(skillOrderMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        List<SkillOrder> result = orderService.getSellerOrders(SELLER_ID);
        assertTrue(result.isEmpty());
    }

    // ==================== helper methods ====================

    private SkillOrder buildOrder(int status) {
        SkillOrder order = new SkillOrder();
        order.setId(ORDER_ID);
        order.setOrderNo("SN" + ORDER_ID);
        order.setBuyerId(BUYER_ID);
        order.setSellerId(SELLER_ID);
        order.setSkillId(SKILL_ID);
        order.setAmount(50);
        order.setFrozenAmount(0);
        order.setStatus(status);
        order.setBuyerConfirm(0);
        order.setSellerConfirm(0);
        order.setContactPhone("13800001111");
        order.setAppointmentTime("明天下午3点");
        order.setAppointmentLocation("图书馆");
        return order;
    }

    private User buildUser(Long id, Integer balance, Integer frozenBalance) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setBalance(balance);
        user.setFrozenBalance(frozenBalance);
        user.setStatus(1);
        user.setRole("USER");
        return user;
    }
}
