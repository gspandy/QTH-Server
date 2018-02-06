package com.nowbook.third.common;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
* Created by zhaop01 on 2014/9/2.
*/
public class CommonUtils {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");


    // 获取XML字符串中的节点内容
    public static List<?> getXmlValue(String text,String pathExpress) throws DocumentException {
        Document doc = DocumentHelper.parseText(text);
        Element e = doc.getRootElement();
        List<?> nodeList = e.selectNodes(pathExpress);
        return nodeList;
    }
    public static String getTime(){
       return  sdf.format(new Date());
    }

    /**
     * 锦霖接口系统参数
     * @param env
     * @return
     */
    public static ApiParams getNbApiParams(String env){
        ApiParams params = new ApiParams();
        if("test".equals(env)){ // 测试环境
            params.setSource("nb");
            params.setCoAppName("nb");
            params.setSecretKey("1234");
            params.setUrl("http://test.nb.com/ids/service?idsServiceType=httpssoservice&serviceName=findUserBySSOID");
            params.setSecurityType("itzTyAusn6b4");
        }else{
            params.setSource("nb");
            params.setCoAppName("nb");
            params.setSecretKey("1234");
            params.setUrl("http://www.nb.com/ids/service?idsServiceType=httpssoservice&serviceName=findUserBySSOID");
            params.setSecurityType("bMy?gQGhjJrj");
        }
        return params;
    }

    /**
     * E锦霖接口系统参数
     * @param env
     * @return
     */
    public static ApiParams getEnbApiParams(String env){
        ApiParams params = new ApiParams();
        if("test".equals(env)){ // 测试环境
            params.setSource("enb");
            params.setCoAppName("nb");
            params.setSecretKey("123456");
            params.setUrl("http://www.testenb.com/api/sso.php");
        }else{
            params.setSource("enb");
            params.setCoAppName("nb");
            params.setSecretKey("enb&2ab*(_");
            params.setUrl("http://www.enb.com/api/sso.php");
        }
        return params;
    }
}
