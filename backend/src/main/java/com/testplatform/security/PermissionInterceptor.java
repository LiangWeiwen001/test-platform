package com.testplatform.security;

import com.testplatform.common.BusinessException;
import com.testplatform.common.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * RBAC 权限拦截器：校验 @RequirePermission 注解声明的权限码
 * <p>
 * 无权限码注解的方法直接放行；有注解但当前用户缺少权限码 → 抛 40300（由全局异常处理器统一响应）。
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            return true;
        }

        LoginUser loginUser = LoginUser.get();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (loginUser.getPerms() == null || !loginUser.getPerms().contains(requirePermission.value())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return true;
    }
}