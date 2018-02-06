package com.nowbook.rlt.settle.dto;

import com.nowbook.rlt.settle.model.SellerSettlement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-07-10 10:00 AM  <br>
 * Author:cheng
 */
@ToString
public class PrintableSettlementDto extends SellerSettlement {

    private static final long serialVersionUID = -5221021777501694567L;

    @Getter
    @Setter
    private String shopName;

}
