package com.nowbook.brand.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.brand.model.BrandClub;
import com.nowbook.brand.model.BrandUser;
import com.nowbook.brand.model.NbBrand;

import java.util.List;

/**
 * brand seller api
 * Created by alfredYe on 2014/7/10.
 */
public interface BrandRegisterService {
    public Response<BrandUser> check(BrandUser brandUser);
    public Response<Boolean> insertBrand(BrandUser brandUser);
    public Response<Boolean> insertBrandProfiles(BrandClub brandUserProfiles);
    public int VertifyProfiles(BrandClub brandUserProfiles);
    public Response<List<BrandClub>> showBrandSeller(@ParamInfo("sellerName") String sellerName,@ParamInfo("brandSearchName") String brandSearchName,@ParamInfo("pinpai") int pinpai,@ParamInfo("status") int status);
    public boolean approSucc(BrandClub brandUserProfiles,NbBrand brand);
    public void approFail(BrandClub brandUserProfiles);
    public void approFrozen(BrandClub brandUserProfiles);
    public void approUnFrozen(BrandClub brandUserProfiles);
    public void insertFee(BrandClub brandClub);
    public int vertifyFee(BrandClub brandClub);
    public void updateFee(BrandClub brandClub);
    Response<List<BrandClub>> findKeyByUser(@ParamInfo("baseUser") BaseUser baseUser);
    BrandClub vertifyBrand(BrandClub brandClub);
    Response<BrandClub> searchReason(BaseUser baseUser);
    Response<BrandClub>findKeyForUpdate(@ParamInfo("baseUser") BaseUser baseUser);
    void updateBrandUserInfos(BrandClub brandClub);
    public boolean isExistBrand(NbBrand brand);
}