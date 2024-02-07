package com.hj.ssyx.payment.controller;

import com.hj.ssyx.common.result.Result;
import com.hj.ssyx.common.result.ResultCodeEnum;
import com.hj.ssyx.payment.service.PaymentInfoService;
import com.hj.ssyx.payment.service.WeixinService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author HuangJin
 * @date 16:04 2024/2/6
 */
@Api(tags = "微信支付接口")
@RestController
@RequestMapping("/api/payment/weixin")
public class WeixinController {

    @Autowired
    private WeixinService weixinService;

    @Autowired
    private PaymentInfoService paymentInfoService;

    // 调用微信支付系统生成预付单
    @GetMapping("/createJsapi/{orderNo}")
    public Result createJsapi(@PathVariable("orderNo") String orderNo) {
        Map<String,String> map = weixinService.createJsapi(orderNo);
        return Result.ok(null);
    }

    // 查询订单支付状态
    @GetMapping("/queryPayStatus/{orderNo}")
    public Result queryPayStatus(@PathVariable("orderNo") String orderNo) {
        //1 调用微信支付系统接口查询订单支付状态
        Map<String,String> resultMap = weixinService.queryPayStatus(orderNo);
        //2 微信支付系统返回值为null，支付失败
        if (resultMap == null) {
            return Result.build(null, ResultCodeEnum.PAYMENT_FAIL);
        }
        //3 如果微信支付系统返回值正常，支付成功
        if ("SUCCESS".equals(resultMap.get("trade_state"))) {
            String outTradeNo = resultMap.get("out_trade_no");
            paymentInfoService.paySuccess(outTradeNo,resultMap);
            Result.ok(null);
        }
        //4 支付中，等待
        return Result.build(null,ResultCodeEnum.PAYMENT_WAITING);
    }
}
