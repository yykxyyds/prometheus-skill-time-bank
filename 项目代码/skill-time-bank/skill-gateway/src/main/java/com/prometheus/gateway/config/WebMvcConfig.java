package com.prometheus.gateway.config;

import com.prometheus.user.interceptor.UserAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Gateway 模块统一注册认证拦截器，覆盖所有 /api/** 路径。
 * 具体哪些接口需要强制登录由方法上的 @RequireAuth 注解控制。
 */
@Configuration("gatewayWebMvcConfig")
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserAuthInterceptor userAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns("/api/**");
    }
}
