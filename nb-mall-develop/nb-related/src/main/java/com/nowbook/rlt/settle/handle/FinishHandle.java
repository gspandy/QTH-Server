package com.nowbook.rlt.settle.handle;

import com.nowbook.common.model.PageInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.rlt.settle.dao.OrderAlipayCashDao;
import com.nowbook.rlt.settle.dao.SettlementDao;
import com.nowbook.rlt.settle.enums.JobStatus;
import com.nowbook.rlt.settle.model.OrderAlipayCash;
import com.nowbook.rlt.settle.model.SettleJob;
import com.nowbook.rlt.settle.model.Settlement;
import com.nowbook.rlt.settle.service.SettlementService;
import com.nowbook.sdp.dao.*;
import com.nowbook.sdp.model.*;
import com.nowbook.sdp.service.UserWalletService;
import com.nowbook.trade.model.Order;
import com.nowbook.trade.service.OrderQueryService;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.nowbook.unionpay.acp.sdk.AcpService;
import com.nowbook.unionpay.acp.sdk.LogUtil;
import com.nowbook.unionpay.acp.sdk.SDKConfig;
import com.nowbook.user.model.User;
import com.nowbook.user.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.nowbook.common.utils.Arguments.equalWith;
import static com.nowbook.common.utils.Arguments.isEmpty;
import static com.nowbook.rlt.settle.util.SettlementVerification.done;
import static com.nowbook.rlt.settle.util.SettlementVerification.isCod;
import static com.nowbook.rlt.settle.util.SettlementVerification.isPlain;
import static org.elasticsearch.common.base.Preconditions.checkState;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-05-30 9:28 PM  <br>
 * Author:cheng
 */
@Slf4j
@Component
public class FinishHandle extends JobHandle {


    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private UserLevelDao userLevelDao;

    @Autowired
    private LevelDao levelDao;
    @Autowired
    private UserWalletDao userWalletDao;
    @Autowired
    private OrderAlipayCashDao orderAlipayCashDao;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserEarningsBonusesDao userEarningsBonusesDao;

    @Autowired
    private PaymentDetailDao paymentDetailDao;

    @Autowired
    private PaymentDetailDayDao paymentDetailDayDao;

    @Autowired
    private UserBankDao userBankDao;
    @Autowired
    private SystemConfRedisDao systemConfRedisDao;
    @Autowired
    private UserWalletService userWalletService;

    @Value("#{app.acpsdkMerId}")
    private String acpsdkMerId;

    @Value("#{app.acpsdkPayUrl}")
    private String acpsdkPayUrl;

