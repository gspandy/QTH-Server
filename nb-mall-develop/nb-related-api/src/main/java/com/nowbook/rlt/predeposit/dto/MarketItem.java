package com.nowbook.rlt.predeposit.dto;

import com.nowbook.item.model.Item;
import com.nowbook.rlt.predeposit.model.PreDeposit;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by yangzefeng on 14-2-13
 */
@ToString
public class MarketItem implements Serializable{

    private static final long serialVersionUID = 8499144076958774869L;
    @Getter
    @Setter
    private PreDeposit preDeposit;

    @Getter
    @Setter
    private Item item;
}
