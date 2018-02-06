package com.nowbook.coupons.service;

import com.nowbook.coupons.model.NbCou;

import java.util.List;
import java.util.Map;

/**
 * Created by yea01 on 2014/11/27.
 */
public interface CouponsManageService {
    public List<NbCou> findAllNbCou(long userId, int pageCount);
    public List<NbCou> findAllBySearch(Map<Object, Object> map);
    public Integer countAllCou(long userId);
    public Integer countCouBySearch(NbCou nbCou);
    public List<Map> findAll(int pageCount);
    public void chexiaoCoupons(long couponsId);
    public List<Map> searchAll(Map<String, Object> map);
    public void stopCoupons(Map<String, Object> map);
    public NbCou findEditById(long couponsId);
}
