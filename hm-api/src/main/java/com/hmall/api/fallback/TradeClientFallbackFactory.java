package com.hmall.api.fallback;

import com.hmall.api.client.TradeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
@Slf4j
public class TradeClientFallbackFactory implements FallbackFactory<TradeClient> {
    @Override
    public TradeClient create(Throwable cause) {
        return new TradeClient() {
            @Override
            public void markOrderPaySuccess(Long orderId) {
                log.error("markOrderPaySuccess error: {}", cause.getMessage());
                throw new RuntimeException(cause);
            }
        };
    }
}
