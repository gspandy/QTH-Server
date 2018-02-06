package com.nowbook.sdp.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class UserLevelUpJob extends PageModel{
    private static final long serialVersionUID = 6404288790255635091L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Integer status;//状态：1 成功 2失败

    @Getter
    @Setter
    private Long cost;//耗时
}
