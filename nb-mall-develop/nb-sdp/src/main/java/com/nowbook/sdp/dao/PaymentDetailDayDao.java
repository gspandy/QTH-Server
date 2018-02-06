package com.nowbook.sdp.dao;

import com.nowbook.common.model.Paging;
import com.nowbook.sdp.model.PaymentDetailDay;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2016/9/6.
 */
@Repository
public class PaymentDetailDayDao extends SqlSessionDaoSupport {
    public void create(PaymentDetailDay paymentDetailDay) {
         getSqlSession().insert("PaymentDetailDayMapper.create", paymentDetailDay);
    }

    public void update(PaymentDetailDay paymentDetailDay) {
        getSqlSession().update("PaymentDetailDayMapper.update", paymentDetailDay);
    }

    public List<PaymentDetailDay> findBy(PaymentDetailDay paymentDetailDay) {
        return getSqlSession().selectList("PaymentDetailDayMapper.findBy", paymentDetailDay);
    }

    public Paging<PaymentDetailDay> paymentDetailDayPaging(PaymentDetailDay paymentDetailDay) {
        Paging<PaymentDetailDay> result = new Paging<PaymentDetailDay>();
        Long total = getSqlSession().selectOne("PaymentDetailDayMapper.countPaymentDetailDay", paymentDetailDay);
        List<PaymentDetailDay> list = getSqlSession().selectList("PaymentDetailDayMapper.findBy", paymentDetailDay);
        result.setData(list);
        result.setTotal(total);
        return result;
    }
}
