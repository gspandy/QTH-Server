package com.nowbook.push.jpush.utils;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.Notification;
import com.nowbook.push.jpush.model.PushBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dpzh
 * @create 2017-07-14 13:24
 * @description: 向Android用户推送方法
 **/
public class PushAndroidUtils {
    protected static final Log loger = LogFactory.getLog(PushAndroidUtils.class);

    private static final String appKey ="96492206da579ad9978453ca";
    private static final String masterSecret = "b6ac2289a1c430f129f86598";

    /**
     * @description: 向单个Android用户推送消息
     * @author dpzh
     * @create 2017/7/18 13:37
     * @param pBean 传入参数Bean
     * @return:void
     **/
    public static void pushAndroidApp(PushBean pBean) throws Exception {
        JPushClient jpushClient = null;
        jpushClient = new JPushClient(masterSecret, appKey, 3);
        try {
            PushPayload payload = null;
            payload = buildPushObject_android_alertWithExtras(pBean);
            PushResult result = jpushClient.sendPush(payload);
            loger.info("Push successful messaging to Android users,parameter:"+pBean.toString());
        } catch (Exception e) {
            loger.error("Push message exceptions to Android users,parameter:"+pBean.toString(),e);
        }
    }

    /**
     * @description: 向全部Android用户推送消息
     * @author dpzh
     * @create 2017/7/18 13:39
     * @param pBean 传入参数Bean
     * @return:void
     **/
    public static void pushAndroidAllApp(PushBean pBean) throws Exception {
        JPushClient jpushClient = null;
        jpushClient = new JPushClient(masterSecret, appKey, 3);
        try {
            PushPayload payload = buildPushObject_android_all_alertWithExtras(pBean);
            jpushClient.sendPush(payload);
            loger.info("Push successful messaging to all Android users,parameter:"+pBean.toString());
        } catch (Exception e) {
            loger.error("Push message exception to all Android users,parameter:"+pBean.toString(),e);
        }
    }

    /**
     * @description: 构造向所有Android用户的推送对象
     * @author dpzh
     * @create 2017/7/18 13:40
     * @param pBean 传入参数Bean
     * @return:cn.jpush.api.push.model.PushPayload
     **/
    private static PushPayload buildPushObject_android_all_alertWithExtras(PushBean pBean) {

        return PushPayload.newBuilder()
                .setPlatform(Platform.android())
                .setAudience(Audience.all())
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setAlert(pBean.getContent())
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
     * @description: 构造向单个Android用户的推送对象
     * @author dpzh
     * @create 2017/7/18 13:40
     * @param pBean 传入参数Bean
     * @return:cn.jpush.api.push.model.PushPayload
     **/
    private static PushPayload buildPushObject_android_alertWithExtras(PushBean pBean){
        return PushPayload.newBuilder()
                .setPlatform(Platform.android())
                .setAudience(Audience.registrationId(pBean.getDeviceId()))
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setAlert(pBean.getContent())
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
