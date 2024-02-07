package com.hj.ssyx.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author HuangJin
 * @date 16:06 2024/2/6
 */
public interface WeixinService {
    // 调用微信支付系统生成预付单
    Map<String, String> createJsapi(String orderNo);

    Map<String, String> queryPayStatus(String orderNo);
}
