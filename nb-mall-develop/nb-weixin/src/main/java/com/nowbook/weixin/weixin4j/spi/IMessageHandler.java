package com.nowbook.weixin.weixin4j.spi;

import com.nowbook.weixin.weixin4j.WeixinException;

import javax.servlet.ServletInputStream;

/**
 * 输入消息处理器
 *
 * @author qsyang
 * @version 1.0
 */
public interface IMessageHandler {

    /**
     * 微信输入消息处理器
     *
     * @param inputStream 输入流
     * @return 返回xml格式的回复消息
     * @throws WeixinException
     */
    String invoke(ServletInputStream inputStream) throws WeixinException;

    /**
     * 微信输入消息处理器
     *
     * @param inputXml 输入xml
     * @return 返回xml格式的回复消息
     * @throws WeixinException
     */
    String invoke(String inputXml) throws WeixinException;
}
