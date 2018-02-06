package com.nowbook.admin.web.controller;

/**
 * Created by winter on 16/4/24.
 */

import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.DistributorsRelation;
import com.nowbook.sdp.service.DistributorsRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequestMapping("/api/admin/distributorsrelation")
public class DistributorsRelationController {

    @Autowired
    private DistributorsRelationService distributorsRelationService;



    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long create(DistributorsRelation distributorsRelation) {
        Response<Long> result = distributorsRelationService.insert(distributorsRelation);
        return  result.getResult();
    }
    @RequestMapping(value = "/insertSelective", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long insertSelective(DistributorsRelation distributorsRelation) {
        Response<Long> result = distributorsRelationService.insertSelective(distributorsRelation);
        return  result.getResult();
    }

    @RequestMapping(value = "/selectByPrimaryKey/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DistributorsRelation selectByPrimaryKey(Long id) {
        Response<DistributorsRelation> result = distributorsRelationService.selectByPrimaryKey(id);
        return  result.getResult();
    }
    @RequestMapping(value = "/updateByPrimaryKeySelective", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateByPrimaryKeySelective(DistributorsRelation distributorsRelation) {
        Response<Boolean> result = distributorsRelationService.updateByPrimaryKeySelective(distributorsRelation);
        return  result.getResult();
    }
    @RequestMapping(value = "/updateByPrimaryKey", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateByPrimaryKey(DistributorsRelation distributorsRelation) {
        Response<Boolean> result = distributorsRelationService.updateByPrimaryKey(distributorsRelation);
        return  result.getResult();
    }

}
