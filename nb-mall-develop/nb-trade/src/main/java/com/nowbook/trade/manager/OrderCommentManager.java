package com.nowbook.trade.manager;

import com.nowbook.trade.dao.OrderCommentDao;
import com.nowbook.trade.dao.OrderItemDao;
import com.nowbook.trade.model.OrderComment;
import com.nowbook.trade.model.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 14-2-13
 * Time: PM6:10
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */

@Slf4j
@Component
public class OrderCommentManager {

    @Autowired
    private OrderCommentDao commentDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Transactional
    public List<Long> create(Iterable<OrderComment> comments) {
        List<Long> result = new ArrayList<Long>();
        for (OrderComment comment: comments) {
            comment.setIsBaskOrder(false);//默认不是晒单评论
            result.add(commentDao.create(comment));

            //将该子订单设为已评价
            OrderItem oi = new OrderItem();
            oi.setId(comment.getOrderItemId());
            oi.setHasComment(true);
            orderItemDao.update(oi);
        }

        return result;
    }
}
