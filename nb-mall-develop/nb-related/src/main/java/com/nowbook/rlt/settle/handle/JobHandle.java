package com.nowbook.rlt.settle.handle;

import com.nowbook.common.model.Response;
import com.nowbook.rlt.settle.dao.SettleJobDao;
import com.nowbook.rlt.settle.enums.JobStatus;
import com.nowbook.rlt.settle.manager.SettlementManager;
import com.nowbook.rlt.settle.model.SettleJob;
import com.nowbook.rlt.settle.model.Settlement;
import com.nowbook.shop.model.Shop;
import com.nowbook.shop.model.ShopExtra;
import com.nowbook.shop.service.ShopService;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

import static com.nowbook.common.utils.Arguments.notNull;
import static org.elasticsearch.common.base.Preconditions.checkState;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-05-30 9:07 PM  <br>
 * Author:cheng
 */
@Slf4j
public abstract class JobHandle {

    @Autowired
    SettleJobDao settleJobDao;

    @Autowired
    ShopService shopService;


    @Autowired
    SettlementManager settlementManager;


    static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");   // 统一日期时间
    static final Integer BATCH_SIZE = 100;     // 批处理数量



    /**
     * 依赖的任务是否成功
     * @param job   任务
     * @return  任务对象
     */
    protected boolean dependencyOk(SettleJob job) {
        if (job.getDependencyId() == null) {
            return true;
        }
        SettleJob dependency = settleJobDao.get(job.getDependencyId());
        return Objects.equal(dependency.getStatus(), JobStatus.DONE.value());
    }


    /**
     * 获取商户的88码
     * @param shop  店铺
     * @return 商家88码
     */
    protected String getOuterCodeOfShop(Shop shop) {
        Response<ShopExtra> extraGetResult = shopService.getExtra(shop.getUserId());
        checkState(extraGetResult.isSuccess(), extraGetResult.getError());
        ShopExtra extra = extraGetResult.getResult();
        checkState(notNull(extra), "shop.extra.not.found");
        return extra.getOuterCode();
    }

    /**
     * 获取商家的行业
     *
     * @param shop  费率
     * @return 行业
     */
    protected Long getBusinessOfShop(Shop shop) {
        return shop.getBusinessId();
    }

    /**
     * 挂账处理
     */
    protected void doRecordIncorrect(Collection<Settlement> settlements, String reason) {
        try {
            settlementManager.recordIncorrectSettlements(settlements, reason);
        } catch (Exception e) {
            log.error("fail to record incorrect settlement {}, cause:{}", settlements, Throwables.getStackTraceAsString(e));
        }
    }

}
