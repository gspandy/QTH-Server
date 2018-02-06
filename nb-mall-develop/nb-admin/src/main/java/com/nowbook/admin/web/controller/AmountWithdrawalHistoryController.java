package com.nowbook.admin.web.controller;

/**
 * Created by winter on 16/4/24.
 */

import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.AmountWithdrawalHistory;
import com.nowbook.sdp.service.AmountWithdrawalHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequestMapping("/api/admin/amountwithdrawalhistory")
public class AmountWithdrawalHistoryController {

    @Autowired
    private AmountWithdrawalHistoryService amountWithdrawalHistoryService;



    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long create(AmountWithdrawalHistory amountWithdrawalHistory) {
        Response<Long> result = amountWithdrawalHistoryService.insert(amountWithdrawalHistory);
        return  result.getResult();
    }
    @RequestMapping(value = "/insertSelective", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long insertSelective(AmountWithdrawalHistory amountWithdrawalHistory) {
        Response<Long> result = amountWithdrawalHistoryService.insertSelective(amountWithdrawalHistory);
        return  result.getResult();
    }

    @RequestMapping(value = "/selectByPrimaryKey/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AmountWithdrawalHistory selectByPrimaryKey(Long id) {
        Response<AmountWithdrawalHistory> result = amountWithdrawalHistoryService.selectByPrimaryKey(id);
        return  result.getResult();
    }
    @RequestMapping(value = "/updateByPrimaryKeySelective", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateByPrimaryKeySelective(AmountWithdrawalHistory amountWithdrawalHistory) {
        Response<Boolean> result = amountWithdrawalHistoryService.updateByPrimaryKeySelective(amountWithdrawalHistory);
        return  result.getResult();
    }
    @RequestMapping(value = "/updateByPrimaryKey", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateByPrimaryKey(AmountWithdrawalHistory amountWithdrawalHistory) {
        Response<Boolean> result = amountWithdrawalHistoryService.updateByPrimaryKey(amountWithdrawalHistory);
        return  result.getResult();
    }

}
