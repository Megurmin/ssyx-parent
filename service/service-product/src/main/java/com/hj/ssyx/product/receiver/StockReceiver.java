package com.hj.ssyx.product.receiver;

import com.hj.ssyx.mq.constant.MQConst;
import com.hj.ssyx.product.service.SkuInfoService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author HuangJin
 * @date 22:40 2024/2/7
 */
@Component
public class StockReceiver {

    @Autowired
    private SkuInfoService skuInfoService;

    // 扣减库存
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_MINUS_STOCK,durable = "true"),
            exchange = @Exchange(value = MQConst.EXCHANGE_ORDER_DIRECT),
            key = {MQConst.ROUTING_MINUS_STOCK}
    ))
    public void minusStock(String orderNo, Message message, Channel channel) throws IOException {
        if (!StringUtils.isEmpty(orderNo)) {
            skuInfoService.minusStock(orderNo);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
