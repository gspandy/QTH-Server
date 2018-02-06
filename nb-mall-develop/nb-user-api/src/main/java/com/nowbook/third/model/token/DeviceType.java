package com.nowbook.third.model.token;

/**
 * Created by robin on 17/7/27.
 * 终端设备类型
 */
public enum DeviceType {
    WEB(0,""),IOS(1,"iPhone"),ANDROID(2,"Android"),WEBMOBLIE(3,"webMobile");
    private int typeId;
    private String name;
    private DeviceType(int typeId,String name){
        this.typeId=typeId;
        this.name=name;
    }


    public static DeviceType from(int typeId) {
        for (DeviceType deviceType : DeviceType.values()) {
            if (deviceType.typeId == typeId) {
                return deviceType;
            }
        }
        return null;
    }
}
