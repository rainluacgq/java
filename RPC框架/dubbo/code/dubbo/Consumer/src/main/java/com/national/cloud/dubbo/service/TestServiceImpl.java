package com.national.cloud.dubbo.service;

import com.alibaba.dubbo.config.annotation.Reference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/8/30
 */


@Slf4j
@Service
public class TestServiceImpl {
    @Reference
    private TestService testService;

    public void Test(Long id){
        log.info("haha {}",id);
    }
}
