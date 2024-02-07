package com.hj.ssyx.client.user;

import com.hj.ssyx.vo.user.LeaderAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author HuangJin
 * @date 23:34 2023/10/25
 */
@FeignClient("service-user")
public interface UserFeignClient {
    // 根据userId查询提货点和团长信息
    @GetMapping("api/user/leader/inner/getUserAddressById/{userId}")
    public LeaderAddressVo getUserAddressByUserId(@PathVariable("userId") Long userId);
}
