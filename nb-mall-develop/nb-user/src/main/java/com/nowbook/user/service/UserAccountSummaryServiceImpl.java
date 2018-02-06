package com.nowbook.user.service;

import com.nowbook.common.model.Response;
import com.nowbook.user.model.UserAccountSummary;
import com.nowbook.user.mysql.UserAccountSummaryDao;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.nowbook.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-06-10 9:34 AM  <br>
 * Author:cheng
 */
@Slf4j
@Service
public class UserAccountSummaryServiceImpl implements UserAccountSummaryService {

    @Autowired
    private UserAccountSummaryDao userAccountSummaryDao;

    /**          s
     * 创建用户引流统计记录
     *
     * @param summary 用户统计信息
     * @return 如果创建成功则返回id
     */
    @Override
    public Response<Long> create(UserAccountSummary summary) {
        Response<Long> result = new Response<Long>();

        try {
            checkArgument(notNull(summary.getChannel()), "channel.can.not.be.empty");
            checkArgument(notNull(summary.getUserId()), "user.id.can.not.be.empty");
            checkArgument(notNull(summary.getLoginType()), "login.type.can.not.be.empty");

            userAccountSummaryDao.create(summary);
            checkState(notNull(summary.getId()), "user.account.summary.persist.error");
            result.setResult(summary.getId());

        } catch (IllegalStateException e) {
            log.error("fail to create userAccountSummary:{}, error:{}", summary, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to create userAccountSummary:{}, error:{}", summary, Throwables.getStackTraceAsString(e));
            result.setError("user.account.summary.create.fail");
        }

        return result;
    }
}
