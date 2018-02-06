package com.nowbook.sdp.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class UserLevelHistory extends PageModel{
    private static final long serialVersionUID = 6404288790255635091L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private Integer level;//用户等级

}
