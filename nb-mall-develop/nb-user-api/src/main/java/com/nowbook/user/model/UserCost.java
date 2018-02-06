package com.nowbook.user.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2016/7/18.
 */
@ToString
@EqualsAndHashCode
public class UserCost implements Serializable {


    @Setter
    @Getter
    private Long id;
    @Setter
    @Getter
    private int totalCost;
    @Setter
    @Getter
    private int efficientCost;
    @Setter
    @Getter
    private Date updateAt;

}
