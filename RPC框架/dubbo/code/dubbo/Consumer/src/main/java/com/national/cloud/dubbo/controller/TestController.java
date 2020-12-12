package com.national.cloud.dubbo.controller;

import com.national.cloud.dubbo.service.TestService;
import com.national.cloud.dubbo.service.TestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/8/30
 */


@RestController
public class TestController {

    @Autowired
    private TestServiceImpl testService;

    @PostMapping("/test")
    public  void Test(@RequestParam(value = "id") Long id){
        testService.Test(id);
    }
}
