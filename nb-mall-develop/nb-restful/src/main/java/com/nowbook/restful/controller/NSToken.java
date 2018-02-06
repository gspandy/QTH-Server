package com.nowbook.restful.controller;

import com.google.common.base.Splitter;
import com.nowbook.common.model.Response;
import com.nowbook.restful.dto.NbResponse;
import com.nowbook.sdp.model.UserTeamMemberSelect;
import com.nowbook.sdp.service.UserLevelService;
import com.nowbook.session.AFSession;
import com.nowbook.session.AFSessionManager;
import com.nowbook.third.model.token.DeviceType;
import com.nowbook.restful.model.token.JwtSettings;
import com.nowbook.third.model.token.JwtToken;
import com.nowbook.restful.model.token.JwtTokenFactory;
import com.nowbook.restful.security.jwt.JwtAuthenticationToken;
import com.nowbook.restful.security.jwt.extractor.TokenExtractor;
import com.nowbook.user.model.LoginInfo;
import com.nowbook.user.model.LoginType;
import com.nowbook.user.model.User;
import com.nowbook.user.service.AccountService;
import com.nowbook.user.service.TokenService;
import com.nowbook.web.controller.api.userEvent.LoginEvent;
import com.nowbook.web.controller.api.userEvent.UserEventBus;
import com.nowbook.web.misc.MessageSources;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.nowbook.common.utils.Arguments.equalWith;
import static com.nowbook.common.utils.Arguments.notEmpty;
import static com.nowbook.common.utils.Arguments.notNull;

/**
 * Date: 27/7/17
 * Time: 15:00
 * Author: 2017年 <a href="mailto:robin@nowbook.com">程程</a>
 */
@Controller
@Slf4j
@RequestMapping(value = "/api/auth")
public class NSToken {
    @Autowired
    private MessageSources messageSources;
    @Autowired
    private JwtTokenFactory tokenFactory;
    @Autowired
    private JwtSettings jwtSettings;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private AccountService<User> accountService;
    @Autowired
    private UserEventBus userEventBus;
    @Autowired
    private UserLevelService userLevelService;
    @Autowired
    @Qualifier("jwtHeaderTokenExtractor")
    private TokenExtractor tokenExtractor;

    private final AFSessionManager sessionManager = AFSessionManager.instance();

