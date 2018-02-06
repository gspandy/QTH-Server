package com.nowbook.rlt.settle.dto;

import com.nowbook.rlt.settle.model.DepositFee;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-05-14 5:50 PM  <br>
 * Author:cheng
 */
@ToString
public class DepositFeeDto extends DepositFee {

    private static final long serialVersionUID = -6226300330025196172L;
    @Getter
    @Setter
    private Double depositOfYuan;

}
