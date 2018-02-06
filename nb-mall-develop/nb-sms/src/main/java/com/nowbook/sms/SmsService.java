package com.nowbook.sms;

import com.nowbook.common.model.Response;
import com.nowbook.sms.exception.SmsException;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-05-23
 */
public interface SmsService {
    /**
     * 发送单条信息
     *
     * @param from    发送方
     * @param to      接收方手机号码
     * @param message 消息体
     * @throws SmsException 异常
     */
    Response<Boolean> sendSingle(String from, String to, String message);
    /**
     * 发送单条信息
     *
     * @param from    发送方
     * @param to      接收方手机号码
     * @param message 消息体
     * @throws SmsException 异常
     */
    Response<Boolean> sendSingle(String from, String to,String template, String message);
    /**
     * 群发信息
     *
     * @param from    接收方
     * @param to      接受方手机号码列表
     * @param message 消息体
     * @throws SmsException 异常
     */
    Response<Boolean> sendGroup(String from, Iterable<String> to, String message);


    /**
     * 查询剩余短信条数
     *
     * @return 剩余短信条数
     */
    Integer available();

}
