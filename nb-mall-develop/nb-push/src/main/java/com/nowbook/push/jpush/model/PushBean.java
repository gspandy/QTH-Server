package com.nowbook.push.jpush.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Administrator on 2017/7/14.
 */

@ToString
public class PushBean {

    private static final long serialVersionUID = -7161289916347440186L;

    @Getter
    @Setter
    private Long noticeId;

    @Getter
    @Setter
    private String content;

    @Getter
    @Setter
    private String deviceId;

    @Getter
    @Setter
    private Integer deviceType;

    @Getter
    @Setter
    private Integer type;

    @Getter
    @Setter
    private Long businessId;  //业务ID

    @Getter
    @Setter
    private Integer subType;

    @Getter
    @Setter
    private String itemImage;

    @Getter
    @Setter
    private Integer itemSize;


}
