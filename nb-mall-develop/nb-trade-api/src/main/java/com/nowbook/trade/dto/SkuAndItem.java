package com.nowbook.trade.dto;

import com.nowbook.item.model.Item;
import com.nowbook.item.model.Sku;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-12-02
 */
public class SkuAndItem implements Serializable {
    private static final long serialVersionUID = 4299217764375423591L;

    @Getter
    @Setter
    private Sku sku;

    @Getter
    @Setter
    private Item item;

}
