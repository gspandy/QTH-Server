package com.nowbook.sdp.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by pujian on 2016/5/24.
 */
public class PageModel implements Serializable {
    @Setter
    @Getter
    private Integer limit;

    @Setter
    @Getter
    private Integer offset;

    @Setter
    @Getter
    private Integer total;

    @Setter
    @Getter
    private Date createAt;//创建时间

    @Setter
    @Getter
    private String createTime;

    @Setter
    @Getter
    private Date updateAt;//更新时间

    @Setter
    @Getter
    private Integer pageNo;

    @Setter
    @Getter
    private Integer pageSize;
}
