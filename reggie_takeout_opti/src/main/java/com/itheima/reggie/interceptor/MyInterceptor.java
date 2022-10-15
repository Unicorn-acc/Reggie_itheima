package com.itheima.reggie.interceptor;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 使用拦截器进行用户登录判定：
 * 1、编写拦截器实现类MyInterceptor
 *      用session判断用户登录
 * 2、编写拦截器配置类
 *      在WebMvcConfig中设置拦截器配置addInterceptors，指定要拦截的请求和排除的请求
 * @author MiracloW
 * @date 2022-10-15 08:02
 */
@Configuration
@Slf4j
public class MyInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        log.info("preHandle方法,id:{}",request.getSession().getId());
//        //使用拦截器进行登录判断
//        // 统一拦截，判断是否有登录（输入有账号密码，user就会存储到session）
//        Object value = request.getSession().getAttribute("user");
//        if (value != null) {
//            return true;
//        }
//        value = request.getSession().getAttribute("employee");
//        if(value != null){
//            return true;
//        }
//        log.info("用户未登录");
//        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
