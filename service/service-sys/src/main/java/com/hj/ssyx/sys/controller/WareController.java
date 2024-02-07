package com.hj.ssyx.sys.controller;

import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.model.sys.Ware;
import com.hj.ssyx.sys.service.WareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author HuangJin
 * @date 23:59 2023/9/13
 */
@Api(tags = "仓库接口")
@RestController
@RequestMapping("admin/sys/ware")
public class WareController {
    @Autowired
    private WareService wareService;

    // 查询所有仓库列表
    @ApiOperation("查询所有仓库")
    @GetMapping("findAllList")
    public Result findAllList() {
        List<Ware> list = wareService.list();
        return Result.ok(list);
    }
}
