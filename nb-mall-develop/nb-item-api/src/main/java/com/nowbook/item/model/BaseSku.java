package com.nowbook.item.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2014-01-17
 */
public class BaseSku implements Serializable {
    private static final long serialVersionUID = -9161545387239480139L;


    @Getter
    @Setter
    protected String outerId; //对应外部的id

    @Getter
    @Setter
    protected String model; //型号

    @Getter
    @Setter
    protected Integer price;

    @Getter
    @Setter
    private Integer customPrice;

    @Getter
    @Setter
    private Integer quasiAngelPrice;     //准天使价格

    @Getter
    @Setter
    private Integer angelPrice;         //天使价格

    @Getter
    @Setter
    private Integer goldPrice;          //金卡价格

    @Getter
    @Setter
    private Integer platinumPrice;      //铂金价格

    @Getter
    @Setter
    private Integer blackPrice;         //黑卡价格


    @Getter
    @Setter
    private Integer partnerPrice;      //合伙人价格

    @Getter
    @Setter
    private Integer comPrice;           //公司价格

    @Getter
    @Setter
    private Integer purchasePrice;  //进价


    @Getter
    @Setter
    private Integer sellingPrice;  //卖价

    @Getter
    @Setter
    private Integer priceType;  //自营优选一口价类型   1：自营公式  2：自营自定义  3：优选公式  4:优选自定义

    @Getter
    @Setter
    private Integer angelRecommendPrice; //天使推荐价格

    @Getter
    @Setter
    protected String attributeKey1;

    @Getter
    @Setter
    protected String attributeName1;

    @Getter
    @Setter
    protected String attributeValue1;

    @Getter
    @Setter
    protected String attributeKey2;

    @Getter
    @Setter
    protected String attributeName2;

    @Getter
    @Setter
    protected String attributeValue2;

}
