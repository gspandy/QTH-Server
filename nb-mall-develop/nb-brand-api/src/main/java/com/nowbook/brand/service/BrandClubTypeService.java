package com.nowbook.brand.service;

import com.nowbook.common.model.Response;
import com.nowbook.brand.model.BrandClubType;

import java.util.List;

/**
 * Created by mark on 2014/7/11.
 */
public interface BrandClubTypeService {
    Response<List<BrandClubType>> findAllBy();
    
}
