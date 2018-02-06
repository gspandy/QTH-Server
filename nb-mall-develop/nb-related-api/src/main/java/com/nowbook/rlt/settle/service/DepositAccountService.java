package com.nowbook.rlt.settle.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.rlt.settle.dto.DepositAccountDto;
import com.nowbook.rlt.settle.model.DepositAccount;
import com.nowbook.user.base.BaseUser;

import javax.annotation.Nullable;

/**
 * 技术服务费服务
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2014-01-18
 */
public interface DepositAccountService {

    /**
     * 查询商家的保证金账户
     * @param name           商家名称,选填
     * @param business       行业类目,选填
     * @param lower          金额下限,选填
     * @param upper          金额上限,选填
     * @param pageNo         页码,从1开始,选填
     * @param size           每页显示条数,选填
     * @return  保证金账户列表
     */
    Response<Paging<DepositAccount>> findBy(@ParamInfo("name") @Nullable String name,
                                            @ParamInfo("business") @Nullable Long business,
                                            @ParamInfo("lower") @Nullable Float lower,
                                            @ParamInfo("upper") @Nullable Float upper,
                                            @ParamInfo("pageNo") @Nullable Integer pageNo,
                                            @ParamInfo("size") @Nullable Integer size,
                                            @ParamInfo("baseUser")BaseUser user);


    /**
     * 查询商家的保证金账户
     *
     * @param user  用户信息
     * @return 保证金账户
     */
    Response<DepositAccountDto> getBy(@ParamInfo("baseUser")BaseUser user);


    /**
     * 创建商家的保证金账户
     * @param shopId        店铺id
     * @param outerCode     商家外部编码
     */
    Response<Long> create(Long shopId, String outerCode);

}
