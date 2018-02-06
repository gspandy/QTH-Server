/*
 * Copyright (c) 2013 大连锦霖科技有限公司
 */

package com.nowbook.web.controller.api.userEvent;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-06-20
 */
public class UserProfileEvent {
    @Getter
    private final Long userId;

    @Getter
    @Setter
    private String mobile;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String realName;

    public UserProfileEvent(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof UserProfileEvent)) {
            return false;
        }
        UserProfileEvent that = (UserProfileEvent) o;
        return Objects.equal(this.userId, that.userId)
                && Objects.equal(this.name, that.name)
                && Objects.equal(this.realName, that.realName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, name, realName);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userId", userId)
                .add("mobile", mobile)
                .add("name", name)
                .add("realName", realName)
                .toString();
    }
}
