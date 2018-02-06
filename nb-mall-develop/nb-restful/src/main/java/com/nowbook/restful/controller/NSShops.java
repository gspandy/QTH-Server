package com.nowbook.restful.controller;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.collect.dto.CollectedSummary;
import com.nowbook.collect.model.CollectedShop;
import com.nowbook.collect.service.CollectedShopService;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.common.utils.BeanMapper;
import com.nowbook.item.dto.ItemsWithTagFacets;
import com.nowbook.item.model.Item;
import com.nowbook.item.service.ItemSearchService;
import com.nowbook.item.service.ItemService;
import com.nowbook.restful.dto.NbResponse;
import com.nowbook.shop.dto.RichShop;
import com.nowbook.shop.model.Shop;
import com.nowbook.shop.model.ShopCategory;
import com.nowbook.shop.service.ItemTagService;
import com.nowbook.shop.service.ShopCategoryService;
import com.nowbook.shop.service.ShopExtraService;
import com.nowbook.shop.service.ShopService;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.model.Address;
import com.nowbook.user.model.UserExtra;
import com.nowbook.user.service.AddressService;
import com.nowbook.user.service.UserExtraService;
import com.nowbook.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nowbook.common.utils.Arguments.notEmpty;
import static com.nowbook.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Date: 4/17/14
 * Time: 14:28
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */

@Controller
@Slf4j
@RequestMapping("/api/extend/shop")
public class NSShops {

    @Autowired
    UserExtraService userExtraService;

    @Autowired
    ShopService shopService;

    @Autowired
    ShopExtraService shopExtraService;

    @Autowired
    ItemTagService itemTagService;

    @Autowired
    MessageSources messageSources;

    @Autowired
    AddressService addressService;

    @Autowired
    ItemSearchService itemSearchService;

    @Autowired
    ItemService itemService;

    @Autowired
    CollectedShopService collectedShopService;

    @Autowired
    ShopCategoryService shopCategoryService;

    @Value("#{app.restkey}")
    private String key;

