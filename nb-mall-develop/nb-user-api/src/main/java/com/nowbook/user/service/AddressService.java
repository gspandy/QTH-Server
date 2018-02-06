package com.nowbook.user.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.user.model.Address;

import java.util.List;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-14
 */
public interface AddressService {

    Response<List<Address>> provinces();

    Response<List<Address>> citiesOf(@ParamInfo("provinceId") Integer provinceId);

    Response<List<Address>> districtOf(@ParamInfo("cityId") Integer cityId);

    Response<Address> findById(@ParamInfo("id") Integer id);

    Response<List<Integer>> ancestorsOf(Integer anyId);

    Response<List<Address>> ancestorOfAddresses(Integer anyId);

    Response<List<Address>> getTreeOf(Integer parentId);

}
