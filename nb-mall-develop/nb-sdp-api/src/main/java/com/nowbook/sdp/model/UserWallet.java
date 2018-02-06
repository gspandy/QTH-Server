package com.nowbook.sdp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UserWallet extends PageModel{

    private static final long serialVersionUID = 6404288790255635091L;

    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private Long userId;//用户ID

    @Setter
    @Getter
    private Long balance;//余额

    @Setter
    @Getter
    private Long advance;//预存款

    @Setter
    @Getter
    private Long deposit;//保证金

    @Setter
    @Getter
    private Long pendingEarnings;//待发放收益

    @Setter
    @Getter
    private Long pendingBonuses;//待发放奖金

    @Setter
    @Getter
    private Long unpaidDeliverFee;//待支付邮费

    @Setter
    @Getter
    private Long totalEarnings;//已结算收益

    @Setter
    @Getter
    private Long totalBonuses;//已结算奖金

    @Setter
    @Getter
    private Long totalDeliverFee;//已支付邮费
}