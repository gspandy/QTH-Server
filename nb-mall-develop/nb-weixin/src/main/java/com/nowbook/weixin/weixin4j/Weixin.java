/*
 * 微信公众平台(JAVA) SDK
 *
 * Copyright (c) 2014, Ansitech Network Technology Co.,Ltd All rights reserved.
 * 
 * http://www.weixin4j.org/sdk/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nowbook.weixin.weixin4j;

import com.nowbook.sdp.model.MPPayment;
import com.nowbook.sdp.model.MPPaymentResult;
import com.nowbook.weixin.weixin4j.http.*;
import com.nowbook.weixin.weixin4j.message.Article;
import com.nowbook.weixin.weixin4j.message.Articles;
import com.nowbook.weixin.weixin4j.message.MediaType;
import com.nowbook.weixin.weixin4j.pay.*;
import com.nowbook.weixin.weixin4j.pay.redpack.SendRedPack;
import com.nowbook.weixin.weixin4j.pay.redpack.SendRedPackResult;
import com.nowbook.weixin.weixin4j.ticket.TicketType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * 微信平台基础支持对象
 *
 * @author weixin4j<weixin4j@ansitech.com>
 */
@Component
public class Weixin extends WeixinSupport implements Serializable {
    /**
     * access_token过期自动登录，默认是
     */
    private boolean access_token_auto_login = true;
    /**
     * 公众号对象
     */
    private OAuth oauth = null;
    /**
     * 公众号Token对象
     */
    private OAuthToken oauthToken = null;

    /**
     * 微信基础支持
     *
     * 默认Access_Token过期后会自动重新登录
     */
    public Weixin() {
    }

    /**
     * 微信基础支持
     *
     * @param access_token_auto_login 是否自动登录
     */
    public Weixin(boolean access_token_auto_login) {
        this.access_token_auto_login = access_token_auto_login;
    }

    /**
     * 获取登录后的OAuthToken对象
     *
     * @return 如果已登录过返回OAuthToken对象，否则返回null
     */
    public OAuthToken getOAuthToken() {
        return oauthToken;
    }

    /**
     * 初始化OAuthToken和OAuth对象
     *
     * <p>
     * 此方法，一般是在数据库中已经有一个未过期的accessToken时调用
     * </p>
     *
     * @param accessToken 获取到的凭证
     * @param appId 开发者Id
     * @param secret 开发者密钥
     * @param expiresIn 计算过后的剩余时间
     * @throws WeixinException
     * @since Weixin 1.0.0
     */
    @Deprecated
    public void init(String accessToken, String appId, String secret, int expiresIn) throws WeixinException {
        if (null == accessToken || accessToken.equals("")) {
            throw new IllegalStateException("access_token is null!");
        }
        oauthToken = new OAuthToken(accessToken, expiresIn);
        oauth = new OAuth(appId, secret);
    }

    /**
     * 初始化OAuthToken和OAuth对象
     *
     * <p>
     * 此方法，一般是在数据库中已经有一个未过期的accessToken时调用
     * </p>
     *
     * @param accessToken 获取到的凭证
     * @param appId 开发者Id
     * @param secret 开发者密钥
     * @param expiresIn 凭证有效时间，单位：秒
     * @param createTime 凭证创建时间
     * @throws WeixinException
     * @since Weixin 1.0.0
     */
    public void init(String accessToken, String appId, String secret, int expiresIn, long createTime) throws WeixinException {
        if (null == accessToken || accessToken.equals("")) {
            throw new IllegalStateException("access_token is null!");
        }
        if (createTime <= 0) {
            throw new IllegalStateException("createTime data error!");
        }
        oauthToken = new OAuthToken(accessToken, expiresIn, createTime);
        oauth = new OAuth(appId, secret);
    }

    /**
     * 获取access_token
     *
     * @param appId 第三方用户唯一凭证
     * @param secret 第三方用户唯一凭证密钥，既appsecret
     * @return 用户凭证
     * @throws WeixinException
     * @since Weixin4J 1.0.0
     */
    public OAuthToken login(String appId, String secret) throws WeixinException {
        return login(appId, secret, "client_credential");
    }

