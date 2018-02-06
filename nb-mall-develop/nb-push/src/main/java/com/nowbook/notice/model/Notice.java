package com.nowbook.notice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author dpzh
 * @create 2017-08-02 10:54
 * @description: 通知存储
 **/

@ToString
public class Notice extends PageModel {

    private static final long serialVersionUID = -7161289916347440115L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long businessId;  //业务ID

    @Getter
    @Setter
    private Long fromUser;  //如果为0，为系统推送，如果为其他则为用户Id推送


    @Getter
    @Setter
    private Long toUser;    //接收消息用户id

    @Getter
    @Setter
    private Integer type;       //消息类别

    @Getter
    @Setter
    private Integer subType;      //子类别

    @Getter
    @Setter
    private String content;    //消息内容

    @Getter
    @Setter
    private String title;       //消息标题

    @Getter
    @Setter
    private Date createdAt; //创建时间

    @Getter
    @Setter
    private Date updatedAt; //修改时间

    @Getter
    @Setter
    private String count;


    @Getter
    @Setter
    private String createdTime; //修改时间


    @Getter
    @Setter
    private String itemImage;

    @Getter
    @Setter
    private Integer itemSize;

    @Getter
    @Setter
    private Integer logistics;

    @Getter
    @Setter
    private String expressNo;




}
