package com.hmall.common.interceptor;

import com.hmall.common.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class UserInfoInteceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("请求头信息: {}", request.getHeaderNames());
        log.info("Authorization头: {}", request.getHeader("Authorization"));
        String userInfo = request.getHeader("user-info");
        log.info("user-info头: {}", userInfo);
        if (userInfo != null) {
           UserContext.setUser(Long.valueOf(userInfo));
           log.info("成功设置用户ID: {}", userInfo);
        } else {
           log.warn("未找到user-info头");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.removeUser();
    }

}
