package com.nowbook.sdp.service;

import com.nowbook.sdp.base.BaseServiceTest;
import com.nowbook.sdp.model.AmountDetail;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by winter on 16/4/27.
 */
public class AmountDetailTest extends BaseServiceTest {

    @Autowired
    private AmountDetailService amountDetailService;
    private AmountDetail amountDetail;

    @Before
    public void init() {
        amountDetail = new AmountDetail();
        amountDetail.setId((long) 001);
        amountDetail.setDistributorsId((long) 001);
        amountDetail.setOrderId("111");
        amountDetail.setIsComplete("1");
        amountDetailService.insert(amountDetail);
    }

   /* @Test
    public void testCreate() throws Exception {
        assertThat(amountDetail.getId(), notNullValue());
    }
    @Test
    public void testUpdate() throws Exception {
        amountDetail.setIsComplete("0");
        amountDetailService.updateByPrimaryKey(amountDetail);
        Response<AmountDetail> result= amountDetailService.selectByPrimaryKey(amountDetail.getId());
        assertThat(result.getResult().getIsComplete(), is("0"));
    }
    @Test
    public void testDelete() throws Exception {
        amountDetailService.deleteByPrimaryKey(amountDetail.getId());
        assertThat(amountDetailService.selectByPrimaryKey(amountDetail.getId()).getResult(), nullValue());
    }*/

//    @Test
//    public void testSelect() throws Exception {
//        assertThat(amountDetailService.AmountDetailForQuery("", 2, 20).getResult().getData().size(), is(0));
//
//    }
}
