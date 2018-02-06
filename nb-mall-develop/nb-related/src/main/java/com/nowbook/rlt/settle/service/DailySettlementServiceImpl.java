package com.nowbook.rlt.settle.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.PageInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.rlt.settle.dao.DailySettlementDao;
import com.nowbook.rlt.settle.model.DailySettlement;
import com.nowbook.user.base.BaseUser;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;

import static com.nowbook.user.util.UserVerification.isAdmin;
import static com.nowbook.user.util.UserVerification.isFinance;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-01-22 4:31 PM  <br>
 * Author:cheng
 */
@Slf4j
@Service
public class DailySettlementServiceImpl implements DailySettlementService {

    @Autowired
    private DailySettlementDao dailySettlementDao;

    private DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");

    /**
     * 根据商户确认的起止日期来查询日结算汇总分页信息
     *
     * @param startAt 起始时间,必输项
     * @param endAt   截止时间,必输项
     * @param pageNo  页码,从1开始
     * @param size    每页显示条数
     * @return 满足条件的日结算信息列表，若查不到则返回空列表
     */
    @Override
    public Response<Paging<DailySettlement>> findBy(@ParamInfo("startAt") @Nullable String startAt,
                                                    @ParamInfo("endAt") @Nullable String endAt,
                                                    @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                    @ParamInfo("size") @Nullable Integer size,
                                                    @ParamInfo("baseUser") BaseUser user) {
        Response<Paging<DailySettlement>> result = new Response<Paging<DailySettlement>>();

        try {

            checkState(isAdmin(user) || isFinance(user), "user.has.no.permission");

            Map<String, Object> params = getDateRangedCriteriaParams(startAt, endAt);
            PageInfo pageInfo = new PageInfo(pageNo, size);
            params.put("offset", pageInfo.offset);
            params.put("limit", pageInfo.limit);

            Paging<DailySettlement> paging = dailySettlementDao.findBy(params);

            result.setResult(paging);
        } catch (IllegalStateException e) {
            log.warn("fail to findBy with startAt:{}, endAt:{}, pageNo:{}, size:{}, user:{}, error:{}",
                    startAt, endAt, pageNo, size, user, e.getMessage());
            result.setResult(Paging.empty(DailySettlement.class));
        } catch (Exception e) {
            log.error("fail to findBy with startAt:{}, endAt:{}, pageNo:{}, size:{}, user:{}, cause:{}",
                    startAt, endAt, pageNo, size, user, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(DailySettlement.class));
        }

        return result;
    }

    private Map<String, Object> getDateRangedCriteriaParams(String startAt, String endAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);

        Date confirmedStartAt;
        Date confirmedEndAt;
        if (!Strings.isNullOrEmpty(startAt)) {   //若为空则默认开始时间一年前
            confirmedStartAt = DFT.parseDateTime(startAt).toDate();
        } else {
            confirmedStartAt = DateTime.now().withTimeAtStartOfDay().minusYears(1).toDate();
        }

        if (!Strings.isNullOrEmpty(endAt)) {   //若为空则默认开始时间为系统时间的次日
            DateTime endDate = DFT.parseDateTime(endAt);
            endDate = endDate.plusDays(1);
            confirmedEndAt = endDate.toDate();

        } else {
            confirmedEndAt =  DateTime.now().plusDays(1).toDate();
        }

        params.put("confirmedStartAt", confirmedStartAt);
        params.put("confirmedEndAt", confirmedEndAt);
        return params;
    }
}
