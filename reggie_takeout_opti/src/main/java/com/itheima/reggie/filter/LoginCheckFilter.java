package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.itheima.reggie.common.R;
/**
 * 检查用户是否已经完成登录
 * @author MiracloW
 * @date 2022-10-10 21:42
 */
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器，支持通配符 /**
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        log.info("线程id:{}", Thread.currentThread().getId());

        //1、获取本次请求的URI
        String requsetURI = request.getRequestURI();

        log.info("拦截到请求：{}",requsetURI);


        //这样做存在问题：当某一端登录成功后，另一端不需要登录也能请求资源
        //修改：把用户端和后台端的放行路径进行分开判断(待完善)
        StringBuffer requestURL = request.getRequestURL();//http://localhost:8080/front/index.html


        String[] urls = new String[]{ //放行的请求
                "/employee/login",
                "/employee/logout",
                "/backend/**", // 静态资源不用处理都放行
                "/common/**",
                "/front/**",
                "/user/sendMsg", //移动端发送短信
                "/user/login"   //移动端登录
        };

        //2、判断本次请求是否需要处理
        boolean check = check(requsetURI,urls);

        //3、如果不需要处理，则直接放行
        if(check){
            log.info("本次请求{}不需要处理",requsetURI);
            filterChain.doFilter(request,response);
            return;
        }
        //4、判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，用户ID为:{}",request.getSession().getAttribute("employee"));

            Long empid = (Long)request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empid);

            filterChain.doFilter(request,response);
            return;
        }

        //加入移动端用户的判定
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户ID为:{}",request.getSession().getAttribute("user"));

            Long userid = (Long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userid);

            filterChain.doFilter(request,response);
            return;
        }

        //前端响应拦截器：if (res.data.code === 0 && res.data.msg === 'NOTLOGIN')
        //前后端分离，后端不再处理页面，只返回数据
        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    //路径匹配，检查这次请求是否需要放行
    public boolean check(String requestURI,String[] urls){
        for(String url : urls){
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }

}
