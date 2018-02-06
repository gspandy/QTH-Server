package com.nowbook.sdp.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.PaymentDetail;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface PaymentDetailService {

    Response<Map<String, Object>> paymentDetailList(@ParamInfo("createTime") @Nullable String createTime,
                                                    @ParamInfo("startAt") @Nullable String startAt,
                                                    @ParamInfo("endAt") @Nullable String endAt,
                                                    @ParamInfo("payStartAt") @Nullable String payStartAt,
                                                    @ParamInfo("payEndAt") @Nullable String payEndAt,
                                                    @ParamInfo("mobile") @Nullable String mobile,
                                                    @ParamInfo("payType") @Nullable Integer payType,
                                                    @ParamInfo("payStatus") @Nullable Integer payStatus,
                                                    @ParamInfo("payResult") @Nullable Integer payResult,
                                                    @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                    @ParamInfo("size") @Nullable Integer size) throws ParseException;

    List<PaymentDetail> select(PaymentDetail paymentDetail);

    void update(PaymentDetail paymentDetail);
}
