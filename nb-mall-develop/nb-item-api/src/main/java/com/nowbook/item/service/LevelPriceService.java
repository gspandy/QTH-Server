package com.nowbook.item.service;

import com.nowbook.item.dto.RichItem;
import com.nowbook.item.model.Item;
import com.nowbook.item.model.Sku;

/**
 * @author dpzh
 * @create 2017-09-27 10:03
 * @description:<类文件描述>
 **/
public interface LevelPriceService {

    /**
     * @description: 获取item对应会员等级价格
     * @author dpzh
     * @create 2017/9/27 10:13
     * @param item
     * @return: 返回该会员对应的商品价格
     **/
    public Integer getUserLevelPrice(RichItem item);

    /**
     * @description: 获取SKU对应会员等级价格
     * @author dpzh
     * @create 2017/9/27 10:13
     * @param sku
     * @return: 返回该会员对应的SKU价格
     **/
    public Integer getUserLevelPrice(Sku sku);

    /**
     * @description: 获取item对应会员等级价格
     * @author dpzh
     * @create 2017/9/27 10:13
     * @param item
     * @return: 返回该会员对应的商品价格
     **/
    public Integer getUserLevelPrice(Item item);

    /**
     * @description: 获取item对应会员等级价格
     * @author dpzh
     * @create 2017/9/27 10:13
     * @param item
     * @return: 返回该会员对应的商品价格
     **/
    public Integer getUserLevelPriceForCustomer(Item item,Long userId);

    /**
     * @description: 获取item各个会员等级价格
     * @author dpzh
     * @create 2017/9/27 10:13
     * @param item
     * @return: 返回含有各个等级价格商品信息
     **/
    public Item getItemLevelPrice(Item item);

}
