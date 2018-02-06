package com.nowbook.rlt.settle.service;

import com.nowbook.rlt.settle.endpoint.SettlementExecutor;
import com.nowbook.rlt.settle.enums.JobType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-03-03 10:21 AM  <br>
 * Author:cheng
 */
@Slf4j
@Service
public class SettlementJobServiceImpl implements SettlementJobService{

    @Autowired
    private SettlementExecutor settlementExecutor;

    @Override
    public void updateOuterCode(Date doneAt) {
        settlementExecutor.call(JobType.UPDATE_OUTER_CODE, doneAt);
    }

    @Override
    public void updateOuterCodeFully(Date doneAt) {
        settlementExecutor.call(JobType.UPDATE_OUTER_CODE_FULL, doneAt);
    }

    @Override
    public void createJobs(Date doneAt) {
        settlementExecutor.call(JobType.CREATE_JOBS, doneAt);
    }


    @Override
    public void markedOrderAsFinished(Date doneAt) {
        settlementExecutor.call(JobType.MARK_SETTLEMENT_FINISHED, doneAt);
    }

    @Override
    public void updateVoucher(Date doneAt) {
        settlementExecutor.call(JobType.UPDATE_VOUCHER, doneAt);
    }

    @Override
    public void payEarningsBonuses(Date doneAt) {
        settlementExecutor.call(JobType.PAY_EARNINGS_BONUSES, doneAt);
    }

    @Override
    public void sumEarningsBonuses(Date doneAt) {
        settlementExecutor.call(JobType.SUM_EARNINGS_BONUSES, doneAt);
    }

    @Override
    public void createSettlements(Date doneAt) {
        settlementExecutor.call(JobType.CREAT_SETTLEMENTS, doneAt);
    }

    @Override
    public void settle(Date doneAt) {
        settlementExecutor.call(JobType.SETTLEMENT, doneAt);
    }

    @Override
    public void summary(Date doneAt) {
        settlementExecutor.call(JobType.SUMMARY_SETTLEMENTS, doneAt);
    }

    @Override
    public void summaryAlipayCash(Date doneAt) {
        settlementExecutor.call(JobType.SUMMARY_ALIPAY_CASHES, doneAt);
    }

    @Override
    public void updateAlipayFee(Date doneAt) {
        settlementExecutor.call(JobType.UPDATE_ALIPAY_FEES, doneAt);
    }

    @Override
    public void syncToJde(Date doneAt) {
        settlementExecutor.call(JobType.SYNC_TO_JDE, doneAt);
    }

    @Override
    public void updateRate(Date doneAt) {
        settlementExecutor.call(JobType.UPDATE_RATE, doneAt);
    }

    @Override
    public void autoConfirmed(Date doneAt) {
        settlementExecutor.call(JobType.AUTO_CONFIRM, doneAt);
    }

    @Override
    public void fix(Date doneAt) {
        settlementExecutor.call(JobType.FIX_SETTLEMENT, doneAt);
    }

    @Override
    public void commission(Date doneAt) {
        settlementExecutor.call(JobType.COMMISSION, doneAt);
    }
}
