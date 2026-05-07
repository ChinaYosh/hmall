package com.hmall.api.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import com.hmall.api.client.UserClient;
@Slf4j
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return new UserClient()
        {


            @Override
            public void deductMoney(String pw, Integer amount) {
                log.error("deductMoney error: {}", cause.getMessage());
                throw new RuntimeException(cause);
            }
        };
    }
}
