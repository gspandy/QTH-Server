package com.nowbook.sdp.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by winter on 16/5/11.
 */
public class DistributionInfo {

    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private Long userId;

    @Setter
    @Getter
    private String shopName;

    @Setter
    @Getter
    private String mobile;

    @Setter
    @Getter
    private String qqNumber;

    @Setter
    @Getter
    private Date openShopTime;

    @Setter
    @Getter
    private String openStatus;

    @Setter
    @Getter
    private Long parentIds;

    @Setter
    @Getter
    private String categories;

    @Setter
    @Getter
    private String step;


    @Setter
    @Getter
    private String userRealName;

}


