package com.nowbook.notice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author dpzh
 * @create 2017-08-21 14:55
 * @description:<类文件描述>
 **/
@ToString
public class Express {

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private String message;

    @Getter
    @Setter
    private String autoCheck;

    @Getter
    @Setter
    private String comOld;

    @Getter
    @Setter
    private String comNew;

    @Getter
    @Setter
    private LastResult lastResult;

}
