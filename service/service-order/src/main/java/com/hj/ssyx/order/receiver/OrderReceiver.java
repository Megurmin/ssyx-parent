package com.hj.ssyx.order.receiver;

import com.hj.ssyx.mq.constant.MQConst;
import com.hj.ssyx.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author HuangJin
 * @date 17:57 2024/2/7
 */
@Component
public class OrderReceiver {

    @Autowired
    private OrderInfoService orderInfoService;

    // 订单支付成功，更新订单状态，扣减库存
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_ORDER_PAY,durable = "true"),
            exchange = @Exchange(value = MQConst.EXCHANGE_PAY_DIRECT),
            key = {MQConst.ROUTING_PAY_SUCCESS}
    ))
    public void orderPay(String orderNo,
                         Message message,
                         Channel channel) throws IOException {
        if (!StringUtils.isEmpty(orderNo)) {
            orderInfoService.orderPay(orderNo);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
