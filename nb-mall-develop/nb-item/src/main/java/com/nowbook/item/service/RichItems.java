/*
 * Copyright (c) 2012 大连锦霖科技有限公司
 */

package com.nowbook.item.service;

import com.nowbook.category.model.RichAttribute;
import com.nowbook.category.model.Spu;
import com.nowbook.category.service.AttributeService;
import com.nowbook.category.service.BackCategoryService;
import com.nowbook.category.service.SpuService;
import com.nowbook.common.model.Response;
import com.nowbook.common.utils.BeanMapper;
import com.nowbook.exception.ServiceException;
import com.nowbook.item.dao.mysql.BrandDao;
import com.nowbook.item.dao.redis.ItemsTagsDao;
import com.nowbook.item.dto.RichItem;
import com.nowbook.item.model.Brand;
import com.nowbook.item.model.Item;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.service.AccountService;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2012-09-07
 */
@Component
public class RichItems {
    private final static Logger log = LoggerFactory.getLogger(RichItems.class);
    private final BackCategoryService backCategoryService;
    private final AttributeService attributeService;
    private final SpuService spuService;
    private final AccountService<? extends BaseUser> accountService;
    private final ItemsTagsDao itemsTagsDao;
    private final BrandDao brandDao;
    private final static Splitter splitter = Splitter.on(",").trimResults().omitEmptyStrings();
    //private final

    @Autowired
    RichItems(BackCategoryService backCategoryService, AttributeService attributeService,
              SpuService spuService, AccountService<? extends BaseUser> accountService, ItemsTagsDao itemsTagsDao, BrandDao brandDao) {
        this.backCategoryService = backCategoryService;
        this.attributeService = attributeService;
        this.spuService = spuService;
        this.accountService = accountService;
        this.itemsTagsDao = itemsTagsDao;
        this.brandDao = brandDao;
    }

    public RichItem make(Item item) {
        RichItem richItem = BeanMapper.map(item, RichItem.class);
        //将品牌放到商品标题内, 以便搜索
        Long brandId = item.getBrandId();
        Brand brand = brandDao.findById(brandId);
        if(brand == null){
            log.error("failed to find brand by id {}", brandId);

        }else{
            richItem.setName(richItem.getName()+"/"+brand.getName());
        }
        Long spuId = item.getSpuId();
        if(spuId == null){
            log.error("item(id={}) has no spuId set", richItem.getId());
            throw new ServiceException("spu.id.null");
        }
        final Response<Spu> spuR = spuService.findById(spuId);
        if(!spuR.isSuccess()){
            log.error("failed to find spu by id={}, error code:{}", spuId, spuR.getError());
            throw new ServiceException("spu.not.found");
        }
        Spu spu = spuR.getResult();
        Long categoryId = spu.getCategoryId();
        Response<List<Long>> ancestorsR = backCategoryService.ancestorsOf(categoryId);
        if(!ancestorsR.isSuccess()) {
            log.error("fail to find ancestor category by leaf category id={}, item id={}, error code:{}",
                    categoryId, item.getId(), ancestorsR.getError());
            throw new ServiceException("category.not.found");
        }
        List<Long> ancestors = ancestorsR.getResult();
        List<RichAttribute> attributes = attributeService.findSpuAttributesBy(spuId);
        richItem.setCategoryIds(ancestors);
        ImmutableSet.Builder<Long> builder = new ImmutableSet.Builder<Long>();
        for (RichAttribute attribute : attributes) {
            builder = builder.add(attribute.getAttributeValueId());
        }
        richItem.setAttributeIds(builder.build());
        Response<? extends BaseUser> sr = accountService.findUserById(item.getUserId());
        if (!sr.isSuccess()) {
            log.error("failed to find seller(id={}),error code:{}", item.getUserId(), sr.getError());
            throw new IllegalStateException("seller not found, userId="+item.getUserId());
        }
        BaseUser seller = sr.getResult();
        richItem.setSellerName(seller.getName());
        Set<String> tags = itemsTagsDao.tagsOfItem(item.getUserId(), item.getId());
        if (tags.isEmpty()) {
            tags = ImmutableSet.of("未分类");
        }
        richItem.setTags(tags);
        richItem.setBrandId(item.getBrandId());
        //add regionIds
        if (item.getRegion()!=null && !item.getRegion().isEmpty()) {
            List<String> regionIds = splitter.splitToList(item.getRegion());
            List<Integer> regionIdi = Lists.transform(regionIds, new Function<String, Integer>() {
                @Override
                public Integer apply(String input) {
                    return Integer.valueOf(input);
                }
            });
            richItem.setRegionIds(regionIdi);
        }
        return richItem;
    }
}
