package com.example.demo.service;

import com.example.demo.pojo.Mail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/6/25
 */


public interface PublishMessageService {
    void send(Mail mail);

}
