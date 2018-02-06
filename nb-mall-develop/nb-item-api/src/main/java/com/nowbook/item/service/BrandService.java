package com.nowbook.item.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.item.model.Brand;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by yangzefeng on 14-1-15
 */
public interface BrandService {

    Response<List<Brand>> findAll();

    Response<Brand> findById(Long id);

    Response<Long> create(Brand brand);

    Response<Boolean> update(Brand brand);

    Response<Paging<Brand>> paging(@ParamInfo("name") @Nullable String name,
                                       @ParamInfo("pageNo") @Nullable Integer pageNo,
                                       @ParamInfo("size") @Nullable Integer size);
}
