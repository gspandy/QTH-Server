package com.nowbook.notice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author dpzh
 * @create 2017-08-21 15:02
 * @description:<类文件描述>
 **/
@ToString
public class LogisticsData {


    @Getter
    @Setter
    private String context;


    @Getter
    @Setter
    private String time;


    @Getter
    @Setter
    private String ftime;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private String areaCode;

    @Getter
    @Setter
    private String areaName;


}
