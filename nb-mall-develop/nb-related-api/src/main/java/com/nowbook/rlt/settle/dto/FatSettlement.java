package com.nowbook.rlt.settle.dto;

import com.nowbook.common.model.Paging;
import com.nowbook.common.utils.BeanMapper;
import com.nowbook.rlt.settle.model.Settlement;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-05-27 2:19 PM  <br>
 * Author:cheng
 */
@ToString
public class FatSettlement extends Settlement {
    private static final long serialVersionUID = 4430012384099665009L;

    @Getter
    @Setter
    private Boolean locked;


    public static Paging<FatSettlement> transform(Paging<Settlement> sourcePaging, Boolean locked) {

        if (sourcePaging.getTotal() == 0) {
            return Paging.empty(FatSettlement.class);
        }

        List<Settlement> settlements = sourcePaging.getData();

        List<FatSettlement> targets = Lists.newArrayListWithCapacity(sourcePaging.getTotal().intValue());

        for (Settlement settlement : settlements) {
            FatSettlement fatSettlement = new FatSettlement();
            BeanMapper.copy(settlement, fatSettlement);
            fatSettlement.setLocked(locked);
            targets.add(fatSettlement);
        }

        return new Paging<FatSettlement>(sourcePaging.getTotal(), targets);
    }

}
