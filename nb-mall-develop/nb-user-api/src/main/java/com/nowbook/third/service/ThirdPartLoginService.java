package com.nowbook.third.service;

import com.nowbook.user.model.User;
import com.nowbook.third.model.ThirdUser;

/**
 * Created by zhaop01 on 2014/9/2.
 */
public interface ThirdPartLoginService {
    /**
     *
     * @param source
     * @param env
     */
    public String process(String source, String env, String ssoSessionId, String sessionId) throws Exception;
    // 判断用户是否存在
   boolean  isExistsByUserName(String username);
   // 注册用户
   void saveUser(User user, String thirdPartName);

   ThirdUser findBySourceName(String sourceName);

   User findByName(String name);

}
