package com.nowbook.restful.controller;

import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.exception.JsonResponseException;
import com.nowbook.image.ImageServer;
import com.nowbook.restful.dto.NbResponse;
import com.nowbook.sdp.model.*;
import com.nowbook.sdp.service.*;
import com.nowbook.sdp.utils.MathUtils;
import com.nowbook.session.AFSessionManager;
import com.nowbook.shop.model.Shop;
import com.nowbook.shop.service.ShopService;
import com.nowbook.trade.dto.RichOrderBuyerView;
import com.nowbook.trade.service.OrderQueryService;
import com.nowbook.user.model.UserImage;
import com.nowbook.user.service.ImageService;
import com.nowbook.web.controller.view.UploadedFile;
import com.nowbook.web.misc.MessageSources;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.nowbook.weixin.weixin4j.Configuration;
import com.nowbook.weixin.weixin4j.Weixin;
import com.nowbook.weixin.weixin4j.WeixinException;
import com.nowbook.weixin.weixin4j.pay.SignUtil;
import com.nowbook.weixin.weixin4j.util.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.nowbook.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
/**
 * Created by winter on 16/5/5.
 */
@Slf4j
@Controller
@RequestMapping("/api/extend/distributor")
public class NSDistributor {

    @Autowired
    private AmountDetailService amountDetailService;
    @Autowired
    private DistributionsService distributionsService;
    @Autowired
    private DistributorSetService distributorSetService;
    @Autowired
    MessageSources messageSources;
    @Autowired
    private ConcernMemberService concernMemberService;
    @Autowired
    private DistributorUserService distributorService;
    @Autowired
    private ImageService imageService;

    @Autowired
    OrderQueryService orderQueryService;
    @Autowired
    private ShopService shopService;

    @Autowired
    private ImageServer imageServer;

    private final AFSessionManager sessionManager = AFSessionManager.instance();

    @Autowired
    private AmountWithdrawalHistoryService amountWithdrawalHistoryService;

    @Value("#{app.restkey}")
    private String key;

    @Value("#{app.distributionPromotionQr}")
    private String distributionPromotionQr;

    @Value("#{app.distributionStoreQr}")
    private String distributionStoreQr;

    @Value("#{app.qr}")
    private String qr;
    @Value("#{app.imageBaseUrl}")
    private String imageBaseUrl;

