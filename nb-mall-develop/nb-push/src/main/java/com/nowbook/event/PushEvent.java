package com.nowbook.event;

import lombok.Data;

/**
 * @author dpzh
 * @create 2017-07-27 14:31
 * @description: 推送
 **/
@Data
public class PushEvent {
    protected long orderId;
    protected long orderItemId;
    protected String content;
    protected String type;
    protected Integer subType;
    protected long businessId;
    protected long userId;

    public PushEvent(long orderId, long orderItemId, String type) {
        this.orderId = orderId;
        this.orderItemId=orderItemId;
        this.type = type;
    }

    public PushEvent(long businessId, String content, Integer subType, long userId, String type ) {
        this.businessId = businessId;
        this.type = type;
        this.subType = subType;
        this.userId = userId;
        this.content=content;
    }



}
