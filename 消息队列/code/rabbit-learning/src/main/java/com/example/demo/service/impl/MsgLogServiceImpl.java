package com.example.demo.service.impl;

import cn.hutool.core.date.DateUtil;
import com.example.demo.mapper.MsgLogMapper;
import com.example.demo.pojo.MsgLog;
import com.example.demo.service.MsgLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/6/25
 */

@Service
public class MsgLogServiceImpl implements MsgLogService {
    @Autowired
    private MsgLogMapper msgLogMapper;

    @Override
    public void updateStatus(String msgId, Integer status) {
        MsgLog msgLog = new MsgLog();
        msgLog.setMsgId(msgId);
        msgLog.setStatus(status);
        msgLog.setUpdateTime(new Date());
        msgLogMapper.updateStatus(msgLog);
    }

    @Override
    public MsgLog selectByMsgId(String msgId) {
        return msgLogMapper.selectByPrimaryKey(msgId);
    }

    @Override
    public List<MsgLog> selectTimeoutMsg() {
        return msgLogMapper.selectTimeoutMsg();
    }

    @Override
    public void updateTryCount(String msgId, Date tryTime) {
        Date nextTryTime = DateUtil.offsetMinute(tryTime, 1);

        MsgLog msgLog = new MsgLog();
        msgLog.setMsgId(msgId);
        msgLog.setNextTryTime(nextTryTime);
        msgLogMapper.updateTryCount(msgLog);

    }
}
