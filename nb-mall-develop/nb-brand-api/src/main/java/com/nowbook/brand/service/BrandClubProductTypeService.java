package com.nowbook.brand.service;

import com.nowbook.common.model.Response;
import com.nowbook.brand.model.BrandClubProductType;

import java.util.List;

/**
 * Created by mark on 2014/7/31
 */
public interface BrandClubProductTypeService {
    Response<List<BrandClubProductType>> findAllBy();
}
