package com.example.demo.pojo;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.example.demo.common.Constant;
import com.example.demo.mq.MessageHelper;
import lombok.*;

import java.util.Date;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/6/25
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MsgLog {
    private String msgId;
    private String msg;
    private String exchange;
    private String routingKey;
    private Integer status;
    private Integer tryCount;
    private Date nextTryTime;
    private Date createTime;
    private Date updateTime;

    public MsgLog(String msgId, Object msg, String exchange, String routingKey) {
        this.msgId = msgId;
        this.msg =  JSONUtil.toJsonStr(msg);
        this.exchange = exchange;
        this.routingKey = routingKey;

        this.status = Constant.MsgLogStatus.DELIVERING;
        this.tryCount = 0;

        Date date = new Date();
        this.createTime = date;
        this.updateTime = date;
        this.nextTryTime = DateUtil.offsetMinute(date,1);
    }
}
