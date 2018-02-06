package com.nowbook.rlt.settle.model;

import java.util.Date;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-03-12 6:39 PM  <br>
 * Author:cheng
 */
public interface Voucher  extends Bill  {

    public void setVoucher(String voucher);

    public void setVouchedAt(Date voucherAt);

    public String getVoucher();

    public Date getVouchedAt();
}
