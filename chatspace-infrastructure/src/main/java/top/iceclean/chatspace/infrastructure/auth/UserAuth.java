package top.iceclean.chatspace.infrastructure.auth;

import com.alibaba.fastjson2.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.iceclean.chatspace.infrastructure.constant.HeaderConst;
import top.iceclean.chatspace.infrastructure.constant.ResponseStatusEnum;
import top.iceclean.chatspace.infrastructure.pojo.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 校验当前用户
 * @author : Ice'Clean
 * @date : 2022-12-03
 */
@Component
@Aspect
@Order(0)
public class UserAuth implements WebMvcConfigurer {

    /** 白名单 */
    private static final String[] WRITE_PATH = {
            "/user/login",
            "/user/register",
            "/user/code"
    };

    private UserAuth() {

    }

    public static String getRequestHeader(String header) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return ((ServletRequestAttributes) attributes).getRequest().getHeader(header);
    }

    /**
     * 获取当前请求的用户 ID
     * 在 Gateway 进行校验时，会在 Header 的 AUTH_USER 写入用户 ID
     * @return 用户 ID
     */
    public static int getUserId() {
        String userId = getRequestHeader(HeaderConst.AUTH_USER);
        return Integer.parseInt(userId);
    }

    public static int getAdmin() {
        String adminTag = getRequestHeader(HeaderConst.ADMIN_TAG);
        return adminTag == null ? -1 : Integer.parseInt(adminTag);
    }

    /**
     * 用户请求头的检查拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object handler) throws IOException {
                final String reqPath = request.getServletPath();
                System.out.println("请求路径：" + reqPath);
                for (String path : WRITE_PATH) {
                    if (reqPath.startsWith(path)) {
                        // 白名单直接放行
                        return true;
                    }
                }
                // 任何请求，没有带 AUTH-USER 的全都赶出去
                if (request.getHeader(HeaderConst.AUTH_USER) != null) {
                    return true;
                }
                // 返回鉴权异常
                String errResp = JSON.toJSONString(new Response(ResponseStatusEnum.AUTHORITY_ERROR));
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().append(errResp);
                return false;
            }
        });
    }

    @Pointcut("@annotation(top.iceclean.chatspace.infrastructure.annotation.AdminCheck)")
    public void adminCheckPointCut() {
        // Pointcut methods should have empty body
    }

    /** 检查当前请求是否为管理员发起 */
    @Around(value = "adminCheckPointCut()")
    private Object adminCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        if (getAdmin() != 1) {
            return new Response(ResponseStatusEnum.AUTHORITY_ERROR);
        }
        return joinPoint.proceed();
    }
}
