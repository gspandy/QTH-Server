package com.nowbook.weixin.weixin4j;

import com.google.common.io.Resources;
import com.nowbook.sdp.dao.UserLevelWaitDao;
import com.nowbook.weixin.weixin4j.http.OAuthToken;
import com.nowbook.weixin.weixin4j.menu.ClickButton;
import com.nowbook.weixin.weixin4j.menu.SingleButton;
import com.nowbook.weixin.weixin4j.menu.ViewButton;
import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 如何实例化Weixin对象
 *
 * @author weixin4j<weixin4j@ansitech.com>
 */
public class WeixinDemo {

    public static void login() throws WeixinException {
        //1.初始化Weixin对象
        Weixin weixin = new Weixin();
        String appId = Configuration.getOAuthAppId();
        String secret = Configuration.getOAuthSecret();
        // 2.登录微信，获取Access_Token
        OAuthToken token = weixin.login(appId, secret);
        String accessToken = token.getAccess_token();
        // 创建菜单
        // oauthToken is null,you must call login first!
        weixin.init(accessToken, appId, secret, token.getExpires_in());
    }
    public static class SimpleOrderInfo implements Serializable {
        private static final long serialVersionUID = -1893738500647474932L;


        @Setter
        @Getter
        private Long id;

        @Setter
        @Getter
        private Integer total;

        @Setter
        @Getter
        private Integer express;
    }
    public static void main(String[] args) throws WeixinException {
//        Long a = 0L;
//        if(!a.equals(0)){
//            System.out.println(a);
//        }
//        SimpleDateFormat  n = new SimpleDateFormat("yyyyMMddHHmmss");
//        String dateFormat=n.format(new Date());
//        DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyyMMddHHmmss");
//        DateTime date = DFT.parseDateTime(dateFormat);
//        System.out.println(date);
//        // 1.初始化Weixin对象
//
//        Weixin weixin = new Weixin();
//        //1.需要先登录
//        String appId = Configuration.getOAuthAppId();
//        String secret = Configuration.getOAuthSecret();
//        //2.登录微信，获取Access_Token
//        weixin.login(appId, secret); //接下来就可以调用Weixin对象的其他方法了 //3.获取关注者列表
//        String openId="oNrS0uG99rSALF0CVyzLibHdWAvU";
//        String txtContent = "aaaa";
//        Articles articles = new Articles();
//        articles.setTitle("aaaaa");
//        articles.setDescription("d");
//        List<Articles> listArticles = new ArrayList<Articles>();
//        listArticles.add(articles);
//        weixin.customSendNews(openId,listArticles);

//        Followers followers = weixin.getUserList(null); //打印：关注者总数
//        System.out.println("关注者总数：" + followers.getTotal()); //打印：本次获取关注者记录数量
//        System.out.println("本次获取关注者数量：" + followers.getCount());
//        //打印：关注者openId数据
//        Data data = followers.getData();
//        //获取openId集合
//        List<String> openIdList = data.getOpenid(); //打印：前3条记录
//        for (int i = 0; i < openIdList.size(); i++) {
//            if (i > 2) {
//                break;
//            }
//            String openId = openIdList.get(i);
//            WxUser u = weixin.getUserInfo(openId);
//            String openid = u.getOpenid(); //用户的标识，对当前公众号唯一
//            String subscribe = u.getSubscribe(); //用户是否订阅该公众号标识，值为0时，代表此用户没有关注该公众号，拉取不到其余信息。
//            String nickname = u.getNickname(); //用户的昵称
//            int sex = u.getSex();
//            //用户的性别，值为1时是男性，值为2时是女性，值为0时是未知
//            String city = u.getCity(); //用户所在城市
//            String country = u.getCountry(); //用户所在国家
//            String province = u.getProvince(); //用户所在省份
//            String language = u.getLanguage();
//            //用户的语言，简体中文为zh_CN
//            String headimgurl = u.getHeadimgurl();
//            //用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空
//            long subscribe_time = u.getSubscribe_time(); //用户关注时间，为时间戳。如果用户曾多次关注，则取最后关注时间
//            String remark = u.getRemark(); //用户备注
//            String groupid = u.getGroupid(); //用户分组
//            System.out.println("第" + i + "条 " + openId + "用户的标识：" + openid +
//                    "是否订阅该公众号标识：" + subscribe + "昵称：" + nickname + "性别：" + sex + "城市：" +
//                    city + "国家：" + country + "省份：" + province + "语言：" + language + "头像："
//                    + headimgurl + "关注时间：，" + subscribe_time + "备注：" + remark + "分组：" +
//                    groupid);
//
//        }


//        login();

		/*OAuth2 oauth=new OAuth2();
		
		String url=oauth.getOAuth2CodeUserInfoUrl(Configuration.getOAuthAppId(), Configuration.getProperty("weixin4j.oauth.url"));
		
		  HttpClient http = new HttpClient();
	        //调用获取access_token接口
	        Response res = http.get(url);*/

//        //获取支付jsapi_ticket
//        String jsapi_ticket = "sM4AOVdWfPE4DxkXGEs8VHswEPEL4mrjIZ71i3ZNphdQlGxgkpu1j_qjD4w1sgkXNXKC0u0Mxv9JbzKylMGcfw";
//        //WeixinManager.getWeixin().getJsApiTicket();
//        //System.out.println("jsapi_ticket = " + jsapi_ticket);
//        String noncestr = java.util.UUID.randomUUID().toString().substring(0, 15);
//        String timestamp = System.currentTimeMillis() / 1000 + "";
//        String url = "http://wechat.ansitech.com/snsapi/base.jsp";
//        String sign = SignUtil.getSignature(jsapi_ticket, noncestr, timestamp, url);
//        System.out.println(sign);

    }