    /**
     * 向微信平台发送获取access_token请求
     *
     * @param appId 第三方用户唯一凭证
     * @param secret 第三方用户唯一凭证密钥，既appsecret
     * @param grantType 获取access_token填写client_credential
     * @return 用户凭证
     * @throws WeixinException
     * @since Weixin4J 1.0.0
     */
    public OAuthToken login(String appId, String secret, String grantType) throws WeixinException {
        if (appId == null || secret == null || appId.equals("") || secret.equals("") || grantType == null || grantType.equals("")) {
            throw new WeixinException("invalid null, appid or secret is null.");
        }
        //发送登陆请求验证，由于接口有频率限制，所以，一次请求后，在过期时间内，不在进行第二次请求
        //所以先从当前HttpClient内验证OAuthToken是否已验证，并且未过期
        if (this.oauth != null && this.oauthToken != null) {
            //判断是否过期
            if (!oauthToken.isExprexpired()) {
                //先验证用户公众号信息是否一致，不一致则需要重新登录获取
                if (this.oauth.getAppId().equals(appId) && this.oauth.getSecret().equals(secret)) {
                    //如果没有过期，则直接返回对象
                    return oauthToken;
                }
            }
        }
        //拼接参数
        String param = "?grant_type=" + grantType + "&appid=" + appId + "&secret=" + secret;
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.get("https://api.weixin.qq.com/cgi-bin/token" + param);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("login返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            }
            //判断是否登录成功，并判断过期时间
            Object obj = jsonObj.get("access_token");
            //登录成功，设置accessToken和过期时间
            if (obj != null) {
                //设置公众号信息
                oauth = new OAuth(appId, secret);
                //设置凭证
                this.oauthToken = (OAuthToken) JSONObject.toBean(jsonObj, OAuthToken.class);
            }
        }
        return oauthToken;
    }

    /**
     * 验证用户登录
     *
     * <p>
     * 调用所有方法之前，应先调用此方法检查用户是否已经登录，或者Token是否失效<br/>
     * 如果没有登录，则跑出异常提示登录，如果失效，密码正确的情况下回自动重新登录。</p>
     *
     * @throws WeixinException
     */
    private void checkLogin() throws WeixinException {
        //自动登录，才验证是否过期，否则不验证，默认不过期
        if (access_token_auto_login) {
            //判断是否过期，如果已过期，则发送重新登录命令
            if (oauthToken == null) {
                throw new WeixinException("oauthToken is null,you must call login or init first!");
            } else //已过期
             if (oauthToken.isExprexpired()) {
                    //如果用户名和密码正确，则自动登录，否则返回异常
                    if (oauth != null) {
                        //自动重新发送登录请求
                        login(oauth.getAppId(), oauth.getSecret());
                    } else {
                        throw new WeixinException("oauth is null and oauthToken is exprexpired, please log in again!");
                    }
                }
        }
    }

    /**
     * 根据OpenId获取用户对象
     *
     * <p>
     * 通过公众号，返回用户对象，进行用户相关操作</p>
     *
     * @param openId 普通用户的标识，对当前公众号唯一
     * @return 用户对象
     * @throws WeixinException
     */
    public WxUser getUserInfo(String openId) throws WeixinException {
        //默认简体中文
        return getUserInfo(openId, "zh_CN");
    }

