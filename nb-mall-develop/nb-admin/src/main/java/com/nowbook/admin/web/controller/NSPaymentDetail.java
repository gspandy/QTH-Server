package com.nowbook.admin.web.controller;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.nowbook.common.model.Response;
import com.nowbook.common.utils.CommonConstants;
import com.nowbook.common.utils.NameValidator;
import com.nowbook.image.ImageServer;
import com.nowbook.image.exception.ImageUploadException;
import com.nowbook.redis.utils.JedisTemplate;
import com.nowbook.restful.controller.MatrixToImageWriter;
import com.nowbook.restful.dto.NbResponse;
import com.nowbook.restful.util.DESUtil;
import com.nowbook.rlt.settle.service.SettlementService;
import com.nowbook.sdp.model.*;
import com.nowbook.sdp.service.PaymentDetailDayService;
import com.nowbook.sdp.service.PaymentDetailService;
import com.nowbook.sdp.service.UserBankService;
import com.nowbook.sdp.service.UserLevelService;
import com.nowbook.session.AFSession;
import com.nowbook.session.AFSessionManager;
import com.nowbook.unionpay.acp.sdk.AcpService;
import com.nowbook.unionpay.acp.sdk.LogUtil;
import com.nowbook.unionpay.acp.sdk.SDKConfig;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.model.User;
import com.nowbook.user.model.UserExtra;
import com.nowbook.user.service.AccountService;
import com.nowbook.user.service.UserExtraService;
import com.nowbook.user.util.RedisKeyUtils;
import com.nowbook.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.nowbook.common.utils.Arguments.equalWith;
import static com.nowbook.common.utils.Arguments.notEmpty;

/**
 * Date: 4/22/14
 * Time: 16:41
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */

@Slf4j
@Controller
@RequestMapping("/api/admin/paymentDetail")
public class NSPaymentDetail {

    private final AFSessionManager sessionManager = AFSessionManager.instance();

    @Autowired
    private PaymentDetailService paymentDetailService;

    @Autowired
    private PaymentDetailDayService paymentDetailDayService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private UserBankService userBankService;

    @Autowired
    private MessageSources messageSources;
    @Autowired
    private SettlementService settlementService;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private UserExtraService userExtraService;
    @Autowired
    protected JedisTemplate template;

    @Value("#{app.acpsdkMerId}")
    private String acpsdkMerId;

    @Value("#{app.acpsdkPayAgainUrl}")
    private String acpsdkPayAgainUrl;

    @Value("#{app.mainSite}")
    private String mainSite;

    @Autowired
    private ImageServer imageServer;

    @Value("#{app.imageBaseUrl}")
    private String imageBaseUrl;

    @Value("#{app.restkey}")
    private String key;

