package com.nowbook.rlt.settle.service;

import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.rlt.BaseServiceTest;
import com.nowbook.rlt.settle.model.DepositAccount;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBean;

import static com.nowbook.rlt.TestConstants.ADMIN;
import static com.nowbook.rlt.TestConstants.SELLER;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-04-09 6:01 PM  <br>
 * Author:cheng
 */
@SuppressWarnings("all")
public class DepositAccountServiceTest extends BaseServiceTest {

    @SpringBean("depositAccountServiceImpl")
    private DepositAccountService depositAccountService;



    @Test
    @DataSet("DepositAccountServiceTest.testQuery.xml")
    public void testFindBy() {

        // 测试没有权限的情况
        Response<Paging<DepositAccount>> actual = depositAccountService.findBy(null, null, null, null, null, null, SELLER);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));

        // 查指定的商户
        actual = depositAccountService.findBy("seller1", null, null, null, null, null, ADMIN);
        assertThat(actual.getResult().getTotal(), is(1L));
        assertThat(actual.getResult().getData().size(), is(1));

        // 指定的商户不存在的情况
        actual = depositAccountService.findBy("sellerNotExist", null, null, null, null, null, ADMIN);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));

        // 查指定行业
        actual = depositAccountService.findBy(null, 2L, null, null, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(2L));
        assertThat(actual.getResult().getData().size(), is(2));


        // 查指定余额范围的账户
        actual = depositAccountService.findBy(null, null, 10f, 100f, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(2L));
        assertThat(actual.getResult().getData().size(), is(2));

    }

}
