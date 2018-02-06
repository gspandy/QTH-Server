package com.nowbook.weixin.weixin4j.pay;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信内网页支付
 *
 * @author qsyang
 * @version 1.0
 */
public class WCPay {

    private final String partnerId;
    private final String appId;             //公众号Id
    private final String timeStamp;         //时间戳
    private final String nonceStr;          //随机字符串
    private final String packages;          //订单详情扩展字符串 统一下单接口返回的prepay_id参数值，提交格式如：prepay_id=***
    private final String signType = "MD5";  //签名方式 签名算法，暂支持MD5
    private final String paySign;           //签名

    /**
     * getBrandWCPayRequest
     *
     * @param appId 公众号Id
     * @param prepay_id 预下单Id
     * @param paternerKey 商户密钥
     */
    public WCPay(String appId, String prepay_id, String paternerKey,String partnerId) {
        this.appId = appId;
        this.timeStamp = System.currentTimeMillis() / 1000 + "";
        this.nonceStr = java.util.UUID.randomUUID().toString().substring(0, 15);
        this.packages = prepay_id;
        this.partnerId =partnerId;

        //对提交的参数进行签名
        Map<String, String> paySignMap = new HashMap<String, String>();
        paySignMap.put("appid", this.appId);
        paySignMap.put("partnerid",this.partnerId);
        paySignMap.put("prepayid", this.packages);
        paySignMap.put("noncestr", this.nonceStr);
        paySignMap.put("timestamp", this.timeStamp);
        paySignMap.put("package", "Sign=WXPay");
        //签名
        this.paySign = SignUtil.getSign(paySignMap, paternerKey);
    }

    public String getAppId() {
        return appId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public String getPackage() {
        return packages;
    }

    public String getSignType() {
        return signType;
    }

    public String getPaySign() {
        return paySign;
    }

    public String getPartnerId(){return partnerId;}
}
