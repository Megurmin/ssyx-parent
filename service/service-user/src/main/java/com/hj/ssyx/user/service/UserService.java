package com.hj.ssyx.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.user.User;
import com.hj.ssyx.vo.user.LeaderAddressVo;
import com.hj.ssyx.vo.user.UserLoginVo;

/**
 * @author HuangJin
 * @date 17:30 2023/10/6
 */
public interface UserService extends IService<User> {
    User getUserByOpenId(String openid);

    LeaderAddressVo getLeaderAddressByUserId(Long userId);

    UserLoginVo getUserLoginVo(Long id);
}
