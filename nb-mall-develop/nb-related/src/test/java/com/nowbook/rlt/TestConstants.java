package com.nowbook.rlt;

import com.nowbook.user.base.BaseUser;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-04-09 4:00 PM  <br>
 * Author:cheng
 */
public class TestConstants {

    public static BaseUser ADMIN = new BaseUser(99L, "admin",  BaseUser.TYPE.ADMIN);
    public static BaseUser SELLER = new BaseUser(999L,"seller", BaseUser.TYPE.SELLER);
    public static BaseUser BUYER = new BaseUser(9999L, "buyer", BaseUser.TYPE.BUYER);

    public static BaseUser NONTYPE = new BaseUser(99999L, "agent", BaseUser.TYPE.AGENT);

}
