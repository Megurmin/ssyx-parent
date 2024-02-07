package com.hj.ssyx.user.api;

import com.hj.ssyx.user.service.UserService;
import com.hj.ssyx.vo.user.LeaderAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HuangJin
 * @date 20:23 2023/10/11
 */
@RestController
@RequestMapping("/api/user/leader")
public class LeaderAddressApiController {
    @Autowired
    private UserService userService;

    // 根据userId查询提货点和团长信息
    @GetMapping("/inner/getUserAddressById/{userId}")
    public LeaderAddressVo getUserAddressByUserId(@PathVariable("userId") Long userId){
        LeaderAddressVo leaderAddressVo = userService.getLeaderAddressByUserId(userId);
        return leaderAddressVo;
    }
}
