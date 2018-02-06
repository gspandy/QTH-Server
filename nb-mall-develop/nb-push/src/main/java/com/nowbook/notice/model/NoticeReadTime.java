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
public class NoticeReadTime implements Serializable {
    private static final long serialVersionUID = -7161289916347440117L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Integer type;       //消息类别

    @Getter
    @Setter
    private Long userId;    //接收消息用户id

    @Getter
    @Setter
    private Date clickTime; //点击时间，统计消息时该时间之后的为新消息

    @Getter
    @Setter
    private Date clearanceTime; //清空时间，查询消息时，只查询该时间以后的消息

    @Getter
    @Setter
    private Date createdAt; //创建时间

    @Getter
    @Setter
    private Date updatedAt; //修改时间



}
