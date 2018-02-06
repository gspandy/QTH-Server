package com.nowbook.restful.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/10.
 */
public class SimpleOrderInfo implements Serializable {
    private static final long serialVersionUID = -1893738500647474932L;


    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private Integer total;

    @Setter
    @Getter
    private Integer express;

    @Setter
    @Getter
    private Integer type;//支付方式 1：支付宝 2：微信
}
