package com.nowbook.push.jpush.service;


import com.nowbook.push.jpush.model.PushBean;
import com.nowbook.push.jpush.utils.PushAndroidUtils;
import com.nowbook.push.jpush.utils.PushIosUtils;
import com.nowbook.push.pushserver.PushServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

/**
 * @author dpzh
 * @create 2017-07-14 13:24
 * @description:<推送实现类>
 **/

@Service
public class JpushServer implements PushServer {
    protected static final Log loger = LogFactory.getLog(PushAndroidUtils.class);

    /**
     * @description:<向单个用户推送消息>
     * @author dpzh
     * @create 2017/7/18 13:27
     * @param pBean <传入参数对象>
     * @return:void
     **/
    @Override
    public  void pushSingleApp(PushBean pBean)  {
        try {
            if(pBean.getDeviceType()==0){
                PushIosUtils.pushIosApp(pBean);
                PushAndroidUtils.pushAndroidApp(pBean);
            }else if (pBean.getDeviceType()==1){
                PushIosUtils.pushIosApp(pBean);
            }else if (pBean.getDeviceType()==2){
                PushAndroidUtils.pushAndroidApp(pBean);
            }
        } catch (Exception e) {
            loger.error("Android push message exception,parameter:"+pBean.toString(),e);
        }

    }

    /**
     * @description:<向全部APP推送消息>
     * @author dpzh
     * @create 2017/7/18 13:26
     * @param pBean <传入的推送参数对象>
     * @return:void
     **/
    @Override
    public  void pushAllApp(PushBean pBean) {
        try {
            if (pBean.getDeviceType()==0){
                PushIosUtils.pushIosAllApp(pBean);
                PushAndroidUtils.pushAndroidAllApp(pBean);
            }else if (pBean.getDeviceType()==1){
                PushAndroidUtils.pushAndroidAllApp(pBean);
            }else if (pBean.getDeviceType()==2){
                PushIosUtils.pushIosAllApp(pBean);
            }
            loger.info("Push messages successfully to all users,parameter:"+pBean.toString());
        } catch (Exception e) {
            loger.error("To all users push message anomaly,parameter:"+pBean.toString(),e);
        }

    }

}
