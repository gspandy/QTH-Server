package com.nowbook.brand.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by yea01 on 2014/7/29.
 */
@ToString
@EqualsAndHashCode
public class NbBrand implements Serializable {

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String brandName;
}
