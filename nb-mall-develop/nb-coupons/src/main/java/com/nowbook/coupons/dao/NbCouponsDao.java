package com.nowbook.coupons.dao;

import com.nowbook.common.model.Paging;
import com.nowbook.coupons.model.NbCou;
import com.nowbook.coupons.model.NbCouOrder;
import com.nowbook.coupons.model.NbCouOrderItem;
import com.nowbook.coupons.model.ShopCoupons;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class NbCouponsDao extends SqlSessionDaoSupport {

	public List<NbCouOrder> findByOrderIds(List<Long> ids) {
		return getSqlSession().selectList("NbCou.findByOrderIds", ids);
	}
    public List<NbCouOrderItem> findOrderItemsByOrderIds(List<Long> ids) {
        return getSqlSession().selectList("NbCou.findOrderItemsByOrderIds", ids);
    }

    /**
     * 查询可用的优惠劵信息
     * 优惠券状态：未生效（0）暂停（1）生效（2）失效(3)
     * **/
    public List<NbCou> queryNbCouponsBy(String nowDate,Long status) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("startTime",nowDate);
        paramMap.put("endTime",nowDate);
        paramMap.put("status",status);
        return getSqlSession().selectList("NbCou.queryNbCouponsBy", paramMap);
    }

    public NbCou queryCouponsById(Long couponsId) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("couponsId",couponsId);
        List<NbCou> resultList = getSqlSession().selectList("NbCou.queryCouponsById", paramMap);
        if(resultList!=null && resultList.size()>0){
            return resultList.get(0);
        }else{
            return null;
        }
    }

    public Boolean updateNbCou(NbCou nbCou) {
        return getSqlSession().insert("NbCou.updateNbCou",nbCou) == 1;
    }

    public Paging<NbCou> queryCouponsByPage(Integer offset, Integer size, Map<String, Object> params) {
        Long total = getSqlSession().selectOne("NbCou.countOf", params);
        if (total > 0L) {
            params.put("offset", offset);
            params.put("limit", size);
            List<NbCou> shops = getSqlSession().selectList("NbCou.pagination", params);
            return new Paging<NbCou>(total, shops);
        }
        return new Paging<NbCou>(0L, Collections.<NbCou>emptyList());
    }

    public Paging<ShopCoupons> queryShopCouponsByPage(Integer offset, Integer size, Map<String, Object> params) {
        Long total = getSqlSession().selectOne("NbCou.countShopOf", params);
        if (total > 0L) {
            params.put("offset", offset);
            params.put("limit", size);
            List<ShopCoupons> shops = getSqlSession().selectList("NbCou.pageShopCou", params);
            return new Paging<ShopCoupons>(total, shops);
        }
        return new Paging<ShopCoupons>(0L, Collections.<ShopCoupons>emptyList());
    }

    public List<NbCou> findAllSellCoupons(long userId,int pageCount){
        if(pageCount!=0){
            pageCount = pageCount*25;
        }
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("userId",userId);
        map.put("page",pageCount);

        return getSqlSession().selectList("NbCou.findSellCoupons",map);
    }
    public List<NbCou> findBySearch(Map<Object,Object> map){

        return getSqlSession().selectList("NbCou.findBySearch",map);
    }
    public Integer countCou(long userId){
        return getSqlSession().selectOne("NbCou.countCou",userId);
    }
    public Integer countCouBySearch(NbCou nbCou){
        return getSqlSession().selectOne("NbCou.countBySearch",nbCou);

    }
    public List<Map> findAdminAll(int page){
        if(page!=0){
            page = page*25;
        }
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("page",page);
        return getSqlSession().selectList("NbCou.findAdminCoupons",map);
    }
    public void chexiaoCoupons(long couponsId){

         getSqlSession().update("NbCou.chexiaoCoupons",couponsId);
    }
    public List<Map> searchAll(Map<String,Object> map){
        return getSqlSession().selectList("NbCou.searchAll",map);

    }

    public NbCou queryShopCouponsById(Long couponsId) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("couponsId",couponsId);
        List<NbCou> resultList = getSqlSession().selectList("NbCou.queryShopCouponsById", paramMap);
        if(resultList!=null && resultList.size()>0){
            return resultList.get(0);
        }else{
            return null;
        }
    }
    public void stopCoupons(Map<String, Object> map){

        getSqlSession().update("NbCou.stopCoupons",map);
    }
    public NbCou findEditById(long couponsId){
        return getSqlSession().selectOne("NbCou.editById",couponsId);
    }

    public List<Map<String, String>> queryListCouName(Long couponsId){
        return getSqlSession().selectList("NbCou.queryCouName",couponsId);

    }


}
