package com.nowbook.sdp.model;

import lombok.Getter;
import lombok.Setter;

public class UserRelation extends PageModel{

    private static final long serialVersionUID = 6404288790255635091L;

    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private Long userId;//下级ID

    @Setter
    @Getter
    private Long parent;//上级ID

    @Setter
    @Getter
    private String parentMobile;//上级ID
    @Setter
    @Getter
    private Integer level;//下级等级

    @Setter
    @Getter
    private Integer parentLevel;//下级等级
}