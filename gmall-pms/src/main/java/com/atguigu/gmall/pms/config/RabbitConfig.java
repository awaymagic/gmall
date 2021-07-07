package com.atguigu.gmall.pms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Date:2021/7/6
 * Author:away
 * Description: Rabbit
 */
@Configuration
@Slf4j
public class RabbitConfig {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @PostConstruct// 项目启动就执行该方法
    public void init() {
        // 是否到达交换机回调。不管有没有到达交换机都会执行
        this.rabbitTemplate.setConfirmCallback((@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause) -> {
            if (!ack) {
                log.error("消息没有到达交换机。原因：{}",cause);
            }
        });
        // 是否到达队列的回调。未到达了队列则会执行
        this.rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey)->{
            log.error("消息没有到达队列。状态码:{},响应内容:{},交换机:{},路由键:{},消息内容:{}",replyCode,replyText,exchange,routingKey,new String(message.getBody()));
        });
    }
}
