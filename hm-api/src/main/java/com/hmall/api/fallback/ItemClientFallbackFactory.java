package com.hmall.api.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
@Slf4j
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("queryItemByIds error: {}", cause.getMessage());
                return Collections.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.error("deductStock error: {}", cause.getMessage());
                throw new RuntimeException(cause);
            }

        };
    }
}
