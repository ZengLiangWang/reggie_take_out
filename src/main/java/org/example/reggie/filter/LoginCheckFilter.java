package org.example.reggie.filter;

import ch.qos.logback.core.joran.action.AbstractEventEvaluatorAction;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.BaseContextUtils;
import org.example.reggie.common.R;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登陆检查器
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //定义路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //1，获取请求URI
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();
        String referer=request.getHeader("Referer");
        log.info("referer: {}",referer);
        log.info("uri: {}",requestURI);
        //2，判断当前URI是否需要处理，如果不需要处理则直接放行
        //定义不需要处理的URI (登陆，退出登录，静态资源)
        String[] uris = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };
        //如果不需要处理，直接放行
        if(check(uris,requestURI)){
            filterChain.doFilter(request,response);
            return;
        }
        //3，判断是否登录
        //查看session中是否有 "employee"(后台管理)
        if(referer.contains("backend") && request.getSession().getAttribute("employee")!=null){
            //将当前登录的 id 存入ThreadLocal 以供 公共字段填充类使用
            Long empId=(Long) request.getSession().getAttribute("employee");
            BaseContextUtils.set(empId);
            //放行
            filterChain.doFilter(request,response);
            return;
        }
        //查看session总是否有"user"（移动端）
        if(referer.contains("front") && request.getSession().getAttribute("user")!= null){
            //将当前登录的 id 存入 ThreadLocal 以供公共字段填充使用
            Long userId=(Long) request.getSession().getAttribute("user");
            BaseContextUtils.set(userId);
            //放行
            filterChain.doFilter(request,response);
            return;
        }
        //4，如果没有登录，跳转到登录页面
        //向浏览器返回没有登陆的响应
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 判断是否需要处理的方法
     * @param uris
     * @param requestURI
     * @return
     */
    public boolean check(String[] uris, String requestURI){
        for (String uri : uris) {
            boolean match = PATH_MATCHER.match(uri, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}