package com.nowbook.user.service;

import com.nowbook.common.model.Response;
import com.nowbook.user.model.UserCost;
import com.nowbook.user.mysql.UserCostDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2016/7/18.
 */
@Service
@Slf4j
public class UserCostImpl implements UserCostService {

    @Autowired
    private UserCostDao userCostDao;
    @Override
    public Response<Boolean> create(UserCost userCost) {
        Response<Boolean> result = new Response<Boolean>();
        result.setResult(userCostDao.create(userCost));
        return result;
    }

    @Override
    public Response<UserCost> selectById(Long id) {
        Response<UserCost> result = new Response<UserCost>();
        result.setResult(userCostDao.selectById(id));
        return result;
    }

    @Override
    public Response<Boolean> update(UserCost userCost) {
        Response<Boolean> result = new Response<Boolean>();
        result.setResult(userCostDao.updateById(userCost));
        return result;
    }
}
