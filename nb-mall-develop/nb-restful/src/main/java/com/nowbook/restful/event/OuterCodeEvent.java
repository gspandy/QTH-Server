package com.nowbook.restful.event;

import com.nowbook.shop.model.Shop;
import lombok.*;

import java.util.List;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-07-07 1:49 PM  <br>
 * Author:cheng
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OuterCodeEvent {

    @Getter
    @Setter
    private List<Shop> shops;   // 受影响的店铺列表

    @Getter
    @Setter
    private String outerCode;   // 商户编码


}
