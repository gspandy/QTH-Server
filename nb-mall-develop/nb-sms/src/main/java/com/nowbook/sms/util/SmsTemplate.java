package com.nowbook.sms.util;

import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * 阿里大鱼短信模版
 * Created by robin on 15/12/29.
 */
public class SmsTemplate {

    public static String message(Map<String, String> message){
        StringBuffer sb=new StringBuffer("{");
        for(String code : message.keySet()) {
            sb.append("\"");
            sb.append(code);
            sb.append("\"");
            sb.append(":");
            sb.append("\"");
            String content=message.get(code);
            sb.append("\"");
            sb.append(",");
        }
        sb=sb.delete(sb.length()-1,sb.length());
        sb.append("}");
        return  sb.toString();
    }
    public static void testSend(String msg){
        String taobaoClientUrl="http://gw.api.taobao.com/router/rest";
        String taobaoAppkey="23281779";
        String taobaoSecret="444a3a0762fa53dd64d96385d68cded0";
        String taobaoSmsFreeSignName="艾麦麦";
        String taobaoSmsTemplateCode="SMS_3140054";

        TaobaoClient client = new DefaultTaobaoClient(taobaoClientUrl, taobaoAppkey, taobaoSecret);
        AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
        //  req.setExtend("123456");
        req.setSmsType("normal");
        req.setSmsFreeSignName(taobaoSmsFreeSignName);
        //msg="测试";
       req.setSmsParamString(msg);
        //req.setSmsParamString(msg);
                req.setRecNum("18640936329");
        req.setSmsTemplateCode(taobaoSmsTemplateCode);

        //发送短信

        AlibabaAliqinFcSmsNumSendResponse rsp = null;
        try {
            rsp = client.execute(req);
            //接收应答请求

        } catch (Exception e) {

            // rsp.setError("sms.send.fail");
        }



    }
    public static void main(String[] args){
        Map map=new HashMap<String, String>();
        map.put("code", "test1");
        map.put("product","测试");
        String str="ces";
       String msg= "{\"code\":\""+str+"\",\"product\":\"alidayu\"}";
        testSend(msg);
    }

}
