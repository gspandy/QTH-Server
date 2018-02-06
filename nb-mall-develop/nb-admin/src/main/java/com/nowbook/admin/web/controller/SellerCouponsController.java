package com.nowbook.admin.web.controller;


import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import com.nowbook.coupons.model.NbCou;
import com.nowbook.coupons.service.CouponsManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by yea01 on 2014/12/1.
 */
@Controller
@RequestMapping("/api/Seller/Coupons")
public class SellerCouponsController {

    @Autowired
    private CouponsManageService couponsManageService;

    @RequestMapping(value = "/findAllCoupons", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> queryCouponsBy() {
        Response<String> result = new Response<String>();
        BaseUser baseUser = UserUtil.getCurrentUser();
        if(baseUser==null){
            result.setResult("404");
        }else {
            long userId = baseUser.getId();
            List<NbCou> list = couponsManageService.findAllNbCou(userId,0);
            if (list != null && list.size() != 0) {
                StringBuilder stb = new StringBuilder();
                for (NbCou nbCou : list) {
                    int tempTerm = nbCou.getTerm() / 100;
                    int tempAmount = nbCou.getAmount();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
                    String startDate = df.format(nbCou.getStartTime());
                    String endDate = df.format(nbCou.getEndTime());
                    String createAt = df.format(nbCou.getCreated_at());
                    String status = nbCou.getStatus() == 0 ? "未生效" : nbCou.getStatus() == 1 ? "暂停" : nbCou.getStatus() == 2 ? "生效" : nbCou.getStatus() == 3 ? "失效" : "获取状态失败";
                }
                result.setResult(stb.toString());
            } else {
                result.setResult("405");
            }
        }
        return result;
    }
}
