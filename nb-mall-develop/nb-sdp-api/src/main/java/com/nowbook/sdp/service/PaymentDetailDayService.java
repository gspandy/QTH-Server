package com.nowbook.sdp.service;


import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.PaymentDetailDay;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.List;

//周结算
public interface PaymentDetailDayService {

    Response<Paging<PaymentDetailDay>> findBy(@ParamInfo("createStartAt") @Nullable String createStartAt,
                                              @ParamInfo("createEndAt") @Nullable String createEndAt,
                                              @ParamInfo("pageNo") @Nullable Integer pageNo,
                                              @ParamInfo("size") @Nullable Integer pageSize) throws ParseException;

    List<PaymentDetailDay> select(PaymentDetailDay paymentDetailDay);

    void update(PaymentDetailDay paymentDetailDay);
}