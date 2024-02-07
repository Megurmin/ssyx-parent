package com.hj.ssyx.common.auth;

import com.hj.ssyx.common.constant.RedisConst;
import com.hj.ssyx.common.utils.JwtHelper;
import com.hj.ssyx.vo.user.UserLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author HuangJin
 * @date 0:30 2023/10/9
 */
public class UserLoginInterceptor implements HandlerInterceptor {

    private RedisTemplate redisTemplate;

    public UserLoginInterceptor(RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        this.getUserLoginVo(request);
        return true;
    }

    private void getUserLoginVo(HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("token");
        // 判断token不为空
        if (!StringUtils.isEmpty(token)) {
            // 从token获取userId
            Long userId = JwtHelper.getUserId(token);
            // 根据userId到Redis获取用户信息
            UserLoginVo userLoginVo = (UserLoginVo) redisTemplate.opsForValue().get(RedisConst.USER_LOGIN_KEY_PREFIX + userId);
            // 获取数据放到ThreadLocal里面
            if (userLoginVo != null) {
                AuthContextHolder.setUserId(userLoginVo.getUserId());
                AuthContextHolder.setWareId(userLoginVo.getWareId());
                AuthContextHolder.setUserLoginVo  (userLoginVo);
            }
        }
    }
}