    /**
     * 创建分销商  真实姓名  手机号 父id
     * **/
    @RequestMapping(value = "/createDistribution", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Long>  distributorsCreate(@RequestBody Distributions distributions){
        NbResponse<Long> result = new NbResponse<Long>();
        Response<Long> ret = distributionsService.distributorsCreateInteger(distributions);
        result.setResult(ret.getResult(), key);
        if(!result.isSuccess()) {
            log.error("find distributionAuditAll failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        Distributions distributions2 = new Distributions();
        distributions2.setId(distributions.getId());

        String content = distributionPromotionQr+distributions.getId()+qr;
        String path = System.getProperty("java.io.tmpdir");
        Map hints = new HashMap();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = null;
        BitMatrix bitMatrix2 = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 400, 400, hints);
            File file = new File(path, ret.getResult() + "distributionPromotionQr.png");
            MatrixToImageWriter.writeToFile(bitMatrix, "png", file);
            String filePath = imageServer.write(file.getName(), file);
            UserImage userImage = new UserImage();
            userImage.setUserId(distributions.getUserId());
            userImage.setFileName(file.getName() + "distributionPromotionQr.png");
            userImage.setFileSize((int)file.length());
            imageService.addUserImage(userImage);
            UploadedFile u = new UploadedFile(userImage.getId(), file.getName(),
                    Long.valueOf(file.length()).intValue(),
                    imageBaseUrl + filePath);
            distributions2.setPromotionQr(imageBaseUrl + filePath);

            String content2 = distributionStoreQr+distributions.getId()+qr;
            String path2 = System.getProperty("java.io.tmpdir");
            Map hints2 = new HashMap();
            hints2.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints2.put(EncodeHintType.MARGIN, 1);

            bitMatrix2 = new MultiFormatWriter().encode(content2, BarcodeFormat.QR_CODE, 400, 400, hints2);
            File file2 = new File(path2, ret.getResult() + "distributionStoreQr.png");
            MatrixToImageWriter.writeToFile(bitMatrix2, "png", file2);
            String filePath2 = imageServer.write(file2.getName(), file2);
            UserImage userImage2 = new UserImage();
            userImage2.setUserId(distributions.getUserId());
            userImage2.setFileName(file.getName() + "distributionStoreQr.png");
            userImage2.setFileSize((int)file.length());
            imageService.addUserImage(userImage);
            UploadedFile u2 = new UploadedFile(userImage2.getId(), file2.getName(),
                    Long.valueOf(file2.length()).intValue(),
                    imageBaseUrl + filePath2);
            distributions2.setStoreQr(imageBaseUrl + filePath2);

            distributionsService.updateDistributor(distributions2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 更新分销商  设置
     * **/
    @RequestMapping(value = "/editSet", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String distributorEdit(@RequestBody Distributions distributions) {
        Response<Boolean> result = distributionsService.distributionsUpdate(distributions);
        if(!result.isSuccess()) {
            log.error("find distributionAuditAll failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "success";
    }
    /**
     * @param id  渠道, 必填
    //     * @param sign     签名, 必填
     *
     *
     * 返回分销商的详细信息(含统计信息)
     */
    @RequestMapping(value = "/distributionInfo/{id}", method = RequestMethod.GET)
    @ResponseBody
    public NbResponse<DistributionInfoForQuery> distributorInfo(@PathVariable("id") Long id,HttpServletRequest request) {
        NbResponse<DistributionInfoForQuery> result = new NbResponse<DistributionInfoForQuery>();
        try {
            Response<DistributionInfoForQuery> distributionInfoResult = distributionsService.selectSummaryByKey(id);
            String avatar = distributionsService.getAvatar(distributionInfoResult.getResult().getUserId());
            distributionInfoResult.getResult().setAvatar(avatar);
            checkState(distributionInfoResult.isSuccess(), distributionInfoResult.getError());
            result.setResult(distributionInfoResult.getResult(), key);
        } catch (IllegalArgumentException e) {
            log.error("fail to get distributorInfo, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get distributorInfo, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get distributorInfo", e);
            result.setError(messageSources.get("distributorInfo.query.fail"));
        }
        return result;
    }

    /**
     * @param id  渠道, 必填
    //     * @param sign     签名, 必填
     *
     *
     * 返回商场所有商品类目
     */
    @RequestMapping(value = "/amountDetail/{id}/{pageNo}/{size}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Paging<AmountDetail>> amountDetail(@PathVariable("id") Long id,@PathVariable("pageNo") Integer pageNo,
                                                                          @PathVariable("size") Integer size) {
        NbResponse<Paging<AmountDetail>> result = new NbResponse<Paging<AmountDetail>>();
        try {
            AmountDetail amountDetail = new AmountDetail();
            amountDetail.setId(id);
            Response<Paging<AmountDetail>> amountDetailResult = amountDetailService.selectAmountDetail(amountDetail, pageNo, size);
            checkState(amountDetailResult.isSuccess(), amountDetailResult.getError());
            result.setResult(amountDetailResult.getResult(), key);
        } catch (IllegalArgumentException e) {
            log.error("fail to get AmountDetail, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get AmountDetail, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get AmountDetail", e);
            result.setError(messageSources.get("AmountDetail.query.fail"));
        }
        return result;
    }
    /**
     * @param parentId  渠道, 必填
    //     * @param sign     签名, 必填
     *
     *
     * 通过parentid和level查询分销商信息
     */
    @RequestMapping(value = "/distributionInfoByLevel/{parentId}/{level}/{pageNo}/{size}", method = RequestMethod.GET)
    @ResponseBody
    public NbResponse<Paging<DistributionInfo>> distributorInfo(@PathVariable("parentId") Long parentId,@PathVariable("level") String level,
                                                                   @PathVariable("pageNo") Integer pageNo, @PathVariable("size") Integer size) {
        NbResponse<Paging<DistributionInfo>> result = new NbResponse<Paging<DistributionInfo>>();

        try {

            Response<Paging<DistributionInfo>> distributionInfoResult = distributionsService.getDistributionByLevel(parentId, level, pageNo, size);
            checkState(distributionInfoResult.isSuccess(), distributionInfoResult.getError());
            result.setResult(distributionInfoResult.getResult(), key);
        } catch (IllegalArgumentException e) {
            log.error("fail to get distributorInfo, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get distributorInfo, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get distributorInfo", e);
            result.setError(messageSources.get("distributorInfo.query.fail"));
        }
        return result;
    }

    @RequestMapping(value = "/getConcernMember/{distributorId}/{pageNo}/{size}", method = RequestMethod.GET)
    @ResponseBody
    public NbResponse<Paging<ConcernMember>> getConcernMember(@PathVariable("distributorId") Long distributorId
            , @PathVariable("pageNo") Integer pageNo, @PathVariable("size") Integer size) {
        NbResponse<Paging<ConcernMember>> result = new NbResponse<Paging<ConcernMember>>();

        try {
            ConcernMember concernMember = new ConcernMember();
            concernMember.setDistributorId(distributorId);
            Response<Paging<ConcernMember>> concernMemberInfoResult = concernMemberService.getConcernMember(concernMember, pageNo, size);
            checkState(concernMemberInfoResult.isSuccess(), concernMemberInfoResult.getError());
            result.setResult(concernMemberInfoResult.getResult(), key);
        } catch (IllegalArgumentException e) {
            log.error("fail to get concernMemberInfo, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get concernMemberInfo, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get concernMemberInfo", e);
            result.setError(messageSources.get("concernMemberInfo.query.fail"));
        }
        return result;
    }

    @RequestMapping(value = "/createConcernMember", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Long>  createConcernMember(@RequestBody ConcernMember concernMember){
        NbResponse<Long> result = new NbResponse<Long>();
        concernMember.setOperTime(new Date());
        Response<Long> ret = concernMemberService.createConcernMember(concernMember);
        result.setResult(ret.getResult(), key);
        if(!result.isSuccess()) {
            log.error("find createConcernMember failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result;
    }

    @RequestMapping(value = "/getDistributorUser/{distributorId}/{pageNo}/{size}", method = RequestMethod.GET)
    @ResponseBody
    public NbResponse<Paging<DistributorUser>> getDistributorUser(@PathVariable("distributorId") Long distributorId
            , @PathVariable("pageNo") Integer pageNo, @PathVariable("size") Integer size) {
        NbResponse<Paging<DistributorUser>> result = new NbResponse<Paging<DistributorUser>>();

        try {
            DistributorUser distributorUser = new DistributorUser();
            distributorUser.setDistributorId(distributorId);
            Response<Paging<DistributorUser>> distributorUserInfoResult = distributorService.getDistributorUser(distributorUser, pageNo, size);
            checkState(distributorUserInfoResult.isSuccess(), distributorUserInfoResult.getError());
            result.setResult(distributorUserInfoResult.getResult(), key);
        } catch (IllegalArgumentException e) {
            log.error("fail to get distributorUserInfo, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get distributorUserInfo, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get distributorUserInfo", e);
            result.setError(messageSources.get("distributorUserInfo.query.fail"));
        }
        return result;
    }

    @RequestMapping(value = "/createDistributorUser", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Long>  createDistributorUser(@RequestBody DistributorUser distributorUser){
        NbResponse<Long> result = new NbResponse<Long>();
        distributorUser.setOperTime(new Date());
        Response<Long> ret = distributorService.createDistributorUser(distributorUser);
        result.setResult(ret.getResult(), key);
        if(!result.isSuccess()) {
            log.error("find createConcernMember failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result;
    }

    /**
     * 获取用户订单列表
     * @param distributorId           分销商id
     * @param pageNo       页数
     * @param size    每页条数
     */
    @RequestMapping(value = "/orders/{distributorId}/{status}/{pageNo}/{size}", method = RequestMethod.GET)
    @ResponseBody
    public NbResponse<Paging<RichOrderBuyerView>> orders(@PathVariable Long distributorId,
                                                            @PathVariable Integer status,
                                                            @PathVariable Integer pageNo,
                                                            @PathVariable Integer size) {
        NbResponse<Paging<RichOrderBuyerView>> result = new NbResponse<Paging<RichOrderBuyerView>>();
        if (status == 999) {
            status = null;
        }
        try {
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//
//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");


            Response<Paging<RichOrderBuyerView>> ordersGet = orderQueryService.findByBuyerId(null, pageNo, size, status, null, distributorId);
            for(RichOrderBuyerView a :ordersGet.getResult().getData()){
                Response<Shop> b = shopService.findByUserId(a.getSiteId());
                a.setShopId(b.getResult().getId());;
            }
            result.setResult(ordersGet.getResult(), key);

        } catch (IllegalArgumentException e) {
            log.error("fail to query orders with distributorId:{}, pageNo:{}, size:{}, session:{}, status:{}, error:{}",
                    distributorId, pageNo, size, null, status, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to query orders with distributorId:{}, pageNo:{}, size:{}, session:{}, status:{}, error:{}",
                    distributorId, pageNo, size, null, status, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query orders with distributorId:{}, pageNo:{}, size:{}, session:{}, status:{}",
                    distributorId, pageNo, size, null, status, e);
            result.setError(messageSources.get("order.query.fail"));
        }
        return result;
    }


    //付款
    @ResponseBody
    @RequestMapping(value = "/withdrawals", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public NbResponse<Boolean> payMoney(
             @RequestParam("openId") String openId
            ,@RequestParam(value = "money") Double money
            ,@RequestParam(value = "distributorId") Long distributorId
            ,HttpServletRequest request) {

        NbResponse<Boolean> result = new NbResponse<Boolean>();

        try {
            String spbillCreateIp = IpUtils.getIp2(request);
            //校验支付条件
            Response<HashMap<String,String>> preconditions  = distributionsService.withdrawalsPreconditions(distributorId, money);

            if(preconditions.getResult().get("retCode").equals("0")){
//            if(true){
                //证书文件路径
                String caFile = Configuration.getProperty("weixin4j.pay.file.path");
                //String caFile = "D:/doc/weixin_cert/apiclient_cert.p12";
                FileInputStream ca =null;
                MPPaymentResult mPPaymentResult = null;
                try {
                    ca = new FileInputStream(new File(caFile));
                    MPPayment payment = new MPPayment();
                    String mchAppid = Configuration.getOAuthAppId();
                    String mchid = Configuration.getProperty("weixin4j.pay.partner.id");
                    String nonceStr = java.util.UUID.randomUUID().toString().substring(0, 15);

                    //生成商户订单号
                    String partnerTradeNo = this.getBusinessOrderID();
                    String checkName = "NO_CHECK";
                    String desc = "企业付款给个人";

                    payment.setMchAppid(mchAppid);				 //公众账号appid
                    payment.setMchid(mchid);					 //商户号
                    payment.setNonceStr(nonceStr);				 //随机字符串
                    payment.setPartnerTradeNo(partnerTradeNo);	 //商户订单号
                    payment.setOpenId(openId);					 //用户openid
                    payment.setCheckName(checkName);			 //校验用户姓名选项
                    payment.setAmount(MathUtils.formaFee2Fen(Double.valueOf(money)));//金额
                    payment.setDesc(desc);              		//企业付款描述信息
                    payment.setSpbillCreateIp(spbillCreateIp);  //Ip地址

                    //获取商户密钥
                    String paternerKey = Configuration.getProperty("weixin4j.pay.partner.key");
                    //生成签名
                    String sign = SignUtil.getSign(payment.toMap(), paternerKey);  //签名 是
                    payment.setSign(sign);						 //签名

                    Weixin wx = new Weixin();
                    mPPaymentResult = Weixin.mchPayment(ca, payment,mchid);
                }
                catch (WeixinException e) {
                    result.setResult(Boolean.FALSE);
                    result.setMsg(e.getMessage());
                } catch (FileNotFoundException e) {
                    result.setResult(Boolean.FALSE);
                    result.setMsg(e.getMessage());
                } catch (CertificateException e) {
                    result.setResult(Boolean.FALSE);
                    result.setMsg(e.getMessage());
                } catch (UnrecoverableKeyException e) {
                    result.setResult(Boolean.FALSE);
                    result.setMsg(e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    result.setResult(Boolean.FALSE);
                    result.setMsg(e.getMessage());
                } catch (KeyStoreException e) {
                    result.setResult(Boolean.FALSE);
                    result.setMsg(e.getMessage());
                } catch (KeyManagementException e) {
                    result.setResult(Boolean.FALSE);
                    result.setMsg(e.getMessage());
                } catch (IOException e) {
                    result.setMsg(e.getMessage());
                    result.setResult(Boolean.FALSE);
                } finally {
                    if (ca != null) {
                        try {
                            ca.close();
                        } catch (IOException e) {
                            result.setMsg(e.getMessage());
                        }
                    }
                }


                //转帐成功
                if (mPPaymentResult.getResult_code().equals("SUCCESS")) {
                    result.setResult(Boolean.TRUE);
                    AmountWithdrawalHistory amountWithdrawalHistory = new AmountWithdrawalHistory();
                    amountWithdrawalHistory.setPayType("0");
                    amountWithdrawalHistory.setBankAccountNumber(openId);
                    amountWithdrawalHistory.setBankSerialNumber(mPPaymentResult.getTransactionId());
                    amountWithdrawalHistory.setBankSysTime(new Date());
                    amountWithdrawalHistory.setMoney(money+"");
                    amountWithdrawalHistory.setOperationTime(new Date());
                    amountWithdrawalHistory.setDistributorsId(distributorId);
                    amountWithdrawalHistoryService.insert(amountWithdrawalHistory);
                } else {
                    result.setMsg("微信转账失败,原因:"+mPPaymentResult.getErr_code_des());
                    result.setResult(Boolean.FALSE);

                }
            }else{//校验条件没有通过
                result.setResult(Boolean.FALSE);
                String msg = preconditions.getResult().get("retMsg");
                result.setMsg(msg);

            }



        } catch (Exception e) {
            result.setMsg(e.getMessage());
            result.setResult(Boolean.FALSE);
        }


        return result;
    }


    //获取商户订单号
    public String getBusinessOrderID(){
        return  System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 8);
    }


    /**
     * @param distibutorId  渠道, 必填
    //     * @param sign     签名, 必填
     *
     *
     * 根据商家id返回提现记录
     */
    @RequestMapping(value = "/withdrawal/{id}/{pageNo}/{size}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Paging<AmountWithdrawalHistory>> withdrawalHistory(@PathVariable("id") Long distibutorId,@PathVariable("pageNo") Integer pageNo,
                                                            @PathVariable("size") Integer size) {
        NbResponse<Paging<AmountWithdrawalHistory>> result = new NbResponse<Paging<AmountWithdrawalHistory>>();
        try {
            AmountWithdrawalHistory withdrawal = new AmountWithdrawalHistory();
            DistributionInfo distributionInfo = new DistributionInfo();
            distributionInfo.setId(distibutorId);
            withdrawal.setDistributionInfo(distributionInfo);
            Response<Paging<AmountWithdrawalHistory>> withdrawalResult = amountWithdrawalHistoryService.selectWithdrawal(withdrawal, pageNo, size);
            checkState(withdrawalResult.isSuccess(), withdrawalResult.getError());
            result.setResult(withdrawalResult.getResult(), key);
        } catch (IllegalArgumentException e) {
            log.error("fail to get AmountDetail, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get AmountDetail, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get AmountDetail", e);
            result.setError(messageSources.get("AmountDetail.query.fail"));
        }
        return result;
    }

    /**
     * 更新分销商  设置
     * **/
    @RequestMapping(value = "/updateDistributor", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean>  updateDistributor(@RequestBody Distributions distribution) {
        Response<Boolean> result = distributionsService.updateDistributor(distribution);
        if(!result.isSuccess()) {
            log.error("find updateDistributor failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result;
    }

    @RequestMapping(value = "/getDistributionConfKey/{key}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> getDistributionConfKey(@PathVariable("key") String key) {
        Response<String> result = new Response<String>();
        try {
            result =  distributionsService.getDistributionConfKey(key);
        } catch (IllegalArgumentException e) {
            log.error("fail to get getDistributionConfKey, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get getDistributionConfKey, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get getDistributionConfKey", e);
            result.setError(messageSources.get(e.getMessage()));
        }
        return result;
    }
}
