package com.hj.ssyx.cart.receive;

import com.hj.ssyx.cart.service.CartInfoService;
import com.hj.ssyx.mq.constant.MQConst;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author HuangJin
 * @date 18:51 2024/2/3
 */
@Component
public class CartReceiver {

    @Autowired
    private CartInfoService cartInfoService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_DELETE_CART,durable = "true"),
            exchange = @Exchange(value = MQConst.EXCHANGE_ORDER_DIRECT),
            key = {MQConst.ROUTING_DELETE_CART}
    ))
    public void deleteCart(Long userId, Message message, Channel channel) throws IOException {
        if (userId != null) {
            cartInfoService.deleteCartChecKed(userId);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
