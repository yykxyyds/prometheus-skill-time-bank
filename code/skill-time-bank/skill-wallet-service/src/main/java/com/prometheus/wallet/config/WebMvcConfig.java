package com.prometheus.wallet.config;

import com.prometheus.user.interceptor.UserAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 钱包模块：注册认证拦截器，覆盖 /api/wallet、/api/review、/api/appeal、/api/announcement 路径。
 * 具体哪些接口需要登录由方法上的 @RequireAuth 注解控制。
 */
@Configuration("walletWebMvcConfig")
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserAuthInterceptor userAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns(
                        "/api/wallet/**",
                        "/api/review/**",
                        "/api/appeal/**",
                        "/api/announcement/**");
    }
}
