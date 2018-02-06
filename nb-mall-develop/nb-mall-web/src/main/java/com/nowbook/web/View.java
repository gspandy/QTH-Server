/*
 * Copyright (c) 2012 大连锦霖科技有限公司
 */

package com.nowbook.web;

import com.nowbook.category.model.FrontCategory;
import com.nowbook.category.service.FrontCategoryService;
import com.nowbook.collect.service.CollectedItemService;
import com.nowbook.common.model.Response;
import com.nowbook.exception.JsonResponseException;
import com.nowbook.item.model.Item;
import com.nowbook.item.model.TitleKeyword;
import com.nowbook.item.service.ItemService;
import com.nowbook.item.service.TitleKeywordService;
import com.nowbook.rlt.buying.model.BuyingItem;
import com.nowbook.rlt.buying.service.BuyingItemService;
import com.nowbook.rlt.popularizeurl.service.PopularizeUrlService;
import com.nowbook.rlt.predeposit.model.PreDeposit;
import com.nowbook.rlt.predeposit.service.PreDepositService;
import com.nowbook.rlt.presale.model.PreSale;
import com.nowbook.rlt.presale.service.PreSaleService;
import com.nowbook.shop.model.Shop;
import com.nowbook.shop.service.ShopService;
import com.nowbook.site.exception.NotFound404Exception;
import com.nowbook.site.exception.Server500Exception;
import com.nowbook.site.model.PageCategory;
import com.nowbook.site.model.Site;
import com.nowbook.site.service.SiteService;
import com.nowbook.user.base.UserUtil;
import com.nowbook.web.controller.view.ViewRender;
import com.nowbook.web.misc.MessageSources;
import com.google.common.base.*;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Desc: 特殊 url 入口
 * Date: 8/16/12 10:48 AM
 */
@Controller
public class View {
    public static final Logger log = LoggerFactory.getLogger(View.class);
    public static final String PRESALE_DOMAIN = "yushou.nowbook.cn";
    public static final String PRESALE_PATH = "pre-items";

    @Autowired
    private ViewRender viewRender;
    @Autowired
    private SiteService siteService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private PreSaleService preSaleService;
    @Autowired
    private MessageSources messageSources;
    @Autowired
    private TitleKeywordService titleKeywordService;
    @Autowired
    private FrontCategoryService frontCategoryService;
    @Autowired
    private CollectedItemService collectedItemService;
    @Autowired
    private BuyingItemService buyingItemService;
    //净水押金模式Service
    @Autowired
    private PreDepositService preDepositService;

    @Autowired
    private PopularizeUrlService popularizeUrlService;

    private String mockStr;

    private static final Splitter splitter = Splitter.on(" ").omitEmptyStrings().trimResults();

