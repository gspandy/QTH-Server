package com.nowbook.user.util;

import com.nowbook.redis.utils.KeyUtils;

/**
 * Created by Kevin on 16-4-12.
 */
public class RedisKeyUtils extends KeyUtils {

    public static String distributionConf(){
        return "distribution:conf";
    }

    public static String shopEvaluation(long shopId) {
        return "shop-evaluation:" + shopId;
    }

    public static String itemIdEvaluation(long itemId) { return "items-detail-image:" + itemId; }
    public static String spuIdEvaluation(long spuId) { return "spu-detail-image:" + spuId; }

    public static String apiToken(Long loginId,String deviceId,Integer type){
        return "api-token:"+type+":"+loginId+":deviceId:"+deviceId;
    }

    public static String apiToken(Long loginId){
        return "api-token:"+loginId;
    }

    public static String apiToken(String deviceId,Integer type){
        return "deviceId:"+deviceId+":"+type;
    }

    public static String profitConf(){
        return "system-conf:profit";
    }

    public static String otherConf(){
        return "system-conf:other";
    }


}
