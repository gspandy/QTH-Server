package com.nowbook.collect.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-10-10 2:14 PM  <br>
 * Author:cheng
 */
@ToString
@EqualsAndHashCode
public class CollectedItem implements Serializable {

    private static final long serialVersionUID = -1490889075763972790L;

    @Getter
    @Setter
    private Long id;                            // 主键

    @Getter
    @Setter
    private Long buyerId;                       // 买家id

    @Getter
    @Setter
    private Long itemId;                        // 商品id

    @Getter
    @Setter
    private String itemNameSnapshot;            // 商品名称（快照）

    @Getter
    @Setter
    private Date createdAt;                     // 创建时间

    @Getter
    @Setter
    private Date updatedAt;                     // 修改时间




}
