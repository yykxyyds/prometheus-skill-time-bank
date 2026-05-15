package com.prometheus.wallet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import com.prometheus.wallet.entity.TimeTransaction;
import com.prometheus.wallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance")
    @RequireAuth
    public Result<Map<String, Object>> getBalance(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            return Result.success(walletService.getBalanceInfo(userId));
        } catch (BusinessException e) {
            if ("用户不存在".equals(e.getMessage())) {
                throw new BusinessException(401, "账号不存在或已被删除，请重新登录");
            }
            throw e;
        }
    }

    @GetMapping("/transactions")
    @RequireAuth
    public Result<Page<TimeTransaction>> getTransactions(
            HttpServletRequest request,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            return Result.success(walletService.getTransactions(userId, page, size));
        } catch (BusinessException e) {
            if ("用户不存在".equals(e.getMessage())) {
                throw new BusinessException(401, "账号不存在或已被删除，请重新登录");
            }
            throw e;
        }
    }
}
