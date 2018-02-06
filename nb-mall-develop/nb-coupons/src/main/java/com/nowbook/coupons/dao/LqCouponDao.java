package com.nowbook.coupons.dao;

import com.google.common.collect.ImmutableMap;
import com.nowbook.coupons.model.LqCouponView;
import com.nowbook.coupons.model.NbCou;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zhua02 on 2014/8/21.
 */
@Repository
public class LqCouponDao extends SqlSessionDaoSupport{

    public List<LqCouponView> findCouponAll(String today){
        return getSqlSession().selectList("LqCouponView.findCouponAll",today);
    }

    public int findUserLimit(int id){
        NbCou nbc=getSqlSession().selectOne("NbCou.findUserLimit", id);
        return nbc.getUseLimit();
    }

    public int findSendNum(int id){
        NbCou nbc=getSqlSession().selectOne("NbCou.findSendNum",id);
        return nbc.getSendNum();
    }

    public int findUseCount(int couponId){
        return (Integer)getSqlSession().selectOne("NbCouponsUser.findUseCount",couponId);
    }

    public int findUserUseCount(int couponId,int userid){
        return (Integer)getSqlSession().selectOne("NbCouponsUser.findUserUseCount", ImmutableMap.of("couponId", couponId, "userid", userid));
    }

    public void addUserCoupon(int couponId,int userid){
        getSqlSession().insert("NbCouponsUser.addUserCoupon",ImmutableMap.of("userId", userid, "couponId", couponId));
    }

    public void updateCouponReceive(int id){
        getSqlSession().update("NbCou.updateCouponReceive",id);
    }
}
