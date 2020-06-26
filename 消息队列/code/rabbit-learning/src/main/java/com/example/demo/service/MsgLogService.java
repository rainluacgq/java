package com.example.demo.service;

import com.example.demo.pojo.MsgLog;

import java.util.Date;
import java.util.List;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/6/25
 */
public interface MsgLogService {
    void updateStatus(String msgId, Integer status);

    MsgLog selectByMsgId(String msgId);

    List<MsgLog> selectTimeoutMsg();

    void updateTryCount(String msgId, Date tryTime);
}
