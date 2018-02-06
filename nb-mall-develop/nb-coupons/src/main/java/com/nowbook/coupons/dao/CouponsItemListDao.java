package com.nowbook.coupons.dao;

import com.nowbook.coupons.model.NbCouponsItemList;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhum01 on 2014/12/1.
 */
@Repository
public class CouponsItemListDao extends SqlSessionDaoSupport {
	public List<NbCouponsItemList> queryCouponsItemListBy(
			HashMap<Object, Object> paramMap) {

		return getSqlSession().selectList("NbCouponsItemList.queryCouponsItemListBy", paramMap);

	}

	public List<NbCouponsItemList> queryCouponsByShopId(Long shopId) {
			return getSqlSession().selectList("NbCouponsItemList.queryCouponsByShopId", shopId);
		
	}

	public int queryUserShopCou(Map<String, Object> params) {

		Integer countNum = getSqlSession().selectOne(
				"NbCouponsItemList.queryUserShopCou", params);

		return Integer.valueOf(countNum).intValue();
	}

	public int querySumUserCou(Map<String, Object> params) {

		Integer countNum = getSqlSession().selectOne(
				"NbCouponsItemList.querySumUserCou", params);

		return Integer.valueOf(countNum).intValue();
	}

}
