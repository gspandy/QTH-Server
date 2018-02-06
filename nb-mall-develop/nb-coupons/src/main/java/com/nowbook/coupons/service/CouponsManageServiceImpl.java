package com.nowbook.coupons.service;

import com.nowbook.coupons.dao.NbCouponsDao;
import com.nowbook.coupons.model.NbCou;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by yea01 on 2014/11/27.
 */
@Service
public class CouponsManageServiceImpl implements CouponsManageService {
    @Autowired
    private NbCouponsDao nbCouponsDao;

    @Override
    public List<NbCou> findAllNbCou(long userId,int pageCount) {
        return nbCouponsDao.findAllSellCoupons(userId,pageCount);
    }

    @Override
    public List<NbCou> findAllBySearch(Map<Object,Object> map) {
        return nbCouponsDao.findBySearch(map);
    }

    @Override
    public Integer countAllCou(long userId) {

        return nbCouponsDao.countCou(userId);
    }

    @Override
    public Integer countCouBySearch(NbCou nbCou) {
            return nbCouponsDao.countCouBySearch(nbCou);
    }

    @Override
    public List<Map> findAll(int pageCount) {
        return nbCouponsDao.findAdminAll(pageCount);
    }

    @Override
    public void chexiaoCoupons(long couponsId) {
         nbCouponsDao.chexiaoCoupons(couponsId);
    }

    @Override
    public List<Map> searchAll(Map<String, Object> map) {
        return nbCouponsDao.searchAll(map);
    }

    @Override
    public void stopCoupons(Map<String, Object> map) {
        nbCouponsDao.stopCoupons(map);
    }

    //编辑查询商家优惠券信息接口
    @Override
    public NbCou findEditById(long couponsId) {
        return nbCouponsDao.findEditById(couponsId);
    }

}
