package com.nowbook.notice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author dpzh
 * @create 2017-08-21 14:59
 * @description:<类文件描述>
 **/

@ToString
public class LastResult {


    @Getter
    @Setter
    private String message;

    @Getter
    @Setter
    private String state;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private String condition;

    @Getter
    @Setter
    private String ischeck;

    @Getter
    @Setter
    private String com;

    @Getter
    @Setter
    private String nu;

    @Getter
    @Setter
    private List<LogisticsData> data;



}
