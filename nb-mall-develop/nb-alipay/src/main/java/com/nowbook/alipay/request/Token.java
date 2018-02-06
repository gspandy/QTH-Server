package com.nowbook.alipay.request;

import lombok.*;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-05-04 9:36 AM  <br>
 * Author:cheng
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    @Getter
    @Setter
    private String pid;         // 支付宝接口的pid

    @Getter
    @Setter
    private String key;         // 支付宝接口的密钥

    @Getter
    @Setter
    private String account;     // 收款账户

    @Getter
    @Setter
    private String gateway;     // 请求网关

}
