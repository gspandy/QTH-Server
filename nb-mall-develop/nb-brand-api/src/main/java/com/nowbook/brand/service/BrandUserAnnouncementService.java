package com.nowbook.brand.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.brand.model.BrandUserAnnouncement;

import java.util.List;

/**
 * Created by zhua02 on 2014/7/25.
 */
public interface BrandUserAnnouncementService {
    Response<List<BrandUserAnnouncement>> findAll(@ParamInfo("baseUser") BaseUser baseUser);
    Response<List<BrandUserAnnouncement>> findByShopId(@ParamInfo("baseUser") BaseUser baseUser);
    List<BrandUserAnnouncement> findByTime(String starttime, String endtime);
    BrandUserAnnouncement findById(int id);
    void addAnn(BrandUserAnnouncement brandUserAnnouncement, BaseUser baseUser);
    void delAnn(int[] idlist);
}
