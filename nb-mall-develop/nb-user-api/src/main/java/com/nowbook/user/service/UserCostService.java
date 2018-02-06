package com.nowbook.user.service;

import com.nowbook.user.model.UserCost;
import com.nowbook.common.model.Response;
/**
 * Created by Administrator on 2016/7/18.
 */
public interface UserCostService {
    Response<Boolean> create(UserCost summary);
    Response<UserCost> selectById(Long id);
    Response<Boolean> update(UserCost summary);
}
