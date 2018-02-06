package com.nowbook.sdp.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.Map;

public interface SystemConfService {

    Response<Map<String, Object>> getProfitDates();

    void updateProfitDate(Map<String, String> map);

    void updateOthers(Map<String, String> map);

    Response<Map<String, Object>> selectOrderDetail(@ParamInfo("createTime") @Nullable String createTime,
                                                    @ParamInfo("startAt") @Nullable String startAt,
                                                    @ParamInfo("endAt") @Nullable String endAt,
                                                    @ParamInfo("mobile") @Nullable String mobile,
                                                    @ParamInfo("orderItemId") @Nullable Long orderItemId,
                                                    @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                    @ParamInfo("size") @Nullable Integer size) throws ParseException;

}
