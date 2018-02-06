package com.nowbook.weixin.weixin4j.message.event;

import com.nowbook.weixin.weixin4j.message.EventType;

/**
 * 取消关注事件
 *
 * @author qsyang
 * @version 1.0
 */
public class UnSubscribeEventMessage extends EventMessage {

    @Override
    public String getEvent() {
        return EventType.Unsubscribe.toString();
    }

}
