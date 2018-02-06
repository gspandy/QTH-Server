package com.nowbook.sdp.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2016/3/25 0025.
 */
@ToString
@EqualsAndHashCode
public class DistributorsAudit implements Serializable {

    private static final long serialVersionUID = -2529130411093340720L;

    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private Long distributorsId;

    @Setter
    @Getter
    private String auditStatus;

    @Setter
    @Getter
    private Date auditTime;

    @Setter
    @Getter
    private String auditPeople;
}
