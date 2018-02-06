package com.nowbook.trade.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.trade.model.UserVatInvoice;
import com.nowbook.user.base.BaseUser;


/**
 *
 * 增值税发票
 *
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-07-05 4:54 PM  <br>
 * Author:cheng
 */
public interface UserVatInvoiceService {


    /**
     * 获取指定用户的增值税发票定义
     *
     * @param userId 用户id
     * @return 增值税发票定义信息
     */
    Response<UserVatInvoice> getByUserId(Long userId);

    /**
     * 获取当前用户的增值税发票定义
     *
     * @param user  用户
     * @return 增值税发票定义信息
     */
    Response<UserVatInvoice> getByUser(@ParamInfo("userId") BaseUser user);


    /**
     * 更新增值税发票定义
     *
     * @param userVatInvoice  待创建的增值税发票定义
     * @param user            创建增值税发票的用户信息
     * @return  创建成功的增值税发票id
     */
    Response<Long> create(UserVatInvoice userVatInvoice, BaseUser user);

    /**
     * 更新增值税发票定义
     *
     * @param userVatInvoice  待更新的增值税发票定义
     * @param user            更新增值税发票的用户信息
     * @return  是否执行成功
     */
    Response<Boolean> update(UserVatInvoice userVatInvoice, BaseUser user);
}
