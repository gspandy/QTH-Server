package com.nowbook.push.pushserver;


import com.nowbook.push.jpush.model.PushBean;

/**
 * @author dpzh
 * @create 2017-07-14 13:24
 * @description: 推送
 **/

public interface PushServer {


    /**
     *向单个APP推送信息
     */
    void pushSingleApp(PushBean pBean);

    /**
     *向全部APP推送信息
     */
    void pushAllApp(PushBean pBean);

}
