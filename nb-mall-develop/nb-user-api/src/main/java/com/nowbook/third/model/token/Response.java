package com.nowbook.third.model.token;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by robin on 17/7/28.
 */
public class Response<T> implements Serializable {
    private static final long serialVersionUID = 568135792523540847L;
    @Setter
    @Getter
    private int status;
    @Setter
    @Getter
    private T data;

}
