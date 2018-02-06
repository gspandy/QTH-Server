package com.nowbook.trade.dto;

import lombok.*;

import java.io.Serializable;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-07-01 9:39 AM  <br>
 * Author:cheng
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderDescription implements Serializable {

    private static final long serialVersionUID = -2341204723214819469L;

    @Getter
    @Setter
    private String title = "艾麦麦订单";           // 订单标题

    @Getter
    @Setter
    private String content = "艾麦麦商品信息";      // 订单内容（商品描述等)

}
