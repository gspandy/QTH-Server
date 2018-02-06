package com.nowbook.sdp.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by winter on 16/5/11.
 */
public class DistributionInfoForQuery {

    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private String parentIds;

    @Setter
    @Getter
    private Long userId;

    @Setter
    @Getter
    private String shopName;

    @Setter
    @Getter
    private Date openShopTime;

    @Setter
    @Getter
    private String openStatus;

    @Setter
    @Getter
    private String realName;

    @Setter
    @Getter
    private String mobile;

    @Setter
    @Getter
    private String qq;

    @Setter
    @Getter
    private String categories;

    @Setter
    @Getter
    private String step;

    @Setter
    @Getter
    private String avatar;

    @Setter
    @Getter
    private String promotionQr;

    @Setter
    @Getter
    private String storeQr;
    @Setter
    @Getter
    private HashMap<String,String> map;


}


