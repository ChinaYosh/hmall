package com.hmall.common.config;

import com.hmall.common.interceptor.UserInfoInteceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@ConditionalOnClass(DispatcherServlet.class)
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInteceptor())

                .addPathPatterns("/**"); // 拦截所有购物车相关的接口;
    }
}
