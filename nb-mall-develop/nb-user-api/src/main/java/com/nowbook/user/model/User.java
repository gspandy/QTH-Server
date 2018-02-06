/*
 * Copyright (c) 2012 大连锦霖科技有限公司
 */

package com.nowbook.user.model;

import com.nowbook.user.base.BaseUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Min;
import java.util.Date;

import static com.google.common.base.Objects.equal;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2012-07-31
 */
@ToString(callSuper = true)
public class User extends BaseUser {

    public static enum STATUS {
        FROZEN(-2, "已冻结"),
        LOCKED(-1, "已锁定"),
        NOT_ACTIVATE(0, "未激活"),
        NORMAL(1, "正常");

        private final int value;

        private final String display;

        private STATUS(int number, String display) {
            this.value = number;
            this.display = display;
        }

        public static STATUS fromNumber(int number) {
            for (STATUS status : STATUS.values()) {
                if (Objects.equal(status.value, number)) {
                    return status;
                }
            }
            return null;
        }

        public int toNumber() {
            return value;
        }


        @Override
        public String toString() {
            return display;
        }
    }

    private static final long serialVersionUID = 5061383195453133821L;

    @Getter
    @Setter
    @JsonIgnore
    private String encryptedPassword;//加密密码

    @Getter
    @Setter
    private Integer status;//用户状态

    @JsonIgnore
    @Getter
    @Setter
    private Date createdAt;

    @JsonIgnore
    @Getter
    @Setter
    private Date updatedAt;

    @Email
    @Getter
    @Setter
    private String email;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof User)) {
            return false;
        }
        User that = (User) obj;
        return equal(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }



}
