package com.nowbook.sdp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

public class UserTeam extends PageModel{

    private static final long serialVersionUID = 6404288790255635091L;

    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private String name;//团队名称

    @Setter
    @Getter
    private Long leader;//领队ID
}