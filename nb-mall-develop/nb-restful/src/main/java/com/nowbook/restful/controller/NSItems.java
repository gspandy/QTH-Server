package com.nowbook.restful.controller;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nowbook.category.dto.FrontCategoryNav;
import com.nowbook.category.service.FrontCategoryService;
import com.nowbook.collect.dto.CollectedBar;
import com.nowbook.collect.dto.CollectedItemInfo;
import com.nowbook.collect.dto.CollectedSummary;
import com.nowbook.collect.service.CollectedItemService;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.coupons.model.NbCouponsItemList;
import com.nowbook.coupons.service.CouponsItemListService;
import com.nowbook.item.dto.FacetSearchResult;
import com.nowbook.item.dto.RecommendSiteItem;
import com.nowbook.item.model.Item;
import com.nowbook.item.model.ItemDetail;
import com.nowbook.item.model.Sku;
import com.nowbook.item.service.ItemSearchService;
import com.nowbook.item.service.ItemService;
import com.nowbook.item.service.LevelPriceService;
import com.nowbook.redis.utils.JedisTemplate;
import com.nowbook.restful.dto.NbResponse;
import com.nowbook.restful.util.Signatures;
import com.nowbook.rlt.grid.service.GridService;
import com.nowbook.rlt.presale.dto.FullItemPreSale;
import com.nowbook.rlt.presale.dto.MarketItem;
import com.nowbook.rlt.presale.service.PreSaleService;
import com.nowbook.rlt.purify.dto.PurifyPageDto;
import com.nowbook.rlt.purify.service.PurifyPageService;
import com.nowbook.sdp.model.Level;
import com.nowbook.sdp.model.UserTeamMemberSelect;
import com.nowbook.sdp.service.DistributionsService;
import com.nowbook.sdp.service.LevelService;
import com.nowbook.sdp.service.UserLevelService;
import com.nowbook.search.ESClient;
import com.nowbook.site.dao.ComponentDao;
import com.nowbook.trade.model.OrderComment;
import com.nowbook.trade.service.OrderCommentService;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import com.nowbook.user.model.User;
import com.nowbook.user.service.AccountService;
import com.nowbook.user.util.RedisKeyUtils;
import com.nowbook.web.controller.view.ViewRender;
import com.nowbook.web.misc.MessageSources;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.nowbook.common.utils.Arguments.notEmpty;

/**
 * Date: 4/10/14
 * Time: 15:11
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */

@Controller
@Slf4j
@RequestMapping("/api/extend/item")
public class NSItems {

    private final static Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();


    @Autowired
    ItemSearchService itemSearchService;

    @Autowired
    AccountService<User> accountService;

    @Autowired
    ItemService itemService;

    @Autowired
    MessageSources messageSources;

    @Autowired
    PurifyPageService purifyPageService;

    @Autowired
    OrderCommentService orderCommentService;

    @Autowired
    PreSaleService preSaleService;

    @Autowired
    GridService gridService;
    @Autowired
    CollectedItemService collectedItemService;

    @Autowired
    CouponsItemListService couponsItemListService;
    @Autowired
    private FrontCategoryService frontCategoryService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private LevelService levelService;

    @Autowired
    private ComponentDao componentDao;

    @Autowired
    private LevelPriceService levelPriceService;


    @Value("#{app.restkey}")
    private String key;

    @Value("#{app.mainSite}")
    private String mainSite;

    @Value("#{app.domain}")
    private String domain;

    @Autowired
    private DistributionsService distributionsService;

    @Autowired
    private ESClient esClient;

    @Autowired
    private ViewRender viewRender;

    @Autowired
    public JedisTemplate jedisTemplate;



