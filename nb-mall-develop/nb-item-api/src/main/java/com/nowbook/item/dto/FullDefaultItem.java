package com.nowbook.item.dto;

import com.nowbook.item.model.BaseSku;
import com.nowbook.item.model.DefaultItem;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yangzefeng on 14-1-10
 */
public class FullDefaultItem implements Serializable{
    private static final long serialVersionUID = -1959323113634356787L;

    @Getter
    @Setter
    private DefaultItem defaultItem;

    @Getter
    @Setter
    private List<BaseSku> skus;

    @Getter
    @Setter
    private SkuGroup skuGroup;
}
