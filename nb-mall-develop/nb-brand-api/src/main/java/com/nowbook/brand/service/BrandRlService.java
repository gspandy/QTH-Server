package com.nowbook.brand.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.brand.model.BrandRlView;
import com.nowbook.brand.model.BrandWRlView;
import com.nowbook.brand.model.BrandsClubKey;

import java.util.List;

/**
 * Created by zhua02 on 2014/7/28.
 */
public interface BrandRlService {
    List<BrandRlView> findByPro(String shopname, int shopid, int businessid, int status, String starttime, String endtime);
    List<BrandRlView> findByPro(String shopname, int shopid, int businessid, int status);
    Response<List<BrandRlView>> findRlzj(@ParamInfo("baseUser") BaseUser baseUser,@ParamInfo("shopname") String shopname);
    Response<List<BrandRlView>> findRl(int brandClubKey);
    Response<List<BrandWRlView>> findWRl(@ParamInfo("baseUser") BaseUser baseUser,@ParamInfo("shopname") String shopname);
    void addRl_Key(int[] shopidlist, BaseUser baseUser);
    void delRl_Key(int[] idlist);

    List<BrandsClubKey> findbrandKeyByShopId(Integer integer);

    List<BrandsClubKey> findbrandKeyByBrandId(Long brandId);
}