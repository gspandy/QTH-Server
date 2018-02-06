package com.nowbook.admin.event;


import lombok.*;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-06-12 1:21 PM  <br>
 * Author:cheng
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OuterCodeSetEvent {

    @Getter
    @Setter
    private Long shopId;            // 店铺id

    @Getter
    @Setter
    private String outerCode;       // 商户的外部编码

}
