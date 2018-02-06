package com.nowbook.sdp.model;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class AmountDetailForQuery {
    private Long id;

    private String shopName;//操作日期

    private Double getAmount;//获得佣金金额
   // @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
//@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date createDate;//


    private String isComplete;//是否完成分成 0 未分成 1已分成
    private Long orderId;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public Double getGetAmount() {
        return getAmount;
    }

    public void setGetAmount(Double getAmount) {
        this.getAmount = getAmount;
    }

    //@JsonSerialize(using=JsonDateSerializer.class)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(String isComplete) {
        this.isComplete = isComplete == null ? null : isComplete.trim();
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}