    /**
     * 周三发放奖金和收益
     *
     * @param job  任务信息
     */
    public void payEarningsBonuses(SettleJob job) {
        if (equalWith(job.getStatus(), JobStatus.DONE.value())) return;      // 完成的任务无需再次处理

        log.info("[PAY-EARNINGS-BONUSES] begin at {}", DFT.print(DateTime.now()));
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            checkState(dependencyOk(job), "job.dependency.not.over");
            settleJobDao.ing(job.getId());

            Integer pageNo = 1;
            Date startAt = new DateTime(job.getDoneAt()).withTimeAtStartOfDay().minusDays(4).toDate();
            Date endAt = new DateTime(job.getDoneAt()).withTimeAtStartOfDay().minusDays(1).toDate();
            PaymentDetailDay paymentDetailDay = new PaymentDetailDay();
            paymentDetailDay.setCreateStartAt(startAt);
            paymentDetailDay.setCreateEndAt(endAt);
            paymentDetailDay.setOffset(0);
            paymentDetailDay.setLimit(10);
            List<PaymentDetailDay> paymentDetailDayList = paymentDetailDayDao.findBy(paymentDetailDay);
            PaymentDetailDay pdd = paymentDetailDayList.get(0);

            boolean next = batchPayEarningsBonuses(pdd, pageNo, BATCH_SIZE);

            while (next) {
                pageNo++;
                next = batchPayEarningsBonuses(pdd, pageNo, BATCH_SIZE);
            }
            PaymentDetailDay newPdd = new PaymentDetailDay();
            newPdd.setId(pdd.getId());
            newPdd.setFailNum(pdd.getFailNum());
            if(newPdd.getFailNum()>0){
                paymentDetailDayDao.update(newPdd);
            }

            settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));
            log.info("[PAY-EARNINGS-BONUSES] successfully done");


        } catch (IllegalStateException e) {
            log.error("[PAY-EARNINGS-BONUSES] failed with job:{}, error:{}", job, e);
            settleJobDao.fail(job.getId());
        } catch (Exception e) {
            log.error("[PAY-EARNINGS-BONUSES] failed with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        log.info("[PAY-EARNINGS-BONUSES] end at {}, cost {}", DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    private boolean batchPayEarningsBonuses(PaymentDetailDay paymentDetailDay, int pageNo, int size) {
        PaymentDetail paymentDetail = new PaymentDetail();
        paymentDetail.setIdNo(paymentDetailDay.getIdNo());
        paymentDetail.setPayStatus(1);
        paymentDetail.setOffset((pageNo-1)*size);
        paymentDetail.setLimit(size);
        List<PaymentDetail> paymentDetailList = paymentDetailDao.findBy(paymentDetail);
        if(paymentDetailList==null || paymentDetailList.size()==0){
            return false;
        }
        int current = paymentDetailList.size();
        for (PaymentDetail pd : paymentDetailList) {
            try {
                if(!pd.getPayStatus().equals(1)){
                    continue;
                }
                UserBank userBank = new UserBank();
                userBank.setUserId(pd.getUserId());
                List<UserBank> userBankList = userBankDao.selectByUserId(userBank);
                if(userBankList==null || userBankList.size()==0 || !userBankList.get(0).getType().equals(3)){
                    PaymentDetail newPd = new PaymentDetail();
                    newPd.setId(pd.getId());
                    newPd.setReason("没有填写银行卡。");
                    newPd.setPayStatus(2);
                    newPd.setPayResult(2);
                    paymentDetailDao.update(newPd);
                    paymentDetailDay.setFailNum(paymentDetailDay.getFailNum()+1);
                    continue;
                }
                Thread.sleep(1000*1);
                SDKConfig.getConfig().loadPropertiesFromSrc();
                Map<String, String> data = new HashMap<String, String>();

                /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
                data.put("version", SDKConfig.getConfig().getVersion());            //版本号 全渠道默认值
                data.put("encoding", "UTF-8");     //字符集编码 可以使用UTF-8,GBK两种方式
                data.put("signMethod", SDKConfig.getConfig().getSignMethod()); //签名方法
                data.put("txnType", "12");              		 	//交易类型 12：代付
                data.put("txnSubType", "00");           		 	//默认填写00
                data.put("bizType", "000401");          		 	//000401：代付
                data.put("channelType", "07");          		 	//渠道类型

                /***商户接入参数***/
                data.put("merId", acpsdkMerId);   		 				//商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
                data.put("accessType", "0");            		 	//接入类型，商户接入填0 ，不需修改（0：直连商户， 1： 收单机构 2：平台商户）
                data.put("orderId", getCurrentTime()+pd.getId().toString());        	 	    	//商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
                data.put("txnTime", getCurrentTime());		 		    	//订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
                data.put("accType", "01");					 		//账号类型 01：银行卡
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
                data.put("encryptCertId",AcpService.getEncryptCertId());      						//上送敏感信息加密域的加密证书序列号
                String accNo = AcpService.encryptData(userBankList.get(0).getBankCardNo(), "UTF-8"); 	//这里测试的时候使用的是测试卡号，正式环境请使用真实卡号
                data.put("accNo", accNo);
                //////////

                /////////商户未开通敏感信息加密的权限那么不对敏感信息加密使用：
                //contentData.put("accNo", userBankList.get(0).getBankCardNo());                  				//这里测试的时候使用的是测试卡号，正式环境请使用真实卡号
                ////////

                //代付交易的上送的卡验证要素：姓名或者证件类型+证件号码
                Map<String,String> customerInfoMap = new HashMap<String,String>();
                customerInfoMap.put("certifTp", "01");						    //证件类型
                customerInfoMap.put("certifId", userBankList.get(0).getBankCardUserNo());		    //证件号码
                customerInfoMap.put("customerNm", "全渠道");					//姓名
                String customerInfoStr = AcpService.getCustomerInfo(customerInfoMap,userBankList.get(0).getBankCardNo(),"UTF-8");

                data.put("customerInfo", customerInfoStr);
                data.put("txnAmt", pd.getMoney().toString());						 		    //交易金额 单位为分，不能带小数点
                data.put("currencyCode", "156");                    	    //境内商户固定 156 人民币
                data.put("billNo", "钱唐荟奖金与收益");                    	            //银行附言。会透传给发卡行，完成改造的发卡行会把这个信息在账单、短信中显示给用户的，请按真实情况填写。


                //后台通知地址（需设置为外网能访问 http https均可），支付成功后银联会自动将异步通知报文post到商户上送的该地址，【支付失败的交易银联不会发送后台通知】
                //后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费交易 商户通知
                //注意:1.需设置为外网能访问，否则收不到通知    2.http https均可  3.收单后台通知后需要10秒内返回http200或302状态码
                //    4.如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200或302，那么银联会间隔一段时间再次发送。总共发送5次，银联后续间隔1、2、4、5 分钟后会再次通知。
                //    5.后台通知地址如果上送了带有？的参数，例如：http://abc/web?a=b&c=d 在后台通知处理程序验证签名之前需要编写逻辑将这些字段去掉再验签，否则将会验签失败
                data.put("backUrl", acpsdkPayUrl);

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
                String requestBackUrl = SDKConfig.getConfig().getBackRequestUrl();									 //交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl

                Map<String, String> rspData = AcpService.post(reqData,requestBackUrl,"UTF-8");        //发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
                /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
                //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
                if(!rspData.isEmpty()){
                    if(AcpService.validate(rspData, "UTF-8")){
                        LogUtil.writeLog("验证签名成功");
                        String respCode = rspData.get("respCode");
                        if(("00").equals(respCode)){
                            PaymentDetail newPd = new PaymentDetail();
                            newPd.setId(pd.getId());
                            newPd.setPayType(3);
                            newPd.setPayMoney(pd.getMoney());
                            newPd.setPayId(userBankList.get(0).getBankCardNo());
                            newPd.setPayName(userBankList.get(0).getBankUser());
                            newPd.setPayStatus(2);
                            paymentDetailDao.update(newPd);
                        }else {
                            PaymentDetail newPd = new PaymentDetail();
                            newPd.setId(pd.getId());
                            newPd.setReason(rspData.get("respMsg"));
                            newPd.setPayStatus(2);
                            newPd.setPayResult(2);
                            paymentDetailDao.update(newPd);
                            paymentDetailDay.setFailNum(paymentDetailDay.getFailNum()+1);
                            continue;
                        }
                    }else{
                        LogUtil.writeErrorLog("验证签名失败");
                        //TODO 检查验证签名失败的原因
                        PaymentDetail newPd = new PaymentDetail();
                        newPd.setId(pd.getId());
                        newPd.setReason("验证签名失败");
                        newPd.setPayStatus(2);
                        newPd.setPayResult(2);
                        paymentDetailDao.update(newPd);
                        paymentDetailDay.setFailNum(paymentDetailDay.getFailNum()+1);
                        continue;
                    }
                }else{
                    //未返回正确的http状态
                    LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
                    PaymentDetail newPd = new PaymentDetail();
                    newPd.setId(pd.getId());
                    newPd.setReason("未获取到返回报文或返回http状态码非200");
                    newPd.setPayStatus(2);
                    newPd.setPayResult(2);
                    paymentDetailDao.update(newPd);
                    paymentDetailDay.setFailNum(paymentDetailDay.getFailNum()+1);
                    continue;
                }
            } catch (Exception e) {
                PaymentDetail newPd = new PaymentDetail();
                newPd.setId(pd.getId());
                newPd.setReason(e.getMessage());
                newPd.setPayStatus(2);
                newPd.setPayResult(2);
                paymentDetailDao.update(newPd);
                paymentDetailDay.setFailNum(paymentDetailDay.getFailNum()+1);
                continue;
            }
        }

        return current == size;  // 判断是否存在下一个要处理的批次
    }
    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }
    /**
     * 周日结算奖金和收益
     *
     * @param job  任务信息
     */
    public void sumEarningsBonuses(SettleJob job) {
        if (equalWith(job.getStatus(), JobStatus.DONE.value())) return;      // 完成的任务无需再次处理

        log.info("[SUM-EARNINGS-BONUSES] begin at {}", DFT.print(DateTime.now()));
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            checkState(dependencyOk(job), "job.dependency.not.over");
            settleJobDao.ing(job.getId());

            PaymentDetailDay paymentDetailDay = new PaymentDetailDay();
            paymentDetailDay.setFee(0L);
            paymentDetailDay.setTotalNum(0);
            paymentDetailDay.setIdNo(getCurrentTime());
            Integer pageNo = 1;
            boolean next = batchSumEarningsBonuses(job.getDoneAt(), pageNo, BATCH_SIZE,paymentDetailDay);

            while (next) {
                pageNo++;
                next = batchSumEarningsBonuses(job.getDoneAt(), pageNo, BATCH_SIZE,paymentDetailDay);
            }
            paymentDetailDay.setSuccessNum(0);
            paymentDetailDay.setFailNum(0);
            Double commissionRate = Double.valueOf(systemConfRedisDao.getDate("unionpay_exchange_rate"));
            paymentDetailDay.setCommissionRate(commissionRate);
            Double commissionFee = paymentDetailDay.getFee() * commissionRate /100;
            paymentDetailDay.setCommissionFee(Math.round(commissionFee));
            paymentDetailDay.setTotalFee(paymentDetailDay.getCommissionFee()+paymentDetailDay.getFee());
            paymentDetailDayDao.create(paymentDetailDay);


            settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));
            log.info("[SUM-EARNINGS-BONUSES] successfully done");


        } catch (IllegalStateException e) {
            log.error("[SUM-EARNINGS-BONUSES] failed with job:{}, error:{}", job, e);
            settleJobDao.fail(job.getId());
        } catch (Exception e) {
            log.error("[SUM-EARNINGS-BONUSES] failed with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        log.info("[SUM-EARNINGS-BONUSES] end at {}, cost {}", DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));

    }

    private boolean batchSumEarningsBonuses(Date doneAt, int pageNo, int size,PaymentDetailDay paymentDetailDay) {
        Date startAt = new DateTime(doneAt).withTimeAtStartOfDay().minusDays(7).toDate();
        Date endAt = new DateTime(doneAt).withTimeAtStartOfDay().toDate();
        Response<Paging<User>> userList = accountService.findUser(new HashMap<String, String>(),pageNo,size);
        List<User> list = userList.getResult().getData();
        int current = userList.getResult().getData().size();
        for (User user : list) {
            try {
                UserEarningsBonuses userEarningsBonuses = new UserEarningsBonuses();
                userEarningsBonuses.setStartAt(startAt);
                userEarningsBonuses.setEndAt(endAt);
                userEarningsBonuses.setUserId(user.getId());
                List<UserEarningsBonuses> userEarningsBonusesList = userEarningsBonusesDao.sum(userEarningsBonuses);
                if(userEarningsBonusesList ==null || userEarningsBonusesList.size() == 0){
                    continue;
                }
                PaymentDetail paymentDetail = new PaymentDetail();
                paymentDetail.setUserId(user.getId());
                for(UserEarningsBonuses ueb : userEarningsBonusesList){
                    if(ueb.getMoneyType().equals(1)){
                        paymentDetail.setEarnings(ueb.getMoney());
                    }
                    if(ueb.getMoneyType().equals(2)){
                        paymentDetail.setBonuses(ueb.getMoney());
                    }
                }
                if(paymentDetail.getBonuses()==null){
                    paymentDetail.setBonuses(0L);
                }
                if(paymentDetail.getEarnings()==null){
                    paymentDetail.setEarnings(0L);
                }
                paymentDetail.setPayStatus(1);
                paymentDetail.setMoney(paymentDetail.getBonuses()+paymentDetail.getEarnings());
                paymentDetail.setDeposit(0L);
                paymentDetail.setIdNo(paymentDetailDay.getIdNo());
                if(paymentDetail.getMoney()>0){
                    UserLevel userLevel = new UserLevel();
                    userLevel.setUserId(user.getId());
                    List<UserLevel> userLevelList = userLevelDao.selectByUserId(userLevel);
                    if(userLevelList ==null || userLevelList.size() ==0){
                        continue;
                    }
                    if(userLevelList.get(0).getLevel().equals(5)){
                        Level level = new Level();
                        level.setLevel(userLevelList.get(0).getLevel());
                        List<Level> levelList = levelDao.selectByLevel(level);
                        if(levelList == null || levelList.size() == 0){
                            continue;
                        }
                        UserWallet userWallet = new UserWallet();
                        userWallet.setUserId(user.getId());
                        List<UserWallet> userWalletList = userWalletDao.selectBy(userWallet);
                        if(userWalletList == null || userWalletList.size() == 0){
                            continue;
                        }
                        if(userWalletList.get(0).getDeposit()<levelList.get(0).getDeposit()){
                            Long money = 0L;
                            money = paymentDetail.getMoney()*10/100;
                            if(userWalletList.get(0).getDeposit()+money>levelList.get(0).getDeposit()){
                                money = levelList.get(0).getDeposit()-userWalletList.get(0).getDeposit();
                            }
                            UserWalletSummary userWalletSummary = new UserWalletSummary();
                            userWalletSummary.setUserId(user.getId());
                            userWalletSummary.setMoney(money);
                            userWalletSummary.setType(33);
                            userWalletService.updateUserWallet(userWalletSummary);

                            paymentDetail.setMoney(paymentDetail.getMoney()-money);
                            paymentDetail.setDeposit(money);
                        }
                        paymentDetailDao.create(paymentDetail);
                    }else{
                        paymentDetailDao.create(paymentDetail);
                    }
                }
                paymentDetailDay.setFee(paymentDetailDay.getFee()+paymentDetail.getMoney());
                paymentDetailDay.setTotalNum(paymentDetailDay.getTotalNum()+1);
            } catch (IllegalArgumentException e) {
                log.error("sumEarningsBonuses({}) as finished raise error", user.getId(), e);
            }
        }
        return current == size;  // 判断是否存在下一个要处理的批次
    }



    /**
     * 添加结算数据，确认收货后7天的数据加入settlement表
     *
     * @param job  任务信息
     */
    public void createSettlements(SettleJob job) {
        if (equalWith(job.getStatus(), JobStatus.DONE.value())) return;      // 完成的任务无需再次处理

        log.info("[CREATE-SETTLEMENT] begin at {}", DFT.print(DateTime.now()));
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            checkState(dependencyOk(job), "job.dependency.not.over");
            settleJobDao.ing(job.getId());

            Integer pageNo = 1;
            boolean next = batchCreateSettlements(job.getTradedAt(), pageNo, BATCH_SIZE);

            while (next) {
                pageNo++;
                next = batchCreateSettlements(job.getTradedAt(), pageNo, BATCH_SIZE);
            }

            settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));
            log.info("[CREATE-SETTLEMENT] successfully done");


        } catch (IllegalStateException e) {
            log.error("[CREATE-SETTLEMENT] failed with job:{}, error:{}", job, e);
            settleJobDao.fail(job.getId());
        } catch (Exception e) {
            log.error("[CREATE-SETTLEMENT] failed with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        log.info("[CREATE-SETTLEMENT] end at {}, cost {}", DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));

    }


    private boolean batchCreateSettlements(Date doneAt, int pageNo, int size) {
        Date startAt = new DateTime(doneAt).withTimeAtStartOfDay().minusDays(Integer.valueOf(systemConfRedisDao.getDate("return_day_limit"))).toDate();
        Date endAt = new DateTime(startAt).plusDays(1).toDate();
        Order order = new Order();
        order.setStatus(3);
        Paging<Order> response = orderQueryService.findByCreateSettlement(order,pageNo,size,startAt,endAt).getResult();

        int current = response.getData().size();

        for (Order o : response.getData()) {
            try {
                if (!equalWith(o.getStatus(), Order.Status.DONE.value())) {
                    log.info("order(id:{}) status is not DONE, skipped", o.getId());
                    continue;
                }

                Response<Long> createResult = settlementService.generate(o.getId());
                checkState(createResult.isSuccess(), createResult.getError());

            } catch (IllegalArgumentException e) {
                log.error("createSettlements({}) as finished raise error", o, e);
            }
        }
        return current == size;  // 判断是否存在下一个要处理的批次
    }

    /**
     * 标记 T-1 已完成（已完成 & 已提现）订单的结算状态为 “已关闭” <br/>
     * 关闭订单的同时需要更新对应的交易状态
     *
     * @param job  任务信息
     */
    public void markSettlementFinished(SettleJob job) {
        if (equalWith(job.getStatus(), JobStatus.DONE.value())) return;      // 完成的任务无需再次处理

        log.info("[MARK-SETTLEMENT-FINISHED] begin at {}", DFT.print(DateTime.now()));
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            checkState(dependencyOk(job), "job.dependency.not.over");
            settleJobDao.ing(job.getId());

            Integer pageNo = 1;
            boolean next = batchMarkSettlementFinished(job.getDoneAt(), pageNo, BATCH_SIZE);

            while (next) {
                pageNo++;
                next = batchMarkSettlementFinished(job.getDoneAt(), pageNo, BATCH_SIZE);
            }

            settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));
            log.info("[MARK-SETTLEMENT-FINISHED] successfully done");


        } catch (IllegalStateException e) {
            log.error("[MARK-SETTLEMENT-FINISHED] failed with job:{}, error:{}", job, e);
            settleJobDao.fail(job.getId());
        } catch (Exception e) {
            log.error("[MARK-SETTLEMENT-FINISHED] failed with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        log.info("[MARK-SETTLEMENT-FINISHED] end at {}, cost {}", DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));

    }

    /**
     * 批量标记 已完成
     *
     * @param size     批次数量
     * @return  是否存在下一个批次
     */
    private boolean batchMarkSettlementFinished(Date doneAt, int pageNo, int size) {
        // 获取所有 “已提现”、但 “未结算” 的订单
        List<Settlement> settlements = getSettlements(doneAt, pageNo, size);
        int current = settlements.size();

        for (Settlement settlement : settlements) {
            try {
                if (equalWith(settlement.getSettleStatus(), Settlement.SettleStatus.ING.value())) {
                    log.info("settlement(id:{}) has been settling, skipped", settlement);
                    continue;
                }

                // 标记订单结束
                markAsFinished(settlement);
            } catch (IllegalArgumentException e) {
                log.error("mark settlement({}) as finished raise error", settlement, e);
            }
        }
        return current == size;  // 判断是否存在下一个要处理的批次
    }

    /**
     * 如果此订单所有提现明细都已经提现，则标记"已提现"，否则标记为"未提现"
     * @param settlement 订单结算信息
     */
    private void markAsCashedAfFinished(Settlement settlement) {
        List<OrderAlipayCash> cashes = orderAlipayCashDao.findByOrderId(settlement.getOrderId());
        Boolean cashed = Boolean.TRUE;

        if (isEmpty(cashes)) {
            cashed = Boolean.FALSE;
        }

        for (OrderAlipayCash cash : cashes) {
            if (!equalWith(cash.getStatus(), OrderAlipayCash.Status.DONE.value())) {  // 只要有一笔提现未提现，标记订单为“未提现”
                cashed = Boolean.FALSE;
                break;
            }
        }

        if (cashed) {
            log.info("settlement(id:{}, orderId:{} sellerId:{}) cashed",
                    settlement.getId(), settlement.getOrderId(), settlement.getSellerId());
            settlement.setCashed(Settlement.Cashed.DONE.value());

        } else {
            log.info("settlement(id:{}, orderId:{} sellerId:{}) not cashed yet",
                    settlement.getId(), settlement.getOrderId(), settlement.getSellerId());
            settlement.setCashed(Settlement.Cashed.NOT.value());
        }
    }


    private List<Settlement> getSettlements(Date doneAt, int pageNo, int size) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        Date startAt = new DateTime(doneAt).minusDays(60).toDate();
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put("paidStartAt", startAt);
        params.put("paidEndAt", doneAt);
        params.put("offset", pageInfo.offset);
        params.put("limit", pageInfo.limit);

        Paging<Settlement> settlementPaging = settlementDao.findBy(params);
        return settlementPaging.getData();
    }


    /**
     * 标记指定的订单信息为已结束
     *
     * @param settlement 订单结算信息
     */
    private void markAsFinished(Settlement settlement) {
        try {

            Long orderId = settlement.getOrderId();
            Response<Order> orderQueryResult = orderQueryService.findById(orderId);
            checkState(orderQueryResult.isSuccess(), orderQueryResult.getError());

            Order order = orderQueryResult.getResult();
            if (order.getFinishedAt() == null) {    // 订单尚未关闭
                return;
            }
            //判断是否 标记为已提现
            markAsCashedAfFinished(settlement);

            settlement.setFinished(Settlement.Finished.DONE.value());
            settlement.setFinishedAt(order.getFinishedAt());     // 记录订单完成时间
            settlement.setTradeStatus(order.getStatus());   // 记录订单关闭时的交易状态


            if (equalWith(settlement.getCashed(), Settlement.Cashed.DONE.value())) {  // 当提现完成 标记结算中
                settlement.setSettleStatus(Settlement.SettleStatus.ING.value());
            }

            if (isPlain(order) && isCod(order) && done(order)) {    // 普通货到付款订单直接已提现，且标记结算中
                settlement.setCashed(Settlement.Cashed.DONE.value());
                settlement.setSettleStatus(Settlement.SettleStatus.ING.value());
            }

            // 创建子订单的结算信息
            settlementManager.finished(settlement);

        } catch (Exception e) {
            log.error("fail to finish settlement({})", settlement, e);
            markFinishedFail(settlement);
        }
    }


    /**
     * 判断所有提现明细是否都已经提现
     *
     * @param settlement 结算明细
     * @return  订单是否已经提现（所有明细都已提现）
     */
    private Boolean isOrderCashed(Settlement settlement) {
        OrderAlipayCash criteria = new OrderAlipayCash();
        criteria.setOrderId(settlement.getOrderId());
        criteria.setStatus(OrderAlipayCash.Status.NOT.value());
        Long notCased  = orderAlipayCashDao.countOf(criteria);
        return equalWith(notCased, 0L);
    }

    private void markFinishedFail(Settlement settlement) {
        Settlement updating = new Settlement();
        updating.setId(settlement.getId());
        updating.setSettleStatus(Settlement.SettleStatus.FAIL.value());
        updating.setFinished(Settlement.Finished.FAIL.value());
        settlementDao.update(updating);
    }
}
