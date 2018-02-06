package com.nowbook.admin.web.jobs;

import com.nowbook.common.model.Response;
import com.nowbook.sdp.service.AmountDetailService;
import com.nowbook.sdp.service.DistributionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by winter on 16/5/12.
 */
@Slf4j
@Component
public class CommissionJobs {

    @Autowired
    private AmountDetailService amountDetailService;
    @Autowired
    private DistributionsService distributionsService;
    /**
     *
     */
    //@Scheduled(cron = "0/5 * * * * *")
    public void calculationCommission() {
        Response<String> ret = distributionsService.withdrawalsTimeIntval();
        //调用汇总存储过程

        //log.info("[CALCULATION-COMMISSION] DONE");
    }

}
