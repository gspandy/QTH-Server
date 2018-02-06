package com.nowbook.web;

import com.nowbook.rlt.code.service.ActivityDefinitionService;
import com.nowbook.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author wanggen on 14-7-4.
 * @Desc: 优惠码
 */
@Controller
@Slf4j
@RequestMapping(value = "/api/coupon-code")
public class CouponCodes {
    @Autowired
    private ActivityDefinitionService activityDefinitionService;

    @Autowired
    private MessageSources messageSources;
}
