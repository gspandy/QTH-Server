package com.nowbook.coupons.dao;

import com.nowbook.coupons.model.NbCouUserView;
import com.nowbook.coupons.model.NbCouUser;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * Created by yea01 on 2014/8/22.
 */

@Repository
public class NbCouUserDao extends SqlSessionDaoSupport {

    public List<NbCouUserView> queryCouponsAllByUser(Long userId, Long status,String nowDate){
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("startTime",nowDate);
        paramMap.put("endTime",nowDate);
        paramMap.put("status",status);
        paramMap.put("userId",userId);
        return getSqlSession().selectList("NbCouponsUserView.queryCouponsAllByUser",paramMap);
    }

    public List<NbCouUser> queryCouponsUserBy(Long userId,Long couponsId) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("couponId",couponsId);
        paramMap.put("userId",userId);
        return getSqlSession().selectList("NbCouponsUser.queryCouponsUserBy",paramMap);
    }

    public void updateCouponUser(Long id) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("id",id);
        getSqlSession().update("NbCouponsUser.updateCouponUser",paramMap);
    }
}
