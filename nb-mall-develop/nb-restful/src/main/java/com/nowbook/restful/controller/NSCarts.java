package com.nowbook.restful.controller;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.common.utils.CommonConstants;
import com.google.common.base.Preconditions;
import com.nowbook.item.dto.FullItem;
import com.nowbook.item.model.Sku;
import com.nowbook.item.service.ItemService;
import com.nowbook.restful.dto.NbResponse;
import com.nowbook.restful.util.NSSessionUID;
import com.nowbook.restful.util.Signatures;
import com.nowbook.session.AFSession;
import com.nowbook.session.AFSessionManager;
import com.nowbook.trade.model.UserCart;
import com.nowbook.trade.service.CartService;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import com.nowbook.web.misc.MessageSources;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.nowbook.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Date: 4/23/14
 * Time: 17:08
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */

@Controller
@RequestMapping("/api/extend/cart")
@Slf4j
public class NSCarts {

    private final AFSessionManager sessionManager = AFSessionManager.instance();
    private final static HashFunction md5 = Hashing.md5();
    private final static int ONE_YEAR = (int) TimeUnit.DAYS.toSeconds(365);
    private final CommonConstants commonConstants;

    @Autowired
    public NSCarts(CartService cartService, CommonConstants commonConstants, MessageSources messageSources) {
        this.cartService = cartService;
        this.commonConstants = commonConstants;
        this.messageSources = messageSources;
    }
    @Autowired
    CartService cartService;
    @Autowired
    ItemService itemService;

    @Autowired
    MessageSources messageSources;

    @Value("#{app.restkey}")
    String key;


