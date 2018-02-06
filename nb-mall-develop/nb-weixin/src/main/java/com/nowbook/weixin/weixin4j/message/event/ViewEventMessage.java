package com.nowbook.weixin.weixin4j.message.event;

import com.nowbook.weixin.weixin4j.message.EventType;

/**
 * 自定义菜单事件
 *
 * 点击菜单跳转链接时的事件推送
 *
 * @author qsyang
 * @version 1.0
 */
public class ViewEventMessage extends EventMessage {

    //事件KEY值，设置的跳转URL
    private String EventKey;

    @Override
    public String getEvent() {
        return EventType.View.toString();
    }

    public String getEventKey() {
        return EventKey;
    }

    public void setEventKey(String EventKey) {
        this.EventKey = EventKey;
    }

}
