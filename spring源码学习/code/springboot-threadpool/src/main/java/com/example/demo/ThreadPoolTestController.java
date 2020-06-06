package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/6/6
 */


@Controller
@RequestMapping("test")
public class ThreadPoolTestController {

    @Autowired
    private  ThreadPoolService poolService;

    @PostMapping("/")
    public  void test() throws InterruptedException {
        poolService.test();
    }
}
