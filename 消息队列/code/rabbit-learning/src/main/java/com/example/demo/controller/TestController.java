package com.example.demo.controller;

import com.example.demo.pojo.Mail;
import com.example.demo.service.PublishMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/6/25
 */


@Api(tags = "testController", description = "测试模块")
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    @Autowired
    private PublishMessageService publishMessageService;

    @ApiOperation(value = "测试发送邮件")
    @PostMapping("send")
    public void sendMail(@Validated Mail mail, Errors errors) {
        if (errors.hasErrors()) {
            String msg = errors.getFieldError().getDefaultMessage();
           log.info(msg);
        }

         publishMessageService.send(mail);
    }
}
