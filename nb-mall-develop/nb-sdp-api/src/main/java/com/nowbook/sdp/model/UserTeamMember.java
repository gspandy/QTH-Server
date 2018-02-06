package com.nowbook.sdp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

public class UserTeamMember extends PageModel{
    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private Long team;//团队ID

    @Setter
    @Getter
    private Long member;//成员ID

    @Setter
    @Getter
    private String role;

    @Setter
    @Getter
    private String nick;//昵称
}