package com.nowbook.user.enums;

import lombok.Getter;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-07-01 3:00 PM  <br>
 * Author:cheng
 */
public enum Business {

    GZMH (1L, "个妆美护", "个妆美护"),
    YYBJ(2L, "营养保健", "营养保健"),
    CSJP (3L, "茶酒食品", "茶酒食品"),
    JJBH (4L, "家居百货", "家居百货"),
    NFSX (5L, "农副生鲜", "农副生鲜"),
    NYWJ (6L, "女婴玩具", "女婴玩具"),
    SMJD (7L, "数码家电", "数码家电"),
    JPXB (8L, "精品箱包", "精品箱包"),
    HWYD (9L, "户外运动", "户外运动"),
    CLFS (10L, "潮流服饰", "潮流服饰");


    private final long value;

    private final String description;

    @Getter
    private final String mall;

    private Business(long value, String description, String mall) {
        this.value = value;
        this.description = description;
        this.mall = mall;
    }

    public static Business from(int value) {
        for (Business b : Business.values()) {
            if (b.value == value) {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return description;
    }

    public Long value() {
        return value;
    }

}
