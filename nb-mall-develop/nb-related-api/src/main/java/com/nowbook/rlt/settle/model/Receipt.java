package com.nowbook.rlt.settle.model;

import java.util.Date;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-05-22 10:40 PM  <br>
 * Author:cheng
 */
public interface Receipt extends Bill {

    public String getReceipt();

    public Date getReceiptedAt();

    public void setReceipt(String receipt);

    public void setReceiptedAt(Date receiptedAt);

}
