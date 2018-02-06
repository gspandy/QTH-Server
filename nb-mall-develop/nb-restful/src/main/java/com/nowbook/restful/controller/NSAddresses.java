package com.nowbook.restful.controller;

import com.nowbook.common.model.Response;
import com.nowbook.common.utils.CommonConstants;
import com.nowbook.restful.dto.NbResponse;
import com.nowbook.restful.util.NSSessionUID;
import com.nowbook.session.AFSession;
import com.nowbook.session.AFSessionManager;
import com.nowbook.trade.model.UserTradeInfo;
import com.nowbook.trade.service.UserTradeInfoService;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import com.nowbook.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.nowbook.common.utils.Arguments.notEmpty;
import static com.nowbook.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;


/**
 * Date: 4/22/14
 * Time: 16:41
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */

@Slf4j
@Controller
@RequestMapping("/api/extend/address")
public class NSAddresses {
    
    private final AFSessionManager sessionManager = AFSessionManager.instance();

    @Autowired
    UserTradeInfoService userTradeInfoService;

    @Autowired
    MessageSources messageSources;

    @Value("#{app.restkey}")
    String key;

    /**
     * 设置一个默认的收获地址
     *
     * @param addressId        默认收货地址id, 必填
//     * @param userId        用户id
//     * @param sessionId 会话id, 必填
//     * @param channel   渠道, 必填
//     * @param sign      签名, 必填
     *
     * @return          操作状态
     */
    @RequestMapping(value = "/default", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Boolean> setDefault(

                                          @RequestParam("addressId") Long addressId,
//                                             @RequestParam("session") String sessionId,
//                                             @RequestParam("channel") String channel,
//                                             @RequestParam("sign") String sign,
                                             HttpServletRequest request) {
        NbResponse<Boolean> result = new NbResponse<Boolean>();
        BaseUser baseUser=new BaseUser();
        try {
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");
//            Long userId = ((Integer) session.getAttribute(CommonConstants.SESSION_USER_ID)).longValue();
            baseUser= UserUtil.getCurrentUser();
            Response<Boolean> setDefaultResult = userTradeInfoService.makeDefault(baseUser.getId(), addressId);
            checkState(setDefaultResult.isSuccess(), setDefaultResult.getError());

            result.setResult(Boolean.TRUE);
//            result.setSessionId(request);

        } catch (IllegalArgumentException e) {
            log.error("fail to set default trade info with tradeInfoId:{},  error:{}", addressId,  e.getMessage());
            result.setError(messageSources.get("trade.info.set.default.fail"));
        } catch (IllegalStateException e) {
            log.error("fail to set default trade info with tradeInfoId:{}, error:{}", addressId, e.getMessage());
            result.setError(messageSources.get("trade.info.set.default.fail"));
        } catch (Exception e) {
            log.error("fail to set default trade info with tradeInfoId:{}", addressId,e);
            result.setError(messageSources.get("trade.info.set.default.fail"));
        }
        return result;

    }

    /**
     * 创建收货地址
     *
     * @param tradeInfo     收货地址信息, 必填
//     * @param sessionId     会话id, 必填
    //     * @param channel       渠道, 必填
//     * @param sign          签名, 必填
     *
     * @return 收货地址id
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Long> create(UserTradeInfo tradeInfo,
//                                      @RequestParam("session") String sessionId,
//                                      @RequestParam("channel") String channel,
//                                      @RequestParam("sign") String sign,
                                      HttpServletRequest request) {
        NbResponse<Long> response = new NbResponse<Long>();
        BaseUser baseUser=new BaseUser();
        try {
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


//            checkState(validateTradeInfo(tradeInfo), "trade.info.invalid");
//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");
//            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
//            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
//            Long uid = uidGetResult.getResult();
            baseUser=UserUtil.getCurrentUser();
            tradeInfo.setUserId(baseUser.getId());
            Response<Long> createResult = userTradeInfoService.create(tradeInfo);
            checkState(createResult.isSuccess(), createResult.getError());

            response.setResult(createResult.getResult());

        } catch (IllegalArgumentException e) {
            log.error("fail to add new tradeInfo with tradeInfo:{},  error:{}", tradeInfo,  e.getMessage());
            response.setError(messageSources.get("trade.info.create.fail"));
        } catch (IllegalStateException e) {
            log.error("fail to add new tradeInfo with tradeInfo:{},  error:{}", tradeInfo,  e.getMessage());
            response.setError(messageSources.get("trade.info.create.fail"));
        } catch (Exception e) {
            log.error("fail to add new tradeInfo with tradeInfo:{}", tradeInfo, e);
            response.setError(messageSources.get("trade.info.create.fail"));
        }
            return response;
    }

    /**
     * 逻辑删除收货地址
     * @param addressId         收货地址id, 必填
//     * @param sessionId  会话id, 必填
//     * @param channel    渠道, 必填
//     * @param sign       签名, 必填
     *
     * @return  是否创建成功
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Boolean> delete(
                                      @RequestParam("addressId") Long addressId,
//            @PathVariable("id") Long id,
//                                         @RequestParam("session") String sessionId,
//                                         @RequestParam("channel") String channel,
//                                         @RequestParam("sign") String sign,
                                         HttpServletRequest request) {
        NbResponse<Boolean> result = new NbResponse<Boolean>();

        try {
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.id.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");
//            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
//            checkState(uidGetResult.isSuccess(), uidGetResult.getError());

            Response<Boolean> deleteResult = userTradeInfoService.invalidate(addressId);
            checkState(deleteResult.isSuccess(), deleteResult.getError());
            result.setResult(Boolean.TRUE);
        } catch (IllegalArgumentException e) {
            log.error("fail to delete tradeInfo with tradeInfoId:{}, error:{}", addressId,e.getMessage());
            result.setError(messageSources.get("trade.info.delete.fail"));
        } catch (IllegalStateException e) {
            log.error("fail to delete tradeInfo with tradeInfoId:{}, error:{}", addressId,  e.getMessage());
            result.setError(messageSources.get("trade.info.delete.fail"));
        } catch (Exception e) {
            log.error("fail to delete tradeInfo with tradeInfoId:{}", addressId, e);
            result.setError(messageSources.get("trade.info.delete.fail"));
        }
        return result;
    }


    /**
     * 修改收货地址,逻辑删除再创建
     *
     * @param tradeInfo     收货地址信息, 必填
//     * @param tradeInfoId   收货地址id, 必填
//     * @param sessionId     会话id, 必填
//     * @param channel       渠道, 必填
//     * @param sign          签名, 必填
     *
     * @return              新的收货地址id
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Long> update(UserTradeInfo tradeInfo,
//                                      @PathVariable("id") Long tradeInfoId,
//                                      @RequestParam("session") String sessionId,
//                                      @RequestParam("channel") String channel,
//                                      @RequestParam("sign") String sign,
                                      HttpServletRequest request) {

        NbResponse<Long> result = new NbResponse<Long>();
        BaseUser baseUser=new BaseUser();
        try {
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

//            checkState(validateTradeInfo(tradeInfo), "trade.info.invalid");
//            tradeInfo.setId(tradeInfoId);

//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");
//            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
//            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
//            Long uid = uidGetResult.getResult();
//            tradeInfo.setUserId(uid);
            baseUser=UserUtil.getCurrentUser();
            tradeInfo.setUserId(baseUser.getId());
            Response<Long> updateResult = userTradeInfoService.update(tradeInfo, tradeInfo.getUserId());
            checkState(updateResult.isSuccess(), updateResult.getError());
            result.setResult(updateResult.getResult());

        } catch (IllegalArgumentException e) {
            log.error("fail to update userTradeInfo with tradeInfo:{},  error:{}",
                    tradeInfo, e.getMessage());
            result.setError(messageSources.get("trade.info.update.fail"));
        } catch (IllegalStateException e) {
            log.error("fail to update userTradeInfo with tradeInfo:{}, error:{}",
                    tradeInfo, e.getMessage());
            result.setError(messageSources.get("trade.info.update.fail"));
        } catch (Exception e) {
            log.error("fail to update userTradeInfo with tradeInfo:{},", tradeInfo, e);
            result.setError(messageSources.get("trade.info.update.fail"));
        }

        return result;
    }


    
    private Boolean validateTradeInfo(UserTradeInfo tradeInfo) {
        return !Objects.equal(tradeInfo, null) &&
                !Objects.equal(tradeInfo.getCityCode(), null) &&
                !Objects.equal(tradeInfo.getDistrictCode(), null) &&
                !Objects.equal(tradeInfo.getProvinceCode(), null) &&
                !Strings.isNullOrEmpty(tradeInfo.getName()) &&
                !Strings.isNullOrEmpty(tradeInfo.getPhone()) &&
                !Strings.isNullOrEmpty(tradeInfo.getStreet()) &&
                !Strings.isNullOrEmpty(tradeInfo.getZip());
    }
}
