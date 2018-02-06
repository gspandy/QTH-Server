/*
 * Copyright (c) 2012 大连锦霖科技有限公司
 */

package com.nowbook.item.dto;

import com.nowbook.item.model.Item;
import com.nowbook.item.model.Sku;
import lombok.Getter;
import lombok.Setter;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2012-09-07
 */
public class RichItem extends Item {
    private static final long serialVersionUID = 3145869110713888943L;
    /**
     * non-persisted properties*
     */
    @Getter
    @Setter
    private Iterable<Long> categoryIds;  //后台类目1-4级

    @Getter
    @Setter
    private Iterable<Long> attributeIds;  //所有spu属性

    @Getter
    @Setter
    private Iterable<String> tags;       //店铺内类目

    @Getter
    @Setter
    private String sellerName;           //商家名称

    @Getter
    @Setter
    private Iterable<Sku> skus;          //sku属性

    @Setter
    @Getter
    private Long brandId;                //品牌id

    @Getter
    @Setter
    private Iterable<Integer> regionIds;  //地址id 从区级-全国

}
