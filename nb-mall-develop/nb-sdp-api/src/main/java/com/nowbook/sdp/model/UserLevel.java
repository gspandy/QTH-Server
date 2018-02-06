package com.nowbook.sdp.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.util.Date;

public class UserLevel extends PageModel{
    private static final long serialVersionUID = 6404288790255635091L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private Integer level;//用户等级

    @Getter
    @Setter
    private Long inviter;//推荐人ID

    @Getter
    @Setter
    private Long blackInviter;//黑卡或黑卡以上的推荐人ID

    @Getter
    @Setter
    private Date levelUpAt;//升级时间

    @Getter
    @Setter
    private Date levelUpBlackAt;//升级到黑卡时间

    @Getter
    @Setter
    private Long money;//缴费

    @Getter
    @Setter
    private Long balance;//线下升级时的余额
    @Getter
    @Setter
    private Long advance;//线下升级时的预存款
    @Getter
    @Setter
    private Long deposit;//线下升级时的保证金
}
