package com.nowbook.admin.web.controller;

import com.nowbook.common.model.Response;
import com.nowbook.exception.JsonResponseException;
import com.nowbook.sdp.model.Distributions;
import com.nowbook.sdp.model.DistributorSet;
import com.nowbook.sdp.service.DistributionsService;
import com.nowbook.sdp.service.DistributorSetService;
import com.nowbook.sdp.service.DistributorsAuditService;
import com.nowbook.web.misc.MessageSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by Administrator on 2016/3/25 0025.
 */

@Controller
@RequestMapping("/api/sdp")
public class SdpDistributionsController {
    private final static Logger log = LoggerFactory.getLogger(SdpDistributionsController.class);


    @Autowired
    private DistributionsService distributionsService;

    @Autowired
    private DistributorsAuditService distributorsAuditService;

    @Autowired
    private DistributorSetService distributorSetService;

    @Autowired
    private MessageSources messageSources;

    /**
     * 更新审核状态
     * **/
    @RequestMapping(value = "/distributorsAudit/auditStatus", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateAuditStatus(@RequestParam("id") Long id,
                                  @RequestParam("auditStatus") String auditStatus) {
        Response<Boolean> result = distributorsAuditService.updateAuditStatus(id, auditStatus);
        if(!result.isSuccess()) {
            log.error("fail to update distributorsAudit method auditStatus by id={}, auditStatus={}, error code:{}",
                    id, auditStatus, result.getError());
        }
    }

    /**
     * 更新店铺状态
     * **/
    @RequestMapping(value = "/distributors/openStatus", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateOpenStatus(@RequestParam("id") Long id,
                                 @RequestParam("openStatus") String openStatus) {
        Response<Boolean> result = distributionsService.updateOpenStatus(id, openStatus);
        if(!result.isSuccess()) {
            log.error("fail to update distributors method openStatus by id={}, openStatus={}, error code:{}",
                    id, openStatus, result.getError());
        }
    }

    /**
     * 修改分销商  真实姓名  手机号
     * **/
    @RequestMapping(value = "/update/disAndUser", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updDistributionAudit(@PathVariable Distributions distributions){
        Response<Boolean> result = distributionsService.distributionsUpdate(distributions);
        if(!result.isSuccess()) {
            log.error("find distributionAuditAll failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }


        return result.getResult();
    }
    /**
     * 修改分销商  真实姓名  手机号
     * **/
    @RequestMapping(value = "/createDistribution", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean distributorsCreate(@PathVariable Distributions distributions){
        Response<Boolean> result = distributionsService.distributorsCreate(distributions);
        if(!result.isSuccess()) {
            log.error("find distributionAuditAll failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }


        return result.getResult();
    }
    /**
     * 更新分销商  设置
     * **/
    @RequestMapping(value = "/editSet", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void distributorEdit(DistributorSet distributorSet) {
        HashMap<String,String> hashMap = new HashMap<String,String>();
        hashMap.put(distributorSet.getKey(),distributorSet.getValue());
        System.out.println(distributorSet.getKey()+"+++"+distributorSet.getValue());
        Response<Boolean> result = distributorSetService.updateDistributor(hashMap);
        // if(!result.isSuccess()) {
//            log.error("fail to update distributorSet method edit by key={}, value={}, error code:{}",
//                    key, value, result.getError());
        // }
    }

}
