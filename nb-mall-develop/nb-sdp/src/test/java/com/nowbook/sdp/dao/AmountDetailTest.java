package com.nowbook.sdp.dao;

import com.nowbook.sdp.base.BaseDaoTest;
import com.nowbook.sdp.model.AmountDetail;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by winter on 16/4/21.
 */
public class AmountDetailTest extends BaseDaoTest {

    @Autowired
    private AmountDetailDao amountDetailDao;
    private AmountDetail amountDetail;

 @Before
    public void init() {
     amountDetail = new AmountDetail();
     amountDetail.setId((long) 001);
     amountDetail.setDistributorsId((long) 001);
     amountDetail.setOrderId("111");
     amountDetail.setIsComplete("1");
     amountDetailDao.insert(amountDetail);
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(amountDetail.getId(), notNullValue());
    }
    @Test
    public void testUpdate() throws Exception {
        amountDetail.setIsComplete("0");
        amountDetailDao.updateByPrimaryKey(amountDetail);
//        AmountDetail amountDetail1 = amountDetailDao.selectByPrimaryKey(amountDetail.getId());
//        assertThat(amountDetail1.getIsComplete(), is("0"));
    }
    @Test
    public void testDelete() throws Exception {
        amountDetail.setIsComplete("0");
//        amountDetailDao.deleteByPrimaryKey(amountDetail.getId());
//        assertThat(amountDetailDao.selectByPrimaryKey(amountDetail.getId()), nullValue());
    }
}

