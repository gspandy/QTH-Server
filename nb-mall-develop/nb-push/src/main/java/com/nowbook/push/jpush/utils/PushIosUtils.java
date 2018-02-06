package com.nowbook.push.jpush.utils;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import com.nowbook.push.jpush.model.PushBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dpzh
 * @create 2017-07-14 13:24
 * @description: 向Ios用户推送方法
 **/
public class PushIosUtils  {
    protected static final Log loger = LogFactory.getLog(PushIosUtils.class);

    private static final String appKey ="96492206da579ad9978453ca";
    private static final String masterSecret = "b6ac2289a1c430f129f86598";

    /**
     * @description: 向单个Ios用户推送消息
     * @author dpzh
     * @create 2017/7/18 13:30
     * @param pBean 传入参数Bean
     * @return: void
     **/
    public static void pushIosApp(PushBean pBean) throws Exception {
        JPushClient jpushClient = null;
        jpushClient = new JPushClient(masterSecret, appKey, 3);
        try {
            PushPayload payload = null;
            payload = buildPushObject_ios_alertWithExtras(pBean);
            PushResult result = jpushClient.sendPush(payload);
            loger.info("Push messages successfully to Ios users,parameter:"+pBean.toString());
        } catch (Exception e) {
            loger.error("Push message exceptions to Ios users,parameter:"+pBean.toString(),e);
        }
    }

    /**
     * @description: 向全部Ios用户推送消息
     * @author dpzh
     * @create 2017/7/18 13:30
     * @param pBean 传入参数Bean
     * @return: void
     **/
    public static void pushIosAllApp(PushBean pBean) throws Exception {
        JPushClient jpushClient = null;
        jpushClient = new JPushClient(masterSecret, appKey, 3);
        try {
            PushPayload payload = buildPushObject_ios_all_alertWithExtras(pBean);
            jpushClient.sendPush(payload);
            loger.info("Push messages successfully to all Ios users,parameter:"+pBean.toString());
        } catch (Exception e) {
            loger.error("Push message exceptions to all Ios users,parameter:"+pBean.toString(),e);
        }
    }
    /**
     * @description: 构造向所有Ios用户的推送对象
     * @author dpzh
     * @create 2017/7/18 13:32
     * @param pBean 传入参数
     * @return:cn.jpush.api.push.model.PushPayload
     **/
    private static PushPayload buildPushObject_ios_all_alertWithExtras(PushBean pBean){
        return PushPayload.newBuilder()
                .setPlatform(Platform.ios())
                .setAudience(Audience.all())
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(IosNotification.newBuilder()
                                .setAlert(pBean.getContent())
                                .setBadge(+1)
                                .setSound("happy")
                                .addExtra("noticeId", pBean.getNoticeId())
                                .addExtra("businessId", pBean.getBusinessId())
                                .addExtra("subType", pBean.getSubType())
                                .addExtra("itemImage", pBean.getItemImage())
                                .addExtra("itemSize", pBean.getItemSize())
                                .addExtra("type", pBean.getType())
                                .build())
                        .build())
                .setOptions(Options.newBuilder()
                        .setApnsProduction(true)
                        .setTimeToLive(864000)
                        .build())
                .build();
    }

    /**
     * @description: 构造向单个Ios用户的推送对象
     * @author dpzh
     * @create 2017/7/18 13:33
     * @param pBean <传入参数>
     * @return:cn.jpush.api.push.model.PushPayload
     **/
    private static PushPayload buildPushObject_ios_alertWithExtras(PushBean pBean){
        return PushPayload.newBuilder()
                .setPlatform(Platform.ios())
                .setAudience(Audience.registrationId(pBean.getDeviceId()))
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(IosNotification.newBuilder()
                                .setAlert(pBean.getContent())
                                .setBadge(1)
                                .setSound("happy")
                                .addExtra("noticeId", pBean.getNoticeId())
                                .addExtra("businessId", pBean.getBusinessId())
                                .addExtra("subType", pBean.getSubType())
                                .addExtra("itemImage", pBean.getItemImage())
                                .addExtra("itemSize", pBean.getItemSize())
                                .addExtra("type", pBean.getType())
                                .build())
                        .build())
                .setOptions(Options.newBuilder()
                        .setApnsProduction(true)
                        .setTimeToLive(864000)
                        .build())
                .build();
    }


}
