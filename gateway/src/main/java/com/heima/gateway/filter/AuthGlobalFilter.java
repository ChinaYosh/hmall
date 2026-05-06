package com.heima.gateway.filter;

import cn.hutool.core.text.AntPathMatcher;
import com.heima.gateway.config.AuthProperties;
import com.heima.gateway.util.JwtTool;
import com.hmall.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthGlobalFilter implements GlobalFilter,Ordered {
    private  final AuthProperties authProperties;
    private  final JwtTool jwtTool;
    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        /**
         * 1.获取请求
         * 2.判断请求是否需要认证
         * 3.如果需要认证，判断请求是否携带token
         * 4.如果请求携带了token，判断token是否有效
         * 5.如果token有效，放行
         * 6.如果token无效，返回未授权
         * 7.如果请求不需要认证，放行
         */
        ServerHttpRequest request =  exchange.getRequest();
        log.info("收到请求: path={}, headers={}", request.getPath(), request.getHeaders().keySet());
        if(isExclude(request.getPath().toString()))
        {
            log.info("该路径在排除列表中，直接放行: {}", request.getPath());
            return chain.filter(exchange);
        }
        var tokenList = request.getHeaders().get("Authorization");
        log.info("获取到的Authorization header: {}", tokenList);
        Long userId = null;
        if(tokenList != null && tokenList.size() > 0)
        {
            String token = tokenList.get(0);
            log.info("原始token: {}", token);
            // 处理 Bearer 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
                log.info("去除Bearer前缀后的token: {}", token);
            }
            try
            {
                userId = jwtTool.parseToken(token);
                log.info("解析得到的userId: {}", userId);
                if(userId != null)
                {
                    String userInfo = userId.toString();
                    // 将用户信息(id)放入请求头，方便下游微服务获取
                     ServerWebExchange ex = exchange.mutate()
                                .request(b -> b.header("user-info", userInfo)).build();
                    log.info("添加user-info header后放行请求");
                    return chain.filter(ex);
                }
            }
            catch (UnauthorizedException e)
            {
                var response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        }
        log.warn("未授权访问: path={}, token={}", request.getPath(), tokenList);
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 0;
    }
    private boolean isExclude(String path) {
      for(String excludePath : authProperties.getExcludePaths())
      {
          if(antPathMatcher.match(excludePath, path))
          {
              return true;
          }
      }
      return false;
    }
}
