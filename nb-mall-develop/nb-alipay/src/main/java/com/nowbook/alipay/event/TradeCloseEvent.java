package com.nowbook.alipay.event;

import com.nowbook.alipay.request.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-08-07 4:30 PM  <br>
 * Author:cheng
 */
@ToString
@AllArgsConstructor
public class TradeCloseEvent {

    @Getter
    @Setter
    private Token token;            // 令牌

    @Getter
    @Setter
    private String outerOrderNo;    // 外部商户号

}