    /**
     * 当前用户的购物车内容
     *
//     * @param sessionId 会话id, 必填
//     * @param channel   渠道, 必填
//     * @param sign      签名, 必填
     *
     * @return          用户购物车内容对象列表
     */
    @RequestMapping(value = "/selectCart",method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<List<UserCart>> list(
//                                                @RequestParam("session") String sessionId,
//                                              @RequestParam("channel") String channel,
//                                              @RequestParam("sign") String sign,
                                              HttpServletRequest request) {
        NbResponse<List<UserCart>> result = new NbResponse<List<UserCart>>();
        BaseUser baseUser=new BaseUser();
        try {
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            Response<Long> idGet = NSSessionUID.getUserId(session);
//            checkState(idGet.isSuccess(), idGet.getError());

//            BaseUser baseUser = new BaseUser();
//            baseUser.setId(idGet.getResult());
            baseUser= UserUtil.getCurrentUser();
            Response<List<UserCart>> cartGetResult  = cartService.getPermanent(baseUser);
            checkState(cartGetResult.isSuccess(), cartGetResult.getError());
            result.setResult(cartGetResult.getResult(), key);

        } catch (IllegalArgumentException e) {
            log.error("fail to query cart list with token:{}, error:{}",  e.getMessage());
            result.setError(messageSources.get("user.cart.query.fail"));
        } catch (IllegalStateException e) {
            log.error("fail to query cart list with token:{}, error:{} ", e.getMessage());
            result.setError(messageSources.get("user.cart.query.fail"));
        } catch (Exception e) {
            log.error("fail to query cart list with token:{}, e:{} ",  e);
            result.setError(messageSources.get("user.cart.query.fail"));
        }
        return result;
    }

    /**
     * 批量删除用户购物车内容
     *
     * @param skuIds    将删除的 sku ID 数组字符串，用“，”分割, 必填
//     * @param sessionId 登录会话id, 必填
//     * @param channel   渠道, 必填
//     * @param sign      签名, 必填
     *
     * @return          操作状态
     */
    @RequestMapping(value = "/remove", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Boolean> remove(@RequestParam("skuIds") String skuIds,
//                                         @RequestParam("channel") String channel,
//                                         @RequestParam("session") String sessionId,
//                                         @RequestParam("sign") String sign,
                                         HttpServletRequest request) {
        NbResponse<Boolean> result = new NbResponse<Boolean>();
        BaseUser baseUser=new BaseUser();
        try {

            checkArgument(notEmpty(skuIds), "skus.can.not.be.empty");
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");


            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            Response<Long> idGet = NSSessionUID.getUserId(session);
//            checkState(idGet.isSuccess(), idGet.getError());
            baseUser=UserUtil.getCurrentUser();
            List<String> parts = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(skuIds);
            List<Long> ids = Lists.newArrayListWithCapacity(parts.size());

            for(String _id : parts) {
                ids.add(Long.parseLong(_id));
            }

            checkState(!ids.isEmpty(), "skus.id.can.not.be.empty");

            Response<Boolean> cartDelResult = cartService.batchDeletePermanent(baseUser.getId(), ids);
            checkState(cartDelResult.isSuccess(), cartDelResult.getError());

            result.setResult(Boolean.TRUE);

        } catch (IllegalStateException e) {
            log.error("fail to delete carts with skuIds:{},  error:{}",
                    skuIds,  e.getMessage());
            result.setError(messageSources.get("user.cart.delete.fail"));
        } catch (Exception e) {
            log.error("fail to delete carts with skuIds:{}",
                    skuIds,  e);
            result.setError(messageSources.get("user.cart.delete.fail"));
        }

        return result;
    }

    /**
     * 清空用户购物车
     *
//     * @param sessionId 会话id, 必填
//     * @param channel   渠道, 必填
//     * @param sign      签名, 必填
     * @return          操作状态, 必填
     */
    @RequestMapping(value = "/empty", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Boolean> empty(
//                                        @RequestParam("session") String sessionId,
//                                        @RequestParam("channel") String channel,
//                                        @RequestParam("sign") String sign,
                                        HttpServletRequest request) {
        NbResponse<Boolean> result = new NbResponse<Boolean>();
        BaseUser baseUser=new BaseUser();
        try {
//            checkArgument(notEmpty(sessionId), "session.id.can.not.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");


            // 校验签名, 先注释方便调试
//            Preconditions.checkArgument(Signatures.verify(request, key), "sign.verify.fail");

//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
//            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            baseUser=UserUtil.getCurrentUser();
            Response<Boolean> emptyCartResult = cartService.empty(baseUser.getId().toString());
            checkState(emptyCartResult.isSuccess(), emptyCartResult.getError());

            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to empty cart with userId:{}, error:{}",baseUser.getId(), e.getMessage());
            result.setError(messageSources.get("user.cart.empty.fail"));
        } catch (IllegalStateException e) {
            log.error("fail to empty cart with userId:{}, error:{}", baseUser.getId(), e.getMessage());
            result.setError(messageSources.get("user.cart.empty.fail"));
        } catch (Exception e) {
            log.error("fail to empty cart with userId:{}",baseUser.getId(), e);
            result.setError(messageSources.get("user.cart.empty.fail"));
        }
        return result;
    }


    /**
     * 变更购物车中的商品
     *
     * @param skuId     库存编号, 必填
     * @param quantity  数量, 必填
//     * @param sessionId 会话id, 必填
//     * @param channel   渠道, 必填
//     * @param sign      签名, 必填
     * @return          操作状态, 必填
     */
        @RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Integer> add(
                                   @RequestParam("skuId") Long skuId,
                                      @RequestParam("quantity") Integer quantity,
//                                      @RequestParam("session") String sessionId,
//                                      @RequestParam("channel") String channel,
//                                      @RequestParam("sign") String sign,
                                      HttpServletRequest request) {

        NbResponse<Integer> result = new NbResponse<Integer>();
            BaseUser baseUser=new BaseUser();
        try {
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");
//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
//            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            baseUser=UserUtil.getCurrentUser();
            Response<Integer> changeResult = cartService.changePermanentCart(baseUser.getId(), skuId, quantity);
            checkState(changeResult.isSuccess(), changeResult.getError());
            result.setResult(changeResult.getResult());

        } catch (IllegalArgumentException e) {
            log.error("fail to add cart with sku:{}, userId:{}, error:{}", skuId, baseUser.getId(), e.getMessage());
            result.setError(messageSources.get("user.cart.add.fail"));
        } catch (IllegalStateException e) {
            log.error("fail to add cart with sku:{}, userId:{}, error:{}", skuId, baseUser.getId(), e.getMessage());
            result.setError(messageSources.get("user.cart.add.fail"));
        } catch (Exception e) {
            log.error("fail to add cart with sku:{}, userId:{}", skuId, baseUser.getId(), e);
            result.setError(messageSources.get("user.cart.add.fail"));
        }


        return result;
    }


    /**
     * 获取永久购物车中的sku的种类个数
     *
     * @return sku的种类个数
     */
    @RequestMapping(value = "/selectCartCount", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Response<Integer> selectCartCount() {
            BaseUser baseUser = UserUtil.getCurrentUser();
            return cartService.getPermanentCount(baseUser);
    }


    /**
     * 增减临时购物车中的物品
     *
     * @param key      cart cookie key
     * @param skuId    sku id
     * @param quantity 变化数量
     */
    @RequestMapping(value = "/changeCookieCart", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Response<Integer> changeCookieCart(@RequestParam("key") String key,
                                       @RequestParam("skuId") Long skuId,
                                      @RequestParam("quantity") Integer quantity) {
        return  cartService.changeTemporaryCart(key, skuId, quantity);
    }
    /**
     * 增减临时购物车中的物品(默认）
     *
     * @param key      cart cookie key
     * @param itemId    itemId
     * @param quantity 变化数量
     */
    @RequestMapping(value = "/changeCookieCartDefault", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Response<Integer> changeCookieCartDefault(@RequestParam("key") String key,
                                              @RequestParam("key") Long itemId,
                                       @RequestParam("quantity") Integer quantity) {
        Response<Map<String, Object>> detailGetResult = itemService.findWithDetailsById(itemId);
        Map<String, Object> a = detailGetResult.getResult();
        FullItem b =(FullItem)a.get("fullItem");
        List<Sku> c =b.getSkus();
        Sku d =c.get(0);
        return  cartService.changeTemporaryCart(key, d.getId(), quantity);
    }
    /**
     * 增减永久购物车中的物品(默认）
     *
//     * @param sessionId      sessionId
     * @param itemId    itemId
     * @param quantity 变化数量
     */
    @RequestMapping(value = "/changeCartDefault", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Integer> changeCartDefault(
                                       @RequestParam("itemId") Long itemId,
                                      @RequestParam("quantity") Integer quantity,
//                                      @RequestParam("session") String sessionId,
//                                      @RequestParam("channel") String channel,
//                                      @RequestParam("sign") String sign,
                                      HttpServletRequest request) {

        NbResponse<Integer> result = new NbResponse<Integer>();
        Response<Map<String, Object>> detailGetResult = itemService.findWithDetailsById(itemId);
        Map<String, Object> a = detailGetResult.getResult();
        FullItem b =(FullItem)a.get("fullItem");
        List<Sku> c =b.getSkus();
        Sku d =c.get(0);
        d.getId();
        Long skuId = d.getId();
        BaseUser baseUser=new BaseUser();
        try {
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");
//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
//            checkState(uidGetResult.isSuccess(), uidGetResult.getError());

            baseUser=UserUtil.getCurrentUser();

            Response<Integer> changeResult = cartService.changePermanentCart(baseUser.getId(), skuId, quantity);
            checkState(changeResult.isSuccess(), changeResult.getError());
            result.setResult(changeResult.getResult());

        } catch (IllegalArgumentException e) {
            log.error("fail to add cart with sku:{}, error:{}", skuId, e.getMessage());
            result.setError(messageSources.get("user.cart.change.fail"));
        } catch (IllegalStateException e) {
            log.error("fail to add cart with sku:{}, error:{}", skuId, e.getMessage());
            result.setError(messageSources.get("user.cart.change.fail"));
        } catch (Exception e) {
            log.error("fail to add cart with sku:{}", skuId,  e);
            result.setError(messageSources.get("user.cart.change.fail"));
        }


        return result;
    }
    /**
     * 获取临时购物车中的sku的种类个数
     *
     * @param key cartCookie
     * @return sku的种类个数
     */
    @RequestMapping(value = "/selectCookieCartCount", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Response<Integer> selectCookieCartCount(@RequestParam("key") String key) {
        return  cartService.getTemporaryCount(key);
    }

    /**
     * 将临时购物车的物品合并到永久购物车中,并删除临时购物车
     *
     * @param key    cookie中带过来了的key
     * @param userId 用户id
     */
    @RequestMapping(value = "/{userId}/cookieCartToCart", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Response<Boolean> cookieCartToCart(@RequestParam("key") String key,
                                       @PathVariable Long userId) {
        return  cartService.merge(key,userId);
    }
}
