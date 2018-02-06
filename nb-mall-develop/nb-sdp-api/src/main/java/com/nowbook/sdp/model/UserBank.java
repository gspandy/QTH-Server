package com.nowbook.sdp.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

public class UserBank extends PageModel{
    private static final long serialVersionUID = 6404288790255635091L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private Integer type;//绑定类型1：支付宝 2：微信 3：银行

    @Getter
    @Setter
    private String openId;//支付宝或者微信的id

    @Getter
    @Setter
    private String bank;//银行

    @Getter
    @Setter
    private Integer bankType;//银行卡类型 1：储蓄 2：信用

    @Getter
    @Setter
    private String bankUser;//绑定银行卡的姓名

    @Getter
    @Setter
    private String bankCardNo;//银行卡号

    @Getter
    @Setter
    private String bankCardUserNo;//银行卡对应的用户身份证

    @Getter
    @Setter
    private String bankMobile;//银行卡对应的手机号
}
