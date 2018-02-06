package com.nowbook.user.model;

import lombok.*;

import java.io.Serializable;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-06-10 9:52 PM  <br>
 * Author:cheng
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class LoginInfo implements Serializable {
    private static final long serialVersionUID = 1711778846285213600L;
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private Integer type;

    @Getter
    @Setter
    private String pushDeviceId;

    @Getter
    @Setter
    private String deviceId;

    @Getter
    @Setter
    private Integer deviceType;

    @Getter
    @Setter
    private String deviceBrand;

    @Getter
    @Setter
    private String token;

    //支付时比较IP

}
