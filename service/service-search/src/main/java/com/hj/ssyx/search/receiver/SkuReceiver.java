package com.hj.ssyx.search.receiver;

import com.hj.ssyx.mq.constant.MQConst;
import com.hj.ssyx.search.service.SkuService;
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
 * @date 23:49 2023/9/30
 */
@Component
public class SkuReceiver {
    @Autowired
    private SkuService skuService;

    // 商品上架
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_GOODS_UPPER, durable = "true"),
            exchange = @Exchange(value = MQConst.EXCHANGE_GOODS_DIRECT),
            key = {MQConst.ROUTING_GOODS_UPPER}
    ))
    public void upperSku(Long skuId, Message message, Channel channel) throws IOException {
        if (skuId != null) {
            skuService.upperSku(skuId);
        }
        /*
         * 第一个参数：表示收到的消息的标号
         * 第二个参数：如果为true表示可以签收多个消息
         */
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    // 商品下架
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_GOODS_LOWER, durable = "true"),
            exchange = @Exchange(value = MQConst.EXCHANGE_GOODS_DIRECT),
            key = {MQConst.ROUTING_GOODS_LOWER}
    ))
    public void lowerSku(Long skuId, Message message, Channel channel) throws IOException {
        if (null != skuId) {
            skuService.lowerSku(skuId);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
