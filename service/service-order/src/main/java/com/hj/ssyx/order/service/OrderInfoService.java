package com.hj.ssyx.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.ssyx.model.order.OrderInfo;
import com.hj.ssyx.vo.order.OrderConfirmVo;
import com.hj.ssyx.vo.order.OrderSubmitVo;

/**
 * @author HuangJin
 * @date 1:37 2024/1/17
 */
public interface OrderInfoService extends IService<OrderInfo> {
    // 确定订单
    OrderConfirmVo confirmOrder();

    // 生成订单
    Long submitOrder(OrderSubmitVo orderSubmitVo);

    // 订单详情
    OrderInfo getOrderInfoById(Long orderId);

    OrderInfo getOrderInfoByOrderNo(String orderNo);

    // 订单支付成功，更新订单状态，扣减库存
    void orderPay(String orderNo);
}
