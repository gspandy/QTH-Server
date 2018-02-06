package com.nowbook.sdp.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class Level extends PageModel{
    private static final long serialVersionUID = 6404288790255635091L;

    public static enum LEVEL {
        NONE(-1, "无等级"),
        READYANGEL(0, "准天使"),
        ANGEL(1, "天使"),
        GOLD(2, "金卡"),
        PLATINUM(3, "白金卡"),
        BLACK(4, "黑卡"),
        PARTNER(5, "合伙人"),
        TOPBOSS(6, "公司");

        private final int value;

        private final String display;

        private LEVEL(int number, String display) {
            this.value = number;
            this.display = display;
        }

        public static LEVEL fromNumber(int number) {
            for (LEVEL level : LEVEL.values()) {
                if (Objects.equal(level.value, number)) {
                    return level;
                }
            }
            return null;
        }

        public int toNumber() {
            return value;
        }


        @Override
        public String toString() {
            return display;
        }
    }

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Integer level;//等级

    @Getter
    @Setter
    private String info;//等级名称

    @Getter
    @Setter
    private Integer discount;//折扣

    @Getter
    @Setter
    private Integer needNum;//升级时推荐同级或同级以上的人数

    @Getter
    @Setter
    private Long balance;//余额

    @Getter
    @Setter
    private Long advance;//预存款

    @Getter
    @Setter
    private Long deposit;//保证金

    @Getter
    @Setter
    private Integer bonusDiscount;//推荐奖折扣

    @Getter
    @Setter
    private Integer talentDiscount;//伯乐奖折扣

    @Getter
    @Setter
    private Long rechargeFactor;//充值倍数

    @Getter
    @Setter
    private Long needMoney;//需要升级的钱数
}
