/*
 * Copyright (c) 2012 大连锦霖科技有限公司
 */

package com.nowbook.admin;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2012-08-22
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:mail-context.xml"})
public class MailClientTest {

    @Autowired
    private MailClient mailClient;

    @Test
    @Ignore
    public void testSend() throws Exception {
        mailClient.send("test", "<h1>hello world</h1>", "12441608@qq.com", "kevin@aimymy.com");
    }
}