    /**
     * 组装菜单数据
     *
     * @return
     */
    public static Menu getMenu() {

        List<SingleButton> button = new ArrayList<SingleButton>();

        ClickButton singleButton1 = new ClickButton("a");
        singleButton1.setName("关于我们");
        ClickButton sub_button1 = new ClickButton("b");
        sub_button1.setName("子菜单11");
        ClickButton sub_button2 = new ClickButton("c");
        sub_button2.setName("子菜单21");
        List<SingleButton> ls11 = new ArrayList<SingleButton>();
        ls11.add(sub_button1);
        ls11.add(sub_button2);
        singleButton1.setSubButton(ls11);

        ClickButton singleButton2 = new ClickButton("d");
        singleButton2.setName("互动一下");
        ViewButton v1 = new ViewButton();
        v1.setName("大转盘");
        v1.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx7327c871a0747c4a&redirect_uri=http%3A%2F%2Fapi.nowbook.com%2Fdraw%2FdrawApi%2FgetOrder?drawId=7c5f0dc790d64ba083e73688949c9cdf&response_type=code&scope=snsapi_userinfo&state=DEFAULT&connect_redirect=1#wechat_redirect");
        ViewButton v2 = new ViewButton();
        v2.setName("得分明细");
        v2.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx7327c871a0747c4a&redirect_uri=http%3A%2F%2Fapi.nowbook.com%2Fdraw%2FdrawApi%2FshowHistory?drawId=7c5f0dc790d64ba083e73688949c9cdf&response_type=code&scope=snsapi_userinfo&state=DEFAULT&connect_redirect=1#wechat_redirect");

        ViewButton v3 = new ViewButton();
        v3.setName("现金提现");
        v3.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx7327c871a0747c4a&redirect_uri=http%3A%2F%2Fapi.nowbook.com%2Fdraw%2FdrawApi%2FgetMoney?drawId=7c5f0dc790d64ba083e73688949c9cdf&response_type=code&scope=snsapi_userinfo&state=DEFAULT&connect_redirect=1#wechat_redirect");

        ViewButton v4 = new ViewButton();
        v4.setName("领取奖品");
        v4.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx7327c871a0747c4a&redirect_uri=http%3A%2F%2Fapi.nowbook.com%2Fdraw%2FdrawApi%2FreceivePrize?drawId=7c5f0dc790d64ba083e73688949c9cdf&response_type=code&scope=snsapi_userinfo&state=DEFAULT&connect_redirect=1#wechat_redirect");


        List<SingleButton> ls21 = new ArrayList<SingleButton>();
        ls21.add(v1);
        ls21.add(v2);
        ls21.add(v3);
        ls21.add(v4);
        singleButton2.setSubButton(ls21);
        //
        // SingleButton singleButton3 = new SingleButton();
        // singleButton3.setName("关与我们");
        // SingleButton sub_button31 = new SingleButton();
        // sub_button31.setName("子菜单31");
        // List<SingleButton> ls31 = new ArrayList<SingleButton>();
        // ls31.add(sub_button31);
        // singleButton3.setSubButton(ls31);
        //
        button.add(singleButton1);
        button.add(singleButton2);
        // button.add(singleButton3);

        Menu menu = new Menu();
        menu.setButton(button);

        return menu;

		/*
		 * {"button":[{"name":"时尚故事","sub_button":[{"name":"菜单11"},{"name":"菜单21"
		 * }]}, {"name":"互动一下","sub_button":[{"name":"菜单21","type":"view","url":
		 * "http://www.163.com/"}]}, {"name":"关与我们","sub_button":[]}] }
		 */

    }
}