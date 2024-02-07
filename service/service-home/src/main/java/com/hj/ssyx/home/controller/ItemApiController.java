package com.hj.ssyx.home.controller;

import com.hj.ssyx.common.auth.AuthContextHolder;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.home.service.ItemService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author HuangJin
 * @date 22:31 2023/12/30
 */
@Api(tags = "商品详情")
@RestController
@RequestMapping("api/home")
public class ItemApiController {
    @Autowired
    private ItemService itemService;

    @GetMapping("item/{id}")
    public Result index(@PathVariable Long id){
        Long userId = AuthContextHolder.getUserId();
        Map<String,Object> map = itemService.item(id,userId);
        return Result.ok(map);
    }
}
