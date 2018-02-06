package com.nowbook.rlt.code.dto;

import com.nowbook.rlt.code.model.ActivityDefinition;
import lombok.Data;

import java.util.List;

/**
 * @author wanggen on 14-7-5.
 * @Desc:
 */
@Data
public class ActivityDefinitionDto {

    private ActivityDefinition activityDefinition;

    private List<Long> itemIds;

    private Integer itemType;

    private List<String> codes;

}
