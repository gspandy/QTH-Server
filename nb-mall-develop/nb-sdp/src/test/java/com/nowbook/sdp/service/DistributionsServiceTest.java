package com.nowbook.sdp.service;

import com.nowbook.sdp.base.BaseServiceTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by winter on 16/5/20.
 */
public class DistributionsServiceTest  extends BaseServiceTest {
    @Autowired
    private DistributionsService distributionsService;


    @Before
    public void init() {

    }

    @Test
    public void testCreate() throws Exception {
        assertThat( distributionsService.bindSdp(Long.valueOf(1),Long.valueOf(2)), notNullValue());
    }
}
