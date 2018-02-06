package com.nowbook.sdp.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class UserTeamMemberSelect extends PageModel{
    private static final long serialVersionUID = 6404288790255635091L;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private Long parent;

    @Getter
    @Setter
    private String nick;

    @Getter
    @Setter
    private String realName;

    @Getter
    @Setter
    private String mobile;

    @Getter
    @Setter
    private String inviterMobile;

    @Getter
    @Setter
    private Integer memberNum;

    @Getter
    @Setter
    private Integer inviterNum;

    @Getter
    @Setter
    private Integer level;

    @Getter
    @Setter
    private Integer levels;

    @Getter
    @Setter
    private String avatar;

    @Getter
    @Setter
    private Long inviter;

    @Getter
    @Setter
    private String qrCodeUrl;

    @Getter
    @Setter
    private Date levelUpAt;

    @Getter
    @Setter
    private String levelUpTime;

    @Getter
    @Setter
    private String isHavePayPass;  //0：没有设置过支付密码 1：设置过支付密码

    @Getter
    @Setter
    private Integer type;

}
