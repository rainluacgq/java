package com.example.demo.pojo;

import lombok.Getter;
import lombok.Setter;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/6/25
 */

@Getter
@Setter
public class Mail {

  //  @Pattern(regexp = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String to;

    //@NotBlank(message = "标题不能为空")
    private String title;

    //@NotBlank(message = "正文不能为空")
    private String content;

    private String msgId;// 消息id
}