    /**
     * 获取用户对象
     *
     * <p>
     * 通过公众号，返回用户对象，进行用户相关操作</p>
     *
     * @param openId 普通用户的标识，对当前公众号唯一
     * @param lang 国家地区语言版本 zh_CN 简体，zh_TW 繁体，en 英语
     * @return 用户对象
     * @throws WeixinException
     */
    public WxUser getUserInfo(String openId, String lang) throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //拼接参数
        String param = "?access_token=" + this.oauthToken.getAccess_token() + "&openid=" + openId + "&lang=" + lang;
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.get("https://api.weixin.qq.com/cgi-bin/user/info" + param);
//        System.out.print("https://api.weixin.qq.com/cgi-bin/user/info" + param);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("getUserInfo返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            }
            //设置公众号信息
            return (WxUser) JSONObject.toBean(jsonObj, WxUser.class);
        }
        return null;
    }

    /**
     * 获取所有关注者列表
     *
     * <p>
     * 通过公众号，返回用户对象，进行用户相关操作</p>
     *
     * @return 关注者对象
     * @throws WeixinException
     */
    public Followers getAllUserList() throws WeixinException {
        Followers allFollower = new Followers();
        Data data = new Data();
        data.setOpenid(new ArrayList<String>());
        allFollower.setData(data);
        String next_openid = "";
        do {
            Followers f = getUserList(next_openid);
            if (f == null) {
                break;
            }
            if (f.getCount() > 0) {
                List<String> openids = f.getData().getOpenid();
                for (String openid : openids) {
                    allFollower.getData().getOpenid().add(openid);
                }
            }
            next_openid = f.getNext_openid();
        } while (!next_openid.equals(""));
        return allFollower;
    }

    /**
     * 获取关注者列表
     *
     * <p>
     * 通过公众号，返回用户对象，进行用户相关操作</p>
     *
     * @param next_openid 第一个拉取的OPENID，不填默认从头开始拉取
     * @return 关注者对象
     * @throws WeixinException when Weixin service or network is unavailable, or
     * the user has not authorized
     */
    public Followers getUserList(String next_openid) throws WeixinException {
        //拼接参数
        String param = "?access_token=" + this.oauthToken.getAccess_token() + "&next_openid=";
        //第一次获取不添加参数
        if (next_openid != null && !next_openid.equals("")) {
            param += next_openid;
        }
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.get("https://api.weixin.qq.com/cgi-bin/user/get" + param);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        Followers follower = null;
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("getUserList返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            }
            Map<String, Class> map = new HashMap<String, Class>();
            map.put("data", Data.class);
            follower = (Followers) JSONObject.toBean(jsonObj, Followers.class, map);
        }
        return follower;
    }

    /**
     * 创建分组
     *
     * @param name 分组名字（30个字符以内）
     * @return 创建成功，返回带Id的Group对象
     * @throws WeixinException 创建分组异常
     */
    public Group createGroup(String name) throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //内部业务验证
        if (name == null || name.equals("")) {
            throw new IllegalStateException("name is null!");
        }
        //拼接参数
        JSONObject postGroup = new JSONObject();
        JSONObject postName = new JSONObject();
        postName.put("name", name);
        postGroup.put("group", postName);
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.post("https://api.weixin.qq.com/cgi-bin/groups/create?access_token=" + this.oauthToken.getAccess_token(), postGroup);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        Group group = null;
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("createGroup返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            }
            JSONObject jsonGroup = jsonObj.getJSONObject("group");
            group = (Group) JSONObject.toBean(jsonGroup, Group.class);
        }
        return group;
    }

    /**
     * 查询所有分组
     *
     * <p>
     * 最多支持创建500个分组</p>
     *
     * @return 分组列表
     * @throws WeixinException 查询所有分组异常
     */
    public List<Group> getGroups() throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        List<Group> groupList = new ArrayList<Group>();
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.post("https://api.weixin.qq.com/cgi-bin/groups/get?access_token=" + this.oauthToken.getAccess_token(), null);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("getGroups返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            }
            //获取分组列表
            JSONArray groups = jsonObj.getJSONArray("groups");
            for (int i = 0; i < groups.size(); i++) {
                JSONObject jsonGroup = groups.getJSONObject(i);
                Group group = (Group) JSONObject.toBean(jsonGroup, Group.class);
                groupList.add(group);
            }
        }
        return groupList;
    }

    /**
     * 查询用户所在分组
     *
     * <p>
     * 通过用户的OpenID查询其所在的GroupID</p>
     *
     * @param openid 用户唯一标识符
     * @return 返回用户所在分组Id
     * @throws WeixinException 查询用户所在分组异常
     */
    public int getGroupId(String openid) throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //内部业务验证
        if (openid == null || openid.equals("")) {
            throw new IllegalStateException("openid is null!");
        }
        int groupId = -1;
        //拼接参数
        JSONObject postParam = new JSONObject();
        postParam.put("openid", openid);
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.post("https://api.weixin.qq.com/cgi-bin/groups/getid?access_token=" + this.oauthToken.getAccess_token(), postParam);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("getGroupId返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            }
            //获取成功返回分组Id
            groupId = jsonObj.getInt("groupid");
        }
        return groupId;
    }

    /**
     * 修改分组名
     *
     * @param id 分组id，由微信分配
     * @param name 分组名字（30个字符以内）
     * @throws WeixinException 修改分组名异常
     */
    public void updateGroup(int id, String name) throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //内部业务验证
        if (id < 0) {
            throw new IllegalStateException("id can not <= 0!");
        }
        if (name == null || name.equals("")) {
            throw new IllegalStateException("name is null!");
        }
        //拼接参数
        JSONObject postGroup = new JSONObject();
        JSONObject postName = new JSONObject();
        postName.put("id", id);
        postName.put("name", name);
        postGroup.put("group", postName);
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.post("https://api.weixin.qq.com/cgi-bin/groups/update?access_token=" + this.oauthToken.getAccess_token(), postGroup);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("updateGroup返回json：" + jsonObj.toString());
            }
            //判断是否修改成功
            //正常时返回 {"errcode": 0, "errmsg": "ok"}
            //错误时返回 示例：{"errcode":40013,"errmsg":"invalid appid"}
            int errcode = jsonObj.getInt("errcode");
            //登录成功，设置accessToken和过期时间
            if (errcode != 0) {
                //返回异常信息
                throw new WeixinException(getCause(errcode));
            }
        }
    }

    /**
     * 删除分组
     *
     * @param groupId 分组Id
     * @throws WeixinException 删除分组异常
     */
    public void deleteGroup(int groupId) throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //拼接参数
        JSONObject postParam = new JSONObject();
        JSONObject group = new JSONObject();
        group.put("id", groupId);
        postParam.put("group", group);
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.post("https://api.weixin.qq.com/cgi-bin/groups/delete?access_token=" + this.oauthToken.getAccess_token(), postParam);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("deleteGroup返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null && !errcode.toString().equals("0")) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            }
        }
    }

    /**
     * 移动用户分组
     *
     * @param openid 用户唯一标识符
     * @param to_groupid 分组id
     * @throws WeixinException 移动用户分组异常
     */
    public void updateMemberGroup(String openid, int to_groupid) throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //内部业务验证
        if (openid == null || openid.equals("")) {
            throw new IllegalStateException("openid is null!");
        }
        if (to_groupid < 0) {
            throw new IllegalStateException("to_groupid can not <= 0!");
        }
        //拼接参数
        JSONObject postParam = new JSONObject();
        postParam.put("openid", openid);
        postParam.put("to_groupid", to_groupid);
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.post("https://api.weixin.qq.com/cgi-bin/groups/members/update?access_token=" + this.oauthToken.getAccess_token(), postParam);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("updateMemberGroup返回json：" + jsonObj.toString());
            }
            //判断是否修改成功
            //正常时返回 {"errcode": 0, "errmsg": "ok"}
            //错误时返回 示例：{"errcode":40013,"errmsg":"invalid appid"}
            int errcode = jsonObj.getInt("errcode");
            //登录成功，设置accessToken和过期时间
            if (errcode != 0) {
                //返回异常信息
                throw new WeixinException(getCause(errcode));
            }
        }
    }

    /**
     * 创建自定义菜单
     *
     * @param menu 菜单对象
     * @throws WeixinException 创建自定义菜单异常
     */
    public void createMenu(Menu menu) throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //内部业务验证
        if (menu == null || menu.getButton() == null) {
            throw new IllegalStateException("menu is null!");
        }
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.post("https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + this.oauthToken.getAccess_token(), menu.toJSONObject());
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("createMenu返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null && !errcode.toString().equals("0")) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            }
        }
    }

    /**
     * 查询自定义菜单
     *
     * <p>
     * 最多支持创建2级自定义菜单</p>
     *
     * @return 自定义菜单对象
     * @throws WeixinException 查询自定义菜单对象异常
     */
    public Menu getMenu() throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.post("https://api.weixin.qq.com/cgi-bin/menu/get?access_token=" + this.oauthToken.getAccess_token(), null);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("getMenu返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            }
            //返回自定义菜单对象
            return new Menu(jsonObj);
        }
        //返回自定义菜单对
        return null;
    }

    /**
     * 删除自定义菜单
     *
     * @throws WeixinException 删除自定义菜单异常
     */
    public void deleteMenu() throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.get("https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=" + this.oauthToken.getAccess_token());
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("deleteMenu返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null && !errcode.toString().equals("0")) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            }
        }
    }

    /**
     * 创建二维码ticket
     *
     * @param ticketType 二维码类型
     * @param fileName 图片文件路径
     * @param scene_id 场景值ID
     * @param expire_seconds 临时二维码过期时间
     * @throws WeixinException
     */
    public void createQrcode(TicketType ticketType, String fileName, int scene_id, int expire_seconds) throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //内部业务验证
        if (ticketType.equals(TicketType.QR_SCENE)) {
            if (scene_id <= 0) {
                throw new IllegalStateException("场景id不能小于等于0");
            }
        } else if (ticketType.equals(TicketType.QR_LIMIT_SCENE)) {
            if (scene_id <= 0 || scene_id > 100000) {
                throw new IllegalStateException("永久场景id参数只支持1-100000");
            }
        }
        JSONObject ticketJson = new JSONObject();
        if (ticketType.equals(TicketType.QR_SCENE)) {
            //临时二维码过期时间
            ticketJson.put("expire_seconds", expire_seconds);
        }
        //二维码类型
        ticketJson.put("action_name", ticketType.toString());

        JSONObject actionInfo = new JSONObject();
        JSONObject scene = new JSONObject();
        scene.put("scene_id", scene_id);
        actionInfo.put("scene", scene);
        //二维码详细信息
        ticketJson.put("action_info", actionInfo);
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用创建Tick的access_token接口
        Response res = http.post("https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + this.oauthToken.getAccess_token(), ticketJson);
        //根据请求结果判定，返回结果
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("createQrcode返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null && !errcode.toString().equals("0")) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            } else {
                try {
                    //通过ticket换取二维码
                    URL url = new URL("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + jsonObj.getString("ticket"));
                    // 打开连接
                    URLConnection con = url.openConnection();
                    // 输入流
                    InputStream is = con.getInputStream();
                    // 1K的数据缓冲
                    byte[] bs = new byte[1024];
                    // 读取到的数据长度
                    int len;
                    // 输出的文件流
                    OutputStream os = new FileOutputStream(fileName);
                    // 开始读取
                    while ((len = is.read(bs)) != -1) {
                        os.write(bs, 0, len);
                    }
                    // 完毕，关闭所有链接
                    os.close();
                    is.close();
                } catch (MalformedURLException ex) {
                    throw new WeixinException("通过ticket换取二维码异常：", ex);
                } catch (IOException ex) {
                    throw new WeixinException("通过ticket换取二维码，下载二维码图片异常：", ex);
                }
            }
        }
    }

    /**
     * 根据OpenID列表群发文本消息
     *
     * @param openIds 粉丝OpenId集合
     * @param txtContent 文本消息内容
     * @return 发送成功则返回群发消息Id，否则返回null
     * @throws com.nowbook.weixin.weixin4j.WeixinException
     */
    public String massSendContent(String[] openIds, String txtContent) throws WeixinException {
        JSONObject json = new JSONObject();
        JSONObject text = new JSONObject();
        text.put("content", txtContent);
        json.put("touser", openIds);
        json.put("text", text);
        json.put("msgtype", "text");
        //创建请求对象
        HttpsClient http = new HttpsClient();
        Response res = http.post("https://api.weixin.qq.com/cgi-bin/message/mass/send?access_token=" + this.oauthToken.getAccess_token(), json);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("群发文本消息返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null && !errcode.toString().equals("0")) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            } else {
                //返回群发消息id
                return jsonObj.getString("msg_id");
            }
        }
        return null;
    }

    /**
     * 根据OpenID列表群发文本消息
     *
     * @param openIds 粉丝OpenId集合
     * @param mediaId 图文消息素材Id
     * @return 发送成功则返回群发消息Id，否则返回null
     * @throws WeixinException
     */
    public String massSendNews(String[] openIds, String mediaId) throws WeixinException {
        JSONObject json = new JSONObject();
        JSONObject media_id = new JSONObject();
        media_id.put("media_id", mediaId);
        json.put("touser", openIds);
        json.put("mpnews", media_id);
        json.put("msgtype", "mpnews");
        //创建请求对象
        HttpsClient http = new HttpsClient();
        Response res = http.post("https://api.weixin.qq.com/cgi-bin/message/mass/send?access_token=" + this.oauthToken.getAccess_token(), json);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("群发图文消息返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null && !errcode.toString().equals("0")) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            } else {
                //返回群发图文id
                return jsonObj.getString("msg_id");
            }
        }
        return null;
    }

    /**
     * 上传图文消息素材
     *
     * @param articles 图文消息，一个图文消息支持1到10条图文
     * @return 上传成功返回图文素材Id，否则返回null
     * @throws WeixinException
     */
    public String uploadnews(List<Article> articles) throws WeixinException {
        JSONObject json = new JSONObject();
        json.put("articles", articles);
        //创建请求对象
        HttpsClient http = new HttpsClient();
        Response res = http.post("https://api.weixin.qq.com/cgi-bin/media/uploadnews?access_token=" + this.oauthToken.getAccess_token(), json);
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("uploadnews返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null && !errcode.toString().equals("0")) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            } else {
                //返回图文消息id
                return jsonObj.getString("media_id");
            }
        }
        return null;
    }

    /**
     * 发送客服文本消息
     *
     * @param openId 粉丝OpenId
     * @param txtContent 文本消息内容
     * @throws com.nowbook.weixin.weixin4j.WeixinException
     */
    public void customSendContent(String openId, String txtContent) throws WeixinException {
        JSONObject json = new JSONObject();
        JSONObject text = new JSONObject();
        text.put("content", txtContent);
        json.put("touser", openId);
        json.put("text", text);
        json.put("msgtype", "text");
        //创建请求对象
        HttpsClient http = new HttpsClient();
        http.post("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + this.oauthToken.getAccess_token(), json);
    }

    /**
     * 发送客服图文消息
     *
     * @param openId 粉丝OpenId
     * @param articles 图文消息，一个图文消息支持1到10条图文
     * @throws com.nowbook.weixin.weixin4j.WeixinException
     */
    public void customSendNews(String openId, List<Articles> articles) throws WeixinException {
        JSONObject json = new JSONObject();
        json.put("touser", openId);
        json.put("msgtype", "news");
        JSONObject news = new JSONObject();
        news.put("articles", articles);
        json.put("news", news);
        //创建请求对象
        HttpsClient http = new HttpsClient();
        http.post("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + this.oauthToken.getAccess_token(), json);
    }

    /**
     * 新增临时素材
     *
     * @param mediaType 媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
     * @param file form-data中媒体文件标识，有filename、filelength、content-type等信息
     * @return 上传成功返回素材Id，否则返回null
     * @throws WeixinException
     */
    public String uploadMedia(MediaType mediaType, File file) throws WeixinException {
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //上传素材，返回JSON数据包
        String jsonStr = http.uploadHttps("https://api.weixin.qq.com/cgi-bin/media/upload?access_token=" + this.oauthToken.getAccess_token() + "&type=" + mediaType.toString(), file);
        JSONObject jsonObj = JSONObject.fromObject(jsonStr);
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("上传多媒体文件返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null && !errcode.toString().equals("0")) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            } else {
                //返回多媒体文件id
                return jsonObj.getString("media_id");
            }
        }
        return null;
    }

    /**
     * 获取临时素材
     *
     * @param mediaId 媒体文件ID
     * @return 正确返回附件对象，否则返回null
     * @throws WeixinException
     */
    public Attachment downloadMedia(String mediaId) throws WeixinException {
        //下载资源
        String url = "https://api.weixin.qq.com/cgi-bin/media/get?access_token=" + this.oauthToken.getAccess_token() + "&media_id=" + mediaId;
        //创建请求对象
        HttpsClient http = new HttpsClient();
        return http.downloadHttps(url);
    }

    /**
     * 上传媒体文件
     *
     * @param mediaType 媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
     * @param file form-data中媒体文件标识，有filename、filelength、content-type等信息
     * @return 上传成功返回素材Id，否则返回null
     * @throws WeixinException
     */
    @Deprecated
    public String upload(String mediaType, File file) throws WeixinException {
        try {
            //创建请求对象
            HttpClient http = new HttpClient();
            //上传素材，返回JSON数据包
            String jsonStr = http.upload("http://file.api.weixin.qq.com/cgi-bin/media/upload?access_token=" + this.oauthToken.getAccess_token() + "&type=" + mediaType, file);
            JSONObject jsonObj = JSONObject.fromObject(jsonStr);
            if (jsonObj != null) {
                if (Configuration.isDebug()) {
                    System.out.println("上传多媒体文件返回json：" + jsonObj.toString());
                }
                Object errcode = jsonObj.get("errcode");
                if (errcode != null && !errcode.toString().equals("0")) {
                    //返回异常信息
                    throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
                } else {
                    //返回多媒体文件id
                    return jsonObj.getString("media_id");
                }
            }
            return null;
        } catch (IOException ex) {
            throw new WeixinException("上传多媒体文件异常:", ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new WeixinException("上传多媒体文件异常:", ex);
        } catch (NoSuchProviderException ex) {
            throw new WeixinException("上传多媒体文件异常:", ex);
        } catch (KeyManagementException ex) {
            throw new WeixinException("上传多媒体文件异常:", ex);
        } catch (NumberFormatException ex) {
            throw new WeixinException("上传多媒体文件异常:", ex);
        } catch (WeixinException ex) {
            throw new WeixinException("上传多媒体文件异常:", ex);
        }
    }

    /**
     * 下载多媒体文件
     *
     * @param mediaId 媒体文件ID
     * @return 正确返回附件对象，否则返回null
     * @throws WeixinException
     */
    @Deprecated
    public Attachment download(String mediaId) throws WeixinException {
        try {
            //下载资源
            String url = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=" + this.oauthToken.getAccess_token() + "&media_id=" + mediaId;
            //创建请求对象
            HttpClient http = new HttpClient();
            return http.download(url);
        } catch (IOException ex) {
            throw new WeixinException("下载多媒体文件异常:", ex);
        }
    }

    /**
     * 获取jsapi_ticket
     *
     * @return 成功返回ticket，失败返回NULL
     * @throws WeixinException
     */
    @Deprecated
    public String getJsApiTicket() throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取jsapi_ticket接口
        Response res = http.get("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + this.oauthToken.getAccess_token() + "&type=jsapi");
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        //成功返回如下JSON:
        //{"errcode":0,"errmsg":"ok","ticket":"bxLdikRXVbTPdHSM05e5u5sUoXNKd8-41ZO3MhKoyN5OfkWITDGgnr2fwJ0m9E8NYzWKVZvdVtaUgWvsdshFKA","expires_in":7200}
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("获取jsapi_ticket返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null && !errcode.toString().equals("0")) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            } else {
                Object ticket = jsonObj.get("ticket");
                if (ticket != null) {
                    return ticket.toString();
                }
            }
        }
        return null;
    }

    /**
     * 获取jsapi_ticket对象，每次都返回最新
     *
     * @return 成功返回jsapi_ticket对象，失败返回NULL
     * @throws WeixinException
     */
    public JsApiTicket getJsApi_Ticket() throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取jsapi_ticket接口
        Response res = http.get("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + this.oauthToken.getAccess_token() + "&type=jsapi");
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        //成功返回如下JSON:
        //{"errcode":0,"errmsg":"ok","ticket":"bxLdikRXVbTPdHSM05e5u5sUoXNKd8-41ZO3MhKoyN5OfkWITDGgnr2fwJ0m9E8NYzWKVZvdVtaUgWvsdshFKA","expires_in":7200}
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("获取jsapi_ticket返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null && !errcode.toString().equals("0")) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            } else {
                return new JsApiTicket(jsonObj.getString("ticket"), jsonObj.getInt("expires_in"));
            }
        }
        return null;
    }

    /**
     * 统一下单
     *
     * @param unifiedorder 统一下单对象
     * @return 下单返回结果对象
     * @throws com.nowbook.weixin.weixin4j.WeixinException
     */
    public UnifiedOrderResult   payUnifiedOrder(UnifiedOrder unifiedorder) throws WeixinException {
        //将统一下单对象转成XML
        String xmlPost = unifiedorder.toXML();
        if (Configuration.isDebug()) {
            System.out.println("调试模式_统一下单接口 提交XML数据：" + xmlPost);
        }
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //提交xml格式数据
        Response res = http.postXml("https://api.mch.weixin.qq.com/pay/unifiedorder", xmlPost);
        //获取微信平台下单接口返回数据
        String xmlResult = res.asString();
        try {
            JAXBContext context = JAXBContext.newInstance(UnifiedOrderResult.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            UnifiedOrderResult result = (UnifiedOrderResult) unmarshaller.unmarshal(new StringReader(xmlResult));
            return result;
        } catch (JAXBException ex) {
            return null;
        }
    }
//    public WCRefundResult wcRefundResult(WCRefund wcRefund) throws WeixinException {
//        //将统一下单对象转成XML
//        String xmlPost = wcRefund.toXML();
//        if (Configuration.isDebug()) {
//            System.out.println("调试模式_统一下单接口 提交XML数据：" + xmlPost);
//        }
//        //创建请求对象
//        HttpsClient http = new HttpsClient();
//        //提交xml格式数据
//        Response res = http.postXml("https://api.mch.weixin.qq.com/secapi/pay/refund", xmlPost);
//        //获取微信平台下单接口返回数据
//        String xmlResult = res.asString();
//        try {
//            JAXBContext context = JAXBContext.newInstance(UnifiedOrderResult.class);
//            Unmarshaller unmarshaller = context.createUnmarshaller();
//            WCRefundResult result = (WCRefundResult) unmarshaller.unmarshal(new StringReader(xmlResult));
//            return result;
//        } catch (JAXBException ex) {
//            return null;
//        }
//    }

    /**
     * 发送现金红包
     *
     * <p>
     * 使用weixin4j.properties的配置</p>
     *
     * @param sendRedPack 现金红包对象
     * @return 发送现金红包返回结果对象
     * @throws com.nowbook.weixin.weixin4j.WeixinException
     */
    public SendRedPackResult sendRedPack(SendRedPack sendRedPack) throws WeixinException {
        return sendRedPack(sendRedPack, null, null, null);
    }

    /**
     * 发送现金红包
     *
     * @param sendRedPack 现金红包对象
     * @param partnerId 商户ID
     * @param certPath 证书路径
     * @param certSecret 证书密钥
     * @return 发送现金红包返回结果对象
     * @throws com.nowbook.weixin.weixin4j.WeixinException
     */
    public SendRedPackResult sendRedPack(SendRedPack sendRedPack, String partnerId, String certPath, String certSecret) throws WeixinException {
        //将统一下单对象转成XML
        String xmlPost = sendRedPack.toXML();
        if (Configuration.isDebug()) {
            System.out.println("调试模式_发送现金红包接口 提交XML数据：" + xmlPost);
        }
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //提交xml格式数据
        Response res;
        if (StringUtils.isEmpty(partnerId) || StringUtils.isEmpty(certPath) || StringUtils.isEmpty(certSecret)) {
            res = http.postXml("https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack", xmlPost, true);
        } else {
            res = http.postXml("https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack", xmlPost, partnerId, certPath, certSecret);
        }
        //获取微信平台下单接口返回数据
        String xmlResult = res.asString();
        try {
            JAXBContext context = JAXBContext.newInstance(SendRedPackResult.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            SendRedPackResult result = (SendRedPackResult) unmarshaller.unmarshal(new StringReader(xmlResult));
            return result;
        } catch (JAXBException ex) {
            return null;
        }
    }

    /**
     * 获取微信服务器IP地址
     *
     * @return 微信服务器IP地址列表
     * @throws com.nowbook.weixin.weixin4j.WeixinException
     */
    public List<String> getCallbackIp() throws WeixinException {
        //必须先调用检查登录方法
        checkLogin();
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取jsapi_ticket接口
        Response res = http.get("https://api.weixin.qq.com/cgi-bin/getcallbackip?access_token=" + this.oauthToken.getAccess_token());
        //根据请求结果判定，是否验证成功
        JSONObject jsonObj = res.asJSONObject();
        //成功返回如下JSON:
        //{"ip_list":["127.0.0.1","127.0.0.1"]}
        if (jsonObj != null) {
            if (Configuration.isDebug()) {
                System.out.println("获取getCallbackIp返回json：" + jsonObj.toString());
            }
            Object errcode = jsonObj.get("errcode");
            if (errcode != null && !errcode.toString().equals("0")) {
                //返回异常信息
                throw new WeixinException(getCause(Integer.parseInt(errcode.toString())));
            } else {
                JSONArray ipList = jsonObj.getJSONArray("ip_list");
                if (ipList != null) {
                    //转换为List
                    return ipList.subList(0, ipList.size());
                }
            }
        }
        return null;
    }


    /**
     * 企业付款 实现企业向个人付款，针对部分有开发能力的商户， 提供通过API完成企业付款的功能。 比如目前的保险行业向客户退保、给付、理赔。
     *
     * @param instream  证书文件(V3版本后缀为*.p12)
     * @param mpPayment 付款信息
     * @param mchid     商户ID
     * @return 付款结果

     * @see <a href="http://pay.weixin.qq.com/wiki/doc/api/mch_pay.php?chapter=14_1">企业付款</a>
     * @throws WeixinException
     */
    public static MPPaymentResult mchPayment(FileInputStream instream, MPPayment mpPayment, String mchid)throws WeixinException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {
        StringBuffer xmlResult = null;
        //将企业付款对象转成XML
        String xmlPost = mpPayment.toXML();
        KeyStore keyStore  = KeyStore.getInstance("PKCS12");
        try {
            keyStore.load(instream, mchid.toCharArray());
        } finally {
            instream.close();
        }
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, mchid.toCharArray()).build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,new String[] { "TLSv1" },null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        try {

            HttpPost httpost = new HttpPost("https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers");
            StringEntity stringEntity = new StringEntity(xmlPost,"UTF-8");
            httpost.setEntity(stringEntity);
            CloseableHttpResponse response = httpclient.execute(httpost);

            System.out.println("executing request" + httpost.getRequestLine());
            try {
                HttpEntity entity = response.getEntity();

                //System.out.println("----------------------------------------");
                //System.out.println(response.getStatusLine());
                if (entity != null) {
                    //System.out.println("Response content length: " + entity.getContentLength());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(),"UTF-8"));
                    String text;
                    //获取微信平台企业向个人付款接口返回xml数据
                    xmlResult = new StringBuffer();
                    while ((text = bufferedReader.readLine()) != null) {
                        //System.out.println(text);
                        xmlResult.append(text);
                    }

                }
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }

        try {
            JAXBContext context = JAXBContext.newInstance(MPPaymentResult.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            MPPaymentResult result = (MPPaymentResult) unmarshaller.unmarshal(new StringReader(xmlResult.toString()));
            return result;
        } catch (JAXBException ex) {
            return null;
        }
    }
    /**
     * 退款
     */
    public static WCRefundResult wcRefundResult(FileInputStream instream, WCRefund wcRefund, String mchid)throws WeixinException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {
        StringBuffer xmlResult = null;
        //将企业付款对象转成XML
        String xmlPost = wcRefund.toXML();
        KeyStore keyStore  = KeyStore.getInstance("PKCS12");
        try {
            keyStore.load(instream, mchid.toCharArray());
        } finally {
            instream.close();
        }
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, mchid.toCharArray()).build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,new String[] { "TLSv1" },null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        try {

            HttpPost httpost = new HttpPost("https://api.mch.weixin.qq.com/secapi/pay/refund");
            StringEntity stringEntity = new StringEntity(xmlPost,"UTF-8");
            httpost.setEntity(stringEntity);
            CloseableHttpResponse response = httpclient.execute(httpost);

            System.out.println("executing request" + httpost.getRequestLine());
            try {
                HttpEntity entity = response.getEntity();

                //System.out.println("----------------------------------------");
                //System.out.println(response.getStatusLine());
                if (entity != null) {
                    //System.out.println("Response content length: " + entity.getContentLength());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(),"UTF-8"));
                    String text;
                    //获取微信平台企业向个人付款接口返回xml数据
                    xmlResult = new StringBuffer();
                    while ((text = bufferedReader.readLine()) != null) {
                        //System.out.println(text);
                        xmlResult.append(text);
                    }
                }
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }

        try {
            JAXBContext context = JAXBContext.newInstance(WCRefundResult.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            WCRefundResult result = (WCRefundResult) unmarshaller.unmarshal(new StringReader(xmlResult.toString()));
            return result;
        } catch (JAXBException ex) {
            return null;
        }
    }

//    public final static void main(String[] args) throws WeixinException{
//        String caFile = "D:/a/apiclient_cert.p12";
//        FileInputStream ca =null;
//        try {
//            ca = new FileInputStream(new File(caFile));
//			//MPPayment payment = new MPPayment(outTradeNo,"oAX5duG-7AtBINu5BwOVJjEMiNck",MPPaymentCheckNameType.FORCE_CHECK, "企业付款测试", 0.5, "127.0.0.1");
//            WCRefund wcRefund = new WCRefund();
//
//            String appId = "wx1497e6e00110f82f";
//            String Mch_id = "1343959601";
//            String secret = "b32626214142232519ac1d8e7968b9d9";
//            String key = "rmcEwtK70lyZzIMfzAVNACyGKfC7XAE1";
//            String transaction_id = "4007552001201606288024107227";
//            String out_refund_no = "245";
//            String total_fee = "100";
//            String refund_fee = "100";
//            String op_user_id = "1343959601";
//
//            wcRefund.setAppid(appId);
//            wcRefund.setMch_id(Mch_id);
//            wcRefund.setNonce_str(java.util.UUID.randomUUID().toString().substring(0, 15));
//            wcRefund.setOp_user_id(op_user_id);
//            wcRefund.setOut_refund_no(out_refund_no);
//            wcRefund.setOut_trade_no("245");
//            wcRefund.setRefund_fee(refund_fee);
//            wcRefund.setTotal_fee(total_fee);
//            wcRefund.setTransaction_id(transaction_id);
//
//			//生成签名
//	        String sign = SignUtil.getSign(wcRefund.toMap(), key);  //签名 是
//            wcRefund.setSign(sign);						 //签名
//
//			System.out.println("-------------");
//
//            WCRefundResult result = wcRefundResult(ca, wcRefund,Mch_id);
//            //转帐成功
//            if (result.isSuccess()) {
//    			System.out.println("转帐成功"+result);
//            } else {
//
//            }
//
//		}
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        finally {
//            if (ca != null) {
//                try {

//                    ca.close();
//                }
//                catch (IOException e) {
//                }
//            }
//        }
//    }
}