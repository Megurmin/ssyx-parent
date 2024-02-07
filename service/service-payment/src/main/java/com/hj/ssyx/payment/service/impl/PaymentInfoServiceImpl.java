package com.hj.ssyx.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.ssyx.common.exception.SsyxException;
import com.hj.ssyx.common.result.ResultCodeEnum;
import com.hj.ssyx.enums.PaymentStatus;
import com.hj.ssyx.enums.PaymentType;
import com.hj.ssyx.model.order.OrderInfo;
import com.hj.ssyx.model.order.PaymentInfo;
import com.hj.ssyx.mq.constant.MQConst;
import com.hj.ssyx.mq.service.RabbitService;
import com.hj.ssyx.order.client.OrderFeignClient;
import com.hj.ssyx.payment.mapper.PaymentInfoMapper;
import com.hj.ssyx.payment.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author HuangJin
 * @date 16:10 2024/2/6
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public PaymentInfo getPaymentInfoByOrderNo(String orderNo) {
        PaymentInfo paymentInfo = baseMapper.selectOne(new LambdaQueryWrapper<PaymentInfo>()
                .eq(PaymentInfo::getOrderNo, orderNo));
        return paymentInfo;
    }

    @Override
    public PaymentInfo savePaymentInfo(String orderNo) {
        // 远程调用，根据orderNo查询订单信息
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderNo);
        if (orderInfo == null) {
            throw new SsyxException(ResultCodeEnum.DATA_ERROR);
        }
        // 封装到PaymentInfo对象
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(PaymentType.WEIXIN);
        paymentInfo.setUserId(orderInfo.getUserId());
        paymentInfo.setOrderNo(orderInfo.getOrderNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        String subject = "userID"+orderInfo.getUserId()+"下订单";
        paymentInfo.setSubject(subject);
        //paymentInfo.setTotalAmount(order.getTotalAmount());
        // TODO 测试，支付0.01
        paymentInfo.setTotalAmount(new BigDecimal("0.01"));

        // 添加
        baseMapper.insert(paymentInfo);
        return  paymentInfo;
    }

    @Override
    public void paySuccess(String orderNo, Map<String, String> resultMap) {
        //1 查询当前订单支付记录表状态是否已经支付
        PaymentInfo paymentInfo = baseMapper.selectOne(new LambdaQueryWrapper<PaymentInfo>()
                .eq(PaymentInfo::getOrderNo,orderNo));
        if (paymentInfo.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }
        //2 如果支付记录表支付状态没有支付，更新
        paymentInfo.setPaymentStatus(PaymentStatus.PAID);
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));
        paymentInfo.setCallbackContent(resultMap.toString());
        baseMapper.updateById(paymentInfo);
        //3 整合RabbitMQ实现 修改订单记录已支付，库存扣减
        rabbitService.sendMessage(MQConst.EXCHANGE_PAY_DIRECT,
                MQConst.ROUTING_PAY_SUCCESS,orderNo);

    }
}
