package com.nowbook.collect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-10-17 5:42 PM  <br>
 * Author:cheng
 */
@ToString
@AllArgsConstructor
public class CollectedSummary implements Serializable {

    private static final long serialVersionUID = -7305947764251953940L;


    @Getter
    @Setter
    private Long id;            // 商品收藏id

    @Getter
    @Setter
    private Long total;         // 商品收藏总数
}