    @RequestMapping(value = "/pay", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> pay(@RequestParam("type") String type, @RequestParam("id") Long id) {
        Response<String> result = new Response<String>();
        try {
            List<PaymentDetail> paymentDetailList = new ArrayList<PaymentDetail>();
            if (type.equals("1")) {
                PaymentDetailDay paymentDetailDay = new PaymentDetailDay();
                paymentDetailDay.setId(id);
                paymentDetailDay.setOffset(0);
                paymentDetailDay.setLimit(10);
                List<PaymentDetailDay> paymentDetailDayList = paymentDetailDayService.select(paymentDetailDay);
                if (paymentDetailDayList == null || paymentDetailDayList.size() == 0) {
                    result.setError("fail");
                    return result;
                }
                Integer pageNo = 1;
                Integer size = 100;
                Boolean next = true;
                while (next) {
                    PaymentDetail paymentDetail = new PaymentDetail();
                    paymentDetail.setIdNo(paymentDetailDayList.get(0).getIdNo());
                    paymentDetail.setOffset((pageNo - 1) * size);
                    paymentDetail.setLimit(size);
                    paymentDetailList = paymentDetailService.select(paymentDetail);
                    if (paymentDetailList != null || paymentDetailList.size() > 0) {
                        batchPayEarningsBonuses(paymentDetailList);
                        if (paymentDetailList.size() < size) {
                            next = false;
                        } else {
                            pageNo++;
                        }
                    } else {
                        next = false;
                    }
                }
            } else if (type.equals("2")) {
                PaymentDetail paymentDetail = new PaymentDetail();
                paymentDetail.setId(id);
                paymentDetail.setOffset(0);
                paymentDetail.setLimit(10);
                paymentDetailList = paymentDetailService.select(paymentDetail);
                batchPayEarningsBonuses(paymentDetailList);
            }

        } catch (Exception e) {
            result.setError(e.getMessage());
        }
        return result;
    }

    private void batchPayEarningsBonuses(List<PaymentDetail> paymentDetailList) {
        for (PaymentDetail pd : paymentDetailList) {
            try {
                if (pd.getPayResult() == null || pd.getPayResult().equals(1)) {
                    continue;
                }
                UserBank userBank = userBankService.viewBankForPayment(pd.getUserId()).getResult();
                if (userBank == null || userBank.getId() == null) {
                    PaymentDetail newPd = new PaymentDetail();
                    newPd.setId(pd.getId());
                    newPd.setReason("没有填写银行卡。");
                    newPd.setPayStatus(2);
                    newPd.setPayResult(2);
                    paymentDetailService.update(newPd);
                    continue;
                }
                Thread.sleep(1000 * 1);
                SDKConfig.getConfig().loadPropertiesFromSrc();
                Map<String, String> data = new HashMap<String, String>();

                /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
                data.put("version", SDKConfig.getConfig().getVersion());            //版本号 全渠道默认值
                data.put("encoding", "UTF-8");     //字符集编码 可以使用UTF-8,GBK两种方式
                data.put("signMethod", SDKConfig.getConfig().getSignMethod()); //签名方法
                data.put("txnType", "12");                        //交易类型 12：代付
                data.put("txnSubType", "00");                    //默认填写00
                data.put("bizType", "000401");                    //000401：代付
                data.put("channelType", "07");                    //渠道类型

                /***商户接入参数***/
                data.put("merId", acpsdkMerId);                        //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
                data.put("accessType", "0");                        //接入类型，商户接入填0 ，不需修改（0：直连商户， 1： 收单机构 2：平台商户）
                data.put("orderId", getCurrentTime() + pd.getId().toString());                        //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
                data.put("txnTime", getCurrentTime());                        //订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
                data.put("accType", "01");                            //账号类型 01：银行卡
                data.put("reqReserved", pd.getId().toString());
                //sourcesOfFunds为01时payerVerifiInfo必送，其他情况不送payerVerifiInfo。
                //付款方账号        payerAccNo     1到19位数字
                //付款方姓名        payerNm 30字节以下，支持汉字，1个汉字算2字节
                //data.put("sourcesOfFunds", "01");
                //data.put("payerVerifiInfo", "{payerAccNo=6226090000000048&payerNm=张三}");


                //收款账号为对公时：测试卡使用 6212142600000000167（单位结算卡）
                //单位结算卡完整账户名称        comDebitCardAccName 120字节以下，支持汉字，1个汉字算2字节
                //营业执照注册号        businessLicenseRegNo 20字节以下，支持汉字，1个汉字算2字节
                //data.put("accType", "04"); //04表示对公账户,当04时不需要送customerInfo
                //data.put("reserved", "{comDebitCardAccName=中国银联单位结算卡&businessLicenseRegNo=1101888888}");

                //////////如果商户号开通了  商户对敏感信息加密的权限那么，需要对 卡号accNo加密使用：
                data.put("encryptCertId", AcpService.getEncryptCertId());                            //上送敏感信息加密域的加密证书序列号
                String accNo = AcpService.encryptData(userBank.getBankCardNo(), "UTF-8");    //这里测试的时候使用的是测试卡号，正式环境请使用真实卡号
                data.put("accNo", accNo);
                //////////

                /////////商户未开通敏感信息加密的权限那么不对敏感信息加密使用：
                //contentData.put("accNo", userBankList.get(0).getBankCardNo());                  				//这里测试的时候使用的是测试卡号，正式环境请使用真实卡号
                ////////

                //代付交易的上送的卡验证要素：姓名或者证件类型+证件号码
                Map<String, String> customerInfoMap = new HashMap<String, String>();
                customerInfoMap.put("certifTp", "01");                            //证件类型
                customerInfoMap.put("certifId", userBank.getBankCardUserNo());            //证件号码
                customerInfoMap.put("customerNm", userBank.getBankUser());                    //姓名
                String customerInfoStr = AcpService.getCustomerInfo(customerInfoMap, userBank.getBankCardNo(), "UTF-8");

                data.put("customerInfo", customerInfoStr);
                data.put("txnAmt", pd.getMoney().toString());                                    //交易金额 单位为分，不能带小数点
                data.put("currencyCode", "156");                            //境内商户固定 156 人民币
                data.put("billNo", "钱唐荟奖金与收益");                                    //银行附言。会透传给发卡行，完成改造的发卡行会把这个信息在账单、短信中显示给用户的，请按真实情况填写。


                //后台通知地址（需设置为外网能访问 http https均可），支付成功后银联会自动将异步通知报文post到商户上送的该地址，【支付失败的交易银联不会发送后台通知】
                //后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费交易 商户通知
                //注意:1.需设置为外网能访问，否则收不到通知    2.http https均可  3.收单后台通知后需要10秒内返回http200或302状态码
                //    4.如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200或302，那么银联会间隔一段时间再次发送。总共发送5次，银联后续间隔1、2、4、5 分钟后会再次通知。
                //    5.后台通知地址如果上送了带有？的参数，例如：http://abc/web?a=b&c=d 在后台通知处理程序验证签名之前需要编写逻辑将这些字段去掉再验签，否则将会验签失败
                data.put("backUrl", acpsdkPayAgainUrl);

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
                Map<String, String> reqData = AcpService.sign(data, "UTF-8");                     //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
                String requestBackUrl = SDKConfig.getConfig().getBackRequestUrl();                                     //交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl

                Map<String, String> rspData = AcpService.post(reqData, requestBackUrl, "UTF-8");        //发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
                /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
                //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
                if (!rspData.isEmpty()) {
                    if (AcpService.validate(rspData, "UTF-8")) {
                        LogUtil.writeLog("验证签名成功");
                        String respCode = rspData.get("respCode");
                        if (("00").equals(respCode)) {
                            PaymentDetail newPd = new PaymentDetail();
                            newPd.setId(pd.getId());
                            newPd.setPayType(3);
                            newPd.setPayMoney(pd.getMoney());
                            newPd.setPayId(userBank.getBankCardNo());
                            newPd.setPayName(userBank.getBankUser());
                            newPd.setPayStatus(2);
                            paymentDetailService.update(newPd);
                        } else {
                            PaymentDetail newPd = new PaymentDetail();
                            newPd.setId(pd.getId());
                            newPd.setReason(rspData.get("respMsg"));
                            newPd.setPayStatus(2);
                            newPd.setPayResult(2);
                            paymentDetailService.update(newPd);
                            continue;
                        }
                    } else {
                        LogUtil.writeErrorLog("验证签名失败");
                        //TODO 检查验证签名失败的原因
                        PaymentDetail newPd = new PaymentDetail();
                        newPd.setId(pd.getId());
                        newPd.setReason("验证签名失败");
                        newPd.setPayStatus(2);
                        newPd.setPayResult(2);
                        paymentDetailService.update(newPd);
                        continue;
                    }
                } else {
                    //未返回正确的http状态
                    LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
                    PaymentDetail newPd = new PaymentDetail();
                    newPd.setId(pd.getId());
                    newPd.setReason("未获取到返回报文或返回http状态码非200");
                    newPd.setPayStatus(2);
                    newPd.setPayResult(2);
                    paymentDetailService.update(newPd);
                    continue;
                }
            } catch (Exception e) {
                PaymentDetail newPd = new PaymentDetail();
                newPd.setId(pd.getId());
                newPd.setReason(e.getMessage());
                newPd.setPayStatus(2);
                newPd.setPayResult(2);
                paymentDetailService.update(newPd);
                continue;
            }
        }
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    @RequestMapping(value = "/levelUp", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void levelUp() {
        userLevelService.userLevelUp();
    }

    @RequestMapping(value = "/settlement", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void levelUp(@RequestParam(value = "type") Integer type, @RequestParam(value = "orderId") Long orderId) {
        if (type.equals(1)) {
            settlementService.generate(orderId);
        } else if (type.equals(2)) {
            settlementService.generateMulti(orderId);
        }
    }

    @RequestMapping(value = "/levelUpForAdmin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> levelUpForAdmin(@RequestParam(value = "userId") Long userId, @RequestParam(value = "level") Integer level, @RequestParam(value = "balance") Long balance, @RequestParam(value = "advance") Long advance, @RequestParam(value = "deposit") Long deposit) {
        Response<String> response = new Response<String>();
        UserLevelWait userLevelWait = new UserLevelWait();
        userLevelWait.setUserId(userId);
        userLevelWait.setMoney(balance * 100 + advance * 100 + deposit * 100);
        userLevelWait.setLevel(level);
        //线下升级
        userLevelWait.setType(3);
        userLevelWait.setPayType(6);
        userLevelWait.setIsSelect(2);
        userLevelWait.setBalance(balance * 100);
        userLevelWait.setAdvance(advance * 100);
        userLevelWait.setDeposit(deposit * 100);
        Response<String> result = userLevelService.applyLevelUp(userLevelWait);
        if (result.isSuccess()) {
            response.setResult("success");
        }
        return response;
    }


    @RequestMapping(value = "/signupForAdmin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<String> signUp(@RequestParam("mobile") String mobile,
                                   @RequestParam("inviter") String inviter,
                                   HttpServletRequest request, HttpServletResponse response) {
        NbResponse<String> result = new NbResponse<String>();
        User oldUser = accountService.findUserByMobile(mobile).getResult();
        if(oldUser !=null){
            result.setError("用户名重复");
            return result;
        }
        User inviterUser = accountService.findUserByMobile(inviter).getResult();
        if(inviterUser ==null){
            result.setError("推荐人不存在");
            return result;
        }
        try {
            User user = new User();
            user.setName(mobile);
            user.setEncryptedPassword("qiantang111");
            user.setType(BaseUser.TYPE.BUYER.toNumber());
            user.setStatus(User.STATUS.NORMAL.toNumber());
            user.setMobile(mobile);
            Response<Long> uidGet = accountService.createUser(user);
            checkState(uidGet.isSuccess(), uidGet.getError());

            // 等级相关
            UserLevel userLevel = new UserLevel();
            userLevel.setUserId(uidGet.getResult());
            userLevel.setLevel(1);
            userLevel.setLevelUpAt(new Date());
            userLevel.setInviter(inviterUser.getId());
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

            result.setResult("success", key);

        } catch (IllegalArgumentException e) {
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            result.setError(messageSources.get(e.getMessage()));
        }
        return result;
    }
}
