package com.example.demo.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/6/25
 */

@Configuration
@MapperScan(basePackages = "com.example.demo.mapper")
public class MyBatisConfig {
}
