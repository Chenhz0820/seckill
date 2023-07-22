package com.xxxx.seckill.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.util.CookieUtil;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    //前置拦截器
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            User user = getUser(request, response);
            UserContext.setUser(user);
            HandlerMethod hm = (HandlerMethod) handler;
            //判断用户请求的方法上面是否带有AccessLimit注解
            AccessLimit limit = hm.getMethodAnnotation(AccessLimit.class);
            if (limit == null) {
                return true;
            }
            int second = limit.second();
            int maxCount = limit.maxCount();
            boolean needLogin = limit.needLogin();
            String key = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    render(response,RespBeanEnum.SESSION_ERROR);
                    return false;
                }
            }
            key = key + ":" + user.getId();
            ValueOperations operations = redisTemplate.opsForValue();
            Integer count = (Integer) operations.get(key);
            if (count == null) {
                operations.set(key,1,second, TimeUnit.SECONDS);
            } else if (count < maxCount) {
                operations.increment(key);
            } else {
                render(response, RespBeanEnum.ACCESS_LIMIT);
                return false;
            }
        }
        return true;
    }

    /**
     * 构建返回对象
     * */
    private void render(HttpServletResponse response, RespBeanEnum respBeanEnum) throws IOException {
        //拼接response的头
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
//        创建打印流
        PrintWriter out = response.getWriter();
        RespBean rb = RespBean.error(respBeanEnum);
        //将通用返回对象转成json写入response中
        out.write(new ObjectMapper().writeValueAsString(rb));
        out.flush();
        out.close();
    }

    /**
     * 获取当前用户
     */
    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        //通过request获取请求中携带的sessionid
        String ticket = CookieUtil.getCookieValue(request, "userTicket");
        if (!StringUtils.hasLength(ticket)) {
            return null;
        }
        //根据页面来的session去redis中获取响应的user对象，如果有对象，就证明当前请求已经登录
        return userService.getUserByCookie(ticket, request, response);
    }
}
