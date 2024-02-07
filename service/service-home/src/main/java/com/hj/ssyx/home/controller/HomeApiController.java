package com.hj.ssyx.home.controller;

import com.hj.ssyx.common.auth.AuthContextHolder;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.home.service.HomeService;
import com.hj.ssyx.vo.search.SkuEsQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author HuangJin
 * @date 0:20 2023/10/10
 */
@Api(tags = "首页接口")
@RestController
@RequestMapping("api/home")
public class HomeApiController {
    @Autowired
    private HomeService homeService;

    @ApiOperation("首页数据显示接口")
    @GetMapping("index")
    public Result index(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId();
        Map<String,Object> map = homeService.homeData(userId);
        return Result.ok(map);
    }
}
