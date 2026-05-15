package com.prometheus.user.interceptor;

import com.prometheus.common.BusinessException;
import com.prometheus.common.JwtUtil;
import com.prometheus.common.annotation.RequireAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        // 非 Controller 方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireAuth requireAuth = handlerMethod.getMethodAnnotation(RequireAuth.class);

        // 从 Authorization 头解析 token（无论是否有 @RequireAuth，有 token 就尝试解析）
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Long userId = JwtUtil.getUserId(token);
                String role = JwtUtil.getRole(token);
                request.setAttribute("userId", userId);
                request.setAttribute("role", role);
                log.debug("认证通过: userId={}, role={}, path={}", userId, role, request.getRequestURI());
                return true;
            } catch (Exception e) {
                log.warn("token解析失败: {}", e.getMessage());
                if (requireAuth != null) {
                    throw new BusinessException(401, "token无效或已过期");
                }
                // token 无效但没有 @RequireAuth，放行（userId 为 null，Controller 自行处理）
                return true;
            }
        }

        // 无 token，但方法要求登录 → 报错
        if (requireAuth != null) {
            throw new BusinessException(401, "未登录或token已过期");
        }

        return true;
    }
}
