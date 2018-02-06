package com.nowbook.rlt.settle.manager;

import com.nowbook.rlt.BaseManagerTest;
import com.nowbook.rlt.settle.dao.SettleJobDao;
import com.nowbook.rlt.settle.enums.JobType;
import com.nowbook.rlt.settle.handle.CashSummaryHandle;
import com.nowbook.rlt.settle.model.SettleJob;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-02-26 5:04 PM  <br>
 * Author:cheng
 */
@Ignore
public class AlipayCashManagerTest extends BaseManagerTest {

    @Autowired
    private SettleJobDao settleJobDao;

    @Autowired
    private JobManager jobManager;

    @Autowired
    private CashSummaryHandle cashSummaryHandle;

    private SettleJob alipayCashJob;

    @Before
    public void setUp() {
        Date doneAt = DateTime.parse("2010-10-11").withTimeAtStartOfDay().toDate();
        Date tradedAt = DateTime.parse("2010-10-10").withTimeAtStartOfDay().toDate();
        jobManager.createJobs(doneAt, tradedAt);
        alipayCashJob = settleJobDao.getByDoneAtAndJobType(doneAt, JobType.SUMMARY_ALIPAY_CASHES.value());
        cashSummaryHandle.summaryAlipayCashes(alipayCashJob);
    }

}
