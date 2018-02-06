package com.nowbook.user.service;

import com.nowbook.common.model.Response;
import com.nowbook.user.model.UserAccountSummary;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-06-09 5:43 PM  <br>
 * Author:cheng
 */
public interface UserAccountSummaryService {



    /**
     * 创建用户引流统计记录
     *
     * @param summary    用户统计信息
     * @return  如果创建成功则返回id
     */
    Response<Long> create(UserAccountSummary summary);










}
