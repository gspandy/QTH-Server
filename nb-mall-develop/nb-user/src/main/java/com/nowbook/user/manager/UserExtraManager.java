package com.nowbook.user.manager;

import com.nowbook.user.model.UserExtra;
import com.nowbook.user.mysql.UserExtraDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Date: 3/19/14
 * Time: 16:32
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */

@Component
public class UserExtraManager {
    @Autowired
    private UserExtraDao userExtraDao;

    @Transactional
    public void bulkInsertOrUpdate(List<UserExtra> userExtras) {
        for (UserExtra extra: userExtras) {
            userExtraDao.insertOrUpdate(extra);
        }
    }

    @Transactional
    public void bulkInsertOrReplace(List<UserExtra> userExtras) {
        for (UserExtra extra: userExtras) {
            userExtraDao.insertOrReplace(extra);
        }
    }
}
