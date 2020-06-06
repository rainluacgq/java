package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/6/6
 */



@Component
public class ThreadPoolService {
        private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolService.class);

        @Async
        public  void test() throws InterruptedException {
            LOGGER.info("current thread name:{} ,id{}",Thread.currentThread().getName(),
                    Thread.currentThread().getId());
            Thread.sleep(300);
        }

}
