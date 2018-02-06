package com.nowbook.web.controller.api.userEvent;

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
    protected String type;

    public PushEvent(long orderId, long orderItemId,String type) {
        this.orderId = orderId;
        this.orderItemId=orderItemId;
        this.type = type;
    }



}
