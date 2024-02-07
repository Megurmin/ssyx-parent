package com.hj.ssyx.order.client;

import com.hj.ssyx.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author HuangJin
 * @date 20:54 2024/2/6
 */
@FeignClient("service-order")
public interface OrderFeignClient {

    // 根据orderNo查询订单信息
    @GetMapping("/api/order/inner/getOrderInfo/{orderNo}")
    public OrderInfo getOrderInfo(@PathVariable("orderNo") String orderNo);
}
