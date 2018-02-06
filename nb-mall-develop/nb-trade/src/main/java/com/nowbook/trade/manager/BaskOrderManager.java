package com.nowbook.trade.manager;

import com.nowbook.trade.dao.BaskOrderDao;
import com.nowbook.trade.dao.OrderCommentDao;
import com.nowbook.trade.dao.OrderItemDao;
import com.nowbook.trade.model.BaskOrder;
import com.nowbook.trade.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by songrenfei on 14-9-18.
 */
@Component
public class BaskOrderManager {

    @Autowired
    private BaskOrderDao baskOrderDao;
    @Autowired
    private OrderCommentDao orderCommentDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Transactional
    public Long updateCommentAndSaveBaskOrder(Long orderCommentId,BaskOrder baskOrder,Long orderItemId){

        OrderItem orderItem = new OrderItem();
        orderItem.setId(orderItemId);
        orderItem.setIsBaskOrder(true);
        orderItemDao.update(orderItem);
       // orderCommentDao.setIsBaskOrder(orderCommentId);//更新评论为晒单评论
        Long id = baskOrderDao.create(baskOrder);
        return id;
    }


}
