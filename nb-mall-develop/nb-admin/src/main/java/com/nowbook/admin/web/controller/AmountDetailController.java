package com.nowbook.admin.web.controller;

/**
 * Created by winter on 16/4/24.
 */

import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.AmountDetail;
import com.nowbook.sdp.service.AmountDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequestMapping("/api/admin/amountdetail")
public class AmountDetailController {

    @Autowired
    private AmountDetailService amountDetailService;



    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long create(AmountDetail amountDetail) {
        Response<Long> result = amountDetailService.insert(amountDetail);
        System.out.println(amountDetail.toString());
        return  result.getResult();
    }
    @RequestMapping(value = "/insertSelective", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long insertSelective(AmountDetail amountDetail) {
        Response<Long> result = amountDetailService.insertSelective(amountDetail);
        System.out.println(amountDetail.toString());
        return  result.getResult();
    }

//    @RequestMapping(value = "/selectByPrimaryKey/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    public AmountDetail selectByPrimaryKey(Long id) {
//        Response<AmountDetail> result = amountDetailService.selectByPrimaryKey(id);
//        System.out.println(id);
//        return  result.getResult();
//    }
    @RequestMapping(value = "/updateByPrimaryKeySelective", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateByPrimaryKeySelective(AmountDetail amountDetail) {
        Response<Boolean> result = amountDetailService.updateByPrimaryKeySelective(amountDetail);
        System.out.println(amountDetail.toString());
        return  result.getResult();
    }
    @RequestMapping(value = "/updateByPrimaryKey", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateByPrimaryKey(AmountDetail amountDetail) {
        Response<Boolean> result = amountDetailService.updateByPrimaryKey(amountDetail);
        System.out.println(amountDetail.toString());
        return  result.getResult();
    }

}
