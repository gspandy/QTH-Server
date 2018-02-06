package com.nowbook.restful.event;

import lombok.*;

/**
 * 第三方注册用户事件
 *
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-06-10 10:11 PM  <br>
 * Author:cheng
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ThirdRegisterEvent  {

    @Getter
    @Setter
    private Long userId;            // 用户id

    @Getter
    @Setter
    private String userName;        // 用户名

    @Getter
    @Setter
    private String channel;         // 渠道

    @Getter
    @Setter
    private String activity;        // 来源

    @Getter
    @Setter
    private String from;            // 活动


}
