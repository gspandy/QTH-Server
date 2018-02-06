package com.nowbook.collect.manager;

import com.nowbook.collect.dao.CollectedItemDao;
import com.nowbook.collect.dao.CollectedShopDao;
import com.nowbook.collect.model.CollectedItem;
import com.nowbook.collect.model.CollectedShop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.nowbook.common.utils.Arguments.isNull;
import static com.nowbook.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-10-13 9:52 AM  <br>
 * Author:cheng
 */
@Component
public class CollectedManager {

    @Autowired
    private CollectedItemDao collectedItemDao;

    @Autowired
    private CollectedShopDao collectedShopDao;


    @Transactional
    public Long createCollectedItem(CollectedItem item) {
        checkArgument(notNull(item.getBuyerId()), "buyer.id.can.not.be.empty");
        checkArgument(notNull(item.getItemId()), "item.id.can.not.be.empty");
        checkArgument(notNull(item.getItemNameSnapshot()), "item.name.snapshot.can.not.be.empty");

        CollectedItem existed = collectedItemDao.getByUserIdAndItemId(item.getBuyerId(), item.getItemId());
        checkState(isNull(existed), "collected.item.already.existed");

        collectedItemDao.create(item);

        checkState(notNull(item.getId()), "collected.item.persist.error");
        return item.getId();
    }

    @Transactional
    public void bulkDeleteCollectedItems(Long buyerId, List<Long> itemIds) {
        checkArgument(notNull(buyerId), "buyer.id.can.not.be.empty");
        for (Long itemId: itemIds) {
            CollectedItem deleting = collectedItemDao.getByUserIdAndItemId(buyerId, itemId);
            if (notNull(deleting)) {
                collectedItemDao.delete(deleting.getId());
            }
        }
    }


    @Transactional
    public Long createCollectedShop(CollectedShop shop) {
        checkArgument(notNull(shop.getBuyerId()), "buyer.id.can.not.be.empty");
        checkArgument(notNull(shop.getShopId()), "shop.id.can.not.be.empty");
        checkArgument(notNull(shop.getShopNameSnapshot()), "shop.name.snapshot.can.not.be.empty");

        CollectedShop existed = collectedShopDao.getByUserIdAndShopId(shop.getBuyerId(), shop.getShopId());
        checkState(isNull(existed), "collected.shop.already.existed");
        collectedShopDao.create(shop);
        checkState(notNull(shop.getId()), "collected.shop.persist.error");
        return shop.getId();
    }


    @Transactional
    public void bulkDeleteCollectedShops(Long buyerId, List<Long> shopIds) {
        checkArgument(notNull(buyerId), "buyer.id.can.not.be.empty");
        for (Long shopId: shopIds) {
            CollectedShop deleting = collectedShopDao.getByUserIdAndShopId(buyerId, shopId);
            if (notNull(deleting)) {
                collectedShopDao.delete(deleting.getId());
            }
        }
    }

}