    /**
     * 返回商城的分页的商品列表
     *
     * @param q       查询关键字
     * @param sort    排序方式
     * @param pageNo  分页, 选填(默认为1)
     * @param size    每页纪录数量, 选填(默认为5)
     * @param bid    品牌id
     * @param p_f    最低价
     * @param p_t    最高价
     * @param pvids
     * @param fcid
//     * @param channel 渠道, 必填
//     * @param sign    签名, 必填
     * @return 搜索的商品结果
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<FacetSearchResult> list(@RequestParam(value = "q", required = false) String q,
                                                 @RequestParam(value = "order", required = false) String sort,
                                                 @RequestParam(value = "bid", required = false) String bid,
                                                 @RequestParam(value = "pvids", required = false) String pvids,
                                                 @RequestParam(value = "p_f", required = false) String p_f,
                                                 @RequestParam(value = "p_t", required = false) String p_t,
                                                 @RequestParam(value = "fcid", required = false) String fcid,
                                                @RequestParam(value = "firstFcid", required = false) String firstFcid,
                                                 @RequestParam(value = "shopId", required = false) String shopId,
                                                @RequestParam(value = "priceType", required = false) String priceType,
                                                 @RequestParam(value = "bids", required = false) String bids,
                                                 @RequestParam(value = "categoryIds", required = false) String categoryIds,
                                                 @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(value = "size", defaultValue = "5") Integer size,
                                                 HttpServletRequest request) {
        NbResponse<FacetSearchResult> result = new NbResponse<FacetSearchResult>();

        try {

//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Map<String, String> params = Maps.newHashMap();
          //  params.put("rid", String.valueOf(region));

            if (notEmpty(q)) params.put("q", q);
            if (notEmpty(sort)) params.put("sort", sort);
            if (notEmpty(bid)) params.put("bid", bid);
            if (notEmpty(pvids)) params.put("pvids", pvids);
            if (notEmpty(p_f)) params.put("p_f", p_f);
            if (notEmpty(p_t)) params.put("p_t", p_t);
            if (notEmpty(fcid)) params.put("fcid", fcid);
            if (notEmpty(priceType)) params.put("priceType", priceType);
            if (notEmpty(shopId)) params.put("shopId", shopId);
            if (notEmpty(bids)) params.put("bids", bids);
            if (notEmpty(categoryIds)) params.put("categoryIds", categoryIds);
            if (notEmpty(firstFcid)) params.put("firstFcid", firstFcid);

            Response<FacetSearchResult> itemGetResult = itemSearchService.facetSearchItem(pageNo, size, params);
            checkState(itemGetResult.isSuccess(), itemGetResult.getError());
            result.setResult(itemGetResult.getResult(), key);

        } catch (IllegalArgumentException e) {
            log.error("fail to query list  pageNo:{}, size:{}, error:{}", pageNo, size, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to query list  pageNo:{}, size:{}, error:{}", pageNo, size, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query list pageNo:{}, size:{}", pageNo, size, e);
            result.setError(messageSources.get("item.search.fail"));
        }

        return result;
    }


    /**
     * 获取分页的商品评价列表
     *
     * @param id      商品的id
//     * @param channel 渠道, 必填
//     * @param sign    签名, 必填
     * @param pageNo  页码, 选填, 默认为1
     * @param size    每页数据条目, 默认为20
     * @return 分页的商品评价
     */
    @RequestMapping(value = "/comments", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Paging<OrderComment>> comments(@RequestParam("id") Long id,
//                                                        @RequestParam(value = "channel") String channel,
//                                                        @RequestParam(value = "sign") String sign,
                                                        @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                        @RequestParam(value = "size", defaultValue = "20") Integer size,
                                                        HttpServletRequest request) {
        NbResponse<Paging<OrderComment>> result = new NbResponse<Paging<OrderComment>>();

        try {
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Response<Paging<OrderComment>> itemsGet = orderCommentService.viewItemComments(id, pageNo, size);
            checkState(itemsGet.isSuccess(), itemsGet.getError());
            result.setResult(itemsGet.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to query comments with itemId:{}, pageNo:{}, size:{}, error:{}", id, pageNo, size, e.getMessage());
        } catch (Exception e) {
            log.error("fail to query comments with itemId:{}, pageNo:{}, size:{}", id, pageNo, size, e);
        }
        return result;
    }


    /**
     * 获取商品详情
     *
     * @param id      商品id
    //     * @param channel 渠道, 必填
    //     * @param sign    签名, 必填
     * @return 商品详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Map<String, Object>> itemDetail(@RequestParam("id") Long id,
//                                                         @RequestParam("channel") String channel,
//                                                         @RequestParam("sign") String sign,
                                                         HttpServletRequest request) {

        NbResponse<Map<String, Object>> result = new NbResponse<Map<String, Object>>();

        try {
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Response<Map<String, Object>> detailGetResult = itemService.findWithDetailsById(id);
            checkState(detailGetResult.isSuccess(), detailGetResult.getError());
            result.setResult(detailGetResult.getResult(), key);
        } catch (IllegalStateException e) {
            log.error("fail to get item or item detail with itemId:{}, error:{}", id, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get item or item detail with itemId:{}", id, e);
            result.setError(messageSources.get("item.query.fail"));
        }
        return result;
    }

    /**
     * 根据skuId查找sku
     *
     * @param skuId sku id
     * @return SKU信息
     */
     @RequestMapping(value = "/findSkuById", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public Response<Sku> findSkuById( @RequestParam("skuId") Long skuId) {
        return itemService.findSkuById(skuId);
     }

    /**
     * 前台预售列表,提供给手机端使用
     */
    @RequestMapping(value = "/preSale/list", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Paging<MarketItem>> preSaleList(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                         @RequestParam(value = "size", required = false) Integer size,
                                                         @RequestParam("channel") String channel,
                                                         @RequestParam("sign") String sign,
                                                         HttpServletRequest request) {
        NbResponse<Paging<MarketItem>> result = new NbResponse<Paging<MarketItem>>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            Response<Paging<MarketItem>> preSaleR = preSaleService.paginationByUser(pageNo, size);
            if (!preSaleR.isSuccess()) {
                log.error("fail to find preSale item list, pageNo={}, size={}, error code:{}",
                        pageNo, size, preSaleR.getError());
                result.setError(messageSources.get(preSaleR.getError()));
                return result;
            }
            result.setResult(preSaleR.getResult());
            return result;

        } catch (IllegalArgumentException ex) {
            log.error("fail to find preSale items pageNo={}, size={}, channel={}, sign={},cause:{}",
                    pageNo, size, channel, sign, ex.getMessage());
            result.setError(messageSources.get(ex.getMessage()));
            return result;
        } catch (Exception e) {
            log.error("fail to find preSale items pageNo={}, size={}, channel={}, sign={},cause:{}",
                    pageNo, size, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("preSale.query.fail"));
            return result;
        }
    }


    /**
     * 预售商品详情页
     */
    @RequestMapping(value = "/preSale/{itemId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<FullItemPreSale> preSaleDetail(@PathVariable Long itemId,
                                                        @RequestParam("channel") String channel,
                                                        @RequestParam("sign") String sign,
                                                        HttpServletRequest request) {
        NbResponse<FullItemPreSale> result = new NbResponse<FullItemPreSale>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            Response<FullItemPreSale> preSaleItemR = preSaleService.findFullItemPreSale(itemId);
            if (!preSaleItemR.isSuccess()) {
                log.error("fail to find preSale item detail by itemId={}, error code:{}",
                        itemId, preSaleItemR.getError());
                result.setError(messageSources.get(preSaleItemR.getError()));
                return result;
            }

            result.setResult(preSaleItemR.getResult());
            return result;

        } catch (IllegalArgumentException ex) {
            log.error("fail to find preSale item detail by itemId={}, channel={}, sign={}, cause:{}",
                    itemId, channel, sign, ex.getMessage());
            result.setError(messageSources.get(ex.getMessage()));
            return result;
        } catch (Exception e) {
            log.error("fail to find preSale item detail by itemId={}, channel={}, sign={}, cause:{}",
                    itemId, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("preSale.item.not.found"));
            return result;
        }
    }


    /**
     * 水机定制
     *
     * @param sid         系列编号, 必填
     * @param assemblyIds 组件页面编号，选填， 默认返回第一步的页面数据
     * @param channel     渠道, 必填
     * @param sign        签名, 必填
     */
    @RequestMapping(value = "/{sid}/customize", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<PurifyPageDto> purifyPage(@PathVariable Long sid,
                                                   @RequestParam(value = "assembles", required = false) String assemblyIds,
                                                   @RequestParam("channel") String channel,
                                                   @RequestParam("sign") String sign,
                                                   HttpServletRequest request) {
        NbResponse<PurifyPageDto> result = new NbResponse<PurifyPageDto>();
        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            Long[] ids;

            if (notEmpty(assemblyIds)) {

                List<String> idList = splitter.splitToList(assemblyIds);
                ids = convertToLong(idList).toArray(new Long[idList.size()]);
            } else {
                ids = null;
            }

            Response<PurifyPageDto> queryResult = purifyPageService.findPurifyPageInfo(sid, ids);
            checkState(queryResult.isSuccess(), queryResult.getError());
            result.setResult(queryResult.getResult());

        } catch (IllegalArgumentException e) {
            log.error("fail to query purifyPage with channel:{}, sign:{}, error:{}", channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to query purifyPage with channel:{}, sign:{}, error:{}", channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query purifyPage with channel:{}, sign:{}", channel, sign);
            result.setError(messageSources.get("purify.query.fail"));
        }

        return result;
    }

    private List<Long> convertToLong(List<String> identities) {
        List<Long> ids = Lists.newArrayListWithCapacity(identities.size());
        for (String identity : identities) {
            ids.add(Long.valueOf(identity));
        }
        return ids;
    }

    @NoArgsConstructor
    @ToString
    private static class ItemDetailDto implements Serializable {
        private static final long serialVersionUID = 6505898257928193272L;
        @Setter
        @Getter
        Item item;

        @Getter
        @Setter
        ItemDetail detail;
    }

    @RequestMapping(value = "/findByIds", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<Item>> findByIds(@RequestParam("listId") String listId) {
        List<Long> ids = new ArrayList<Long>();
        String[] list = listId.split(",");
         for(String  id : list){
            ids.add(Long.valueOf(id));
         }
        Response<List<Item>> result = itemService.findByIds(ids);
        return result;
    }

    /**
     * 站点推荐商品或者模版商品
     * @param spuIds spuId, 在推荐模版商品时不能为空
     * @param dateSource manual 为推荐模版商品，auto为推荐商品
     * @param size 商品数量
     * @param order 排序
     * @param brandId 品牌id
     * @param categoryId 前台类目id
     * @return  推荐商品列表
     */
    @RequestMapping(value = "/recommend", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<RecommendSiteItem> findDefaultItems(@RequestParam(value = "spuIds", required = false) String spuIds,
                                                    @RequestParam(value = "dataSource") String dateSource,
                                                    @RequestParam(value = "size", defaultValue = "8") Integer size,
                                                    @RequestParam(value = "order", required = false) String order,
                                                    @RequestParam(value = "bid", required = false) Integer brandId,
                                                    @RequestParam(value = "fcid", required = false) Integer categoryId,
                                                    HttpServletRequest request) {
//        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieKVs = Maps.newHashMap();
//        for(Cookie cookie : cookies) {
//            cookieKVs.put(cookie.getName(), cookie.getValue());
//        }
        Response<Integer> regionR = gridService.findRegionFromCookie(cookieKVs);
//        if(!regionR.isSuccess()) {
////            log.warn("region not found in cookies");
//            throw new JsonResponseException(400, messageSources.get(regionR.getError()));
//        }
        Integer region = regionR.getResult();
        Response<List<RecommendSiteItem>> result = itemSearchService.recommendItemOrDefaultItemInSite(dateSource, spuIds, size, order,brandId, categoryId, region);
//        if(!result.isSuccess()) {
//            log.error("fail to find default item by spuIds={}, error code:{}", spuIds, result.getError());
//            throw new JsonResponseException(500, messageSources.get(result.getError()));
//        }
        return result.getResult();
    }
    @RequestMapping(value = "/domainName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String domainName() {
        return mainSite;
    }



    /**
     * 添加商品收藏记录
     *
//     * @param userId    用户id
     * @param itemId    商品id
     * @return  操作是否成功
     */
    @RequestMapping(value = "/collect", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<CollectedSummary> itemCreateCollected(@RequestParam("itemId") Long itemId) {
        BaseUser baseUser=new BaseUser();
        baseUser= UserUtil.getCurrentUser();
        Response<CollectedSummary> itemCreateCollected = collectedItemService.create(baseUser.getId(),itemId);
        return itemCreateCollected;
    }

    /**
     * 删除商品收藏记录
     *
//     * @param userId 用户id
     * @param itemId 商品id
     * @return 操作是否成功
     */
    @RequestMapping(value = "/deleteCollect", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean> itemDeleteCollected(@RequestParam("itemId") Long itemId) {
        BaseUser baseUser=new BaseUser();
        baseUser= UserUtil.getCurrentUser();
        Response<Boolean> itemDeleteCollected = collectedItemService.delete(baseUser.getId(),itemId);
        return itemDeleteCollected;
    }

    /**
     * 查询用户收藏的商品
     *
     * @param itemName 商品名称，选填，模糊匹配
     * @param pageNo    页码
     * @param size      分页大小
//     * @param baseUser 查询用户
     * @return 收藏商品分页
     */
    @RequestMapping(value = "/selectItemCollect", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Paging<CollectedItemInfo>> selectItemCollect(@RequestParam(value = "itemName",required = false) String itemName,
                                                 @RequestParam("pageNo") Integer pageNo,
                                                 @RequestParam("size") Integer size
                                                ) {
        BaseUser baseUser=new BaseUser();
        baseUser=UserUtil.getCurrentUser();
        Response<Paging<CollectedItemInfo>> selectItemCollect = collectedItemService.findBy(itemName,pageNo,size,baseUser);
        return selectItemCollect;
    }


    @RequestMapping(value = "/selectItemIsCollect", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<CollectedBar> selectItemIsCollect(@RequestParam("itemId") Long itemId) {
        BaseUser baseUser=new BaseUser();
        baseUser=UserUtil.getCurrentUser();
        Response<CollectedBar> selectItemIsCollect = collectedItemService.collected(itemId,baseUser.getId());
        return selectItemIsCollect;
    }
    /**
     * 查询商品优惠劵
     *
     * @param itemId 商品id,多个用逗号分开
     * @param shopId 商店id
     * @return 商品优惠劵
     */
    @RequestMapping(value = "/{shopId}/selectCouponsByShopId", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Paging<CollectedItemInfo>> findCouponsByShopId(@RequestParam("itemId") String itemId,
                                                                   @PathVariable Long shopId) {
        List<NbCouponsItemList> Coupons = couponsItemListService.findCouponsbyShopId(shopId);
        for(NbCouponsItemList b :Coupons){
            int d =b.getUseLimit();
            int c =0;
        }
        Response<Paging<CollectedItemInfo>> a = new Response<Paging<CollectedItemInfo>>();
        return a;
    }
    @RequestMapping(value="/suggest",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<String> suggest(@RequestParam("t") String indexName,@RequestParam("q")String term){
        return esClient.suggest(indexName,"name",term);
    }
    //ficds要二级类目，多个用逗号分开。
    @RequestMapping(value="/recommendItems",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<Integer,List<RecommendSiteItem>> suggest(@RequestParam(value = "fcids", required = false) String fcids) {
        Map<Integer, List<RecommendSiteItem>> result = new HashMap<Integer, List<RecommendSiteItem>>();
        if (fcids != null) {
           String[]  fcidList = fcids.split(",");
            int ids = 0;
            for (String fcid : fcidList) {
                Response<List<RecommendSiteItem>> recommendSiteItem = itemSearchService.recommendItemOrDefaultItemInSite("auto", null, 18, null, null,Integer.valueOf(fcid), 1);
                result.put(ids, recommendSiteItem.getResult());
                ids++;
            }
        } else {
            Response<List<FrontCategoryNav>> categories = frontCategoryService.findSecondAndThirdLevel(Long.valueOf("0"));
            int ids = 0;
            for (FrontCategoryNav category : categories.getResult()) {
                Long id = category.getThirdLevel().get(0).getId();
                Response<List<RecommendSiteItem>> recommendSiteItem = itemSearchService.recommendItemOrDefaultItemInSite("auto", null, 18, null, null, id.intValue(), 1);
                result.put(ids, recommendSiteItem.getResult());
                ids++;
            }
        }
        return result;
    }

    // 获取用户折扣
    private Integer getDiscount() {
        BaseUser baseUser = UserUtil.getCurrentUser();
        Integer discount = 100;
        if (baseUser != null) {
            UserTeamMemberSelect userTeamMemberSelect = userLevelService.selectUser(baseUser.getId()).getResult();
            Level level = new Level();
            level.setLevel(userTeamMemberSelect.getLevel());
            discount = levelService.selectByLevel(level).get(0).getDiscount();
        }
        return discount;
    }

    // 获取自营推荐商品
    @RequestMapping(value = "/selfBrands", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<Item>> selfBrands() {
        Response<List<Item>> response = new Response<List<Item>>();
        List<Item> list = new ArrayList<Item>();
        String brands = componentDao.getData("channel_floor/proprietary_goods_recommendation:release");
        JSONObject jo = JSONObject.fromObject(brands);
        if (jo.size() != 0) {
            String[] bs = jo.get("ids") != null && !jo.get("ids").equals("") ? jo.get("ids").toString().split(" ") : new String[0];
            for (String s : bs) {
                Item item = itemService.findById(Long.valueOf(s)).getResult();
                //通过item获取对应的各个等级价格 2017-09-27 dpzh
                item.setSalePrice(levelPriceService.getUserLevelPrice(item));
                if(item.getPriceType()==2){
                    item.setPrice(item.getSellingPrice());
                }if(item.getPriceType()==3){
                    item.setPrice(item.getCustomPrice());
                }
                list.add(item);
            }
        }
        response.setResult(list);
        return response;
    }

    // 获取推荐商品
    @RequestMapping(value = "/brands", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<Item>> brands() {
        Response<List<Item>> response = new Response<List<Item>>();
        List<Item> list = new ArrayList<Item>();
        String brands = componentDao.getData("channel_floor/goods_recommendation:release");
        JSONObject jo = JSONObject.fromObject(brands);
        if (jo.size() != 0) {
            String[] bs = jo.get("ids") != null && !jo.get("ids").equals("") ? jo.get("ids").toString().split(" ") : new String[0];
            for (String s : bs) {
                Item item = itemService.findById(Long.valueOf(s)).getResult();
                //通过item获取对应的各个等级价格 2017-09-27 dpzh
                item.setSalePrice(levelPriceService.getUserLevelPrice(item));
                if(item.getPriceType()==2){
                    item.setPrice(item.getSellingPrice());
                }else if(item.getPriceType()==3){
                    item.setPrice(item.getCustomPrice());
                }
                list.add(item);
            }
        }
        response.setResult(list);
        return response;
    }

    // 获取单个楼层列表
    @RequestMapping(value = "/floor", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<Map<String, String>>> floor() {
        Response<List<Map<String, String>>> response = new Response<List<Map<String, String>>>();
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        String floors = componentDao.getData("common/floors:release");
        if (floors != null && !floors.equals("") && !floors.equals("[]")) {
            JSONArray ja = JSONArray.fromObject(floors);
            Integer index = 0;
            for (Object o : ja) {
                JSONObject jo = JSONObject.fromObject(o);
                Map<String, String> m = new HashMap<String, String>();
                m.put("image", jo.get("image1") != null ? jo.get("image1").toString() : "");
                m.put("title", jo.get("title") != null ? jo.get("title").toString() : "");
                m.put("description", jo.get("description") != null ? jo.get("description").toString() : "");
                m.put("type", jo.get("type") != null ? jo.get("type").toString() : "");
                m.put("floorId", index.toString());
                list.add(m);
                index += 1;
            }
        }
        response.setResult(list);
        return response;
    }

    // 根据楼层ID(floorId)获取商品列表
    @RequestMapping(value = "/floorView", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Map<String, Object>> floorView(@RequestParam("floorId") String floorId) {
        Response<Map<String, Object>> response = new Response<Map<String, Object>>();
        Map<String, Object> result = new HashMap<String, Object>();
        if (floorId != null && !floorId.equals("")) {
            String floors = componentDao.getData("common/floors:release");
            if (floors != null && !floors.equals("") && !floors.equals("[]")) {
                JSONArray ja = JSONArray.fromObject(floors);
                Integer index = Integer.valueOf(floorId);
                if (index < ja.size()) {
                    Object object = ja.get(Integer.valueOf(floorId));
                    JSONObject jo = JSONObject.fromObject(object);
                    List<Item> l = new ArrayList<Item>();
                    Object theItems = jo.get("items");
                    if (theItems != null && !theItems.equals("")) {
                        String[] items = theItems.toString().split(",");
                        for (String itemId : items) {
                            Item item = itemService.findById(Long.valueOf(itemId)).getResult();
                            //通过item获取对应的各个等级价格 2017-09-27 dpzh
                            if(item !=null && item.getStatus().equals(Item.Status.ON_SHELF)){
                                item.setSalePrice(levelPriceService.getUserLevelPrice(item));
                                if(item.getPriceType()==2){
                                    item.setPrice(item.getSellingPrice());
                                }if(item.getPriceType()==3){
                                    item.setPrice(item.getCustomPrice());
                                }
                                l.add(item);
                            }
                        }
                    }
                    result.put("image", jo.get("image2") != null ? jo.get("image2").toString() : "");
                    result.put("items", l);
                }
            }
        }
        response.setResult(result);
        return response;
    }

    // 获取商品详情
    @RequestMapping(value = "/itemDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public void itemDetail(@RequestParam("itemId") final Long itemId, HttpServletRequest request, HttpServletResponse response) {
        String image = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(RedisKeyUtils.itemIdEvaluation(itemId));
            }
        });
        Map<String, Object> map = new HashMap<String, Object>();
        List<String> images=new ArrayList<String>();
        if(image!=null){
            images = Arrays.asList(image.split(","));
        }
        map.put("_IMAGES_", images);
        viewRender.view(domain, "seller/itemDetail", request, response, map);
    }


    // 获取客服商品详情
    @RequestMapping(value = "/itemDetailForCustomer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,Object> itemDetailForCustomer(@RequestParam("itemid") Long itemId, @RequestParam("itemparam") Long userId,HttpServletRequest request, HttpServletResponse response) {
        Map<String,Object> result = new HashMap<String, Object>();
        Response<Map<String, Object>> mapResponse = itemService.findWithDetailsByIdForCustomer(itemId,userId);
        result.put("item",mapResponse.getResult());
        return result;
    }
}
