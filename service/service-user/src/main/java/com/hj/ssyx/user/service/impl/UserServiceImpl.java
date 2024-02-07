package com.hj.ssyx.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.model.user.Leader;
import com.hj.ssyx.model.user.User;
import com.hj.ssyx.model.user.UserDelivery;
import com.hj.ssyx.user.mapper.LeaderMapper;
import com.hj.ssyx.user.mapper.UserDeliveryMapper;
import com.hj.ssyx.user.mapper.UserMapper;
import com.hj.ssyx.user.service.UserService;
import com.hj.ssyx.vo.user.LeaderAddressVo;
import com.hj.ssyx.vo.user.UserLoginVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author HuangJin
 * @date 17:32 2023/10/6
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserDeliveryMapper userDeliveryMapper;
    @Autowired
    private LeaderMapper leaderMapper;
    @Override
    public User getUserByOpenId(String openid) {
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenId, openid));
        return user;
    }

    @Override
    public LeaderAddressVo getLeaderAddressByUserId(Long userId) {
        UserDelivery userDelivery = userDeliveryMapper.selectOne(new LambdaQueryWrapper<UserDelivery>().eq(UserDelivery::getUserId, userId).eq(UserDelivery::getIsDefault, 1));
        if (userDelivery == null) {
            return null;
        }
        // 拿着上面查询团长id查询leader表查询团长其他信息
        Leader leader = leaderMapper.selectById(userDelivery.getLeaderId());
        // 封装数据到LeaderAddressVo
        LeaderAddressVo leaderAddressVo = new LeaderAddressVo();
        BeanUtils.copyProperties(leader, leaderAddressVo);
        leaderAddressVo.setUserId(userId);
        leaderAddressVo.setLeaderId(leader.getId());
        leaderAddressVo.setLeaderName(leader.getName());
        leaderAddressVo.setLeaderPhone(leader.getPhone());
        leaderAddressVo.setWareId(userDelivery.getWareId());
        leaderAddressVo.setStorePath(leader.getStorePath());
        return leaderAddressVo;
    }

    @Override
    public UserLoginVo getUserLoginVo(Long id) {
        User user = baseMapper.selectById(id);
        UserLoginVo userLoginVo = new UserLoginVo();
        userLoginVo.setUserId(id);
        userLoginVo.setNickName(user.getNickName());
        userLoginVo.setPhotoUrl(user.getPhotoUrl());
        userLoginVo.setIsNew(user.getIsNew());
        userLoginVo.setOpenId(user.getOpenId());

        UserDelivery userDelivery = userDeliveryMapper.selectOne(new LambdaQueryWrapper<UserDelivery>().eq(UserDelivery::getUserId, id).eq(UserDelivery::getIsDefault, 1));
        if (userDelivery != null) {
            userLoginVo.setLeaderId(userDelivery.getLeaderId());
            userLoginVo.setWareId(userDelivery.getWareId());
        } else {
            userLoginVo.setLeaderId(1L);
            userLoginVo.setWareId(1L);
        }
        return userLoginVo;
    }
}
