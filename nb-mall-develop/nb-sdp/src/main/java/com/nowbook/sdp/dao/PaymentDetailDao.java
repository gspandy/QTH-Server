package com.nowbook.sdp.dao;

import com.nowbook.common.model.Paging;
import com.nowbook.sdp.model.PaymentDetail;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2016/9/6.
 */
@Repository
public class PaymentDetailDao extends SqlSessionDaoSupport {
    public void create(PaymentDetail paymentDetail) {
        getSqlSession().insert("PaymentDetailMapper.create", paymentDetail);
    }

    public void update(PaymentDetail paymentDetail) {
        getSqlSession().update("PaymentDetailMapper.update", paymentDetail);
    }

    public List<PaymentDetail> findBy(PaymentDetail paymentDetail) {
        return getSqlSession().selectList("PaymentDetailMapper.findBy", paymentDetail);
    }

    public Paging<PaymentDetail> paymentDetailPaging(PaymentDetail paymentDetail) {
        Paging<PaymentDetail> result = new Paging<PaymentDetail>();
        Long total = getSqlSession().selectOne("PaymentDetailMapper.paymentDetailListCount", paymentDetail);
        List<PaymentDetail> list = getSqlSession().selectList("PaymentDetailMapper.paymentDetailList", paymentDetail);
        result.setData(list);
        result.setTotal(total);
        return result;
    }
}
