package com.nowbook.trade.dto;

import com.nowbook.trade.model.FreightModel;
import com.nowbook.trade.model.LogisticsSpecial;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Desc:运费模板
 * Mail:v@nowbook.io
 * Created by Michael Zhao
 * Date:2014-04-21.
 */
@ToString
public class FreightModelDto extends FreightModel {
    private static final long serialVersionUID = 166290856122282637L;
    @Getter
    @Setter
    private List<LogisticsSpecial> logisticsSpecialList;
}
