package com.nowbook.admin.web.controller;

import com.nowbook.common.model.Response;
import com.nowbook.exception.JsonResponseException;
import com.nowbook.rlt.predeposit.model.PreDeposit;
import com.nowbook.rlt.predeposit.service.PreDepositService;
import com.nowbook.user.base.UserUtil;
import com.nowbook.web.misc.MessageSources;
import com.nowbook.web.utils.DateEditor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Created by yangzefeng on 14-2-13
 */
@Controller
@Slf4j
@RequestMapping("/api/admin/preDeposits")
public class DepositMarketTools {

    @Autowired
    private PreDepositService preDepositService;

    @Autowired
    private MessageSources messageSources;

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void create(PreDeposit preSale) {
        //假如预售时间是2014-05-13, 则表示到2014-05-13 23:59:59,而数据库中只存到天, 也就是2014-05-13 00:00:00, 故要加上一天
        Response<Boolean> result = preDepositService.create(preSale, UserUtil.getCurrentUser());
        if(!result.isSuccess()) {
            log.error("failed to create market tool {}, error code:{}", preSale, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/{preSaleId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateSoldQuantity(@PathVariable("preSaleId") Long id, Integer quantity) {
        Response<Boolean> result = preDepositService.updateQuantity(id, quantity);

        if (!result.isSuccess()) {
            log.error("failed to update quantity:{} of presale(id:{}), error:{}", quantity, id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }



    @RequestMapping(value = "/{preSaleId}/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void update(PreDeposit preSale,
                       @PathVariable("preSaleId") Long preSaleId) {
        preSale.setId(preSaleId);
        Response<Boolean> result = preDepositService.update(preSale, UserUtil.getCurrentUser());
        if(!result.isSuccess()) {
            log.error("failed to update market tool{}, error code:{}", preSale, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/{preSaleId}/release", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void release(@PathVariable("preSaleId") Long preSaleId) {
        Response<Boolean> result = preDepositService.release(preSaleId);
        if(!result.isSuccess()) {
            log.error("failed to release market tool, error code:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }


    @RequestMapping(value = "/{preSaleId}/stop", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void stop(@PathVariable("preSaleId") Long preSaleId) {
        Response<Boolean> result = preDepositService.stop(preSaleId);
        if(!result.isSuccess()) {
            log.error("failed to stop presale, error code:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }


    @InitBinder
    protected void initBinder(HttpServletRequest request,
                              ServletRequestDataBinder binder) throws Exception {
        //对于需要转换为Date类型的属性，使用DateEditor进行处理
        binder.registerCustomEditor(Date.class, new DateEditor());
    }
}
