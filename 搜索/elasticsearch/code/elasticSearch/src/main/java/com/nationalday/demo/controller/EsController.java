package com.nationalday.demo.controller;

import com.nationalday.demo.service.EsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/11/8
 */

@Controller
public class EsController {
    @Autowired
    private EsService esService;

    @RequestMapping("/")
    @ResponseBody
    public  void testEs(){
        esService.testInsert();
    }

    @RequestMapping("/query")
    @ResponseBody
    public  void testEsQuery(@Param("keyword") String keyword){
        esService.testSearch(keyword);
    }
}
