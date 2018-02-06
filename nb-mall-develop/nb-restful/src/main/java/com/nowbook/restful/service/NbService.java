package com.nowbook.restful.service;

import com.nowbook.restful.dto.NbResponse;
import com.nowbook.restful.dto.OuterIdDto;

import java.util.List;

/**
 * Created by yangzefeng on 14-1-18
 */
public interface NbService {

    /**
     * 商品自动发布或者同步库存,如果出现异常，直接跳过
     * @param outerIdDtos 结构为outerIdDto列表的json字串
     * @return 操作结果
     */
    NbResponse<Boolean> autoReleaseOrUpdateItem(List<OuterIdDto> outerIdDtos);
}
