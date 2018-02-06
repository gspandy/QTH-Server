package com.nowbook.rlt.predeposit.dto;


import com.nowbook.item.dto.FullItem;
import com.nowbook.rlt.predeposit.model.PreDeposit;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by yangzefeng on 14-2-13
 */
@ToString
public class FullItemPreDeposit implements Serializable{

    private static final long serialVersionUID = -2757333834365076103L;
    @Getter
    @Setter
    private FullItem fullItem;

    @Getter
    @Setter
    private PreDeposit preDeposit;
}
