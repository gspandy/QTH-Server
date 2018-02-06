package com.nowbook.coupons.dao;

import com.nowbook.common.model.Paging;
import com.nowbook.coupons.model.NbCou;
import com.nowbook.coupons.model.NbShowCouponView;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by zhum01 on 2014/8/19.
 */


@Repository
public class CouponsNbDao extends SqlSessionDaoSupport {
    public Integer adminCount(){

        return getSqlSession().selectOne("NbCou.adminCount");
    }
    public List<NbShowCouponView> getCouByUserId(Long userId){
        return getSqlSession().selectList("NbCouponsViews.getList",userId);
    }
    public Paging<NbCou> findCouponsAll(Map<String, Object> paramMap){
        Long total = getSqlSession().selectOne("NbCou.countOf", paramMap);
        if(total==null || total==0)
            return new Paging<NbCou>(0L, Collections.<NbCou>emptyList());
        if(!paramMap.containsKey("offset"))
            paramMap.put("offset",0);
        if(!paramMap.containsKey("limit"))
            paramMap.put("limit", total);
        List<NbCou> nbCous = getSqlSession().selectList("NbCou.pagination",paramMap);
        return new Paging<NbCou>(total, nbCous);

    }

    public int addCoupon(Map<String, Object> paramMap){
        return getSqlSession().insert("NbCou.addCoupon",paramMap);
    }
    public int updateCoupon(Map<String, Object> paramMap){
        return getSqlSession().update("NbCou.updateCoupon",paramMap);
    }
    public int updateCouponStatus(Map<String, Object> paramMap){
        return getSqlSession().update("NbCou.updateCouponStatus",paramMap);
    }
    public List<Map<String, Object>> findCategory(int categoryId){
        return getSqlSession().selectList("NbCou.findCategory",categoryId);
    }

    public List<NbCou> querySellerCouponsByParam(Map<String, Object> params) {
        return getSqlSession().selectList("NbCou.querySellerCouponsByParam",params);
    }

    public Paging<NbCou> queryCouponsByShopId(Map<String, Object> params){
        Long total = getSqlSession().selectOne("NbCou.queryCouponsCountByShopId", params);
        if (total > 0L) {
            List<NbCou> shops = getSqlSession().selectList("NbCou.queryCouponsByShopId", params);
            return new Paging<NbCou>(total, shops);
        }
        return new Paging<NbCou>(0L, Collections.<NbCou>emptyList());
    }
    public Integer insertItemIds(List<Map<String, Object>> items) {
        if (items.size() == 0)
            return 0;
        return getSqlSession().insert("NbCou.insertItemIds",items);
    }
    public List<Map<String,Object>> findEditItems(String couponsId){
        return getSqlSession().selectList("NbCou.findEditItems",couponsId);
    }
    public Integer deleteCouponsId(String couponsId) {
        return getSqlSession().delete("NbCou.deleteCouponsId",couponsId);
    }

}
