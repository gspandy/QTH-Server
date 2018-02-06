package com.nowbook.restful.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.google.common.base.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.common.utils.CommonConstants;
import com.nowbook.common.utils.JsonMapper;
import com.nowbook.common.utils.NameValidator;
import com.nowbook.coupons.model.NbCou;
import com.nowbook.coupons.model.NbCouOrderItem;
import com.nowbook.coupons.model.NbCouUser;
import com.nowbook.coupons.service.CouponsNbService;
import com.nowbook.coupons.service.NbCouOrderItemService;
import com.nowbook.exception.JsonResponseException;
import com.nowbook.image.ImageServer;
import com.nowbook.image.exception.ImageUploadException;
import com.nowbook.item.model.Item;
import com.nowbook.item.model.Sku;
import com.nowbook.item.service.ItemService;
import com.nowbook.redis.utils.JedisTemplate;
import com.nowbook.restful.controller.testPostOrder.bin.demo.src.util.MD5;
import com.nowbook.restful.dto.NbResponse;
import com.nowbook.restful.dto.NbUserDto;
import com.nowbook.restful.dto.SimpleOrderInfo;
import com.nowbook.restful.event.ThirdRegisterEvent;
import com.nowbook.restful.model.token.JwtSettings;
import com.nowbook.restful.model.token.JwtTokenFactory;
import com.nowbook.restful.security.jwt.JwtAuthenticationToken;
import com.nowbook.restful.service.NeusoftHelperService;
import com.nowbook.restful.util.DESUtil;
import com.nowbook.restful.util.MobileDESUtil;
import com.nowbook.restful.util.NSSessionUID;
import com.nowbook.restful.util.Signatures;
import com.nowbook.rlt.code.dto.DiscountAndUsage;
import com.nowbook.rlt.code.model.CodeUsage;
import com.nowbook.rlt.code.service.ActivityBindService;
import com.nowbook.rlt.code.service.ActivityCodeService;
import com.nowbook.rlt.code.service.CodeUsageService;
import com.nowbook.rlt.grid.service.GridService;
import com.nowbook.rlt.presale.service.PreSaleService;
import com.nowbook.rlt.settle.service.SettlementService;
import com.nowbook.sdp.model.*;
import com.nowbook.sdp.service.DistributionsService;
import com.nowbook.sdp.service.UserBankService;
import com.nowbook.sdp.service.UserLevelService;
import com.nowbook.sdp.service.UserWalletService;
import com.nowbook.session.AFSession;
import com.nowbook.session.AFSessionManager;
import com.nowbook.shop.service.ShopService;
import com.nowbook.sms.SmsService;
import com.nowbook.third.model.token.DeviceType;
import com.nowbook.third.model.token.JwtToken;
import com.nowbook.third.service.UserPaymentTokenService;
import com.nowbook.trade.dto.FatOrder;
import com.nowbook.trade.dto.RichOrderBuyerView;
import com.nowbook.trade.model.Order;
import com.nowbook.trade.model.OrderItem;
import com.nowbook.trade.model.UserCart;
import com.nowbook.trade.model.UserTradeInfo;
import com.nowbook.trade.service.CartService;
import com.nowbook.trade.service.OrderQueryService;
import com.nowbook.trade.service.OrderWriteService;
import com.nowbook.trade.service.UserTradeInfoService;
import com.nowbook.unionpay.acp.sdk.AcpService;
import com.nowbook.unionpay.acp.sdk.LogUtil;
import com.nowbook.unionpay.acp.sdk.SDKConfig;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import com.nowbook.user.dto.UserProfileDto;
import com.nowbook.user.model.LoginInfo;
import com.nowbook.user.model.LoginType;
import com.nowbook.user.model.User;
import com.nowbook.user.model.UserExtra;
import com.nowbook.user.service.AccountService;
import com.nowbook.user.service.TokenService;
import com.nowbook.user.service.UserExtraService;
import com.nowbook.user.service.UserProfileService;
import com.nowbook.user.util.LoginInfoUtil;
import com.nowbook.user.util.RedisKeyUtils;
import com.nowbook.web.controller.api.CaptchaGenerator;
import com.nowbook.web.controller.api.userEvent.LoginEvent;
import com.nowbook.web.controller.api.userEvent.LogoutEvent;
import com.nowbook.web.controller.api.userEvent.UserEventBus;
import com.nowbook.web.controller.api.userEvent.UserProfileEvent;
import com.nowbook.web.controller.api.validator.SmsCountValidator;
import com.nowbook.web.misc.MessageSources;
import com.nowbook.weixin.weixin4j.*;
import com.nowbook.weixin.weixin4j.http.HttpsClient;
import com.nowbook.weixin.weixin4j.http.OAuth2Token;
import com.nowbook.weixin.weixin4j.pay.*;
import com.nowbook.weixin.weixin4j.token.service.RefurbishAccessTokenService;
import com.nowbook.weixin.weixin4j.util.IpUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.nowbook.common.utils.Arguments.*;
import static com.nowbook.user.util.UserVerification.active;

/**
 * Date: 3/26/14
 * Time: 15:00
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */

@Controller
@Slf4j
@RequestMapping(value = "/api/extend/user")
public class NSUsers {

    public final static JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();
    private static final Pattern mobilePattern = Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");
    private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private final AFSessionManager sessionManager = AFSessionManager.instance();
    private final Random random = new Random();
    private final Splitter splitter = Splitter.on('@').trimResults();
    @Autowired
    OrderWriteService orderWriteService;
    @Autowired
    OrderQueryService orderQueryService;
    @Autowired
    GridService gridService;
    @Autowired
    CartService cartService;
    @Autowired
    PreSaleService preSaleService;
    @Autowired
    ActivityBindService activityBindService;
    @Autowired
    DistributionsService distributionsService;
    @Autowired
    private NeusoftHelperService neusoftHelperService;
    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private CaptchaGenerator captchaGenerator;
    @Autowired
    private AccountService<User> accountService;
    @Autowired
    private MessageSources messageSources;
    @Autowired
    private SmsService smsService;
    @Autowired
    private UserTradeInfoService userTradeInfoService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private UserLevelService userLevelService;
    @Autowired
    private UserWalletService userWalletService;
    @Autowired
    private UserBankService userBankService;
    @Autowired
    private RefurbishAccessTokenService refurbishAccessTokenService;
    @Autowired
    private UserPaymentTokenService userPaymentTokenService;
    @Autowired
    private TokenService tokenService;

    @Autowired
    protected JedisTemplate template;
    @Autowired
    private JwtSettings jwtSettings;

    @Value("#{app.restkey}")
    private String key;
    @Value("#{app.mainSite}")
    private String mainSite;
    @Value("#{app.domain}")
    private String domain;
    @Value("#{app.alipayRefundSuffix}")
    private String notifyUrl;
    @Value("#{app.shopUrl}")
    private String shopUrl;
    @Value("#{app.distributionUrl}")
    private String distributionUrl;
    @Autowired
    private UserEventBus userEventBus;
    @Autowired
    private SmsCountValidator smsCountValidator;
    @Autowired
    private CodeUsageService codeUsageService;
    @Autowired
    private ActivityCodeService activityCodeService;
    @Autowired
    private CouponsNbService couponsNbService;

    @Autowired
    private NbCouOrderItemService nbCouOrderItemService;

    @Autowired
    public JedisTemplate jedisTemplate;

    @Autowired
    private UserExtraService userExtraService;

    @Autowired
    private ImageServer imageServer;
    @Autowired
    private ItemService itemService;

    @Value("#{app.imageBaseUrl}")
    private String imageBaseUrl;
    @Autowired
    private JwtTokenFactory tokenFactory;

    @Value("#{app.tokenSigningKey}")
    private String tokenSigningKey;

    @Value("#{app.alipayAppId}")
    private String alipayAppId;

    @Value("#{app.alipayAppPrivateKey}")
    private String alipayAppPrivateKey;

    @Value("#{app.alipayAppPublicKey}")
    private String alipayAppPublicKey;

    @Value("#{app.alipayBody}")
    private String alipayBody;

    @Value("#{app.alipaySubject}")
    private String alipaySubject;

    @Value("#{app.alipayNotifyUrl}")
    private String alipayNotifyUrl;

    @Value("#{app.alipayCharset}")
    private String alipayCharset;

    @Value("#{app.wxMchKey}")
    private String wxMchKey;

    @Value("#{app.wxMchId}")
    private String wxMchId;

    @Value("#{app.wxAppID}")
    private String wxAppID;

    @Value("#{app.wxAppSecret}")
    private String wxAppSecret;

    @Value("#{app.wxNotifyUrl}")
    private String wxNotifyUrl;

    @Value("#{app.wxBody}")
    private String wxBody;

    @Value("#{app.acpsdkMerId}")
    private String acpsdkMerId;

    @Value("#{app.acpsdkPayUrl}")
    private String acpsdkPayUrl;

    @Autowired
    private SettlementService settlementService;

    private final static HashFunction sha512 = Hashing.sha512();

