package com.hj.ssyx.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.order.PaymentInfo;

import java.util.Map;

/**
 * @author HuangJin
 * @date 16:09 2024/2/6
 */
public interface PaymentInfoService extends IService<PaymentInfo> {
    PaymentInfo getPaymentInfoByOrderNo(String orderNo);

    PaymentInfo savePaymentInfo(String orderNo);

    void paySuccess(String outTradeNo, Map<String, String> resultMap);
}
