package com.hj.ssyx.mq.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * @author HuangJin
 * @date 21:17 2023/9/30
 */
@Configuration
public class MQConfig {
    @Bean
    public MessageConverter messageConverter() {
        return  new Jackson2JsonMessageConverter();
    }
}
