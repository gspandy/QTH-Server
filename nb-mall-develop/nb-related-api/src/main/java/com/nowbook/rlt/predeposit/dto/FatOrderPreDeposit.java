package com.nowbook.rlt.predeposit.dto;

import com.nowbook.rlt.predeposit.model.PreDeposit;
import com.nowbook.trade.dto.FatOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by yangzefeng on 14-2-14
 */
@ToString
public class FatOrderPreDeposit extends FatOrder{

    private static final long serialVersionUID = -185193156698887331L;
    @Getter
    @Setter
    private PreDeposit preDeposit;
}
