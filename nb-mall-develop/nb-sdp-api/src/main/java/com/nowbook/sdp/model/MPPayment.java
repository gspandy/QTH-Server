package com.nowbook.sdp.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业付款
 * 
 * @className MPPayment
 * @author jy
 * @date 2015年4月1日
 * @since JDK 1.7
 * @see
 */
public class MPPayment {
	
	private static final long serialVersionUID = 3734639674346425312L;
	
	private String mchAppid;
	private String mchid;
	private String nonceStr;
	private String sign;
	private String partnerTradeNo;
	private String openId;
	private String checkName;
	private String amount;
	private String desc;
	private String spbillCreateIp;
	
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("amount", amount);
        map.put("check_name", checkName);
        map.put("desc", desc);        
        map.put("mch_appid", mchAppid);
        map.put("mchid", mchid);
        map.put("nonce_str", nonceStr);
        map.put("openid", openId);        
        map.put("partner_trade_no", partnerTradeNo);
        map.put("sign", sign);
        map.put("spbill_create_ip", spbillCreateIp);
        return map;
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        sb.append("<amount><![CDATA[").append(amount).append("]]></amount>");
        sb.append("<check_name><![CDATA[").append(checkName).append("]]></check_name>");        
        sb.append("<desc><![CDATA[").append(desc).append("]]></desc>");        
        sb.append("<mch_appid><![CDATA[").append(mchAppid).append("]]></mch_appid>");
        sb.append("<mchid><![CDATA[").append(mchid).append("]]></mchid>");
        sb.append("<nonce_str><![CDATA[").append(nonceStr).append("]]></nonce_str>");
        sb.append("<openid><![CDATA[").append(openId).append("]]></openid>");
        sb.append("<partner_trade_no><![CDATA[").append(partnerTradeNo).append("]]></partner_trade_no>");
        sb.append("<sign><![CDATA[").append(sign).append("]]></sign>");
        sb.append("<spbill_create_ip><![CDATA[").append(spbillCreateIp).append("]]></spbill_create_ip>");
        sb.append("</xml>");
        return sb.toString();
    }
	
	
	public String getMchAppid() {
		return mchAppid;
	}
	public void setMchAppid(String mchAppid) {
		this.mchAppid = mchAppid;
	}
	public String getMchid() {
		return mchid;
	}
	public void setMchid(String mchid) {
		this.mchid = mchid;
	}
	public String getNonceStr() {
		return nonceStr;
	}
	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getPartnerTradeNo() {
		return partnerTradeNo;
	}
	public void setPartnerTradeNo(String partnerTradeNo) {
		this.partnerTradeNo = partnerTradeNo;
	}
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	public String getCheckName() {
		return checkName;
	}
	public void setCheckName(String checkName) {
		this.checkName = checkName;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getSpbillCreateIp() {
		return spbillCreateIp;
	}
	public void setSpbillCreateIp(String spbillCreateIp) {
		this.spbillCreateIp = spbillCreateIp;
	}
}
