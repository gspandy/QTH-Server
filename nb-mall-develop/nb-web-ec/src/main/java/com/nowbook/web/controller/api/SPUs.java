/*
 * Copyright (c) 2013 大连锦霖科技有限公司
 */

package com.nowbook.web.controller.api;

import com.nowbook.category.model.AttributeKey;
import com.nowbook.category.model.AttributeValue;
import com.nowbook.category.model.RichAttribute;
import com.nowbook.category.model.Spu;
import com.nowbook.category.service.AttributeService;
import com.nowbook.category.service.SpuService;
import com.nowbook.common.model.Response;
import com.nowbook.exception.JsonResponseException;
import com.nowbook.item.model.DefaultItem;
import com.nowbook.item.service.DefaultItemService;
import com.nowbook.web.misc.MessageSources;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2012-09-13
 */
@Controller
@RequestMapping("/api")
public class SPUs {
    private final static Logger log = LoggerFactory.getLogger(SPUs.class);

    private final AttributeService attributeService;

    private final SpuService spuService;

    private final MessageSources messageSources;

    private final DefaultItemService defaultItemService;

    @Autowired
    public SPUs(AttributeService attributeService, SpuService spuService,
                MessageSources messageSources, DefaultItemService defaultItemService) {
        this.attributeService = attributeService;
        this.spuService = spuService;
        this.messageSources = messageSources;
        this.defaultItemService = defaultItemService;
    }

    @RequestMapping(value = "/spus/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> find(@PathVariable("id") Long spuId) {
        Response<Spu> result = spuService.findById(spuId);
        if (!result.isSuccess()) {
            log.error("find spu(id={}) failed,error code:{}", spuId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        List<RichAttribute> spuAttributes = attributeService.findSpuAttributesBy(spuId);
        Map<AttributeKey, List<AttributeValue>> skuAttributeMap = attributeService.findSkuAttributesBy(spuId);
        List<AttributeKeyValues> skuKeyValues = from(skuAttributeMap);

        Response<DefaultItem> defaultItemR = defaultItemService.findDefaultItemBySpuId(spuId);
        if (!defaultItemR.isSuccess()) {
            log.warn("failed to find defaultItem for spuId={}, error code:{}", spuId, defaultItemR.getError());
            return ImmutableMap.of("spu", result.getResult(), "spuAttributes", spuAttributes, "skuAttributes", skuKeyValues);
        }

        DefaultItem defaultItem = defaultItemR.getResult();
        return ImmutableMap.of("defaultItem", defaultItem, "spu", result.getResult(),
                "spuAttributes", spuAttributes, "skuAttributes", skuKeyValues);

    }

    @RequestMapping(value = "/spus/simple/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Spu findSimple(@PathVariable("id") Long spuId) {
        Response<Spu> result = spuService.findById(spuId);
        if (!result.isSuccess()) {
            log.error("find spu(id={}) failed,error code:{}", spuId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/category/{categoryId}/spus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Spu> listOf(@PathVariable("categoryId") Long categoryId) {

        Response<List<Spu>> result = spuService.findByCategoryId(categoryId);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to find spus under categoryId:{},cause:{}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

    }

    public static class AttributeKeyValues {
        @Getter
        @Setter
        private AttributeKey attributeKey;

        @Getter
        @Setter
        private List<AttributeValue> attributeValues;

    }

    private List<AttributeKeyValues> from(Map<AttributeKey, List<AttributeValue>> skuAttributeMap) {
        List<AttributeKeyValues> result = Lists.newArrayListWithCapacity(skuAttributeMap.keySet().size());
        for (AttributeKey attributeKey : skuAttributeMap.keySet()) {
            AttributeKeyValues akv = new AttributeKeyValues();
            akv.setAttributeKey(attributeKey);
            akv.setAttributeValues(skuAttributeMap.get(attributeKey));
            result.add(akv);
        }
        return result;
    }

}
