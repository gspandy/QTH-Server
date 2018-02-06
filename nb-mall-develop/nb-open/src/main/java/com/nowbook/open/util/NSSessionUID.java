package com.nowbook.open.util;

import com.nowbook.common.model.Response;
import com.nowbook.common.utils.CommonConstants;
import com.nowbook.session.AFSession;
import com.nowbook.session.AFSessionManager;
import com.google.common.base.Objects;

import static com.nowbook.common.utils.Arguments.notNull;


/**
 * Date: 4/23/14
 * Time: 17:15
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */
public class NSSessionUID {

    public static Response<Long> checkLogin(AFSession session, Long uid) {
        Response<Long> result = new Response<Long>();

        if (session.getAttribute(CommonConstants.SESSION_USER_ID)==null) {
            result.setError("用户未登录");
            return result;
        }

        Long id = ((Integer)session.getAttribute(CommonConstants.SESSION_USER_ID)).longValue();
        if (!Objects.equal(id, uid)) {
            result.setError("用户未登录");
            return result;
        }

        result.setResult(id);
        return result;
    }

    public static Response<Long> getUserId(AFSession session) {
        Response<Long> result = new Response<Long>();

        Object o = session.getAttribute(CommonConstants.SESSION_USER_ID);
        if (Objects.equal(o, null)) {
            result.setError("user.not.login");
            return result;
        }
        Long uid = Long.valueOf((Integer)o);
        result.setResult(uid);

        if (notNull(uid)) {
            AFSessionManager.instance().refreshExpireTime(session, 30*60);
        }

        return result;
    }
}
