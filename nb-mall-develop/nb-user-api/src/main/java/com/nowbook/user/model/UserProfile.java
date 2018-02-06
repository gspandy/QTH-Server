/*
 * Copyright (c) 2012 大连锦霖科技有限公司
 */

package com.nowbook.user.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Date;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2012-08-17
 */
public class UserProfile implements Serializable {
    private static final long serialVersionUID = -7547958430147682873L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @Min(0)
    private Long userId;

    @Getter
    @Setter
    private String phone; //联系电话,在注册成功后添加

    @Getter
    @Setter
    private String realName;

    @Getter
    @Setter
    private String idCardNum;

    @Getter
    @Setter
    private Date birthday;//生日

    @Getter
    @Setter
    private Integer gender;  //1-male, 2-female

    @Getter
    @Setter
    private Integer provinceId;  //see address

    @Getter
    @Setter
    private Integer cityId;  //see address

    @Getter
    @Setter
    private Integer regionId; //see address

    @Getter
    @Setter
    private String address;

    @Getter
    @Setter
    private String qq;

    @Getter
    @Setter
    @Min(0)
    private Integer buyerCredit;

    @Getter
    @Setter
    @Min(0)
    private Integer sellerCredit;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String extra;

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private Date updatedAt;


    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof UserProfile)) {
            return false;
        }
        UserProfile that = (UserProfile) o;
        return Objects.equal(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
}
