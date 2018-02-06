package com.nowbook.restful.event;

import com.nowbook.common.model.Response;
import com.nowbook.rlt.settle.service.SettlementService;
import com.nowbook.shop.model.Shop;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-07-07 1:50 PM  <br>
 * Author:cheng
 */
@Slf4j
@Component
public class OuterCodeEventListener {

    private final RestEventBus eventBus;

    private final SettlementService settlementService;

    @Autowired
    public OuterCodeEventListener(RestEventBus eventBus, SettlementService settlementService) {
        this.eventBus = eventBus;
        this.settlementService = settlementService;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void updateOuterCode(OuterCodeEvent outerCodeEvent) {
        List<Shop> shops = outerCodeEvent.getShops();
        String outerCode = outerCodeEvent.getOuterCode();
        if (CollectionUtils.isEmpty(shops)) return;
        for (Shop shop : shops) {
            Response<Boolean> updateResult = settlementService.batchUpdateOuterCodeOfShopRelated(outerCode, shop);
            if (!updateResult.isSuccess()) {
                log.error("fail to update outerCode:{} of shop(id:{}, name:{}, userId:{}, userName:{}",
                        shop.getId(), shop.getName(), shop.getUserId(), shop.getUserName());
            }
        }
    }
}
