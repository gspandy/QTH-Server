package com.nowbook.sdp.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/3/25 0025.
 */

public class DistributorSet implements Serializable {

    private static final long serialVersionUID = 8811322825266279011L;

    @Setter
    @Getter
    private String key;

    @Setter
    @Getter
    private String value;


}
