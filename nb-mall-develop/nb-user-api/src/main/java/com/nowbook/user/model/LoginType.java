package com.nowbook.user.model;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-11-28
 */
public enum LoginType {
    EMAIL(1),
    MOBILE(2),
    NAME(3);

    private  int value;

    private LoginType(int value) {
        this.value = value;
    }

    public static LoginType from(int value) {
        for (LoginType loginType : LoginType.values()) {
            if (loginType.value == value) {
                return loginType;
            }
        }
        return null;
    }
    public  String getValue(){
        return String.valueOf(value);
    }
}
