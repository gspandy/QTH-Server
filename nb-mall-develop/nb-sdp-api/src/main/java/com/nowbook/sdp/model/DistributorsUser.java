package com.nowbook.sdp.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/3/25 0025.
 */
@ToString
@EqualsAndHashCode
public class DistributorsUser implements Serializable {

    private static final long serialVersionUID = 8272452385963190364L;

    @Setter
    @Getter
    private String name;

    @Setter
    @Getter
    private String mobile;

    @Setter
    @Getter
    private String realName;
}
