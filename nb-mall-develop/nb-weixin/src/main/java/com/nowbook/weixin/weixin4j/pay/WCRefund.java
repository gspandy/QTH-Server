package com.nowbook.weixin.weixin4j.pay;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jinlin on 2016/6/22.
 */
public class WCRefund {
    private String appid;               //公众账号ID 是
    private String mch_id;              //商户号 是
    private String nonce_str;           //随机字符串 是
    private String sign;                //签名 是
    private String transaction_id;
    private String out_trade_no;
    private String out_refund_no;
    private String total_fee;
    private String refund_fee;
    private String op_user_id;


    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("appid", appid);
        map.put("mch_id", mch_id);
        map.put("nonce_str", nonce_str);
//        map.put("op_user_id", op_user_id);
        map.put("out_refund_no", out_refund_no);
        map.put("refund_fee", refund_fee);
        map.put("total_fee", total_fee);
        map.put("transaction_id", transaction_id);
        return map;
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        sb.append("<appid>").append(appid).append("</appid>");
        sb.append("<mch_id>").append(mch_id).append("</mch_id>");
        sb.append("<nonce_str>").append(nonce_str).append("</nonce_str>");
//        sb.append("<op_user_id><![CDATA[").append(op_user_id).append("]]></op_user_id>");
        sb.append("<out_refund_no>").append(out_refund_no).append("</out_refund_no>");
        sb.append("<refund_fee>").append(refund_fee).append("</refund_fee>");
        sb.append("<total_fee>").append(total_fee).append("</total_fee>");
        sb.append("<transaction_id>").append(transaction_id).append("</transaction_id>");
        sb.append("<sign>").append(sign).append("</sign>");
        sb.append("</xml>");
        return sb.toString();
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public void setMch_id(String mch_id) {
        this.mch_id = mch_id;
    }

    public void setNonce_str(String nonce_str) {
        this.nonce_str = nonce_str;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }

    public void setTransaction_id(String transaction_id) {this.transaction_id = transaction_id;}

    public void setOut_refund_no(String out_refund_no) {this.out_refund_no = out_refund_no;}

    public void setRefund_fee(String refund_fee) {this.refund_fee = refund_fee;}

    public void setOp_user_id(String op_user_id) {this.op_user_id = op_user_id;}
}
