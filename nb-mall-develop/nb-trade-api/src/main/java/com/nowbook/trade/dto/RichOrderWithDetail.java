package com.nowbook.trade.dto;

import com.nowbook.trade.model.OrderExtra;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-12-03
 */
public class RichOrderWithDetail<T extends RichOrder> implements Serializable {
    private static final long serialVersionUID = -8414897473203806243L;

    @Getter
    @Setter
    private T richOrder;

    @Getter
    @Setter
    private OrderExtra orderExtra;

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("richOrder", richOrder)
                .add("orderExtra", orderExtra)
                .toString();
    }
}
