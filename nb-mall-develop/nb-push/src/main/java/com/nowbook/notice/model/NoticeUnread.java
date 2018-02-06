package com.nowbook.notice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author dpzh
 * @create 2017-08-09 9:32
 * @description: 消息已读时间
 **/
@ToString
public class NoticeUnread implements Serializable {
    private static final long serialVersionUID = -7161289916347440119L;

    @Getter
    @Setter
    private Integer type;       //消息类别

    @Getter
    @Setter
    private Date time;

    @Getter
    @Setter
    private String count;

    @Getter
    @Setter
    private String firstNotice;

    @Getter
    @Setter
    private Integer subType;



}
