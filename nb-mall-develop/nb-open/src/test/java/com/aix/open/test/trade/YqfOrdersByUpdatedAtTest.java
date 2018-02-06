package com.aix.open.test.trade;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.Calendar;
import java.util.Map;

/**
 * Created by neusoft on 14-8-7.
 */
public class YqfOrdersByUpdatedAtTest {
    public static void main(String... args) {

        Calendar c1 = Calendar.getInstance();
        c1.set(2014, 9-1, 15, 14, 0, 0);
        Calendar c2 = Calendar.getInstance();
        c2.set(2014, 9-1, 15, 16, 0, 0);
        String channel = "shop_enb_1";
        String cid ="1234";
        String updateStartTime = c1.getTimeInMillis()/1000+"";
        String updateEndTime = c2.getTimeInMillis()/1000+"";
        String key = "fb5eef4ccab3ec3831f8de0c2e44226e";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("cid", cid);
        map.put("updateStartTime", updateStartTime);
        map.put("updateEndTime", updateEndTime);
        String sign = Sign.buildParams(map, key);
        //String response = HttpRequest
        //        .post("http://console.aimymy.com/api/shop/info").send("channel="+channel+"&shopId="+shopId+"&outerCode="+outerCode+"&sign=" + key).body();
        String response = HttpRequest
               .post("http://console.aimymy.com/api/open/trade/yqf/orders/byUpdatedAt", true,
                       "channel", channel,
                       "cid", cid,
                       "updateStartTime", updateStartTime,
                       "updateEndTime", updateEndTime,
                       "sign", sign).body();
        //String response = HttpRequest
        //        .get("http://console.aimymy.com/api/shop/info", true, "channel", channel, "shopId", shopId, "outerCode", outerCode, "sign", sign).body();
        System.out.println(response);
    }
}
