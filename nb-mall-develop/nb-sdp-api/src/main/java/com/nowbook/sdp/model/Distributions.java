package com.nowbook.sdp.model;

import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Date;

/**
 * Created by Administrator on 2016/3/25 0025.
 */
@ToString
@EqualsAndHashCode
public class Distributions implements Serializable {

    private static final long serialVersionUID = -3973304591309605612L;

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
    private Date openShopTime;

    @Setter
    @Getter
    private String openStatus;

    @Setter
    @Getter
    private Long parentId;

    @Setter
    @Getter
    private DistributorsAudit distributorsAudits;

    @Setter
    @Getter
    private DistributorsUser distributorsUsers;



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
    private String parentIds;

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

}
