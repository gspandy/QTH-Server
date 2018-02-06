package com.nowbook.rlt.buying.dto;

import com.nowbook.common.model.Paging;
import com.nowbook.rlt.buying.model.BuyingTempOrder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by songrenfei on 14-9-23
 */
@Data
public class BuyingTempOrderDto implements Serializable {


    private static final long serialVersionUID = 5187723011250989794L;


    private Paging<BuyingTempOrder> paging; //虚拟订单列表


    private Date systemAt;      //系统时间

}
