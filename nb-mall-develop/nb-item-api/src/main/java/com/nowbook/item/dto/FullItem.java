package com.nowbook.item.dto;

import com.nowbook.category.model.RichAttribute;
import com.nowbook.item.model.Item;
import com.nowbook.item.model.ItemDetail;
import com.nowbook.item.model.Sku;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-11-14
 */
public class FullItem implements Serializable {
    private static final long serialVersionUID = -2287329799936191583L;

    @Getter
    @Setter
    private Item item;

    @Getter
    @Setter
    private ItemDetail itemDetail;

    @Getter
    @Setter
    private List<Sku> skus;

    @Getter
    @Setter
    private Map<String, List<Map<String, String>>> skuGroup = Collections.emptyMap(); //for dubbo serialization sake

    @Getter
    @Setter
    private List<RichAttribute> attributes = Collections.emptyList();  //for dubbo serialization sake
}
