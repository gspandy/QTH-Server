/*
 * Copyright (c) 2012 大连锦霖科技有限公司
 */

package com.nowbook.user.service;

import com.nowbook.user.BaseServiceTest;
import com.nowbook.user.model.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/*
* Author: jl
* Date: 2012-11-08
*/
public class AccountServiceImplTest extends BaseServiceTest {
    @Autowired
    private AccountService<User> accountService;

//    @Autowired
//    private PasswordService passwordService;

    private User user;

    @Before
    public void setUp() throws Exception {
        user = newUser("dadu", "test");
        accountService.createUser(user);
    }


    @Test
    public void testChangePassword() throws Exception {
        Long id = user.getId();
        String origin = user.getEncryptedPassword();
        accountService.changePassword(user.getId(), "test", "new",null);
        User updated = (User)accountService.findUserById(id).getResult();
        assertThat(origin, not(is(updated.getEncryptedPassword())));
    }

    private User newUser(String name, String password) {
        User user = new User();
        user.setEmail(name + "@example.com");
        user.setStatus(0);
        user.setEncryptedPassword(password);
        user.setName(name);
        user.setType(1);
        return user;
    }
}
