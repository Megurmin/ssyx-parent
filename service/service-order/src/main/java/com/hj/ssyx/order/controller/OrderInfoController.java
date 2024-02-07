package com.hj.ssyx.order.controller;

import com.hj.ssyx.common.auth.AuthContextHolder;
import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.model.order.OrderInfo;
import com.hj.ssyx.order.service.OrderInfoService;
import com.hj.ssyx.vo.order.OrderConfirmVo;
import com.hj.ssyx.vo.order.OrderSubmitVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author HuangJin
 * @date 1:37 2024/1/17
 */
@RestController
@RequestMapping("/api/order")
public class OrderInfoController {
    @Autowired
    private OrderInfoService orderInfoService;

    @ApiOperation("确认订单")
    @GetMapping("auth/confirmOrder")
    public Result confirm() {
        OrderConfirmVo orderConfirmVo = orderInfoService.confirmOrder();
        return Result.ok(orderConfirmVo);
    }

    @ApiOperation("生产订单")
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderSubmitVo orderSubmitVo) {
        // 获取用户Id
        Long userId = AuthContextHolder.getUserId();
        Long orderId = orderInfoService.submitOrder(orderSubmitVo);
        return Result.ok(orderId);
    }

    @ApiOperation("获取订单详情")
    @GetMapping("auth/getOrderInfoById/{orderId}")
    public Result getOrderInfoById(@PathVariable("orderId") Long orderId) {
        OrderInfo orderInfo = orderInfoService.getOrderInfoById(orderId);
        return Result.ok(orderInfo);
    }

    // 根据orderNo查询订单信息
    @GetMapping("inner/getOrderInfo/{orderNo}")
    public OrderInfo getOrderInfo(@PathVariable("orderNo") String orderNo) {
        return orderInfoService.getOrderInfoByOrderNo(orderNo);
    }
}
