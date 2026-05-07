
package com.hmall.api.config;

import com.hmall.api.fallback.ItemClientFallbackFactory;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
@Slf4j
public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLogLevel(){
        return Logger.Level.FULL;
    }
    @Bean
    public RequestInterceptor userInfoRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Long id = UserContext.getUser();
                log.info("【Feign传递检查】当前线程名称: {}, 获取到的用户ID: {}", Thread.currentThread().getName(), id);
                if (id != null) {
                    requestTemplate.header("user-info", id.toString());
                }
            }
        };
    }
    @Bean
    public ItemClientFallbackFactory itemClientFallbackFactory() {
        return new ItemClientFallbackFactory();
    }

}