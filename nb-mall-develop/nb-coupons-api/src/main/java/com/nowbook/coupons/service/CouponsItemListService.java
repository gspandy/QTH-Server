package com.nowbook.coupons.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.coupons.model.NbCouponsItemList;

import java.util.List;
import java.util.Map;

/**
 * Created by zhum01 on 2014/12/1.
 */
public interface CouponsItemListService {
    Response<List<NbCouponsItemList>> queryCouponsItemListBy(@ParamInfo("couponsId") Long couponsId, @ParamInfo("itemId") Long itemId, @ParamInfo("shopId") Long shopId, @ParamInfo("couponsCode") String couponsCode);
    
    List<NbCouponsItemList> findCouponsbyShopId(@ParamInfo("shopId") Long shopId);
    
    int queryUserShopCou(Map<String, Object> params);	
    
    int sumUserCou(Map<String, Object> params);

}