    private final Splitter splitter = Splitter.on('@').trimResults();
    /**
     * 刷新token
     * 从head中获取刷新token并且生成新的token返回客户端
     */
    @RequestMapping(value="/token", method= RequestMethod.GET, produces={ MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public NbResponse<JwtToken> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String subject=null;
        int type=0;
        String pushDeviceId=null;
        String deviceId=null;
        int deviceType=0;
        String deviceBrand=null;
        JwtToken token=null;
        String tokenPayload=null;
        NbResponse<JwtToken> result = new NbResponse<JwtToken>();
        try {
            //导出token
            tokenPayload = request.getHeader("refreshToken");
            JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(tokenPayload);
            Jws<Claims> claims = jwtAuthenticationToken.parseClaims(jwtSettings.getTokenSigningKey());
            //获取用户登录名
            subject=claims.getBody().getSubject();
            type=(Integer)claims.getBody().get("type");
            deviceId=(String)claims.getBody().get("deviceId");
            deviceType=(Integer)claims.getBody().get("deviceType");
            //通过刷新token的用户id，设备id，登录类型获取登录信息
            LoginInfo info=tokenService.getRedisToken(Long.parseLong(subject), deviceId, deviceType);
            //创建新的token
            token=tokenFactory.createResponseToken(info);
            info.setToken(token.getToken());
            //保存登录信息到redis
//            tokenService.verifyToken(tokenPayload,Long.parseLong(subject),deviceId, deviceType);
            tokenService.saveRedisToken(info);

        }
        catch (IllegalArgumentException e) {
            log.error("fail to login with tokenPayload:{},  error:{}", tokenPayload,e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }

        result.setSuccess(true);
        result.setResult(token);
        return result;
    }
    /**
     * 用户登录
     *
     * @param loginId  登录凭证(登录名|邮箱|手机), 必填
     * @param password 密码, 必填
     * @param pushDeviceId    通道推送设备id，例如极光
     * @param deviceId     手机的设备id
     * @param deviceType    设备类型 WEB 0,IOS 1 ,ANDROID 2,WEBMOBLIE 3;
     * @param deviceBrand   设备厂商名称
     * @return NbResponse
     */
    @RequestMapping(value = "/login",method= RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<JwtToken> login(@RequestParam("loginId") String loginId,
                                      @RequestParam("password") String password,
                                      @RequestParam(value = "type", defaultValue = "2") Integer type,
                                      @RequestParam("pushDeviceId") String pushDeviceId,
                                      @RequestParam("deviceId") String deviceId,
                                      @RequestParam("deviceType") Integer deviceType,
                                      @RequestParam("deviceBrand") String deviceBrand,
                                      HttpServletRequest request, HttpServletResponse response) {

        NbResponse<JwtToken> result = new NbResponse<JwtToken>();

        try {
            //  checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            //  checkArgument(Signatures.verify(request, key), "sign.verify.fail");
            checkArgument(notEmpty(loginId), "login.id.can.not.be.empty");
            //登录类型 1:邮箱 2:手机 3:登录名
            LoginType loginType = LoginType.from(type);
            checkArgument(notNull(loginType), "incorrect.login.type");
            checkArgument(notEmpty(pushDeviceId), "push.device.id.can.not.be.empty");
            checkArgument(notEmpty(deviceId), "device.id.can.not.be.empty");
            DeviceType devType = DeviceType.from(deviceType);
            checkArgument(notNull(devType), "device.type.can.not.be.empty");
            checkArgument(notEmpty(deviceBrand), "device.brand.can.not.be.empty");

            Response<User> loginResult = accountService.userLogin(loginId, loginType, password);
            checkState(loginResult.isSuccess(), loginResult.getError());
            User user = loginResult.getResult();

            LoginInfo loginInfo=new LoginInfo(user.getId().toString(),type,pushDeviceId,deviceId, deviceType,deviceBrand,null);
            JwtToken token=tokenFactory.createResponseToken(loginInfo);
            loginInfo.setToken(token.getToken());
            //保存登录信息到redis
            tokenService.saveRedisToken(loginInfo);
            LoginEvent loginEvent = new LoginEvent(user.getId(), request, response);
            userEventBus.post(loginEvent);

            UserTeamMemberSelect userTeamMemberSelect = userLevelService.selectUser(user.getId()).getResult();
            token.setMobile(userTeamMemberSelect.getMobile());
            token.setNick(userTeamMemberSelect.getNick());
            token.setRealName(userTeamMemberSelect.getRealName());
            result.setResult(token);

        } catch (IllegalArgumentException e) {
            log.error("fail to login with loginId:{}, type:{}, deviceId:{}, error:{}", loginId,type, deviceId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("fail to login with loginId:{}, type:{}, deviceId:{}, error:{}", loginId,type, deviceId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (Exception e) {
            log.error("fail to login with loginId:{}, type:{}, deviceId:{}, error:{}", loginId,type, deviceId, e.getMessage());
            result.setError(messageSources.get("user.login.fail"));

        }
        return result;
    }

    /**
     * 验证码登录
     *
     * @param loginId  登录凭证(登录名|邮箱|手机), 必填
//     * @param password 密码, 必填
     * @param pushDeviceId    通道推送设备id，例如极光
     * @param deviceId     手机的设备id
     * @param deviceType    设备类型 WEB 0,IOS 1 ,ANDROID 2,WEBMOBLIE 3;
     * @param deviceBrand   设备厂商名称
     * @param code     短信验证码
     * @return NbResponse
     */
    @RequestMapping(value = "/verifyCodeLogin",method= RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<JwtToken> loginSms(@RequestParam("loginId") String loginId,
//                                      @RequestParam("password") String password,
                                         @RequestParam("session") String sessionId,
                                         @RequestParam("captcha") String code,
                                      @RequestParam(value = "type", defaultValue = "2") Integer type,
                                      @RequestParam("pushDeviceId") String pushDeviceId,
                                      @RequestParam("deviceId") String deviceId,
                                      @RequestParam("deviceType") Integer deviceType,
                                      @RequestParam("deviceBrand") String deviceBrand,
                                      HttpServletRequest request, HttpServletResponse response) {

        NbResponse<JwtToken> result = new NbResponse<JwtToken>();

        try {
            //  checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            //  checkArgument(Signatures.verify(request, key), "sign.verify.fail");
            checkArgument(notEmpty(loginId), "login.id.can.not.be.empty");
            //登录类型 1:邮箱 2:手机 3:登录名
            LoginType loginType = LoginType.from(type);
            checkArgument(notNull(loginType), "incorrect.login.type");
            checkArgument(notEmpty(pushDeviceId), "push.device.id.can.not.be.empty");
            checkArgument(notEmpty(deviceId), "device.id.can.not.be.empty");
            DeviceType devType = DeviceType.from(deviceType);
            checkArgument(notNull(devType), "device.type.can.not.be.empty");
            checkArgument(notEmpty(deviceBrand), "device.brand.can.not.be.empty");

            //验证码登录
            AFSession session = new AFSession(sessionManager, request, sessionId);
            String temp = (String) session.getAttribute("code");
            checkState(notEmpty(temp), "user.code.not.found");

            List<String> parts = splitter.splitToList(temp);
            String expected = parts.get(0);
            checkState(equalWith(code, expected), "user.code.mismatch");

            //如果匹配了code,则删除在session中的值
            request.getSession().removeAttribute("code");

            Response<User> loginResult = accountService.userLoginSms(loginId,loginType);
            checkState(loginResult.isSuccess(), loginResult.getError());
            User user = loginResult.getResult();

            LoginInfo loginInfo=new LoginInfo(user.getId().toString(),type,pushDeviceId,deviceId, deviceType,deviceBrand,null);
            JwtToken token=tokenFactory.createResponseToken(loginInfo);
            loginInfo.setToken(token.getToken());
            //保存登录信息到redis
            tokenService.saveRedisToken(loginInfo);
            LoginEvent loginEvent = new LoginEvent(user.getId(), request, response);
            userEventBus.post(loginEvent);
            result.setResult(token);

        } catch (IllegalArgumentException e) {
            log.error("fail to login with loginId:{}, type:{}, deviceId:{}, error:{}", loginId,type, deviceId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("fail to login with loginId:{}, type:{}, deviceId:{}, error:{}", loginId,type, deviceId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (Exception e) {
            log.error("fail to login with loginId:{}, type:{}, deviceId:{}, error:{}", loginId,type, deviceId, e.getMessage());
            result.setError(messageSources.get("user.login.fail"));

        }
        return result;
    }
}