    /**
     * 获取商店基本信息
     *
     * @param id        店铺id, 必填
//     * @param channel   渠道, 必填
//     * @param sign      签名, 必填
     * @return   店铺基本信息
     */
    @RequestMapping(value = "/info", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<RichShop> baseInfo(@RequestParam("shopId") Long id,
//                                            @RequestParam("channel") String channel,
//                                            @RequestParam("sign") String sign,
                                            HttpServletRequest request) {
        NbResponse<RichShop> result = new NbResponse<RichShop>();

        try {
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Response<Shop> shopGetResult = shopService.findById(id);
            checkState(shopGetResult.isSuccess(), shopGetResult.getError());

            Shop shop = shopGetResult.getResult();
            RichShop richShop = BeanMapper.map(shop, RichShop.class);

            if (notNull(shop.getRegion())) {
                Response<List<Address>> regionGetResult = addressService.ancestorOfAddresses(shop.getRegion());
                checkState(regionGetResult.isSuccess(), regionGetResult.getError());
                setShopAddress(richShop, regionGetResult.getResult());
            }

            Response<UserExtra> extraGetResult = userExtraService.findByUserId(shop.getUserId());
            checkState(extraGetResult.isSuccess(), extraGetResult.getError());
            UserExtra userExtra = extraGetResult.getResult();

            richShop.setSoldQuantity(Objects.firstNonNull(userExtra.getTradeQuantity(), 0));
            richShop.setSale(Objects.firstNonNull(userExtra.getTradeSum(), 0L));
            if(richShop.getRDescribe()==0){
                richShop.setRDescribe(5);
            }
            if(richShop.getRService()==0){
                richShop.setRService(5);
            };
            if(richShop.getRExpress()==0){
                richShop.setRExpress(5);
            };

            Response<Long> countResult = itemService.countOnShelfByShopId(Long.valueOf(id));
            checkState(countResult.isSuccess(), countResult.getError());
            richShop.setItemCount(countResult.getResult().intValue());

            result.setResult(richShop, key);
        } catch (IllegalStateException e) {
            log.error("fail to get shop with id:{}, error:{}", id, e.getMessage());
            result.setError(messageSources.get("shop.query.fail"));
        } catch (Exception e) {
            log.error("fail to get shop with id:{}", id, e);
            result.setError(messageSources.get("shop.query.fail"));
        }

        return result;
    }
    /**
     * 获取商店额外信息
     *
     * @param id        店铺id, 必填
     * @return   店铺额外信息
     */
    @RequestMapping(value = "/extra", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<HashMap<String, String>> shopExtraSelect(@RequestParam("shopId") Long id) {
        Response<HashMap<String, String>> extraQueryResult = new Response<HashMap<String, String>>();
        HashMap<String, String> extraResult = shopService.getShopEvaluationMap(id);
        extraResult.put("id",id.toString());
        if(extraResult.get("rDescribe")== null){
            extraResult.put("rDescribe","0");
        };
        if(extraResult.get("rService")== null){
            extraResult.put("rService","0");
        };
        if(extraResult.get("rExpress")== null){
            extraResult.put("rExpress","0");
        };
        extraQueryResult.setResult(extraResult);
        return extraQueryResult;
    }


    private void setShopAddress(RichShop richShop, List<Address> result) {
        for (Address address: result) {
            String addressName = address.getName();
            switch (address.getLevel()) {
                case 1:
                    richShop.setProvinceName(addressName);
                    break;
                case 2:
                    richShop.setCityName(addressName);
                    break;
                case 3:
                    richShop.setRegionName(addressName);
                    break;
                default:break;
            }
        }
    }

    /**
     * 获取店铺首页商品
     *
     * @param id        店铺id, 必填
     * @param order     返回排序方式, 必填
     * @param size      每页数量, 必填
//     * @param channel   渠道, 必填
//     * @param sign      签名, 必填
     */
    @RequestMapping(value = "/recommend", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<List<Item>> recommend(@RequestParam("shopId") Long id,
                                               @RequestParam(value = "order", defaultValue = "hot") String order,
                                               @RequestParam(value = "size", defaultValue = "12") Integer size,
//                                               @RequestParam(value = "channel") String channel,
//                                               @RequestParam(value = "sign") String sign,
                                               HttpServletRequest request) {

            NbResponse<List<Item>> result = new NbResponse<List<Item>>();

        try {
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Response<Shop> shopGet = shopService.findById(id);
            checkState(shopGet.isSuccess(), shopGet.getError());

            Shop shop = shopGet.getResult();

            Response<List<Item>> itemsGet = itemSearchService.recommendItemInShop(shop.getUserId(), "auto", null,
                    size, order, null);

            checkState(itemsGet.isSuccess(), itemsGet.getError());
            result.setResult(itemsGet.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to get recommend items with id:{}, order:{}, size:{}, error:{}", id, order, size, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get recommend items with id:{}, order:{}, size:{}", id, order, size, e);
            result.setError(messageSources.get("item.query.fail"));
        }

        return result;
    }


    /**
     * 店铺内所有商品信息
     *
     * @param id        店铺id, 必填
     * @param order     返回排序方式, 必填
     * @param size      每页数量, 必填
     * @param pageNo    页数, 必填
     */
    @RequestMapping(value = "/items", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<ItemsWithTagFacets> items(@RequestParam("shopId") Long id,
                                           @RequestParam("order") String order,
                                           @RequestParam(value = "size", defaultValue = "12") Integer size,
                                           @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
//                                           @RequestParam("channel") String channel,
//                                           @RequestParam("sign") String sign,
                                           HttpServletRequest request) {

        NbResponse<ItemsWithTagFacets> result = new NbResponse<ItemsWithTagFacets>();

        try {
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Map<String, String> params = Maps.newHashMap();

            Response<Shop> shopGet = shopService.findById(id);
            checkState(shopGet.isSuccess(), shopGet.getError());
            Shop shop = shopGet.getResult();

            params.put("sellerId", String.valueOf(shop.getUserId()));
//            params.put("q", q);
//            params.put("sort", order);

            Response<ItemsWithTagFacets> itemsGet = itemSearchService.searchOnShelfItemsInShop(pageNo, size, params);
            checkState(itemsGet.isSuccess(), itemsGet.getError());
            result.setResult(itemsGet.getResult());

        } catch (IllegalStateException e) {
            log.error("fail to search items with id:{}, order:{}, q:{}, size:{}, pageNo:{}, error:{}",
                      id,
//                    order, q,
                    size, pageNo, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to search items with id:{}, order:{}, q:{}, size:{}, pageNo:{}",
                    id,
//                    order, q,
                    size, pageNo, e);
            result.setError(messageSources.get("item.query.fail"));
        }
        return result;
    }

    /**
     * 查询店铺内商品
     *
     * @param id        店铺id, 必填
    //     * @param order     返回排序方式, 必填
    //     * @param q         查询关键字, 必填
     * @param size      每页数量, 必填
     * @param pageNo    页数, 必填
     */
    @RequestMapping(value = "/selectItems", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<ItemsWithTagFacets> items(@RequestParam("shopId")Long id,
                                                   @RequestParam(value = "order",defaultValue = "0_0_0_0") String order,
                                                   @RequestParam("q") String q,
                                                   @RequestParam(value = "size", defaultValue = "12") Integer size,
                                                   @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
//                                                 @RequestParam("channel") String channel,
//                                                 @RequestParam("sign") String sign,
                                                   HttpServletRequest request) {

        NbResponse<ItemsWithTagFacets> result = new NbResponse<ItemsWithTagFacets>();

        try {
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Map<String, String> params = Maps.newHashMap();

            Response<Shop> shopGet = shopService.findById(id);
            checkState(shopGet.isSuccess(), shopGet.getError());
            Shop shop = shopGet.getResult();

            params.put("sellerId", String.valueOf(shop.getUserId()));
            params.put("q", q);
            params.put("sort", order);

            Response<ItemsWithTagFacets> itemsGet = itemSearchService.searchOnShelfItemsInShop(pageNo, size, params);
            checkState(itemsGet.isSuccess(), itemsGet.getError());
            result.setResult(itemsGet.getResult());

        } catch (IllegalStateException e) {
            log.error("fail to search items with id:{}, order:{}, q:{}, size:{}, pageNo:{}, error:{}",
                    id,
//                    order, q,
                    size, pageNo, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to search items with id:{}, order:{}, q:{}, size:{}, pageNo:{}",
                    id,
//                    order, q,
                    size, pageNo, e);
            result.setError(messageSources.get("item.query.fail"));
        }
        return result;
    }

    /**
     * 店铺所有商品分类
     *
     * @param id            店铺id, 必填
//     * @param channel       渠道, 必填
//     * @param sign          签名, 必填
     */
    @RequestMapping(value = "/categories", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<String> shopCategories(@RequestParam("shopId") Long id,
//                                                @RequestParam("channel") String channel,
//                                                @RequestParam("sign") String sign,
                                                HttpServletRequest request) {
        NbResponse<String> result = new NbResponse<String>();

        try {
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");



            Response<String> cateGet = itemTagService.findTree(id);
            result.setResult(cateGet.getResult());

        } catch (Exception e) {
            log.error("fail to query categories with id:{}", id);
            result.setError(messageSources.get("shop.categories.query.fail"));
        }
        return result;
    }

    /**
     * 查询所有店铺基本信息
     *
     * @param pageNo       页数
     * @param size          个数
     * @param params        状态
     */
    @RequestMapping(value = "/select", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Paging<RichShop>> shopSelect(@RequestParam("size") Integer size,
                                              @RequestParam("pageNo") Integer pageNo,
                                             @RequestParam(value = "params", required = false) Map<String, String> params) {
        List<Integer> statuses = Lists.newArrayList(Shop.Status.FAIL.value(), Shop.Status.FROZEN.value(), Shop.Status.OK.value());
        Response<Paging<RichShop>> shopQueryResult = shopService.searchShop( pageNo,size, params);
        return shopQueryResult;
    }


    /**
     * 获取某个父类目下面所有的子类目
     *
     * @param parentId parent id, 一级类目的parentId=0
     * @return 子类目列表
     */
    @RequestMapping(value = "/category", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<ShopCategory>> shopCategory(@RequestParam("id") Long id) {
        Response<List<ShopCategory>> shopCategory = new Response<List<ShopCategory>>();
        shopCategory = shopCategoryService.findByParentId(id);
        return shopCategory;
    }

    /**
     * 查询用户收藏的店铺
     *
     * @param shopName  店铺名称，选填，模糊匹配
     * @param pageNo    页码
     * @param size      分页大小
     * @param userId  查询用户
     * @return  收藏店铺分页
     */
    @RequestMapping(value = "/{userId}/selectShopCollect", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Response<Paging<CollectedShop>> selectShopCollection(@ParamInfo("shopName") @Nullable String shopName,
                                           @ParamInfo("pageNo") @Nullable Integer pageNo,
                                           @ParamInfo("size") @Nullable Integer size,
                                                         @PathVariable Long userId){
        Response<Paging<CollectedShop>> collectedShop = new Response<Paging<CollectedShop>>();
        BaseUser baseUser = new BaseUser();
        baseUser.setId(userId);
        collectedShop = collectedShopService.findBy(shopName, pageNo,size, baseUser);
        return collectedShop;
    }
    /**
     * 添加店铺收藏记录
     *
     * @param userId    用户id
     * @param shopId    店铺id
     * @return  操作是否成功
     */
    @RequestMapping(value = "/{userId}/{shopId}/collect", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<CollectedSummary> shopCreateCollected(@PathVariable Long userId,
                                                          @PathVariable Long shopId) {
        Response<CollectedSummary> shopCreateCollected = collectedShopService.create(userId,shopId);
        return shopCreateCollected;
    }
    /**
     * 删除店铺收藏记录
     *
     * @param userId 用户id
     * @param shopId 店铺id
     * @return 操作是否成功
     */
    @RequestMapping(value = "/{userId}/{shopId}/deleteShopCollect", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean> shopDeleteCollected(@PathVariable Long userId,
                                                 @PathVariable Long shopId) {
        Response<Boolean> shopDeleteCollected = collectedShopService.delete(userId,shopId);
        return shopDeleteCollected;
    }


    /**
     * 根据店铺名查找店铺
     *
     * @param keywords 店铺名
     * @return 查找结果
     */
    @RequestMapping(value = "/selectShopByName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
        Response<Paging<RichShop>>  selectShopByName(@RequestParam("pageNo") Integer pageNo,
                                                 @RequestParam("size") Integer size,
                                                    @RequestParam("keywords") String keywords){
        Response<Paging<RichShop>>  result = new  Response<Paging<RichShop>>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("q",keywords);
        result = shopService.searchShop(pageNo,size,params);
        return result;
    }
}
