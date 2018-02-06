package com.nowbook.weixin.weixin4j.token.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Created by Administrator on 2016/7/12.
 */
@ToString
public class AccessToken {
    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private String access_token;
    @Getter
    @Setter
    private Date updated_at;

}
