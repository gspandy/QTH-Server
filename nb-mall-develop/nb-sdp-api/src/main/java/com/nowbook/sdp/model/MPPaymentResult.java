package com.nowbook.sdp.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 企业付款结果
 * 
 * @className MPPaymentResult
 * @author jy
 * @date 2015年4月1日
 * @since JDK 1.7
 * @see
 */
@XmlRootElement(name = "xml")
public class MPPaymentResult {

	private static final long serialVersionUID = 1110472826089211646L;

    /**
     * 字段名：返回状态码
     *
     * 必填：是
     *
     * 类型：String(16)
     *
     * 描述：SUCCESS/FAIL
     *
     * 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
     */
    private String return_code;
    /**
     * 字段名：返回信息
     *
     * 必填：否
     *
     * 类型：String(128)
     *
     * 描述：返回信息，如非空，为错误原因
     *
     * 签名失败、参数格式校验错误等
     */
    private String return_msg;
    //*** 以下字段在return_code为SUCCESS的时候有返回 ***//
    private String mchAppid;           //公众账号ID
    private String mchid;          //商户号
    private String device_info;     //设备号
    private String nonce_str;       //随机字符串
    private String sign;            //签名
    private String result_code;     //业务结果  SUCCESS/FAIL
    private String err_code;        //错误代码
    private String err_code_des;    //错误代码描述
    //*** 以下字段在return_code 和result_code都为SUCCESS的时候有返回 ***//
    private String transactionId;      //微信订单订单号
    private String outTradeNo;       //商户订单号
    private String paymentTime;        //支付时间
	
    /**
     * 通信是否成功
     *
     * @return 成功返回True，否则返回false
     */
    public boolean isSuccess() {
        if (return_code == null || return_code.equals("")) {
            return false;
        }
        return return_code.toUpperCase().equals("SUCCESS");
    }

	public String getReturn_code() {
        return return_code;
    }

    @XmlElement(name = "return_code")
    public void setReturn_code(String return_code) {
        this.return_code = return_code;
    }

    public String getReturn_msg() {
        return return_msg;
    }

    @XmlElement(name = "return_msg")
    public void setReturn_msg(String return_msg) {
        this.return_msg = return_msg;
    }

	public String getMchAppid() {
		return mchAppid;
	}
    @XmlElement(name = "mch_appid")
	public void setMchAppid(String mchAppid) {
		this.mchAppid = mchAppid;
	}
	public String getMchid() {
		return mchid;
	}
    @XmlElement(name = "mchid")	
	public void setMchid(String mchid) {
		this.mchid = mchid;
	}
    public String getDevice_info() {
        return device_info;
    }

    @XmlElement(name = "device_info")
    public void setDevice_info(String device_info) {
        this.device_info = device_info;
    }

    public String getNonce_str() {
        return nonce_str;
    }

    @XmlElement(name = "nonce_str")
    public void setNonce_str(String nonce_str) {
        this.nonce_str = nonce_str;
    }

    public String getSign() {
        return sign;
    }

    @XmlElement(name = "sign")
    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getResult_code() {
        return result_code;
    }

    @XmlElement(name = "result_code")
    public void setResult_code(String result_code) {
        this.result_code = result_code;
    }

    public String getErr_code() {
        return err_code;
    }

    @XmlElement(name = "err_code")
    public void setErr_code(String err_code) {
        this.err_code = err_code;
    }

    public String getErr_code_des() {
        return err_code_des;
    }

    @XmlElement(name = "err_code_des")
    public void setErr_code_des(String err_code_des) {
        this.err_code_des = err_code_des;
    }
    
    public String getTransactionId() {
		return transactionId;
	}
	@XmlElement(name = "payment_no")
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}
	@XmlElement(name = "partner_trade_no")
	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public String getPaymentTime() {
		return paymentTime;
	}
	@XmlElement(name = "payment_time")
	public void setPaymentTime(String paymentTime) {
		this.paymentTime = paymentTime;
	}
	

/*	*//**
	 * 微信订单订单号
	 *//*
	@JSONField(name = "payment_no")
	@XmlElement(name = "payment_no")
	private String transactionId;
	*//**
	 * 商户订单号
	 *//*
	@JSONField(name = "partner_trade_no")
	@XmlElement(name = "partner_trade_no")
	private String outTradeNo;
	*//**
	 * 支付时间
	 *//*
	@JSONField(name = "payment_time")
	@XmlElement(name = "payment_time")
	private String paymentTime;*/
}
