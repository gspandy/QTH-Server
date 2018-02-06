package com.nowbook.alipay.dto;

import com.google.common.base.Objects;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-03-13 11:55 AM  <br>
 * Author:cheng
 */
@XStreamAlias("alipay")
public class AlipaySyncResponse {


    @Setter
    @XStreamAlias("is_success")
    private String success;

    @Getter
    @Setter
    @XStreamAlias("error")
    private String error;


    public boolean isSuccess() {
        return Objects.equal(success, "T");
    }

}
