package com.nowbook.item.service;

import com.nowbook.common.model.Response;

/**
 * @Description: <br/>
 * @Author: Benz.Huang@aimymy.com <br/>
 * @DATE: 2014/12/22 <br/>
 */
public interface FeedItemService {

    /**
     * 记录库存发生更新的items
     * @return
     */
    Response<Boolean>  createChangeItem(Long itemId);
}
