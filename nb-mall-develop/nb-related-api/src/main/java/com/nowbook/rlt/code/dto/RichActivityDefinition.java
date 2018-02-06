package com.nowbook.rlt.code.dto;

import com.nowbook.rlt.code.model.ActivityDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author wanggen on 14-7-7.
 * @Desc: 更完整的信息
 */
public class RichActivityDefinition extends ActivityDefinition {
    private static final long serialVersionUID = -5802110093216217991L;

    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private List<Long> itemIds;
}
