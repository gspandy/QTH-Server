package com.nowbook.user.manager;

import com.nowbook.user.base.BaseUser;
import com.nowbook.user.model.User;
import com.nowbook.user.model.UserExtra;
import com.nowbook.user.mysql.UserDao;
import com.nowbook.user.mysql.UserExtraDao;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Date: 3/31/14
 * Time: 15:59
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */

@Component
public class AccountManager {

    @Autowired
    UserDao userDao;

    @Autowired
    UserExtraDao userExtraDao;

    @Transactional
    public List<Long> bulkUpdateUserType(List<User> users, Integer type, Integer businessId) {
        List<Long> illegel = Lists.newArrayList();
        BaseUser.TYPE t = BaseUser.TYPE.fromNumber(type);
        for (User user:users) {
            if (Objects.equal(t, BaseUser.TYPE.SITE_OWNER)) {
                UserExtra cratical = new UserExtra();
                cratical.setUserId(user.getId());
                cratical.setBusinessId(businessId);
                cratical.setTradeQuantity(0);
                cratical.setTradeSum(0l);
                userExtraDao.insertOrUpdate(cratical);
            }

            User cratical = new User();
            cratical.setId(user.getId());
            cratical.setType(type);
            if(!userDao.update(cratical)) {
                illegel.add(user.getId());
            }
        }

        return illegel;
    }
}
