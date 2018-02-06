package com.nowbook.sdp.service;

import com.nowbook.sdp.base.BaseServiceTest;
import com.nowbook.sdp.model.AmountWithdrawalHistory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by winter on 16/4/27.
 */
public class AmountWithdrawalHistoryServiceTest extends BaseServiceTest {

    @Autowired
    private AmountWithdrawalHistoryService amountWithdrawalHistoryService;
    private AmountWithdrawalHistory amountWithdrawalHistory;

    @Before
    public void init() {
        amountWithdrawalHistory = new AmountWithdrawalHistory();
        amountWithdrawalHistory.setId((long) 111);
        //amountWithdrawalHistory.setAmountStatus("0");
        amountWithdrawalHistory.setOperationTime(new Date());
        amountWithdrawalHistory.setDistributorsId((long) 111);
        //amountWithdrawalHistory.setWithdrawalInformation("111");
        amountWithdrawalHistoryService.insert(amountWithdrawalHistory);
    }

   /* @Test
    public void testCreate() throws Exception {
        assertThat(amountWithdrawalHistory.getId(), notNullValue());
    }
    @Test
    public void testUpdate() throws Exception {
        amountWithdrawalHistory.setAmountStatus("1");
        amountWithdrawalHistoryService.updateByPrimaryKey(amountWithdrawalHistory);
        Response<AmountWithdrawalHistory> result= amountWithdrawalHistoryService.selectByPrimaryKey(amountWithdrawalHistory.getId());
        assertThat(result.getResult().getAmountStatus(), is("1"));
    }
    @Test
    public void testDelete() throws Exception {
        amountWithdrawalHistoryService.deleteByPrimaryKey(amountWithdrawalHistory.getId());
        assertThat(amountWithdrawalHistoryService.selectByPrimaryKey(amountWithdrawalHistory.getId()).getResult(), nullValue());
    }*/

    @Test
    public void testSelect() throws Exception {
        assertThat(amountWithdrawalHistoryService.AmountWithdrawalHistoryForQuery("", 2, 20).getResult().getData().size(), is(0));

    }

}
