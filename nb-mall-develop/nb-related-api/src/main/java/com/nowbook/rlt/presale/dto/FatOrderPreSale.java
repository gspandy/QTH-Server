package com.nowbook.rlt.presale.dto;

import com.nowbook.rlt.presale.model.PreSale;
import com.nowbook.trade.dto.FatOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by yangzefeng on 14-2-14
 */
@ToString
public class FatOrderPreSale extends FatOrder{

    private static final long serialVersionUID = -185193156698887331L;
    @Getter
    @Setter
    private PreSale preSale;
}
