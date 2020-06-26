package com.example.demo.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.example.demo.config.RabbitConfig;
import com.example.demo.mapper.MsgLogMapper;
import com.example.demo.mq.MessageHelper;
import com.example.demo.pojo.Mail;
import com.example.demo.pojo.MsgLog;
import com.example.demo.service.PublishMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/6/25
 */

@Service
@Slf4j
public class PublishMessageServiceImpl implements PublishMessageService {
    @Autowired
    private MsgLogMapper msgLogMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void send(Mail mail) {
        String msgId = UUID.randomUUID().toString();
        mail.setMsgId(msgId);

        MsgLog msgLog = new MsgLog(msgId, mail, RabbitConfig.MAIL_EXCHANGE_NAME, RabbitConfig.MAIL_ROUTING_KEY_NAME);
        msgLogMapper.insert(msgLog);// 消息入库

        CorrelationData correlationData = new CorrelationData(msgId);
        rabbitTemplate.convertAndSend(RabbitConfig.MAIL_EXCHANGE_NAME, RabbitConfig.MAIL_ROUTING_KEY_NAME, MessageHelper.objToMsg(mail), correlationData);// 发送消息

    }
}