    @RequestMapping(value = "/getToken", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Map<String, String>> getToken(@RequestParam("openId") String openId, HttpServletRequest request) {
        Response<Long> userGet = accountService.findByOpenId(openId);
        NbResponse<Map<String, String>> result = new NbResponse<Map<String, String>>();
        if (userGet.getResult() != null) {
            Long userId = userGet.getResult();
            Map<String, String> data = new HashMap<String, String>();
            SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = SDF.format(new Date());
            String token = getEncryptedToken(userId, timestamp, openId);

            data.put("timestamp", timestamp);
            data.put("token", token);
            data.put("userId", userId.toString());
            result.setResult(data);
            return result;
        } else {
            return null;
        }
    }

    /**
     * 微信无密登录接口
     * <p>
     * <p>
     * //     * @param channel       渠道, 必填
     *
     * @param uid       用户id, 必填
     * @param timestamp 时戳,必填
     * @param token     登录令牌,必填
     *                  //     * @param sign          签名,必填
     * @return 登录成功
     */
    @RequestMapping(value = "/weixin/login", method =
            RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<String> loginWithNoPass(
//            @RequestParam(value = "channel") String channel,
            @RequestParam("userId") Long uid,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("token") String token,
            @RequestParam("openId") String openId,
//                                               @RequestParam("sign") String sign,
            HttpServletRequest request) {
        NbResponse<String> result = new NbResponse<String>();

        try {
            // 校验登录参数
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            checkArgument(notNull(uid), "uid.can.not.be.empty");
            checkArgument(notNull(timestamp), "timestamp.can.not.be.empty");
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");
            DateTime date = DFT.parseDateTime(timestamp);
//
            // 校验时戳超时或失效
            checkState(Minutes.minutesBetween(date, DateTime.now()).isLessThan(Minutes.minutes(5)), "timestamp.ahead.or.expired");


            // 校验是否微信合法用户
//            User user = getUser(uid);
//            Integer thirdPartyType = user.getThirdPartType();
//            checkState(equalWith(User.ThirdPartType.WEIXIN.value(), thirdPartyType), "third.party.account.type.incorrect");
//
//            String thirdPartyId = user.getThirdPartId();
//            checkState(notEmpty(thirdPartyId), "third.party.id.empty");
//
//            // 校验登录令牌
//            String expected = getEncryptedToken(uid, timestamp, thirdPartyId);
//            checkState(equalWith(token, expected), "third.party.token.mismatch");
//
//            // 持久化用户session
//            request.getSession().setAttribute(CommonConstants.SESSION_USER_ID, user.getId());
//            Weixin weixin = new Weixin();
//            String appId = Configuration.getOAuthAppId();
//            String secret = Configuration.getOAuthSecret();
//            Response<AccessToken> atResult= refurbishAccessTokenService.selectAccessToken();
//            if(atResult.getResult() == null){
//                weixin.login(appId, secret);
//                AccessToken accessToken = new AccessToken();
//                accessToken.setAccess_token(weixin.getOAuthToken().getAccess_token());
//                accessToken.setUpdated_at(new Date());
//                refurbishAccessTokenService.creatAccessToken(accessToken);
//            }else{
//                AccessToken accessTokenResult = atResult.getResult();
//                String access_token = accessTokenResult.getAccess_token();
//                Long updated_at = accessTokenResult.getUpdated_at().getTime();
//                weixin.init(access_token,appId,secret,7200,updated_at);
//                weixin.login(appId, secret);
//                if(!atResult.getResult().getAccess_token().equals(weixin.getOAuthToken().getAccess_token())){
//                    refurbishAccessTokenService.updateAccessToken(weixin.getOAuthToken().getAccess_token());
//                }
//            }
//            WxUser u = weixin.getUserInfo(openId);
//            String headimgurl = u.getHeadimgurl();
//            User users = new User();
//            users.setId(user.getId());
//            users.setAvatar(headimgurl);
//            accountService.updateUser(users);
//
//
//            result.setResult(user.getName());
//            result.setSessionId(request);

        } catch (IllegalArgumentException e) {
//            log.error("fail to login with channel:{}, uid:{}, timestamp:{}, token:{}, sign:{}, error:{}",
//                    channel, uid, timestamp, token, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
//            log.error("fail to login with channel:{}, uid:{}, timestamp:{}, token:{}, sign:{}, error:{}",
//                    channel, uid, timestamp, token, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
//            log.error("fail to login with channel:{}, uid:{}, timestamp:{}, token:{}, sign:{}, cause:{}",
//                    channel, uid, timestamp, token, sign, Throwables.getStackTraceAsString(e));
            e.printStackTrace();
            result.setError(messageSources.get("user.login.fail"));
        }

        return result;
    }

    private User getUser(Long uid) {
        Response<User> userQueryResult = accountService.findUserById(uid);
        checkState(userQueryResult.isSuccess(), userQueryResult.getError());
        return userQueryResult.getResult();
    }

    private String getEncryptedToken(Long uid, String timestamp, String thirdPartyId) {
        Map<String, Object> mappedToken = Maps.newTreeMap();
        mappedToken.put("uid", uid);
        mappedToken.put("timestamp", timestamp);

        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(mappedToken);

        return Hashing.md5().newHasher()
                .putString(toVerify, Charsets.UTF_8)
                .putString(thirdPartyId, Charsets.UTF_8).hash().toString();
    }

    @RequestMapping(value = "/improved/signup", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<NbUserDto> improvedSignUp(User user,
                                                @RequestParam("password") String password,
                                                @RequestParam(value = "channel") String channel,
                                                @RequestParam(value = "active") String activity,
                                                @RequestParam(value = "source") String from,
                                                @RequestParam("sign") String sign,
                                                @RequestParam(value = "type", defaultValue = "3") Integer loginType,
                                                @RequestParam(value = "token", required = false) String token,
                                                @RequestParam(value = "third", defaultValue = "false") Boolean third,
                                                HttpServletRequest request) {


        NbResponse<NbUserDto> result = new NbResponse<NbUserDto>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            checkArgument(notEmpty(user.getName()), "user.name.can.not.be.empty");
            checkArgument(notEmpty(password), "password.can.not.be.empty");
            Preconditions.checkArgument(Signatures.verify(request, key), "sign.verify.fail");


//            if (third) {  // 第三方登录时候需要token&mobile
//                checkArgument(notEmpty(token) && notEmpty(user.getMobile()));
//                user.setThirdPartId(token);
//                user.setThirdPartType(User.ThirdPartType.WEIXIN.value());
//
//
//                // 如果mobile已经存在的情况下，走微信绑定流程，返回应该是成功的标识
//                User mobileUser = getUserByMobile(user.getMobile());
//                if (notNull(mobileUser)) {   // 若手机已经被注册，则绑定token
//                    mobileUser.setThirdPartId(token);
//                    mobileUser.setThirdPartType(User.ThirdPartType.WEIXIN.value());
//                    // 重新绑定不需要再次加密
//                    mobileUser.setEncryptedPassword(null);
//
//                    Response<Boolean> updatingResult = accountService.updateUser(mobileUser);
//                    checkState(updatingResult.isSuccess(), updatingResult.getError());
//
//                    NbUserDto dto = new NbUserDto(mobileUser.getId(), mobileUser.getName(),
//                            mobileUser.getMobile(), mobileUser.getEmail(), token);
//                    result.setResult(dto);
//                    return result;
//                }
//
//            } else {  // 非第三方需要邮箱和手机至少有一个
//                checkArgument(notEmpty(user.getEmail()) || notEmpty(user.getMobile()), "email.or.mobile.can.not.empty");
//            }


            // 检测用户是否已注册
            checkArgument(NameValidator.isAllowedUserName(user.getName()), "user.name.duplicate");
            Response<Boolean> existResult = accountService.userExists(user.getName(), LoginType.from(loginType));
            checkState(existResult.isSuccess(), existResult.getError());
            checkState(!existResult.getResult(), "user.name.duplicate");

            // 创建用户
            user.setEncryptedPassword(password);
            user.setType(BaseUser.TYPE.BUYER.toNumber());
            user.setStatus(User.STATUS.NORMAL.toNumber());

            Response<Long> registerResult = accountService.createUser(user);
            checkState(registerResult.isSuccess(), registerResult.getError());
            Long userId = registerResult.getResult();

            // 异步记录注册统计信息
            userEventBus.post(new ThirdRegisterEvent(userId, user.getName(), channel, activity, from));
            NbUserDto dto = new NbUserDto(userId, user.getName(), user.getMobile(), user.getEmail(), token);
            result.setResult(dto);

        } catch (IllegalArgumentException e) {
            log.error("fail to sign up with user:{}, channel:{}, active:{}, source:{}, sign:{}, token:{}, third:{}, error:{}",
                    user, channel, activity, from, sign, token, third, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to sign up with user:{}, channel:{}, active:{}, source:{}, sign:{}, token:{}, third:{}, error:{}",
                    user, channel, activity, from, sign, token, third, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to sign up with user:{}, channel:{}, active:{}, source:{}, sign:{}, token:{}, third:{}, error:{}",
                    user, channel, activity, from, sign, token, third, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("user.signup.fail"));
        }

        return result;
    }


    private User getUserByMobile(String mobile) {
        Response<User> userQueryResult = accountService.findUserByMobile(mobile);
        checkState(userQueryResult.isSuccess(), userQueryResult.getError());
        return userQueryResult.getResult();
    }


    /**
     * 新增用户接口
     *用于app登录
     * @param user      包含基本信息的用户对象, 必填
     * @param code      短信验证码, 必填
     * @param password  密码, 必填
     * @param sessionId 会话id, 必填
     *                  //     * @param channel   渠道, 必填
     *                  //     * @param sign      签名
     * @return 注册成功的用户id
     */
    @RequestMapping(value = "/signup", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Long> signUp(User user,
                                   @RequestParam("code") String code,
                                   @RequestParam("password") String password,
                                   @RequestParam("session") String sessionId,
                                   @RequestParam("inviter") String inviter,
                                   @RequestParam(value = "key", required = false) String cart,
                                   HttpServletRequest request, HttpServletResponse response) {
        NbResponse<Long> result = new NbResponse<Long>();

        // 判断邀请人ID是否为空，是否可以成功解析
        if (inviter != null && !inviter.equals("")) {
            String theInviter = DESUtil.decrypt(inviter);
            if (theInviter != null && !theInviter.equals("")) {
                try {
                    // checkArgument(notEmpty(sign), "sign.can.not.be .empty");
                    //  checkArgument(notEmpty(user.getName()), "user.name.can.not.be.empty");
                    // checkArgument(notEmpty(user.getMobile()), "email.or.mobile.can.not.empty");
                    checkArgument(notEmpty(user.getMobile()), "mobile.can.not.be.empty");


                    AFSession session = new AFSession(sessionManager, request, sessionId);
                    String _code = (String) session.getAttribute("code");
                    String expectedCode = splitter.splitToList(_code).get(0);


                    checkArgument(notEmpty(_code), "sms.code.not.presence");
                    checkArgument(equalWith(code, expectedCode), "sms.code.mismatch");
                    session.removeAttribute("code");

                    checkArgument(NameValidator.isAllowedUserName(user.getMobile()), "user.name.duplicate");
                    checkIfMobileUsed(user.getMobile());
                    user.setName(user.getMobile());
                    user.setEncryptedPassword(password);
                    user.setType(BaseUser.TYPE.BUYER.toNumber());
                    user.setStatus(User.STATUS.NORMAL.toNumber());
                    user.setMobile(user.getMobile());
                    Response<Long> uidGet = accountService.createUser(user);
                    checkState(uidGet.isSuccess(), uidGet.getError());

                    // 等级相关
                    UserLevel userLevel = new UserLevel();
                    userLevel.setUserId(uidGet.getResult());
                    userLevel.setLevel(1);
                    userLevel.setLevelUpAt(new Date());
                    userLevel.setInviter(Long.valueOf(theInviter));
                    Response<Boolean> newInitation = userLevelService.initiation(userLevel);
                    checkState(newInitation.isSuccess(), newInitation.getError());

                    // 二维码生成
                    Long id = uidGet.getResult();
                    String sid = DESUtil.encrypt(id.toString());
                    if (sid != null && !sid.equals("")) {
                        String pagePath = template.execute(new JedisTemplate.JedisAction<String>() {
                            @Override
                            public String action(Jedis jedis) {
                                return jedis.hget(RedisKeyUtils.otherConf(), "page_path");
                            }
                        });
                        if (pagePath == null) {
                            pagePath = mainSite;
                        }
                        String content = pagePath + "/mall/share.html?userId=" + sid;
                        String path = System.getProperty("java.io.tmpdir");
                        Map hints = new HashMap();
                        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                        hints.put(EncodeHintType.MARGIN, 1);
                        try {
                            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 400, 400, hints);
                            String uuid = UUID.randomUUID().toString().substring(0, 10);
                            File file = new File(path, uuid + ".png");
                            MatrixToImageWriter.writeToFile(bitMatrix, "png", file);
                            String qrCodeUrl = imageServer.write(file.getName(), file);
                            UserExtra userExtra = new UserExtra();
                            userExtra.setUserId(id);
                            userExtra.setQrCodeUrl(imageBaseUrl + qrCodeUrl);
                            Response<Boolean> theResult = userExtraService.updateByUserId(userExtra);
                            checkState(theResult.isSuccess(), theResult.getError());
                        } catch (WriterException e) {
                            log.error("二维码图片生成失败");
                        } catch (IOException e) {
                            log.error("二维码图片保存失败");
                        } catch (ImageUploadException e) {
                            log.error("二维码图片上传失败");
                        }
                    }

                    //用户注册成功之后自动登录
                    session.setAttribute(CommonConstants.SESSION_USER_ID, uidGet.getResult());
                    result.setResult(uidGet.getResult(), key);

                } catch (IllegalArgumentException e) {
                    log.error("fail to signup with user:{}, code:{}, sessionId:{}, error:{}", user, code, sessionId, e.getMessage());
                    result.setError(messageSources.get(e.getMessage()));
                } catch (IllegalStateException e) {
                    log.error("fail to signup with user:{}, code:{}, sessionId:{}, error:{}", user, code, sessionId, e.getMessage());
                    result.setError(messageSources.get(e.getMessage()));
                } catch (Exception e) {
                    log.error("fail to signup with user:{}, code:{}, sessionId:{}", user, code, sessionId, e);
                    result.setError(messageSources.get(e.getMessage()));
                }
            }
        }

        return result;
    }


    /**
     * 更新用户资料
     *
     * @param userExtra 用户资料, 必填
     * @return 用户id
     */
    @RequestMapping(value = "/update-profile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Boolean> updateUser(UserExtra userExtra,
                                          HttpServletRequest request) {
        NbResponse<Boolean> result = new NbResponse<Boolean>();
        BaseUser baseUser=new BaseUser();
        try {
            baseUser=UserUtil.getCurrentUser();
            userExtra.setUserId(baseUser.getId());
            Response<Boolean> userExtraResult = userExtraService.updateByUserId(userExtra);
            checkState(userExtraResult.isSuccess(), userExtraResult.getError());
            result.setResult(true);
        } catch (IllegalArgumentException e) {
            log.error("fail to update user profile with userExtra:{},   error:{}",
                    userExtra, e.getMessage());
            result.setError(messageSources.get("user.profile.update.fail"));
        } catch (IllegalStateException e) {
            log.error("fail to update user profile with userExtra:{}, error:{}",
                    userExtra, e.getMessage());
            result.setError(messageSources.get("user.profile.update.fail"));
        } catch (Exception e) {
            log.error("fail to update user profile with userExtra:{},",
                    userExtra, e);
            result.setError(messageSources.get("user.profile.update.fail"));
        }
        return result;
    }

    /**
     * @description: 上传头像
     * @author dpzh
     * @create 2017/7/31 16:31
//     * @param fileName  图片名带后缀  例：1.jpg
     * @param file  图片文件
     * @return: 完整的图片url
     **/
    @RequestMapping(value = "/update-userAvatar", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<String> updateUserAvatar(HttpServletRequest request){
        NbResponse<String> result = new NbResponse<String>();
        BaseUser baseUser=new BaseUser();
        try {
            baseUser=UserUtil.getCurrentUser();
            MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
            MultipartFile file=req.getFile("file");
            String fileName=file.getOriginalFilename();
            Response<String> avatar=userExtraService.uploadUserAvatar(fileName,file,baseUser.getId());
            result.setResult(avatar.getResult());
        } catch (Exception e) {
            log.error("Avatar upload userId:{}", baseUser.getId(), e);
            result.setError(messageSources.get("user.avatar.update.fail"));
        }
        return result;
    }




    @RequestMapping(value = "/sms", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public  NbResponse<Map<String, Object>> sendSms(@RequestParam("mobile") String mobile, HttpServletRequest request) {
        NbResponse<Map<String, Object>> resultMap=new  NbResponse<Map<String, Object>>();
        if (mobilePattern.matcher(mobile).matches()) {
            HttpSession session = request.getSession();
            String activateCode = (String) session.getAttribute("code");
            Map<String, Object> results = new HashMap<String, Object>();
            Response<Boolean> result = new Response<Boolean>();
            result.setSuccess(false);
            result.setResult(false);
            if (!Strings.isNullOrEmpty(activateCode)) {//判断是否需要重新发送激活码
                List<String> parts = splitter.splitToList(activateCode);
                long sendTime = Long.parseLong(parts.get(1));
                if (System.currentTimeMillis() - sendTime < TimeUnit.MINUTES.toMillis(1)) { //
                    throw new JsonResponseException(500, "1分钟内只能获取一次验证码");
                } else {
                    if (smsCountValidator.check(mobile)) {
                        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
//                        String message = messageSources.get("sms.templates.register", code);
                        String message = "{\"code\":\"" + code + "\"}";
                        session.setAttribute("code", code + "@" + System.currentTimeMillis() + "@" + mobile);
//                        result = smsService.sendSingle("000000", mobile, "SMS_4040003", message);
                        result = smsService.sendSingle("000000", mobile, "SMS_34290479", message);
                    } else {
                        throw new JsonResponseException(500, messageSources.get("sms.send.limit"));
                    }
                }
            } else { //新发送激活码
                if (smsCountValidator.check(mobile)) {
                    String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
                    //String message = messageSources.get("sms.templates.active", code);
                    String message = "{\"code\":\"" + code + "\"}";
                    session.setAttribute("code", code + "@" + System.currentTimeMillis() + "@" + mobile);
//                    result = smsService.sendSingle("000000", mobile, "SMS_4040003", message);
                    result = smsService.sendSingle("000000", mobile, "SMS_34290479", message);
                } else {
                    throw new JsonResponseException(400, messageSources.get("sms.send.limit"));
                }
            }
            if (!result.isSuccess()) {
                log.error("send sms single fail, cause:{}", result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
            results.put("code", (session.getAttribute("code")));
            results.put("result", result);
            results.put("sessionId", session.getId());
            resultMap.setResult(results);

        } else {
            throw new JsonResponseException(400, "错误的手机号码");
        }

        return resultMap;
    }

    /**
     * 获取用户信息
     *
     //     * @param uid       用户id, 必填
     //     * @param sessionId 会话id, 必填
     *                  //     * @param channel       渠道, 必填
     *                  //     * @param sign          签名, 必填
     * @return 用户信息
     */
    @RequestMapping(value = "/userInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<UserProfileDto> userInfo(
//                                                @PathVariable Long uid,
//                                               @RequestParam("session") String sessionId,
//                                                  @RequestParam("channel") String channel,
//                                                  @RequestParam("sign") String sign,
            HttpServletRequest request) {
        NbResponse<UserProfileDto> result = new NbResponse<UserProfileDto>();
        BaseUser baseUser=new BaseUser();
        try {

//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            Response<Long> idGet = NSSessionUID.checkLogin(session, uid);
//            checkState(idGet.isSuccess(), idGet.getError());

            baseUser = UserUtil.getCurrentUser();
            Response<UserProfileDto> profileGet = userProfileService.findUserProfileByUser(baseUser);
            checkState(profileGet.isSuccess(), profileGet.getError());

//            result.setSessionId(request);
            result.setResult(profileGet.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to get userInfo with userId:{}, error:{}",baseUser.getId(), e.getMessage());
            result.setError(messageSources.get("user.profile.get.fail"));
        } catch (Exception e) {
            log.error("fail to get userInfo with userId:{}",baseUser.getId(), e);
            result.setError(messageSources.get("user.profile.get.fail"));
        }
        return result;
    }


    /**
     * 获取验证码图片
     *
     * @param channel 渠道, 必填
     * @param sign    签名, 必填
     * @return 图片的BASE64
     */
    @RequestMapping(value = "/captcha", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<String> captcha(@RequestParam("channel") String channel,
                                      @RequestParam("sign") String sign,
                                      HttpServletRequest request) {
        NbResponse<String> result = new NbResponse<String>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            byte[] data = captchaGenerator.captcha(request.getSession());
            result.setResult(BaseEncoding.base64().encode(data), key);

        } catch (IllegalArgumentException e) {
            log.error("fail to get captcha with channel:{}, sign:{}", channel, sign, e.getMessage());
            result.setError(messageSources.get("captcha.get.fail"));
        } catch (Exception e) {
            log.error("fail to get captcha with channel:{}, sign:{}", channel, sign, e.getMessage());
            result.setError(messageSources.get("captcha.get.fail"));
        }
        return result;
    }

    @RequestMapping(value = "/{uid}/openId", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String openId(@PathVariable Long uid, @RequestParam("openId") String openId, @RequestParam("session") String sessionId, HttpServletRequest request) {
        checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");

        AFSession session = new AFSession(sessionManager, request, sessionId);
        Response<Long> idGet = NSSessionUID.checkLogin(session, uid);
        checkState(idGet.isSuccess(), idGet.getError());
        Weixin weixin = new Weixin();
        //1.需要先登录
        String appId = Configuration.getOAuthAppId();
        String secret = Configuration.getOAuthSecret();
        //2.登录微信，获取Access_Token
        try {
            weixin.login(appId, secret);
            WxUser u = weixin.getUserInfo(openId);
            String headimgurl = u.getHeadimgurl();
            User user = new User();
            user.setId(uid);
//            user.setAvatar(headimgurl);
//            user.setThirdPartType(3);
//            user.setThirdPartId(openId);
            accountService.updateUser(user);
            return null;
        } catch (WeixinException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 用户登录
     *
     * @param loginId  登录凭证(登录名|邮箱|手机), 必填
     * @param password 密码, 必填
     * @param type     登录类型 1:邮箱 2:手机 3:登录名
     *                 //     * @param sign      签名, 必填
     * @return 用户id
     */
    @RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<JwtToken> login(@RequestParam("loginId") String loginId,
                                      @RequestParam("password") String password,
                                      @RequestParam("deviceId") String deviceId,
                                      @RequestParam("deviceType") Integer deviceType,
                                      @RequestParam(value = "type", defaultValue = "1") Integer type,
                                      @RequestParam(value = "key", required = false) String key,
                                      @RequestParam(value = "thirdPartId", required = false) String thirdPartId,
                                      @RequestParam(value = "thirdPartType", required = false) Integer thirdPartType,
//                                     @RequestParam("sign") String sign,
                                      HttpServletRequest request, HttpServletResponse response) {

        NbResponse<JwtToken> result = new NbResponse<JwtToken>();

        try {
            //  checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            //  checkArgument(Signatures.verify(request, key), "sign.verify.fail");
            checkArgument(notEmpty(loginId), "login.id.can.not.be.empty");
            checkArgument(notEmpty(deviceId), "device.id.can.not.be.empty");
            DeviceType devType = DeviceType.from(type);
            checkArgument(notNull(devType), "device.type.can.not.be.empty");
            LoginType loginType = LoginType.from(type);
            checkArgument(notNull(loginType), "incorrect.login.type");


            Response<User> loginResult = accountService.userLogin(loginId, loginType, password);
            checkState(loginResult.isSuccess(), loginResult.getError());
            User user = loginResult.getResult();
           // request.getSession().setAttribute(CommonConstants.SESSION_USER_ID, user.getId());
            LoginEvent loginEvent = new LoginEvent(user.getId(), request, response);
            userEventBus.post(loginEvent);
            //JwtToken token=tokenFactory.createResponseToken(loginId, deviceId, deviceType);
            //result.setSessionId(request);
            // result.setResult(token);


            if (thirdPartId != null && !thirdPartId.equals("") && thirdPartId != "") {
                Weixin weixin = new Weixin();
                //1.需要先登录
                String appId = Configuration.getOAuthAppId();
                String secret = Configuration.getOAuthSecret();
                //2.登录微信，获取Access_Token

                weixin.login(appId, secret);
                WxUser u = weixin.getUserInfo(thirdPartId);
                String headimgurl = u.getHeadimgurl();
                User users = new User();
                users.setId(user.getId());
//                users.setAvatar(headimgurl);
//                users.setThirdPartType(thirdPartType);
//                users.setThirdPartId(thirdPartId);
                accountService.updateUser(users);
            }
            if (key != null) {
                cartService.merge(key, user.getId());
            }
        } catch (IllegalArgumentException e) {
            log.error("fail to login with loginId:{}, type:{}, sign:{}, error:{}", loginId, type, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("fail to login with loginId:{}, type:{}, sign:{}, error:{}", loginId, type, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (Exception e) {
            log.error("fail to login with loginId:{}, type:{}, sign:{}", loginId, type, e);
            result.setError(messageSources.get("user.login.fail"));

        }
        return result;
    }


    /**
     * 注销用户
     *
     *                  //     * @param channel       渠道, 必填
     *                  //     * @param sign          签名, 必填
     * @return 是否注销成功
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Boolean> logout(
//            @PathVariable Long uid,
//                                      @RequestParam(value = "session", required = false) String sessionId,
//                                         @RequestParam(value = "channel") String channel,
//                                         @RequestParam(value = "sign") String sign,
            HttpServletRequest request,
            HttpServletResponse response) {
        NbResponse<Boolean> result = new NbResponse<Boolean>();
        BaseUser baseUser=new BaseUser();
        try {
            baseUser=UserUtil.getCurrentUser();
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");
//            BaseUser baseUser = new BaseUser();
//            baseUser.setId(((Integer) session.getAttribute(CommonConstants.SESSION_USER_ID)).longValue());
//            session.invalidate();

            //delete login token cookie
            LogoutEvent logoutEvent = new LogoutEvent(baseUser.getId(), request, response);
            userEventBus.post(logoutEvent);
            LoginInfo info= LoginInfoUtil.getLoginInfo();
            tokenService.deleteToken(info);   //从redis上删除登录信息
            result.setResult(Boolean.TRUE);
            return result;
        } catch (IllegalStateException e) {
            log.error("failed to logout user with userId:{}, error:{}", baseUser.getId(), e.getMessage());
            result.setError(messageSources.get("user.logout.fail"));
            return result;
        } catch (Exception e) {
            log.error("failed to logout user with userId:{}", baseUser.getId(), e);
            result.setError(messageSources.get("user.logout.fail"));
            return result;
        }
    }


    /**
     * 此接口暂时先不校验渠道以及签名
     * <p>
     * 判断指定session的用户是否处于登录状态
     *
     * @param sessionId 会话id, 必填
     * @return 登录中的用户id
     */
    @RequestMapping(value = "/check-session", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Long> checkSession(@RequestParam(value = "session") String sessionId,
                                         HttpServletRequest request) {
        NbResponse<Long> result = new NbResponse<Long>();

        try {

            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGet = NSSessionUID.getUserId(session);
            checkState(uidGet.isSuccess(), "user.not.login");
//            result.setSessionId(sessionId);
            result.setResult(uidGet.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to check session with session:{}, error:{}", sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to check session with session:{}", sessionId, e);
            result.setError(messageSources.get("user.session.check.fail"));
        }

        return result;
    }


//    /**
//     * 登录并返回指定的URL地址
//     *
//     * @param url      前台跳转的URL
//     * @param loginId  登录id, 必填
//     * @param password 密码, 必填
//     * @param type     登录类型 1:邮箱 2:手机 3:登录名
//     * @param channel  渠道, 必填
//     * @param sign     签名, 必填
//     * @return 跳转的地址
//     */
//    @RequestMapping(value = "/login-return", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    public String loginReturn(@RequestParam("returnURL") String url,
//                              @RequestParam("loginId") String loginId,
//                              @RequestParam("password") String password,
//                              @RequestParam("type") Integer type,
//                              @RequestParam("channel") String channel,
//                              @RequestParam("sign") String sign,
//                              HttpServletRequest request,
//                              HttpServletResponse response) {
//
//       NbResponse<Long> result = login(loginId, password, type, request, response);
//        String forward = mainSite;
//
//        try {
//
//            if (result.isSuccess()) {
//                forward = url;
//            }
//
//        } catch (Exception e) {
//            log.error("fail to login-return with returnUrl:{}, loginId:{}, type:{}, sign:{}", url, loginId, type, sign, e);
//            result.setError(messageSources.get("user.login.fail"));
//        }
//
//        return forward;
//    }


    /**
     * 注销后跳转的地址
     *
     * @param url           前台跳转的URL
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     * @param uid           用户id, 必填
     * @return 注销成功跳转的URL
     */
//    @RequestMapping(value = "/{uid}/logout-return", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    public String logoutReturn(@RequestParam("returnURL") String url,
//                               @RequestParam("session") String sessionId,
//                               @RequestParam("channel") String channel,
//                               @RequestParam("sign") String sign,
//                               @PathVariable Long uid,
//                               HttpServletRequest request,
//                               HttpServletResponse response) {
//        NbResponse<Boolean> result = logout(uid, sessionId, channel, sign, request, response);
//        String forward = mainSite;
//
//        try {
//            if (result.isSuccess()) {
//                forward = url;
//            }
//
//        } catch (Exception e) {
//            log.error("fail to login-return with url:{}, sessionId:{}, uid:{}", url, sessionId, uid, e);
//            result.setError(messageSources.get("user.login.fail"));
//        }
//
//        return forward;
//    }

    /**
     * 验证用户信息是否重复
     *
     * @param type      验证字段，有name，email，mobile
     * @param content   验证内容
     * @param operation 1为创建时验证，2为修改时验证
     * @return 是否已存在
     */
    @RequestMapping(value = "/verify", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean> verify(@RequestParam("type") Integer type,
                                    @RequestParam("content") String content,
                                    @RequestParam(value = "operation", defaultValue = "1") Integer operation) {
        Response<Boolean> resultResponse = new Response<Boolean>();
        LoginType loginType = LoginType.from(type);
        Long userId = UserUtil.getUserId();
        if (loginType == null) {
            throw new JsonResponseException("unknown login type:" + type);
        }
        if (!Objects.equal(operation, 1) && !Objects.equal(operation, 2)) {
            throw new JsonResponseException("unknown operation");
        }
        Response<User> result = accountService.findUserBy(content, loginType);
        if (Objects.equal(operation, 1)) {
            if (result.isSuccess()) {
                resultResponse.setResult(false);
                return resultResponse;
            }
        } else {
            if (result.isSuccess() && !Objects.equal(result.getResult().getId(), userId)) {
                resultResponse.setResult(false);
                return resultResponse;
            }
        }
        resultResponse.setResult(true);
        return resultResponse;
    }

    /**
     * 获取用户的配送地址
     *
     //         * @param userId       用户id, 必填
     //         * @param sessionId 会话id, 必填
     *                  //     * @param channel       渠道, 必填
     *                  //     * @param sign          签名, 必填
     * @return 用户配送地址列表
     */
    @RequestMapping(value = "/trade-infos", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<List<UserTradeInfo>> userTradeInfos(
//                @RequestParam("userId") Long userId,
//                @RequestParam(value = "session",required = false) String sessionId,
//                                                             @RequestParam("channel") String channels,
//                                                             @RequestParam("sign") String sign,
            HttpServletRequest request) {
        NbResponse<List<UserTradeInfo>> result = new NbResponse<List<UserTradeInfo>>();
        BaseUser user=new BaseUser();
        try {
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            Response<Long> idGet = NSSessionUID.checkLogin(session, userId);
//            checkState(idGet.isSuccess(), idGet.getError());


//            Long userId = idGet.getResult();
            user=UserUtil.getCurrentUser();
            Response<List<UserTradeInfo>> infoGet = userTradeInfoService.findTradeInfosByUserId(user.getId());
            result.setResult(infoGet.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to query trade info with userId:{}, error:{}", user.getId(),  e.getMessage());
            result.setError(messageSources.get("user.trade.info.query.fail"));
        } catch (Exception e) {
            log.error("fail to query trade info with userId:{}",  user.getId(),e);
            result.setError(messageSources.get("user.trade.info.query.fail"));
        }
        return result;
    }


    /**
     * 获取买家购物车
     *
     //     * @param id 用户id
     * @return 购物车列表
     */
    @RequestMapping(value = "/cart", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<List<UserCart>> cartItems(
//            @PathVariable Long id,
//                                                   @RequestParam("channel") String channel,
//                                                   @RequestParam("sign") String sign,
            HttpServletRequest request) {
        NbResponse<List<UserCart>> result = new NbResponse<List<UserCart>>();
        BaseUser user=new BaseUser();
        try {
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            user=UserUtil.getCurrentUser();
            Response<List<UserCart>> cartGet = cartService.getPermanent(new BaseUser(user.getId(), "", 1));
            result.setResult(cartGet.getResult());
        } catch (Exception e) {
            log.error("fail to get user cart with uid:{}", user.getId());
            result.setError(messageSources.get("cart.get.fail"));
        }

        return result;
    }

    /**
     * 快速注册
     *
     * @param phone 手机号
     */
    @RequestMapping(value = "/fast-register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Long> fastRegister(@RequestParam("channel") String channel,
                                         @RequestParam("phone") String phone,
                                         @RequestParam("sign") String sign) {

        NbResponse<Long> result = new NbResponse<Long>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            checkArgument(mobilePattern.matcher(phone).matches(), "incorrect.mobile");

            String i = String.valueOf(random.nextInt());
            int l = i.length();
            String password = i.substring(l - 6, l);

            // start check and set user
            // 检查手机号是否已经被注册
            checkIfMobileUsed(phone);  // throw illegal state exception
            User user = new User();
            user.setName(phone);
            user.setMobile(phone);
            checkArgument(NameValidator.isAllowedUserName(user.getName()), "user.name.duplicate");

            user.setEncryptedPassword(password);
            user.setType(BaseUser.TYPE.BUYER.toNumber());
            user.setStatus(User.STATUS.NORMAL.toNumber());
            // start check and set user

            // start send sms
            Response<Boolean> resultGet = neusoftHelperService.checkMobileSendable(phone);
            checkState(resultGet.isSuccess(), resultGet.getError());

            String passwordSms = smsHelper(password, TYPE.PASSWORD);

            checkState(smsCountValidator.check(phone), "sms.send.limit");
            Response<Boolean> smsSent = smsService.sendSingle("000000", phone, passwordSms);
            checkState(smsSent.isSuccess(), smsSent.getError());

            neusoftHelperService.setMobileSent(phone, password);
            // end send sms


            // create user
            Response<Long> uidGet = accountService.createUser(user);
            checkState(uidGet.isSuccess(), uidGet.getError());
            result.setResult(uidGet.getResult());

        } catch (IllegalArgumentException e) {
            log.error("fail to fast-register with channel:{}, phone:{}, sign:{}, error:{}", channel, phone, sign, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to fast-register with channel:{}, phone:{}, sign:{}, error:{}", channel, phone, sign, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to fast-register with channel:{}, phone:{}, sign:{}", channel, phone, sign, e);
            result.setError("user.register.faster.fail");
        }

        return result;
    }

    /**
     * 忘记密码
     *
     * @param mobile   手机号
     * @param password 新密码
     * @param code     短信验证码
     */
    @RequestMapping(value = "/forget-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Boolean> forgetPassword(@RequestParam("mobile") String mobile,
                                              @RequestParam("password") String password,
                                              @RequestParam("captcha") String code,
                                              @RequestParam("type") String type,
                                              @RequestParam("session") String sessionId,
//                                                 @RequestParam("channel") String channel,
//                                                 @RequestParam("sign") String sign,
                                              HttpServletRequest request) {

        NbResponse<Boolean> result = new NbResponse<Boolean>();

        try {
            checkArgument(notEmpty(mobile), "mobile.can.not.be.empty");
            checkArgument(notEmpty(password), "password.can.not.be.empty");
            checkArgument(notEmpty(code), "code.can.not.be.empty");
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Response<User> userGetResult = accountService.findUserBy(mobile, LoginType.MOBILE);

            checkArgument(userGetResult.isSuccess(), userGetResult.getError());
            User user = userGetResult.getResult();

            //对于未激活过的用户,不允许修改密码
            checkState(notNull(user) && active(user), "user.not.active");
            checkArgument(notEmpty(code), "user.code.can.not.be.empty");


            AFSession session = new AFSession(sessionManager, request, sessionId);
            String temp = (String) session.getAttribute("code");
            checkState(notEmpty(temp), "user.code.not.found");


            List<String> parts = splitter.splitToList(temp);
            String expected = parts.get(0);
            checkState(equalWith(code, expected), "user.code.mismatch");


            //如果匹配了code,则删除在session中的值
            request.getSession().removeAttribute("code");

            Response<Boolean> resetPassResult = accountService.resetPassword(user.getId(), password,type);
            checkState(resetPassResult.isSuccess(), resetPassResult.getError());

            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to reset password with mobile:{}, captcha:{}, error:{}",
                    mobile,code, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("fail to reset password with mobile:{}, captcha:{}, error:{}",
                    mobile,  code, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (Exception e) {
            log.error("fail to reset password with mobile:{}, captcha:{}",
                    mobile,  code, e);
            result.setError(messageSources.get(e.getMessage()));

        }
        return result;

    }


    /**
     * 更改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @RequestMapping(value = "/change-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Boolean> changePassword(@RequestParam("old") String oldPassword,
                                              @RequestParam("new") String newPassword,
                                              @RequestParam("type") String type,
                                              HttpServletRequest request) {
        NbResponse<Boolean> result = new NbResponse<Boolean>();

        BaseUser user=new BaseUser();
        try {
            checkArgument(notEmpty(oldPassword), "user.old.pass.empty");
            checkArgument(notEmpty(newPassword), "user.new.pass.empty");
            checkArgument(notEmpty(type), "user.type.empty");

            user=UserUtil.getCurrentUser();

            Response<Boolean> pwdChangeResult = accountService.changePassword(user.getId(), oldPassword, newPassword,type);
            checkState(pwdChangeResult.isSuccess(), pwdChangeResult.getError());


//            result.setSessionId(request);
            result.setResult(true);
            return result;

        } catch (IllegalArgumentException e) {
            log.error("fail to change with token:{}, error:{} ",  e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to change with token:{}, error:{} ", e.getMessage());
            result.setError("原密码错误");
        } catch (Exception e) {
            log.error("fail to change with token:{} ", e);
            result.setError("更改密码失败");
        }

        return result;
    }


    /**
     * 获取用户订单列表
     *
     //     * @param userId        用户id
     * @param status    订单状态，选填
    //     * @param sessionId 会话id, 必填
     *                  //     * @param channel      渠道, 必填
     *                  //     * @param sign         签名, 必填
     */
    @RequestMapping(value = "/orders", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Paging<RichOrderBuyerView>> orders(
//                                                        @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "orderId", required = false) Long orderId,
//                                                         @RequestParam("session") String sessionId,
//                                                            @RequestParam("channel") String channel,
//                                                            @RequestParam("sign") String sign,
            HttpServletRequest request) {
        NbResponse<Paging<RichOrderBuyerView>> result = new NbResponse<Paging<RichOrderBuyerView>>();
        BaseUser baseUser = new BaseUser();
        try {
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");
            baseUser =UserUtil.getCurrentUser();
//            baseUser.setId(((Integer) session.getAttribute(CommonConstants.SESSION_USER_ID)).longValue());
            Response<Paging<RichOrderBuyerView>> ordersGet = orderQueryService.findByBuyerIdForNS(baseUser, pageNo, size, status, orderId, null);
//            for (RichOrderBuyerView a : ordersGet.getResult().getData()) {
//                Response<Shop> b = shopService.findByUserId(a.getSiteId());
//                a.setShopId(b.getResult().getId());
//            }


            result.setResult(ordersGet.getResult(), key);

        } catch (IllegalArgumentException e) {
            log.error("fail to query orders with userId:{}, pageNo:{}, size:{},  status:{}, error:{}",
                    baseUser.getId(), pageNo, size, status, e.getMessage());
            result.setError(messageSources.get("order.query.fail"));
        } catch (IllegalStateException e) {
            log.error("fail to query orders with userId:{}, pageNo:{}, size:{},  status:{}, error:{}",
                    baseUser.getId(), pageNo, size, status, e.getMessage());
            result.setError(messageSources.get("order.query.fail"));
        } catch (Exception e) {
            log.error("fail to query orders with userId:{}, pageNo:{}, size:{},  status:{}",
                    baseUser.getId(), pageNo, size,  status, e);
            result.setError(messageSources.get("order.query.fail"));
        }
        return result;
    }

    private int changeCouponsOrderItem(String couponsId, int total, Set<Long> ids, Long userId) {
        log.debug("init changeCouponsOrderItem begin ...");
        int resultTotal = total;
        if (!StringUtils.isEmpty(couponsId)) {
            Response<NbCou> nbCouObj = couponsNbService.queryCouponsById(Long.valueOf(couponsId));//根据ID查询对应的对象
            NbCou nbCou;
            if (nbCouObj != null && nbCouObj.isSuccess()) {
                nbCou = nbCouObj.getResult();
                resultTotal = total - nbCou.getAmount();//总额 减去 优惠券面额优惠金额
//                -----------------------------------------------------------------
                List<OrderItem> orderItemsAllList = new ArrayList<OrderItem>();//根据订单信息获取订单明细(产品) 计算产品的优惠百分比

                Iterator<Long> it = ids.iterator();
                while (it.hasNext()) {
                    Long id = it.next();
                    Response<List<OrderItem>> orderItemList = orderQueryService.findSubsByOrderId(id);
                    if (orderItemList.isSuccess()) {
                        orderItemsAllList.addAll(orderItemList.getResult());
                    }
                }

//                for (Long id : ids) {
//                    Response<List<OrderItem>> orderItemList =  orderQueryService.findOrderItemByOrderId(id);
//                    if(orderItemList.isSuccess()){
//                        orderItemsAllList.addAll(orderItemList.getResult());
//                    }
//                }

                List<OrderItem> orderItemsArrayList = new ArrayList<OrderItem>();//根据订单信息获取订单明细(产品) 计算产品的优惠百分比
                int totalCoupons = 0;
                if (!orderItemsAllList.isEmpty()) {
                    Iterator<OrderItem> its = orderItemsAllList.iterator();
                    while (its.hasNext()) {
                        OrderItem orderItem = its.next();
//                        Response<Long>  resultValue =  couponsNbService.checkJoin(orderItem.getItemId());//判断该产品是否参加优惠分配
                        Response<Long> resultValue = couponsNbService.checkJoinAndUser(orderItem.getItemId(), userId);//判断该产品是否参加优惠分配
                        if (resultValue.isSuccess()) {
                            if (Objects.equal(resultValue.getResult(), 1L)) {
                                totalCoupons += orderItem.getFee();
                                orderItemsArrayList.add(orderItem);
                            }
                        }
                    }
                }

                if (!orderItemsArrayList.isEmpty()) {
                    NbCouOrderItem nbCouOrderItem = new NbCouOrderItem();
                    int joinItems = orderItemsArrayList.size();//总共参加优惠产品
                    double freeAmount = 0;//优惠金额 = (产品金额 / 参加优惠的产品总额之和) * 优惠券金额  最后一个产品是前面几个产品剩余优惠
                    int allFreeAmount = 0;//计算总共
                    int orderFreeAmount = 0;//计算订单的优惠价格
                    HashMap<Long, Integer> orderFreeMap = new HashMap<Long, Integer>();
                    for (int i = 0; i < joinItems; i++) {
                        OrderItem orderItem = orderItemsArrayList.get(i);

                        if ((i + 1) == joinItems) {//最后一个产品是前面几个产品剩余优惠
                            freeAmount = nbCou.getAmount() - allFreeAmount;
                        } else {
                            double modeV = Math.round((orderItem.getFee().doubleValue() / Double.valueOf(totalCoupons)) * 100000);
                            freeAmount = Math.round(modeV * nbCou.getAmount() / 100000);
                            allFreeAmount += freeAmount;
                        }

                        int freeAmouts = Double.valueOf(freeAmount).intValue();

                        Long mapKey = orderItem.getOrderId();
                        if (orderFreeMap.containsKey(mapKey)) {
                            Integer freeA = orderFreeMap.get(mapKey);
                            orderFreeMap.put(orderItem.getOrderId(), freeAmouts + freeA);//总订单的优惠金额计算
                        } else {
                            orderFreeMap.put(orderItem.getOrderId(), freeAmouts);//总订单的优惠金额计算
                        }

                        orderItem.setFee(orderItem.getFee() - freeAmouts);
                        orderWriteService.updateOrderItem(orderItem);

                        //用户使用优惠券 的对应产品优惠信息
                        nbCouOrderItem.setCouponsId(Long.valueOf(couponsId));//优惠券Id
                        nbCouOrderItem.setItemId(orderItem.getItemId());//订单明细ID
                        nbCouOrderItem.setOrderId(orderItem.getOrderId());//订单ID
                        nbCouOrderItem.setSkuId(orderItem.getSkuId());//增加 skuid字段用户查询
                        nbCouOrderItem.setUserId(userId);//用户ID
                        nbCouOrderItem.setFreeAmount(BigDecimal.valueOf(freeAmount));//优惠金额

                        nbCouOrderItemService.saveCouOrderItem(nbCouOrderItem);
                    }
                    //用户购买时会进行拆单 需要计算拆单之后该订单的优惠价格 即需要计算该订单下的产品优惠金额
                    if (!orderFreeMap.isEmpty()) {
                        for (Long orderId : orderFreeMap.keySet()) {
                            Integer freeMapAmoutn = Integer.valueOf(orderFreeMap.get(orderId));
                            Response<Order> getOrder = orderQueryService.findById(orderId);
                            if (getOrder.isSuccess()) {
                                Order order = getOrder.getResult();
                                order.setFee(order.getFee() - freeMapAmoutn);
                                orderWriteService.updateOrder(order);
                            }
                        }
                    }
                }
//                -----------------------------------------------------------------
                //修改已使用优惠券信息 couponUse
                nbCou.setCouponUse(nbCou.getCouponUse() + 1);
                couponsNbService.updateNbCou(nbCou);

                Response<NbCouUser> resutUser = couponsNbService.queryCouponsUserBy(userId, Long.parseLong(couponsId));
                if (resutUser.isSuccess()) {
                    NbCouUser nbCouUser = resutUser.getResult();
                    log.info("init nbCouUser end ...coupuons" + nbCouUser.getId());
                    couponsNbService.updateCouponUser(nbCouUser.getId());
                }
            }
        }
        log.debug("init changeCouponsOrderItem end ...coupuons" + resultTotal);
        return resultTotal;
    }

    private void recordCodeUsage(DiscountAndUsage discountAndUsage, Map<Long, Long> sellerIdAndOrderId) {
        Map<Long, CodeUsage> sellerIdAndCodeUsage = discountAndUsage.getSellerIdAndUsage();
        Map<Long, Integer> activityCodeIdAndUsage = discountAndUsage.getActivityCodeIdAndUsage();

        //设置codeUsage的orderId
        List<CodeUsage> creates = Lists.newArrayList();
        for (Long sellerId : sellerIdAndCodeUsage.keySet()) {
            Long orderId = sellerIdAndOrderId.get(sellerId);
            CodeUsage cu = sellerIdAndCodeUsage.get(sellerId);
            cu.setOrderId(orderId);
            creates.add(cu);
        }
        //调用批量创建接口
        Response<Boolean> batchCreateR = codeUsageService.batchCreateCodeUsage(creates);
        if (!batchCreateR.isSuccess()) {
            log.error("fail to batch create codeUsage by creates={}, error code:{}", creates, batchCreateR.getError());
        }
        //调用批量更新优惠码使用数量的接口
        Response<Boolean> updateR = activityCodeService.batchUpdateByIds(activityCodeIdAndUsage);
        if (!updateR.isSuccess()) {
            log.error("fail to batch update activityCode usage by map={}, error code={},",
                    activityCodeIdAndUsage, updateR.getError());
        }

    }

    private String smsHelper(String sms, TYPE type) {
        switch (type) {
            case PASSWORD:
                return "您的初始登录密码是：" + sms + "，请尽快更换。【艾麦麦商场】";
            case CODE:
                return "您的验证码是：" + sms + "。【艾麦麦商场】";
            default:
                return null;
        }
    }

    private void checkIfMobileUsed(String phone) throws IllegalStateException {
        // 检查用户是否被注册
        checkState(notNull(phone) || notEmpty(phone), "user.mobile.duplicate"); // now empty as duplicate
        Response<User> userGetResult = accountService.findUserByMobile(phone);
        checkState(userGetResult.isSuccess(), userGetResult.getError());
        checkState(isNull(userGetResult.getResult()), "user.mobile.duplicate");
    }

    @RequestMapping(value = "/weixin/getOpenId", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<String> WxLogin(@RequestParam("code") String code, HttpServletRequest request) {
        NbResponse<String> result = new NbResponse<String>();
        try {
            String appId = Configuration.getOAuthAppId();
            String secret = Configuration.getOAuthSecret();
            OAuth2 oAuth2 = new OAuth2();
            OAuth2Token oAuth2Token = oAuth2.login(appId, secret, code);
            String openId = oAuth2Token.getOpenid();
            result.setResult(openId);
        } catch (WeixinException e) {
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/change_mobile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String changeMobile(@RequestParam("mobile") String mobile,
                               @RequestParam("password") String password,
                               @RequestParam("captcha") String code, HttpServletRequest request) {
        Long userId = UserUtil.getUserId();

        HttpSession session = request.getSession();
        String temp = (String) session.getAttribute("code");
        if (Strings.isNullOrEmpty(temp)) {
            throw new JsonResponseException(500, "验证码不匹配");
        }

        List<String> parts = splitter.splitToList(temp);
        String expected = parts.get(0);
        if (!Objects.equal(expected, code)) {
            throw new JsonResponseException(500, "验证码不匹配");
        }
        //如果匹配了code,则删除在session中的值
        request.getSession().removeAttribute("code");
        Response<Boolean> cr = accountService.changeMobile(userId, mobile, password);
        if (!cr.isSuccess()) {
            log.error("failed to change mobile for user(id={}),error code:{}", userId, cr.getError());
            throw new JsonResponseException(500, messageSources.get(cr.getError()));
        }
        UserProfileEvent event = new UserProfileEvent(userId);
        event.setMobile(mobile);
        userEventBus.post(event);
        return "ok";

    }

    @RequestMapping(value = "/makeOrder", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Object> makeOrder (@RequestParam("tradeId") Long tradeId,
                                         @RequestParam("data") String data,
                                         @RequestParam(value = "couponsId", required = false) String couponsId){
        NbResponse<Object> result = new NbResponse<Object>();
        Map<String,Object> map = new HashMap<String, Object>();
        BaseUser baseUser = new BaseUser();
        baseUser=UserUtil.getCurrentUser();
        List<SimpleOrderInfo> simpleOrderInfo = Lists.newArrayList();
        try {
            List<FatOrder> fatOrders = JSON_MAPPER.fromJson(data, JSON_MAPPER.createCollectionType(List.class, FatOrder.class));
            checkState(!CollectionUtils.isEmpty(fatOrders), "data.de.serialize.fail");

            for(FatOrder fatOrder : fatOrders){
                Map<Long, Integer> skuIdAndQuantity = fatOrder.getSkuIdAndQuantity();
                for (Long skuId : skuIdAndQuantity.keySet()) {
                    Integer quantity = skuIdAndQuantity.get(skuId);
                    Response<Sku> skuR = itemService.findSkuById(skuId);
                    Sku sku = skuR.getResult();
                    if(sku == null){
                        result.setError("未找到所选商品。");
                        return result;
                    }
                    Response<Item> ir = itemService.findById(sku.getItemId());
                    if(ir.getResult() == null){
                        result.setError("未找到所选商品。");
                        return result;
                    }
                    if(!ir.getResult().getStatus().equals(1)){
                        result.setError(ir.getResult().getName()+"已下架，请去购物车里清理。");
                        return result;
                    }
                    if (sku.getStock() < quantity) {
                        result.setError(ir.getResult().getName()+"的库存暂时不足，请去购物车里清理。");
                        return result;
                    }
                }
            }
            Response<User> userR = accountService.findUserById(baseUser.getId());
            if (!userR.isSuccess()) {
                log.error("fail to find user by id={}, error code:{}", baseUser.getId(), userR.getError());
                result.setError(messageSources.get(userR.getError()));
                return result;
            }
            User user = userR.getResult();

            //计算优惠价
            Response<DiscountAndUsage> discountAndUsageR = activityBindService.processOrderCodeDiscount(fatOrders, user);
            if (!discountAndUsageR.isSuccess()) {
                log.error("fail to process order code discount. fatOrders={}, buyerId={},error code:{}",
                        fatOrders, user.getId(), discountAndUsageR.getError());
                throw new JsonResponseException(500, messageSources.get(discountAndUsageR.getError()));
            }
            DiscountAndUsage discountAndUsage = discountAndUsageR.getResult();

            Response<Map<Long, Long>> orderResult = orderWriteService.create(baseUser.getId(), tradeId, fatOrders, discountAndUsage.getSkuIdAndDiscount(), null);
            checkState(orderResult.isSuccess(), orderResult.getError());

            //使用完优惠券后记录使用情况
            recordCodeUsage(discountAndUsage, orderResult.getResult());

            // 返回 id 和 应付金额
            Map<Long, Long> sellerIdAndOrderId = orderResult.getResult();
            List<Long> orderIds = Lists.newArrayListWithCapacity(sellerIdAndOrderId.keySet().size());
            for (Long sellerId : sellerIdAndOrderId.keySet()) {
                orderIds.add(sellerIdAndOrderId.get(sellerId));
            }

            Response<List<Order>> orderGet = orderQueryService.findByIds(orderIds);
            checkState(orderResult.isSuccess(), orderGet.getError());

            List<Order> orders = orderGet.getResult();


            int total = 0;
            Set<Long> ids = Sets.newHashSet();
            Integer isBalance=1;
            for (Order order : orders) {
                SimpleOrderInfo info = new SimpleOrderInfo();
                info.setExpress(order.getDeliverFee());
                info.setId(order.getId());

                ids.add(order.getId());
                total += order.getFee();

                info.setTotal(order.getFee());
                simpleOrderInfo.add(info);

                if(order.getIsBalance()==0){
                    isBalance=0;
                }
            }

            //ids 为拆分之后的订单ID add by cwf
            //获取是否选择了优惠券信息
//            if (!StringUtils.isEmpty(couponsId) && !couponsId.equals("-1")) {
//                total = changeCouponsOrderItem(couponsId, total, ids, baseUser.getId());
//            }
            map.put("isBalance",isBalance);
            map.put("total",total);
            map.put("orderIds",ids);
            result.setResult(map);
        } catch (IllegalArgumentException e) {
            log.error("fail to submit order with userId:{}, tradeId:{}, data:{}, error:{}",
                    baseUser.getId(), tradeId,data, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("fail to submit order with userId:{}, tradeId:{}, data:{}, error:{}",
                    baseUser.getId(), tradeId,  data, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (Exception e) {
            log.error("fail to submit order with userId:{}, tradeId:{}, data:{}",
                    baseUser.getId(), tradeId,  data, e);
            result.setError(messageSources.get(e.getMessage()));

        }
        return result;
    }


    @RequestMapping(value = "/pay", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Object> pay (@RequestParam("type") Integer type,
                                   @RequestParam("payType") Integer payType,
                                   @RequestParam(value ="money", required = false) Long money,
                                   @RequestParam(value ="orderId", required = false) String orderId,
                                   @RequestParam(value ="level", required = false) Integer level,
                                   @RequestParam("deviceId") String deviceId,
                                   HttpServletRequest request){
        NbResponse<Object> result = new NbResponse<Object>();
        BaseUser baseUser=UserUtil.getCurrentUser();
//        LoginInfo loginInfo = LoginInfoUtil.getLoginInfo();
//        if(!deviceId.equals(loginInfo.getDeviceId())){
//            result.setError("user not login");
//            return result;
//        }

        List<SimpleOrderInfo> simpleOrderInfo = Lists.newArrayList();
        Long total = 0L;
        Map<String,String> tradeNo = new HashMap<String, String>();
        //验证数据
        if(type.equals(1)){
            //购物
            if(orderId == null){
                result.setError("没有找到订单");
                return result;
            }
            for(String oId : orderId.split(",")){
                Response<Order> order = orderQueryService.findById(Long.valueOf(oId));
                if (order.getResult() == null) {
                    result.setError("订单号有误");
                    return result;
                }
                if (!order.getResult().getStatus().equals(0)) {
                    result.setError("订单状态有误");
                    return result;
                }
                SimpleOrderInfo info = new SimpleOrderInfo();
                info.setExpress(order.getResult().getDeliverFee());
                info.setId(order.getResult().getId());
                info.setTotal(order.getResult().getFee());
                simpleOrderInfo.add(info);
            }
            for(SimpleOrderInfo info : simpleOrderInfo){
                total = total + info.getTotal();
                info.setType(type);
            }
            tradeNo.put("type","1");
            tradeNo.put("orderId",orderId);
        }else if(type.equals(2)){
            //预存款充值
            if(money == null || money<=0){
                result.setError("金额错误");
                return result;
            }
            if(payType<3 || payType>5){
                result.setError("支付方式错误");
                return result;
            }
            Response<Map<String,Object>> mapResponse = userLevelService.selectMyLevel(baseUser.getId());
            if(!mapResponse.isSuccess()){
                result.setError(mapResponse.getError());
                return result;
            }
            UserTeamMemberSelect userTeamMemberSelect =(UserTeamMemberSelect)mapResponse.getResult().get("user");
            Long rechargeFactor = 0L;
            List<Level> levelList = (List<Level>) mapResponse.getResult().get("level");
            for(Level levels : levelList){
                if(levels.getLevel().equals(userTeamMemberSelect.getLevel())){
                    rechargeFactor = levels.getRechargeFactor();
                }
            }
            if(!rechargeFactor.equals(0L)){
                if(money%rechargeFactor !=0){
                    result.setError("充值钱数必须为"+rechargeFactor/100+"元的整数倍");
                    return result;
                }
            }
            total = money;
            tradeNo.put("type","2");
            tradeNo.put("total",total.toString());
            tradeNo.put("userId",baseUser.getId().toString());
        }else if(type.equals(3)){
            //缴费升级
            if(level == null || level>5 || level<1){
                result.setError("用户等级错误");
                return result;
            }
            if(payType<3 || payType>5){
                result.setError("支付方式错误");
                return result;
            }
            UserLevelWait userLevelWait =new UserLevelWait();
            userLevelWait.setIsSelect(1);
            userLevelWait.setUserId(baseUser.getId());
            Response<String> response = userLevelService.applyLevelUp(userLevelWait);
            if(!response.getResult().equals("noSubmitted")){
                result.setError(response.getResult());
                return result;
            }
            Response<Map<String,Object>> mapResponse = userLevelService.selectMyLevel(baseUser.getId());
            if(!mapResponse.isSuccess()){
                result.setError(mapResponse.getError());
                return result;
            }
            UserTeamMemberSelect userTeamMemberSelect =(UserTeamMemberSelect)mapResponse.getResult().get("user");
            if(userTeamMemberSelect.getLevel()>=level){
                result.setError("用户等级错误");
                return result;
            }
            List<Level> levelList = (List<Level>) mapResponse.getResult().get("level");
            for(Level levels : levelList){
                if(levels.getLevel().equals(level)){
                    total = levels.getNeedMoney();
                }
            }
            if(total<=0){
                result.setError("金额合计错误");
                return result;
            }
            tradeNo.put("type","3");
            tradeNo.put("level",level.toString());
            tradeNo.put("total",total.toString());
            tradeNo.put("userId",baseUser.getId().toString());
        }else if(type.equals(4)){
            //补齐保证金升级
            if(level == null || level>5 || level<3){
                result.setError("用户等级错误");
                return result;
            }
            if(payType<3 || payType>5){
                result.setError("支付方式错误");
                return result;
            }
            Response<Map<String,Object>> response = userLevelService.selectIsContentLevelUp(baseUser.getId());
            if(!response.isSuccess()){
                result.setError(response.getError());
                return result;
            }
            total = Long.valueOf(response.getResult().get("needDeposit").toString());
            level = Integer.valueOf(response.getResult().get("nextLevel").toString());
            tradeNo.put("type","4");
            tradeNo.put("level",level.toString());
            tradeNo.put("userId",baseUser.getId().toString());
            tradeNo.put("total",total.toString());
        }
        JSONObject tradeNoJson = JSONObject.fromObject(tradeNo);
        Map<String,Object> resultMap = new HashMap<String, Object>();
        resultMap.put("payType",payType.toString());

        //余额支付或者预存款支付
        if(payType.equals(1) || payType.equals(2)){
            //1：余额  2：预存款
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("buyerId",baseUser.getId().toString());
            map.put("deviceId",deviceId);
            map.put("payType",payType);
            map.put("simpleOrderInfo",simpleOrderInfo);
            JSONObject json = JSONObject.fromObject(map);
            JwtToken token=tokenFactory.createPaymentToken(baseUser.getId().toString(),orderId,payType.toString(),MD5.encode(json.toString()));
            userPaymentTokenService.saveUserPaymentToken(baseUser.getId(),orderId,token);
            resultMap.put("sign",token.getToken());
            resultMap.put("orderId",orderId);
            result.setResult(resultMap);
            return  result;
        }else if(payType.equals(3)){
            //支付宝
            //实例化客户端
            AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", alipayAppId, alipayAppPrivateKey, "json", alipayCharset, alipayAppPublicKey, "RSA2");
            //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
            AlipayTradeAppPayRequest requests = new AlipayTradeAppPayRequest();
            //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
            model.setBody(messageSources.get("alipayBody"));
            model.setSubject(messageSources.get("alipaySubject"));
            model.setOutTradeNo(getCurrentTime()+baseUser.getId());
            model.setTimeoutExpress("30m");
            model.setTotalAmount(String.valueOf(Double.valueOf(total)/100));
            model.setProductCode("QUICK_MSECURITY_PAY");
            model.setPassbackParams(tradeNoJson.toString());
            requests.setBizModel(model);
            requests.setNotifyUrl(alipayNotifyUrl);
            try {
                //这里和普通的接口调用不同，使用的是sdkExecute
                AlipayTradeAppPayResponse response = alipayClient.sdkExecute(requests);
                resultMap.put("sign",response.getBody());
                result.setResult(resultMap);
                return  result;
            } catch (AlipayApiException e) {
                result.setError("支付宝支付失败");
                return  result;
            }
        }else if(payType.equals(4)){
            //微信
            UnifiedOrder unifiedorder = new UnifiedOrder();
            unifiedorder.setAppid(wxAppID);
            unifiedorder.setAttach(tradeNoJson.toString());
            unifiedorder.setMch_id(wxMchId);
            unifiedorder.setNonce_str(java.util.UUID.randomUUID().toString().substring(0, 15));
            unifiedorder.setBody(messageSources.get("alipayBody"));
            unifiedorder.setNotify_url(wxNotifyUrl);
            unifiedorder.setTrade_type("APP");
            unifiedorder.setSpbill_create_ip(IpUtils.getIp2(request));
            unifiedorder.setTotal_fee(total.toString());
            unifiedorder.setOut_trade_no(getCurrentTime()+baseUser.getId());
            String sign = SignUtil.getSign(unifiedorder.toMap(), wxMchKey);
            unifiedorder.setSign(sign);
            Weixin weixin = new Weixin();
            try {
                UnifiedOrderResult unifiedOrderResult = weixin.payUnifiedOrder(unifiedorder);
                if (unifiedOrderResult.isSuccess()) {
                    String prepay_id = unifiedOrderResult.getPrepay_id();
                    WCPay wcPay = PayUtil.getBrandWCPayRequest(wxAppID, prepay_id, wxMchKey,wxMchId);
                    resultMap.put("sign",wcPay);
                    result.setResult(resultMap);
                    return  result;
                }
            }catch (Exception e){
                result.setError("微信支付失败");
                return  result;
            }
        }else if(payType.equals(5)){
            //银联
            SDKConfig.getConfig().loadPropertiesFromSrc();
            Map<String, String> contentData = new HashMap<String, String>();
            contentData.put("version", SDKConfig.getConfig().getVersion());                  		     //版本号
            contentData.put("encoding", "UTF-8");            		 //字符集编码 可以使用UTF-8,GBK两种方式
            contentData.put("signMethod", SDKConfig.getConfig().getSignMethod());    //签名方法  01:RSA证书方式  11：支持散列方式验证SHA-256 12：支持散列方式验证SM3
            contentData.put("txnType", "01");                              			 //交易类型 95-银联加密公钥更新查询
            contentData.put("txnAmt", total.toString());    //金额，单位为分
            contentData.put("currencyCode", "156");      //币种，必填156
            contentData.put("txnSubType", "01");                           			 //交易子类型  默认00
            contentData.put("bizType", "000201");                          			 //业务类型  默认
            contentData.put("channelType", "08");                          			 //渠道类型
            contentData.put("backUrl", SDKConfig.getConfig().getBackUrl());
            contentData.put("certType", "01");							   			 //01：敏感信息加密公钥(只有01可用)
            contentData.put("merId", acpsdkMerId);                   			 //商户号码（商户号码777290058110097仅做为测试调通交易使用，该商户号配置了需要对敏感信息加密）测试时请改成自己申请的商户号，【自己注册的测试777开头的商户号不支持代收产品】
            contentData.put("accessType", "0");                            			 //接入类型，商户接入固定填0，不需修改
            contentData.put("orderId", getCurrentTime()+baseUser.getId()); //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
            contentData.put("txnTime", getCurrentTime());         		     //订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效                         //账号类型
            contentData.put("reqReserved", tradeNoJson.toString());
            Map<String, String> reqData = AcpService.sign(contentData,"UTF-8");			   //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
            String appRequestUrl = SDKConfig.getConfig().getAppRequestUrl();
            Map<String, String> rspData = AcpService.post(reqData,appRequestUrl,"UTF-8");
            if(!rspData.isEmpty()){
                if(AcpService.validate(rspData, "UTF-8")){
                    LogUtil.writeLog("验证签名成功");
                    String respCode = rspData.get("respCode") ;
                    if(("00").equals(respCode)){
//                        int resultCode = AcpService.updateEncryptCert(rspData,"UTF-8");
//                        if (resultCode == 1) {
//                            LogUtil.writeLog("加密公钥更新成功");
//                        } else if (resultCode == 0) {
//                            LogUtil.writeLog("加密公钥无更新");
//                        } else {
//                            LogUtil.writeLog("加密公钥更新失败");
//                        }
                        resultMap.put("sign",rspData.get("tn"));
                        result.setResult(resultMap);
                        return  result;
                    }else{
                        //其他应答码为失败请排查原因
                        //TODO
                    }
                    result.setError("银联支付失败");
                    return  result;
                }else{
                    LogUtil.writeErrorLog("验证签名失败");
                    //TODO 检查验证签名失败的原因
                    result.setError("银联支付失败");
                    return  result;
                }
            }else{
                //未返回正确的http状态
                LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
                result.setError("银联支付失败");
                return  result;
            }
        }
        return null;
    }


    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    @RequestMapping(value = "/notify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Boolean> notify (@RequestParam("password") String password,
                                       @RequestParam("payToken") String payToken,
                                       @RequestParam("orderIds") String orderIds,
                                       @RequestParam("deviceId") String deviceId,
                                       HttpServletRequest request){
        NbResponse<Boolean> result = new NbResponse<Boolean>();
//        LoginInfo loginInfo = LoginInfoUtil.getLoginInfo();
//        if(!deviceId.equals(loginInfo.getDeviceId())){
//            result.setError("user not login");
//            return result;
//        }

        BaseUser baseUser=UserUtil.getCurrentUser();
        JwtToken jwtToken =userPaymentTokenService.getUserPaymentToken(baseUser.getId(),orderIds.toString());

        if(jwtToken == null || jwtToken.getToken() ==null || jwtToken.getToken().equals("")){
            result.setError("缺少支付信息");
            return result;
        }

        if(!jwtToken.getToken().equals(payToken)){
            result.setError("支付信息异常");
            return result;
        }
        Integer payType =0;
        try {
            JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwtToken.getToken());
            Jws<Claims> claims = jwtAuthenticationToken.parseClaims(tokenSigningKey);
            String username = claims.getBody().getSubject();
            payType = Integer.valueOf(claims.getBody().get("payType").toString());
            if(!Long.valueOf(username).equals(baseUser.getId())){
                result.setError("用户信息异常");
                return result;
            }
        }catch (Exception e){
            result.setError("支付超时");
            return result;
        }

        Response<UserExtra> userExtraResponse = userExtraService.findByUserId(baseUser.getId());
        if(userExtraResponse.getResult() ==null){
            result.setError("用户信息异常");
            return result;
        }
        if(userExtraResponse.getResult().getPayPassword() ==null || userExtraResponse.getResult().getPayPassword().equals("")){
            result.setError("用户支付密码异常");
            return result;
        }

        if (!passwordMatch(password, userExtraResponse.getResult().getPayPassword())) {
            result.setError("用户支付密码错误");
            return result;
        }

        List<String> identities = Splitter.on(",").splitToList(orderIds);
        int orderNum = identities.size();
        checkArgument(orderNum > 0, "订单个数有误");

        Long totalFee = 0L;
        Long totalDeliverFee = 0L;
        for(String orderId : identities){
            Order order = getOrder(Long.valueOf(orderId));
            totalFee = totalFee + order.getFee();
            totalDeliverFee = totalDeliverFee + order.getDeliverFee();
        }


        Response<Boolean> response = userWalletService.pay(baseUser.getId(),payType,totalFee,totalDeliverFee);
        if(!response.isSuccess()){
            result.setError(response.getError());
            return result;
        }

        String paymentCode = getCurrentTime()+identities.get(0).toString();
        updatePaymentSucceed(orderIds,payType,paymentCode);

        result.setResult(true);
        return result;
    }

    private boolean passwordMatch(String password, String encryptedPassword) {
        Iterable<String> parts = splitter.split(encryptedPassword);
        String salt = Iterables.get(parts, 0);
        String realPassword = Iterables.get(parts, 1);
        return Objects.equal(sha512.hashUnencodedChars(password + salt).toString().substring(0, 20), realPassword);
    }

    private void updatePaymentSucceed(String orderIds,Integer type, String paymentCode) {
        List<String> identities = Splitter.on(",").splitToList(orderIds);
        int orderNum = identities.size();
        checkArgument(orderNum > 0, "订单个数错误");


        Iterator<String> it = identities.iterator();
        Long firstOrderId = Long.valueOf(it.next());
        Order order = getOrder(firstOrderId);

        if (isPlainOrder(order) && isSingleOrder(orderNum)) {
            if (isEmpty(order.getPaymentCode())) {
                updateOrderAsPaid(order.getId(), paymentCode,type.toString(),new Date());
            }

            // 创建结算信息
//            order.setStatus(Order.Status.PAID.value());
//            if (needSettlementAfterPaid(order)) {
//                createOrderSettlement(order);
//            }
            return;
        }

        if (isPlainOrder(order) && isMultiOrder(orderNum)) {
            List<Long> ids = convertToLong(identities);
            updateMultiOrderAsPaid(ids, paymentCode, type.toString(),new Date());
            return;
        }

//        if (isPreSaleOrder(order) && isPreSaleOrderNum(orderNum)) {   // 预售订单约定为 "订单号,子订单号" 的形式
//
//            order.setStatus(Order.Status.PAID.value());
//            Long orderItemId = Long.valueOf(identities.get(1));
//
//            // 标记预售订单支付方式
//            List<Long> orderIds = new ArrayList<Long>();
//            orderIds.add(order.getId());
//            List<Long> orderItemIds = new ArrayList<Long>();
//            orderItemIds.add(orderItemId);
//            this.setPaymentPlatform(orderIds, orderItemIds, null, "1", 1);
//
//            //  更新押金订单状态
//            updateDeposit(order, orderItemId);
//            updatePreSaleOrderAsPaid(orderItemId, paymentCode,paidAt,order);
//
//            return;
//        }

        throw new IllegalStateException("impossible.exception.raised");
    }
    private List<Long> convertToLong(List<String> identities) {
        List<Long> ids = Lists.newArrayListWithCapacity(identities.size());
        for (String identity : identities) {
            ids.add(Long.valueOf(identity));
        }
        return ids;
    }
    private Order getOrder(Long orderId) {
        Response<Order> getOrder = orderQueryService.findById(orderId);
        checkState(getOrder.isSuccess(), getOrder.getError());
        return getOrder.getResult();
    }

    private boolean isPlainOrder(Order order) {
        return Objects.equal(order.getType(), Order.Type.PLAIN.value());
    }

    private boolean isMultiOrder(int orderNum) {
        return !isSingleOrder(orderNum);
    }

    private boolean isSingleOrder(int orderNum) {
        return orderNum == 1;
    }

    private void updateOrderAsPaid(Long orderId, String paymentCode,String paymentPlatform, Date paidAt) {
        Response<Boolean> updatePaid = orderWriteService.normalOrderPaid(orderId, paymentCode,paymentPlatform,paidAt);
        checkState(updatePaid.isSuccess(), updatePaid.getError());
    }

    private void updateMultiOrderAsPaid(List<Long> ids, String paymentCode, String paymentPlatform,Date paidAt) {
        Response<Boolean> batchUpdatePaid = orderWriteService.batchNormalOrderPaid(ids, paymentCode,paymentPlatform, paidAt);
        checkState(batchUpdatePaid.isSuccess(), batchUpdatePaid.getError());
        for (Long id : ids) {
            try {
                Order order = getOrder(id);
                // 创建结算信息
//                order.setStatus(Order.Status.PAID.value());
//                if (needSettlementAfterPaid(order)) {
//                    createMultiOrderSettlement(order);
//                }
            } catch (IllegalStateException e) {
                log.error("fail to handle order:(id={}), error:{}", id, e.getMessage());
            }
        }

    }

//    private void createOrderSettlement(Order order) {
//        Response<Long> created = settlementService.generate(order.getId());
//        checkState(created.isSuccess(), created.getError());
//    }
//    private void createMultiOrderSettlement(Order order) {
//        Response<Long> created = settlementService.generateMulti(order.getId());
//        checkState(created.isSuccess(), created.getError());
//    }

    @RequestMapping(value = "/ceshi", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Boolean> ceshi(HttpServletRequest request) {

        NbResponse<Boolean> result = new NbResponse<Boolean>();

        try {
            SDKConfig.getConfig().loadPropertiesFromSrc();
            Map<String, String> data = new HashMap<String, String>();

            /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
            data.put("version", SDKConfig.getConfig().getVersion());            //版本号 全渠道默认值
            data.put("encoding", "UTF-8");     //字符集编码 可以使用UTF-8,GBK两种方式
            data.put("signMethod", SDKConfig.getConfig().getSignMethod()); //签名方法
            data.put("txnType", "21");              		 	//交易类型 12：代付
            data.put("txnSubType", "03");           		 	//默认填写00
            data.put("bizType", "000401");          		 	//000401：代付
            data.put("channelType", "07");          		 	//渠道类型

            /***商户接入参数***/
            data.put("merId", acpsdkMerId);   		 				//商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
            data.put("accessType", "0");            		 	//接入类型，商户接入填0 ，不需修改（0：直连商户， 1： 收单机构 2：平台商户）


            data.put("batchNo", "0001");            			//批量交易时填写，当天唯一,0001-9999，商户号+批次号+上交易时间确定一笔交易
            data.put("txnTime", getCurrentTime());  					//前8位需与文件中的委托日期保持一致
            data.put("totalQty", "10");             			//批量交易时填写，填写批量文件中总的交易比数
            data.put("totalAmt", "1000");           			//批量交易时填写，填写批量文件中总的交易金额


            //后台通知地址（需设置为外网能访问 http https均可），支付成功后银联会自动将异步通知报文post到商户上送的该地址，【支付失败的交易银联不会发送后台通知】
            //后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费交易 商户通知
            //注意:1.需设置为外网能访问，否则收不到通知    2.http https均可  3.收单后台通知后需要10秒内返回http200或302状态码
            //    4.如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200或302，那么银联会间隔一段时间再次发送。总共发送5次，银联后续间隔1、2、4、5 分钟后会再次通知。
            //    5.后台通知地址如果上送了带有？的参数，例如：http://abc/web?a=b&c=d 在后台通知处理程序验证签名之前需要编写逻辑将这些字段去掉再验签，否则将会验签失败
            data.put("fileContent", AcpService.enCodeFileContent("D://workspace//ACPSample_DaiFu//src//assets//DF00000000777290058110097201507140002I.txt","UTF-8"));

            // 请求方保留域，
            // 透传字段，查询、通知、对账文件中均会原样出现，如有需要请启用并修改自己希望透传的数据。
            // 出现部分特殊字符时可能影响解析，请按下面建议的方式填写：
            // 1. 如果能确定内容不会出现&={}[]"'等符号时，可以直接填写数据，建议的方法如下。
//		data.put("reqReserved", "透传信息1|透传信息2|透传信息3");
            // 2. 内容可能出现&={}[]"'符号时：
            // 1) 如果需要对账文件里能显示，可将字符替换成全角＆＝｛｝【】“‘字符（自己写代码，此处不演示）；
            // 2) 如果对账文件没有显示要求，可做一下base64（如下）。
            //    注意控制数据长度，实际传输的数据长度不能超过1024位。
            //    查询、通知等接口解析时使用new String(Base64.decodeBase64(reqReserved), DemoBase.encoding);解base64后再对数据做后续解析。
//		data.put("reqReserved", Base64.encodeBase64String("任意格式的信息都可以".toString().getBytes(DemoBase.encoding)));


            /**对请求参数进行签名并发送http post请求，接收同步应答报文**/
            Map<String, String> reqData = AcpService.sign(data,"UTF-8");			 		 //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
            String requestBackUrl = SDKConfig.getConfig().getBatchTransUrl();									 //交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl

            Map<String, String> rspData = AcpService.post(reqData,requestBackUrl,"UTF-8");        //发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
            /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
            //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
            if(!rspData.isEmpty()){
                if(AcpService.validate(rspData, "UTF-8")){
                    LogUtil.writeLog("验证签名成功");
                    String respCode = rspData.get("respCode");
                    if(("00").equals(respCode)){
                        //交易已受理(不代表交易已成功），等待接收后台通知确定交易成功，也可以主动发起 查询交易确定交易状态。
                        //TODO

                        //如果返回卡号且配置了敏感信息加密，解密卡号方法：
                        //String accNo1 = resmap.get("accNo");
                        //String accNo2 = AcpService.decryptPan(accNo1, "UTF-8");	//解密卡号使用的证书是商户签名私钥证书acpsdk.signCert.path
                        //LogUtil.writeLog("解密后的卡号："+accNo2);
                    }else if(("03").equals(respCode) ||
                            ("04").equals(respCode) ||
                            ("05").equals(respCode) ||
                            ("01").equals(respCode) ||
                            ("12").equals(respCode) ||
                            ("60").equals(respCode) ){
                        //后续需发起交易状态查询交易确定交易状态。
                        //TODO
                    }else{
                        //其他应答码为失败请排查原因
                        //TODO
                    }
                }else{
                    LogUtil.writeErrorLog("验证签名失败");
                    //TODO 检查验证签名失败的原因
                }
            }else{
                //未返回正确的http状态
                LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
            }
        } catch (IllegalArgumentException e) {
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            result.setError(messageSources.get("user.password.reset.fail"));
        }
        return result;
    }

    @RequestMapping(value = "/bindingBank", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Boolean> bindingBank(@RequestParam("bankMobile") String bankMobile,
                                            @RequestParam("bankUser") String bankUser,
                                            @RequestParam("bankCardNo") String bankCardNo,
                                            @RequestParam("card") String card,
                                            @RequestParam("code") String code,
                                            @RequestParam("session") String sessionId,
                                            HttpServletRequest request) {

        NbResponse<Boolean> result = new NbResponse<Boolean>();

        try {
            BaseUser baseUser = UserUtil.getCurrentUser();
            Response<UserBank> userBankResponse = userBankService.viewBank(baseUser.getId());
            if (userBankResponse.getResult() != null && userBankResponse.getResult().getId() != null) {
                result.setError("已经绑定过银行卡");
                return result;
            }

            AFSession session = new AFSession(sessionManager, request, sessionId);
            String temp = (String) session.getAttribute("code");
            checkState(notEmpty(temp), "user.code.not.found");


            List<String> parts = splitter.splitToList(temp);
            String expected = parts.get(0);
            checkState(equalWith(code, expected), "user.code.mismatch");
            //如果匹配了code,则删除在session中的值
            request.getSession().removeAttribute("code");


            HttpsClient http = new HttpsClient();
            com.nowbook.weixin.weixin4j.http.Response res = http.get("https://ccdcapi.alipay.com/validateAndCacheCardInfo.json?_input_charset=utf-8&cardNo="+bankCardNo+"&cardBinCheck=true");
            JSONObject jsonObj = res.asJSONObject();
            String bank = "";
            if(jsonObj.get("validated").toString().equals("true")){
                if(jsonObj.get("cardType").toString().equals("CC")){
                    result.setError("只能绑定借记卡");
                }
                bank = messageSources.get(jsonObj.get("bank").toString());
                if(bank==null || bank.equals("")){
                    bank = "银行卡";
                }
            }else {
                result.setError("请输入正确的银行卡号");
            }

            UserBank userBank = new UserBank();
            userBank.setUserId(baseUser.getId());
            userBank.setBankCardNo(bankCardNo);
            userBank.setBankMobile(bankMobile);
            userBank.setBankType(1);
            userBank.setType(3);
            userBank.setBank(bank);
            userBank.setBankUser(bankUser);
            userBank.setBankCardUserNo(card);
            userBankService.bindingBank(userBank);
            result.setResult(Boolean.TRUE);
        }catch (Exception e) {
            log.error("fail to binding bank with bankCardNo:{}, captcha:{}，error:{}",
                    bankCardNo,code, e.getMessage());
            result.setError("绑定银行卡失败");

        }
        return result;

    }

    @RequestMapping(value = "/viewBank", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<UserBank> viewBank(HttpServletRequest request) {
        NbResponse<UserBank> result = new NbResponse<UserBank>();
        BaseUser baseUser = UserUtil.getCurrentUser();
        Response<UserBank> userBankResponse =userBankService.viewBank(baseUser.getId());
        result.setResult(userBankResponse.getResult());
        return result;
    }

    @RequestMapping(value = "/unBindingBank", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Boolean> unBindingBank(@RequestParam("id") Long id,HttpServletRequest request) {
        NbResponse<Boolean> result = new NbResponse<Boolean>();
        BaseUser baseUser = UserUtil.getCurrentUser();
        UserBank userBank= new UserBank();
        userBank.setId(id);
        userBank.setUserId(baseUser.getId());
        Response<Boolean> userBankResponse =userBankService.unBindingBank(userBank);
        result.setResult(userBankResponse.getResult());
        return result;
    }

    @RequestMapping(value = "/applyLevelUp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> applyLevelUp(UserLevelWait userLevelWait) {
        if(userLevelWait.getUserIdListString()!=null){
            List<Long> userIdList = new ArrayList<Long>();
            for(String userId : userLevelWait.getUserIdListString().split(",")){
                userIdList.add(Long.valueOf(userId));
            }
            userLevelWait.setUserIdList(userIdList);
        }
        BaseUser baseUser = UserUtil.getCurrentUser();
        userLevelWait.setUserId(baseUser.getId());
        return  userLevelService.applyLevelUp(userLevelWait);
    }

    //查询我的等级，升级界面用
    @RequestMapping(value = "/selectMyLevel", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Map<String,Object>> selectMyLevel() {
        BaseUser baseUser = UserUtil.getCurrentUser();
        return  userLevelService.selectMyLevel(baseUser.getId());
    }

    //查询我的推荐人
    @RequestMapping(value = "/selectMyInviter", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Map<String,Object>> selectMyInviter(@RequestParam(value = "level",required = false) Integer level,@RequestParam("type") Integer type,@RequestParam("pageNo") Integer pageNo,@RequestParam("pageSize") Integer pageSize) {
        BaseUser baseUser = UserUtil.getCurrentUser();
        return  userLevelService.selectMyInviter(baseUser.getId(),level,type,pageNo,pageSize);
    }

    //查询我的团队下级
    @RequestMapping(value = "/selectTeamMember", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Map<String,Object>> selectTeamMember(@RequestParam(value = "level",required = false) Integer level,@RequestParam("pageNo") Integer pageNo,@RequestParam("pageSize") Integer pageSize) {
        BaseUser baseUser = UserUtil.getCurrentUser();
        return  userLevelService.selectTeamMember(baseUser.getId(),level,pageNo,pageSize);
    }

    //查询我的总页面，分为查询钱包，查询订单各状态数量，查询金卡 白金是否达到升级状态
    @RequestMapping(value = "/selectMy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Map<String,Object>> selectUserWallet() {
        BaseUser baseUser = UserUtil.getCurrentUser();
        Response<Map<String,Object>> result = new Response<Map<String, Object>>();
        Map<String,Object> map = new HashMap<String, Object>();
        Map<String,String> order = orderQueryService.countStatus(baseUser.getId());
        UserWallet userWallet = userWalletService.selectUserWallet(baseUser.getId()).getResult();
        Map<String,Object> isLevelUp = userLevelService.selectIsContentLevelUp(baseUser.getId()).getResult();
        UserTeamMemberSelect userTeamMemberSelect = userLevelService.selectUser(baseUser.getId()).getResult();
        map.put("userTeamMemberSelect",userTeamMemberSelect);
        map.put("order",order);
        map.put("userWallet",userWallet);
        map.put("isLevelUp",isLevelUp);
        result.setResult(map);
        return  result;
    }

    @RequestMapping(value = "/selectUserWalletSummary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Map<String,Object>> selectUserWalletSummary(@RequestParam(value = "type1",required = false) Integer type1,@RequestParam(value = "type2",required = false) Integer type2,@RequestParam("pageNo") Integer pageNo,@RequestParam("pageSize") Integer pageSize) {
        BaseUser baseUser = UserUtil.getCurrentUser();
        UserWalletSummary userWalletSummary = new UserWalletSummary();
        userWalletSummary.setUserId(baseUser.getId());
        userWalletSummary.setType1(type1);
        userWalletSummary.setType2(type2);
        userWalletSummary.setPageNo(pageNo);
        userWalletSummary.setPageSize(pageSize);
        return  userWalletService.selectUserWalletSummary(userWalletSummary);
    }

    private enum TYPE {
        PASSWORD,           // 用户的初始密码
        CODE         // 用来注册的验证码

    }

    // 为每个会员生成二维码
    // status 会员状态 默认为1   count 每页查询数量 默认为100
    @RequestMapping(value = "/usersEncodeCreate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean> usersEncodeCreate(Integer status, Integer count) {
        Response<Boolean> result = new Response<Boolean>();
        if (status == null || status == 0) {
            status = 1;
        }
        if (count == null || count == 0) {
            count = 100;
        }
        Response<Paging<User>> first = accountService.list(status, 1, count);
        if (first.isSuccess()) {
            Paging<User> pagings = first.getResult();
            Long total = pagings.getTotal();
            Long pageCount = total / count + 1;
            String pagePath = template.execute(new JedisTemplate.JedisAction<String>() {
                @Override
                public String action(Jedis jedis) {
                    return jedis.hget(RedisKeyUtils.otherConf(), "page_path");
                }
            });
            if (pagePath == null) {
                pagePath = mainSite;
            }
            for (int i = 1; i <= pageCount; i++) {
                Response<Paging<User>> list = accountService.list(status, i, count);
                List<User> users = list.getResult().getData();
                for (User user : users) {
                    Long id = user.getId();
                    String sid = DESUtil.encrypt(id.toString());
                    if (sid != null && !sid.equals("")) {
                        String content = pagePath + "/mall/share.html?userId=" + sid;
                        String path = System.getProperty("java.io.tmpdir");
                        Map hints = new HashMap();
                        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                        hints.put(EncodeHintType.MARGIN, 1);
                        try {
                            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 400, 400, hints);
                            String uuid = UUID.randomUUID().toString().substring(0, 10);
                            File file = new File(path, uuid + ".png");
                            MatrixToImageWriter.writeToFile(bitMatrix, "png", file);
                            String qrCodeUrl = imageServer.write(file.getName(), file);
                            UserExtra userExtra = new UserExtra();
                            userExtra.setUserId(id);
                            userExtra.setQrCodeUrl(imageBaseUrl + qrCodeUrl);
                            Response<Boolean> theResult = userExtraService.updateByUserId(userExtra);
                        } catch (WriterException e) {
                            log.error("二维码图片生成失败");
                            result = new Response<Boolean>();
                            return result;
                        } catch (IOException e) {
                            log.error("二维码图片保存失败");
                            result = new Response<Boolean>();
                            return result;
                        } catch (ImageUploadException e) {
                            log.error("二维码图片上传失败");
                            result = new Response<Boolean>();
                            return result;
                        }
                    }
                }
            }
        }
        result.setSuccess(true);
        return result;
    }

    // 为单个会员生成二维码
    @RequestMapping(value = "/userEncodeCreate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> userEncodeCreate(Long userId) {
        Response<String> result = new Response<String>();
        String pagePath = template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hget(RedisKeyUtils.otherConf(), "page_path");
            }
        });
        if (pagePath == null) {
            pagePath = mainSite;
        }
        String sid = DESUtil.encrypt(userId.toString());
        if (sid != null && !sid.equals("")) {
            String content = pagePath + "/mall/share.html?userId=" + sid;
            String path = System.getProperty("java.io.tmpdir");
            Map hints = new HashMap();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            try {
                BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 400, 400, hints);
                String uuid = UUID.randomUUID().toString().substring(0, 10);
                File file = new File(path, uuid + ".png");
                MatrixToImageWriter.writeToFile(bitMatrix, "png", file);
                String qrCodeUrl = imageServer.write(file.getName(), file);
                UserExtra userExtra = new UserExtra();
                userExtra.setUserId(userId);
                userExtra.setQrCodeUrl(imageBaseUrl + qrCodeUrl);
                Response<Boolean> theResult = userExtraService.updateByUserId(userExtra);
                if (theResult.isSuccess()) {
                    if (theResult.getResult()) {
                        result.setResult(userExtra.getQrCodeUrl());
                        return result;
                    }
                }
            } catch (WriterException e) {
                log.error("二维码图片生成失败");
            } catch (IOException e) {
                log.error("二维码图片保存失败");
            } catch (ImageUploadException e) {
                log.error("二维码图片上传失败");
            }
        }
        return result;
    }

    // 根据加密后的token获取用户ID再进行加密
    @RequestMapping(value = "/getUserIdByToken", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> getUserIdByToken(String secretToken) {
        Response<String> result = new Response<String>();
        String userId = "";
        String token = MobileDESUtil.decrypt(secretToken);
        if (token != null && !token.equals("")) {
            JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(token);
            Jws<Claims> claims = jwtAuthenticationToken.parseClaims(jwtSettings.getTokenSigningKey());
            userId = claims.getBody().getSubject();
            userId = DESUtil.encrypt(userId);
        }
        result.setResult(userId);
        return result;
    }

    // 根据加密的用户ID获取用户登录名
    @RequestMapping(value = "/getMobileByInviter", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> getMobileByInviter(String inviter) {
        Response<String> result = new Response<String>();
        if (DESUtil.decrypt(inviter) != null && !"".equals(DESUtil.decrypt(inviter))) {
            Response<User> user = accountService.findUserById(Long.valueOf(DESUtil.decrypt(inviter)));
            if (user.isSuccess()) {
                if (user.getResult() != null) {
                    result.setResult(user.getResult().getMobile());
                }
            }
        }
        return result;
    }

    // 根据加密的用户ID获取用户登录名
    @RequestMapping(value = "/update", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> update() {
        Response<String> result = new Response<String>();
        userLevelService.update();
        result.setSuccess(true);
        return result;
    }


    // 启动页图片获取
    @RequestMapping(value = "/pictureStartUpGet", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> pictureStartUpGet() {
        Response<String> response = new Response<String>();
        String imageUrl = "";
        String startUp = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get("picture_start_up");
            }
        });
        if(startUp !=null && !startUp.equals("")){
            imageUrl = startUp.toString();
        }
        response.setResult(imageUrl);
        return response;
    }

    // 引导页图片获取
    @RequestMapping(value = "/pictureGuideGet", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,String> pictureGuideGet() {
        Map<String,String> imageUrl = new HashMap<String, String>();
        String startUp = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get("picture_guide");
            }
        });
        if(startUp !=null && !startUp.equals("")){
            Integer i = 1;
            for(String image : startUp.split(",")){
                imageUrl.put("imageUrl"+i,image);
                i++;
            }
        }
        return imageUrl;
    }
}

