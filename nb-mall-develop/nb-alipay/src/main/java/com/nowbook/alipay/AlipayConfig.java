package com.nowbook.alipay;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-05-30
 */
public class AlipayConfig {
    /**
     * alipay接口的pid和key*
     */
    public static final String ALIPAY_PID = "2088121659081944";

    /**
     * 密钥
     */
    public static final String ALIPAY_KEY = "f6jx8rzebd64ydk3slox6d6i8zcothxg";

    /**
     * 支付宝提供给商户的服务接入网关URL(新)
     */
    public static final String ALIPAY_GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?";
    //public static final String ALIPAY_GATEWAY_NEW = "http://www.nowbook.cn/demo/pay?";
    public static final String ALIPAY_GATEWAY_REFUND = "http://www.nowbook.cn/api/demo/pay/refund?";


    /**
     * 卖家收款账号*
     */
    public static final String SELLER_ACCOUNT = "robin@aimymy.com";

    /**
     * 支付宝前台通知url
     */
    public static final String RETURN_URL = "http://www.nowbook.cn/buyer/trade-success";

    /**
     * 支付宝后台通知url
     */
    public static final String NOTIFY_URL = "http://www.nowbook.cn/api/alipay/notify?";

    /**
     * 支付宝退款后台通知
     */
    public static final String REFUND_NOTIFY_URL = "http://www.nowbook.cn/api/alipay/refund/notify?";

    /**
     * 转账 与 在线支付
     */
    public static final String TRANS_CODE = "3011,6001";

}
