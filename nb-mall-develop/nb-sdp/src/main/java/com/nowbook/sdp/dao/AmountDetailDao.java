package com.nowbook.sdp.dao;


import com.nowbook.common.model.Paging;
import com.nowbook.sdp.model.AmountDetail;
import com.nowbook.sdp.model.ItemSettlement;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AmountDetailDao extends SqlSessionDaoSupport {
    public void deleteByPrimaryKey(Long id) {
        getSqlSession().delete("AmountDetailMapper.deleteByPrimaryKey", id);
    }

    public void insert(AmountDetail record) {
        getSqlSession().insert("AmountDetailMapper.insert", record);
    }

    public void insertSelective(AmountDetail record) {
        getSqlSession().insert("AmountDetailMapper.insertSelective", record);
    }

    public void updateByPrimaryKeySelective(AmountDetail record) {
        getSqlSession().update("AmountDetailMapper.updateByPrimaryKeySelective", record);
    }

    public void updateByPrimaryKey(AmountDetail record) {
        getSqlSession().update("AmountDetailMapper.updateByPrimaryKey", record);
    }

    public Paging<AmountDetail> selectAmountDetail(AmountDetail amountDetail,Integer offset, Integer limit) {
        amountDetail.setOffset(offset);
        amountDetail.setLimit(limit);
        Long total = getSqlSession().selectOne("AmountDetailMapper.amountDetailCount", amountDetail);
        if(total == 0) {
            return new Paging<AmountDetail>(0L, Collections.<AmountDetail>emptyList());
        }
        List<AmountDetail> disAllPage = getSqlSession().selectList("AmountDetailMapper.selectAmountDetail",amountDetail);
        return new Paging<AmountDetail>(total, disAllPage);
    }
    @Transactional
    public void jobUpdateOrder(String intval) {

//        getSqlSession().delete("AmountDetailMapper.deleteAmountTemp");
//        getSqlSession().insert("AmountDetailMapper.insertAmountTemp", ImmutableMap.of("intval", intval));
        getSqlSession().update("AmountDetailMapper.jobUpdateOrder", ImmutableMap.of("intval", intval));


    }
    public Paging<String> selectDistibutorId(Integer offset, Integer limit) {
        Long total = getSqlSession().selectOne("AmountDetailMapper.selectDistibutorIdCount", ImmutableMap.of("offset", offset, "limit", limit));
        if(total == 0) {
            return new Paging<String>(0L, Collections.<String>emptyList());
        }
        List<String> disAllPage = getSqlSession().selectList("AmountDetailMapper.selectDistibutorId", ImmutableMap.of("offset", offset, "limit", limit));
        return new Paging<String>(total, disAllPage);
    }
    public String selectAmount(String distributorsId) {

        String ret = getSqlSession().selectOne("AmountDetailMapper.selectAmount", ImmutableMap.of("distributorsId", distributorsId));
        return ret;
    }
    //invalid
    public void deleteSum() {
        getSqlSession().update("AmountDetailMapper.deleteSum");
    }

    public void updateSum(String distrabutorId) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("distrabutorId", distrabutorId);

        getSqlSession().selectOne("AmountDetailMapper.callSum", param);
    }

    public void updateByOrder(AmountDetail record) {
        getSqlSession().update("AmountDetailMapper.updateByOrder", record);
    }

    /**
     * 根据子订单号查询子订单结算分页信息
     *
     * @param orderItemId   子订单号
     * @return 符合条件的分页信息
     */
    public ItemSettlement findByOrderItemId(Long orderItemId) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("orderItemId", orderItemId);
        return getSqlSession().selectOne("AmountDetailMapper.findByOrderItemId", params);
    }
}