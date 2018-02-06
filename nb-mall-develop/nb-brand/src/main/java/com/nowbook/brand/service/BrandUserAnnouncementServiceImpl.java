package com.nowbook.brand.service;

import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.brand.dao.BrandClubDao;
import com.nowbook.brand.dao.BrandUserAnnouncementDao;
import com.nowbook.brand.model.BrandClub;
import com.nowbook.brand.model.BrandUserAnnouncement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhua02 on 2014/7/25.
 */
@Service
public class BrandUserAnnouncementServiceImpl implements BrandUserAnnouncementService{

    @Autowired
    private BrandUserAnnouncementDao buadao;

    @Autowired
    private BrandClubDao bcldao;

    @Override
    public Response<List<BrandUserAnnouncement>> findAll(BaseUser baseUser) {
        BrandClub brandClub=bcldao.findBrandClubByUid(baseUser.getId());
        Response<List<BrandUserAnnouncement>> result = new Response<List<BrandUserAnnouncement>>();
        result.setResult(buadao.findAll(brandClub.getId().intValue()));
        return result;
    }

    @Override
    public Response<List<BrandUserAnnouncement>> findByShopId(BaseUser baseUser) {
        Response<List<BrandUserAnnouncement>> result = new Response<List<BrandUserAnnouncement>>();
        result.setResult(buadao.findByShopId(baseUser.getId().intValue()));
        return result;
    }

    @Override
    public List<BrandUserAnnouncement> findByTime(String starttime,String endtime) {
        return buadao.findByTime(starttime, endtime);
    }

    @Override
    public BrandUserAnnouncement findById(int id) {
        return buadao.findById(id);
    }

    @Override
    public void addAnn(BrandUserAnnouncement brandUserAnnouncement,BaseUser baseUser) {
        BrandClub brandClub=bcldao.findBrandClubByUid(baseUser.getId());
        brandUserAnnouncement.setBrandUserId(brandClub.getId().intValue());
        buadao.addAnn(brandUserAnnouncement);
    }

    @Override
    public void delAnn(int[] idlist) {
        buadao.delAnn(idlist);
    }
}
