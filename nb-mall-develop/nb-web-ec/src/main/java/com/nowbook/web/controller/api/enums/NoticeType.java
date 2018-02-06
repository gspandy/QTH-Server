package com.nowbook.web.controller.api.enums;

import com.fasterxml.jackson.annotation.JsonValue;



/**
 * @author dpzh
 * @create 2017-08-03 11:35
 * @description: 通知类型枚举类
 **/
public enum NoticeType {

    ORDER_SHIPPED("2","订单已发货"),
    ORDER_APPLY_REFUND("6","退款申请已审核通过"),
    ORDER_RETURNED_PURCHASE("7","退货申请已审核通过"),
    ORDER_APPLY_REFUND_NOT_PASS("4","退款申请审核未通过"),
    ORDER_RETURNED_PURCHASE_NOT_PASS("5","退货申请审核未通过"),
    ASSETS_FLUCTUATION("11","资金变动消息");

    private String noticeType;
    private String message;

    private NoticeType(String noticeType,String message){
        this.noticeType=noticeType;
        this.message=message;
    }

    @JsonValue
    public String getNoticeType(){
        return noticeType;
    }

    public String getMessage() {
        return message;
    }




}
