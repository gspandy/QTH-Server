package com.nowbook.alipay.request;

import org.junit.Test;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-03-25 12:11 PM  <br>
 * Author:cheng
 */
public class AlipayPayRequestTest {

    @Test
    public void testPayRequestOk() {
        Token token = new Token("todo", "todo", "todo", "https://mapi.alipay.com/gateway.do");
        CallBack notify = new CallBack("http://beta.nowbook.com/api/alipay/notify");
        CallBack forward = new CallBack("http://beta.nowbook.com/buyer/trade-success");

        notify.append("zzz", "zzz");
        notify.append("aaa", "aaa");
        forward.append("bbb", "bbb");


        String url = PayRequest.build(token).title("酱油").outerTradeNo("XSSSS001102").total(1)
                .notify(notify).forward(forward).pay();
        System.out.print(url);
    }

}
