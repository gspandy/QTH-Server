package com.nowbook.restful.controller.testPostOrder.bin.demo.src.demo;

import java.util.HashMap;

import com.nowbook.restful.controller.testPostOrder.bin.demo.src.util.MD5;

public class Query {
	public static void main(String[] args) throws Exception {

		String param ="{\"com\":\"shentong\",\"num\":\"227297913364\"}";
		String customer ="28AD9CCB56ED8D902B22AB0D29746729";
		String key = "lynJmOJO368";
		String sign = MD5.encode(param + key + customer);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("param",param);
		params.put("sign",sign);
		params.put("customer",customer);
		String resp;
		System.out.println(sign);
		try {
			resp = new HttpRequest().postData("http://poll.kuaidi100.com/poll/query.do", params, "utf-8").toString();
			System.out.println(resp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
