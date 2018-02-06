package com.nowbook.weixin.weixin4j.pay;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by jinlin on 2016/6/22.
 */
@XmlRootElement(name = "xml")
public class WCRefundResult {

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
    private String appid;           //公众账号ID
    private String mch_id;          //商户号
    private String device_info;     //设备号
    private String nonce_str;       //随机字符串
    private String sign;            //签名
    private String transaction_id;
    private String out_trade_no;
    private String out_refund_no;
    private String refund_id;
    private String refund_channel;
    private String refund_fee;
    private String settlement_refund_fee_$n;
    private String total_fee;
    private String settlement_total_fee;
    private String fee_type;
    private String cash_fee;
    private String cash_refund_fee;
    private String coupon_type_$n;
    private String coupon_refund_fee_$n;
    private String coupon_refund_id_$n_$m;
    private String coupon_refund_fee_$n_$m;
    private String result_code;     //业务结果  SUCCESS/FAIL
    private String err_code;        //错误代码
    private String err_code_des;    //错误代码描述

    @XmlElement(name = "out_trade_no")
    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getCoupon_refund_fee_$n_$m() {
        return coupon_refund_fee_$n_$m;
    }
    @XmlElement(name = "coupon_refund_fee_$n_$m")
    public void setCoupon_refund_fee_$n_$m(String coupon_refund_fee_$n_$m) {
        this.coupon_refund_fee_$n_$m = coupon_refund_fee_$n_$m;
    }

    public String getCoupon_refund_id_$n_$m() {
        return coupon_refund_id_$n_$m;
    }
    @XmlElement(name = "coupon_refund_id_$n_$m")
    public void setCoupon_refund_id_$n_$m(String coupon_refund_id_$n_$m) {
        this.coupon_refund_id_$n_$m = coupon_refund_id_$n_$m;
    }

    public String getCoupon_refund_fee_$n() {
        return coupon_refund_fee_$n;
    }
    @XmlElement(name = "coupon_refund_fee_$n")
    public void setCoupon_refund_fee_$n(String coupon_refund_fee_$n) {
        this.coupon_refund_fee_$n = coupon_refund_fee_$n;
    }

    public String getCoupon_type_$n() {
        return coupon_type_$n;
    }
    @XmlElement(name = "coupon_type_$n")
    public void setCoupon_type_$n(String coupon_type_$n) {
        this.coupon_type_$n = coupon_type_$n;
    }

    public String getCash_refund_fee() {
        return cash_refund_fee;
    }
    @XmlElement(name = "cash_refund_fee")
    public void setCash_refund_fee(String cash_refund_fee) {
        this.cash_refund_fee = cash_refund_fee;
    }

    public String getCash_fee() {
        return cash_fee;
    }
    @XmlElement(name = "cash_fee")
    public void setCash_fee(String cash_fee) {
        this.cash_fee = cash_fee;
    }

    public String getFee_type() {
        return fee_type;
    }
    @XmlElement(name = "fee_type")
    public void setFee_type(String fee_type) {
        this.fee_type = fee_type;
    }

    public String getSettlement_total_fee() {
        return settlement_total_fee;
    }
    @XmlElement(name = "settlement_total_fee")
    public void setSettlement_total_fee(String settlement_total_fee) {
        this.settlement_total_fee = settlement_total_fee;
    }

    public String getTotal_fee() {
        return total_fee;
    }
    @XmlElement(name = "total_fee")
    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }

    public String getSettlement_refund_fee_$n() {
        return settlement_refund_fee_$n;
    }
    @XmlElement(name = "settlement_refund_fee_$n")
    public void setSettlement_refund_fee_$n(String settlement_refund_fee_$n) {
        this.settlement_refund_fee_$n = settlement_refund_fee_$n;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public String getOut_refund_no() {
        return out_refund_no;
    }
    @XmlElement(name = "out_refund_no")
    public void setOut_refund_no(String out_refund_no) {
        this.out_refund_no = out_refund_no;
    }

    public String getRefund_id() {
        return refund_id;
    }
    @XmlElement(name = "refund_id")
    public void setRefund_id(String refund_id) {
        this.refund_id = refund_id;
    }

    public String getRefund_channel() {
        return refund_channel;
    }
    @XmlElement(name = "refund_channel")
    public void setRefund_channel(String refund_channel) {
        this.refund_channel = refund_channel;
    }

    public String getRefund_fee() {
        return refund_fee;
    }
    @XmlElement(name = "refund_fee")
    public void setRefund_fee(String refund_fee) {
        this.refund_fee = refund_fee;
    }


    public String getTransaction_id() {
        return transaction_id;
    }
    @XmlElement(name = "transaction_id")
    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    /**
     * 通信是否成功
     *
     * @return 成功返回True，否则返回false
     */
    public boolean isSuccess() {
        if (result_code == null || result_code.equals("")) {
            return false;
        }
        return result_code.toUpperCase().equals("SUCCESS");
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

    public String getAppid() {
        return appid;
    }

    @XmlElement(name = "appid")
    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getMch_id() {
        return mch_id;
    }

    @XmlElement(name = "mch_id")
    public void setMch_id(String mch_id) {
        this.mch_id = mch_id;
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

}
