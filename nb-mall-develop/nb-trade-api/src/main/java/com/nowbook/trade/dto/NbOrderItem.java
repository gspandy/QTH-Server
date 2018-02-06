package com.nowbook.trade.dto;

import com.nowbook.common.utils.BeanMapper;
import com.nowbook.trade.model.OrderItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-01-17 11:44 AM  <br>
 * Author:cheng
 */
@ToString
public class NbOrderItem implements Serializable {

    private static final long serialVersionUID = 1621460808885468875L;
    @Getter
    @Setter
    private Long id;                // 主键

    @Getter
    @Setter
    private Integer fee;            // 金额: 单位分

    @Getter
    @Setter
    private Integer deliverFee;         //运费

    @Getter
    @Setter
    private Integer originPrice;    // 原始价,即单价: 单位分

    @Getter
    @Setter
    private Integer price;          // 折扣后的价钱

    @Getter
    @Setter
    private Long skuId;             // NB库存

    @Getter
    @Setter
    private String outerId;         // 锦霖库存

    @Getter
    @Setter
    private Long itemId;            // 商品编号

    @Getter
    @Setter
    private String itemName;        // 商品名称

    @Getter
    @Setter
    private Long brandId;           // 品牌id

    @Getter
    @Setter
    private Integer quantity;       // 数量

    @Getter
    @Setter
    private Integer discount;       // 折扣

    @Getter
    @Setter
    private Integer status;         // 订单状态

    @Getter
    @Setter
    private Integer type;           // 子订单类型

    @Getter
    @Setter
    private String createdDate;     // 创建时间

    @Getter
    @Setter
    private Date updatedDate;       // 修改时间

    @Getter
    @Setter
    private String deliveryPromise;     //送达承诺


    public static NbOrderItem transform(OrderItem item) {
        NbOrderItem dto = new NbOrderItem();
        BeanMapper.copy(item, dto);
        dto.setQuantity(item.getQuantity());
        return dto;
    }
}
