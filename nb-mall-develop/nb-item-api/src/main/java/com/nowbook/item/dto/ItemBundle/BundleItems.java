package com.nowbook.item.dto.ItemBundle;

import com.nowbook.item.model.Item;
import com.nowbook.item.model.ItemBundle;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yangzefeng on 14-4-24
 */
public class BundleItems implements Serializable {
    private static final long serialVersionUID = -4003926908602718100L;

    @Getter
    @Setter
    private ItemBundle itemBundle;

    @Getter
    @Setter
    private List<Item> items;
}
