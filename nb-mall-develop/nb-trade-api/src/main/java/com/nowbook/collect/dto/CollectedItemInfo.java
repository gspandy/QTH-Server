package com.nowbook.collect.dto;

import com.nowbook.collect.model.CollectedItem;
import com.nowbook.common.utils.BeanMapper;
import com.nowbook.item.model.Item;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-10-13 1:06 PM  <br>
 * Author:cheng
 */
@ToString
public class CollectedItemInfo extends CollectedItem {

    private static final long serialVersionUID = -4471407965228129689L;

    @Getter
    @Setter
    private Item item;


    public static CollectedItemInfo transform(CollectedItem collectedItem, Item item) {
        CollectedItemInfo collectedItemInfo = new CollectedItemInfo();
        BeanMapper.copy(collectedItem, collectedItemInfo);
        collectedItemInfo.setItem(item);
        return collectedItemInfo;
    }
}