    @PostConstruct
    private void init() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "</head>\n" +
                "<body>");
        sb.append(Strings.repeat("0", 320000));
        sb.append("</body>\n" +
                "</html>");
        mockStr = sb.toString();
    }

    /**
     * Item页面渲染入口
     *
     * @param itemId 商品ID
     */
    @RequestMapping(value = "/items/{itemId}", method = RequestMethod.GET)
    public void item(HttpServletRequest request, HttpServletResponse response, @RequestHeader("Host") String domain,
                     @PathVariable Long itemId, @RequestParam Map<String, Object> context) {

        Response<BuyingItem> result = buyingItemService.findLatestByItemId(itemId);
        if (result.isSuccess() && result.getResult()!=null) {
            BuyingItem buyingItem = result.getResult();
            try {
                response.sendRedirect("/buying-items/"+buyingItem.getBuyingActivityId()+"/"+itemId);
            } catch (IOException e) {
                log.error("Failed to send redirect url.", e);
                // Ignore error on purpose.
            }
            return;
        }

        context.put("itemId", itemId);
        Response<Item> itemR = itemService.findById(itemId);
        if(!itemR.isSuccess()){
            log.error("failed to find item(id={}),error code:{}",itemId,itemR.getError());
            throw new NotFound404Exception(messageSources.get(itemR.getError()));
        }
        Item item = itemR.getResult();
        context.put("spuId", item.getSpuId());
        context.put("buyerId", UserUtil.getUserId());
        context.put("seo", buildSEOInfoForItem(item));
        renderShop(request, response, item.getUserId(), PageCategory.DETAIL.getPath(), context);
    }

    @RequestMapping(value = "/buying-items/{activityId}/{itemId}", method = RequestMethod.GET)
    public void buyingItem(HttpServletRequest request, HttpServletResponse response,
                           @PathVariable("activityId") Long activityId,
                           @RequestHeader("Host") String domain,
                           @PathVariable("itemId") Long itemId,
                           @RequestParam Map<String,Object> context) {
        context.put("itemId", itemId);
        context.put("activityId", activityId);
        context.put("itemIdOrBundleId", itemId);
        Response<Item> itemR = itemService.findById(itemId);
        if(!itemR.isSuccess()){
            log.error("failed to find item(id={}),error code:{}",itemId,itemR.getError());
            throw new NotFound404Exception(messageSources.get(itemR.getError()));
        }
        Item item = itemR.getResult();
        Response<Shop> shopR = shopService.findByUserId(item.getUserId());
        if(!shopR.isSuccess()) {
            log.error("fail to find shop by user id={}, error code={}", item.getUserId(), shopR.getError());
            throw new NotFound404Exception(messageSources.get(shopR.getError()));
        }
        Shop shop = shopR.getResult();
        context.put("sellerId", shop.getUserId());
        context.put("spuId", item.getSpuId());
        context.put("detailType", 0);
        viewRender.view(domain.split(":")[0], "buying-items", request, response, context);
    }

    @RequestMapping(value = "/pre-items/{itemId}", method = RequestMethod.GET)
    public void preItem(HttpServletRequest request, HttpServletResponse response,
                        @RequestHeader("Host") String domain,
                        @PathVariable Long itemId, @RequestParam Map<String, Object> context) {

        try {
            context.put("itemId", itemId);
            Response<Item> itemR = itemService.findById(itemId);
            if(!itemR.isSuccess()){
                log.error("failed to find item(id={}),error code:{}",itemId,itemR.getError());
                throw new NotFound404Exception(messageSources.get(itemR.getError()));
            }
            Item item = itemR.getResult();
            //find random sellerId for preSale item
            Response<PreSale> preSaleR = preSaleService.findPreSaleByItemId(item.getId());
            if(!preSaleR.isSuccess()) {
                log.error("fail to find preSale by itemId={}, error code:{}",item.getId(),preSaleR.getError());
                throw new NotFound404Exception(messageSources.get(preSaleR.getError()));
            }
            PreSale preSale = preSaleR.getResult();
            String shopIds = preSale.getShopIds();
            List<Long> parsingIds = Lists.transform(splitter.splitToList(shopIds), new Function<String, Long>() {
                @Override
                public Long apply(String input) {
                    return Long.valueOf(input);
                }
            });
            //商品id mod 符合授权店铺数量， 保证详情页和下单预览页的商家是同一个
            if(parsingIds.isEmpty()) {
                log.error("fail to find auth shop id by preSale id={}",preSale.getId());
                throw new JsonResponseException(500, messageSources.get("seller.not.found"));
            } 
            
            Long index = itemId % parsingIds.size();
            Long shopId = parsingIds.get(index.intValue());

            Response<Shop> shopR = shopService.findById(shopId);
            if(!shopR.isSuccess()) {
                log.error("fail to find shop by id={}, error code={}",shopId, shopR.getError());
                throw new NotFound404Exception(messageSources.get(shopR.getError()));
            }
            Shop shop = shopR.getResult();
            context.put("sellerId", shop.getUserId());
            context.put("spuId", item.getSpuId());
            context.put("seo", buildSEOInfoForItem(item));
            viewRender.view(domain.split(":")[0], "pre-items", request, response, context);

        } catch (Exception e) {
            log.error("render pre-items raise {}", Throwables.getStackTraceAsString(e));
        }
    }

    @RequestMapping(value = "/shops/{sellerId}", method = RequestMethod.GET)
    public void shop(HttpServletRequest request, HttpServletResponse response,
                     @PathVariable Long sellerId, @RequestParam Map<String, Object> context) {
        Response<Shop> shopR = shopService.findByUserId(sellerId);
        if(!shopR.isSuccess()) {
            log.error("shop not found with sellerId={}, error code={}", sellerId, shopR.getError());
            throw new JsonResponseException(500, messageSources.get(shopR.getError()));
        }
        //if shop status frozen return 404
        if(Objects.equal(shopR.getResult().getStatus(), Shop.Status.FROZEN.value())) {
            log.warn("shop(id={}) is frozen, can not visit", shopR.getResult().getId());
            throw new NotFound404Exception();
        }
        Response<Shop> shopResult = shopService.findByUserId(sellerId);
        if (shopResult.isSuccess()){
            Shop shop = shopResult.getResult();
            context.put("seo", buildSEOInfoForShop(shop));
        }
        renderShop(request, response, sellerId, PageCategory.INDEX.getPath(), context);
    }

    @RequestMapping(value = "/shops/{sellerId}/{path}", method = RequestMethod.GET)
    public void shopPath(HttpServletRequest request, HttpServletResponse response,
                         @PathVariable Long sellerId, @PathVariable String path,
                         @RequestParam Map<String, Object> context) {
        Response<Shop> shopResult = shopService.findByUserId(sellerId);
        if (shopResult.isSuccess()){
            Shop shop = shopResult.getResult();
            context.put("seo", buildSEOInfoForShop(shop));
        }
        renderShop(request, response, sellerId, path, context);
    }

    @RequestMapping(value = "/mock_test", method = RequestMethod.GET)
    public void mockTest(HttpServletRequest request, HttpServletResponse response) {
        response.setContentLength(mockStr.length());
        try {
            response.getWriter().write(mockStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public void search(HttpServletRequest request,
                       HttpServletResponse response,
                       @RequestHeader("Host") String domain,
                       @RequestParam(value = "fcid", required = false) Long fcid,
                       @RequestParam Map<String, Object> context){
        if(fcid != null){
            Response<TitleKeyword> result = titleKeywordService.findByNameId(fcid);
            if(result.isSuccess()){
                context.put("seo", result.getResult());
            }
            Response<Set<Long>> idsResult = frontCategoryService.ancestorsOf(fcid);
            if (idsResult.isSuccess()) {
                Response<FrontCategory> rootCategoryResult = frontCategoryService.findById(idsResult.getResult().iterator().next());
                if (rootCategoryResult.isSuccess()) {
                    context.put("mallChannel", rootCategoryResult.getResult().getName());
                }
            }
        }
        viewRender.view(domain.split(":")[0], "search", request, response, context);
    }

    private void renderShop(HttpServletRequest request, HttpServletResponse response, Long sellerId, String path, Map<String, Object> context) {
        context.put("sellerId", sellerId);
        Response<Site> sr = siteService.findShopByUserId(sellerId);
        if(!sr.isSuccess()){
            log.error("failed to find site for seller(id={}),error code:{}",sellerId,sr.getError());
            throw new Server500Exception(messageSources.get(sr.getError()) );
        }
        Site site = sr.getResult();
        viewRender.viewSite(site.getReleaseInstanceId(), path, request, response, false, context);
    }

    private TitleKeyword buildSEOInfoForItem(Item item){
        TitleKeyword titleKeyword = new TitleKeyword();
        titleKeyword.setTitle(messageSources.get("item.templates.title", item.getName()));
        titleKeyword.setKeyword(messageSources.get("item.templates.keywords", item.getName()));
        titleKeyword.setDesc(messageSources.get("item.templates.description", item.getName()));
        return titleKeyword;
    }

    private TitleKeyword buildSEOInfoForShop(Shop shop){
        TitleKeyword titleKeyword = new TitleKeyword();
        String title = Joiner.on("|").skipNulls().join(shop.getName(), shop.getStreet(), shop.getPhone());
        String desc = Joiner.on("、").skipNulls().join(shop.getName(), shop.getStreet(), shop.getPhone());
        titleKeyword.setTitle(messageSources.get("shop.templates.title", title));
        titleKeyword.setKeyword(messageSources.get("shop.templates.keywords", title));
        titleKeyword.setDesc(messageSources.get("shop.templates.description", desc));
        return titleKeyword;
    }

    /*
    *押金模式入口
     */
    @RequestMapping(value = "/predeposit-items/{itemId}", method = RequestMethod.GET)
    public void waterPreItem(HttpServletRequest request, HttpServletResponse response,
                        @RequestHeader("Host") String domain,
                        @PathVariable Long itemId, @RequestParam Map<String, Object> context) {

        try {
            context.put("itemId", itemId);
            // 根据id查找商品信息
            Response<Item> itemR = itemService.findById(itemId);
            if(!itemR.isSuccess()){
                log.error("failed to find item(id={}),error code:{}",itemId,itemR.getError());
                throw new NotFound404Exception(messageSources.get(itemR.getError()));
            }
            Item item = itemR.getResult();
            //在pre_sales（预售信息表）中查找预售信息
            Response<PreDeposit> preSaleR = preDepositService.findPreDepositByItemId(item.getId());
            if(!preSaleR.isSuccess()) {
                log.error("fail to find preSale by itemId={}, error code:{}",item.getId(),preSaleR.getError());
                throw new NotFound404Exception(messageSources.get(preSaleR.getError()));
            }
            PreDeposit preSale = preSaleR.getResult();
            //获取店铺信息
            String shopIds = preSale.getShopIds();
            List<Long> parsingIds = Lists.transform(splitter.splitToList(shopIds), new Function<String, Long>() {
                @Override
                public Long apply(String input) {
                    return Long.valueOf(input);
                }
            });
            //商品id mod 符合授权店铺数量， 保证详情页和下单预览页的商家是同一个
            if(parsingIds.isEmpty()) {
                log.error("fail to find auth shop id by preSale id={}",preSale.getId());
                throw new JsonResponseException(500, messageSources.get("seller.not.found"));
            }

            Long index = itemId % parsingIds.size();
            Long shopId = parsingIds.get(index.intValue());

            Response<Shop> shopR = shopService.findById(shopId);
            if(!shopR.isSuccess()) {
                log.error("fail to find shop by id={}, error code={}",shopId, shopR.getError());
                throw new NotFound404Exception(messageSources.get(shopR.getError()));
            }
            Shop shop = shopR.getResult();
            context.put("sellerId", shop.getUserId());
            context.put("spuId", item.getSpuId());
            context.put("seo", buildSEOInfoForItem(item));
            viewRender.view(domain.split(":")[0], "predeposit-items", request, response, context);

        } catch (Exception e) {
            log.error("render pre-items raise {}", Throwables.getStackTraceAsString(e));
        }
    }

    /*
    *推广端连接
     */
    @RequestMapping(value = "/u/{popUrlCode}")
    public void getPopularizeUrl(HttpServletRequest request, HttpServletResponse response,
                             @RequestHeader("Host") String domain,
                             @PathVariable String popUrlCode, @RequestParam Map<String, Object> context) {

        try {
            context.put("popUrlCode", popUrlCode);

            Response<String> urlR = popularizeUrlService.getUrl(popUrlCode);

           if (!urlR.isSuccess()) {

               throw new NotFound404Exception(messageSources.get(urlR.getError()));
            }

           response.sendRedirect(urlR.getResult());

        } catch (Exception e) {
            log.error("get PopularizeUrl fail {}", Throwables.getStackTraceAsString(e));
        }
    }
}