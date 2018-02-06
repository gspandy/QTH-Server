package com.nowbook.user.mysql;

import com.nowbook.user.model.UserCost;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2016/7/18.
 */
@Repository
public class UserCostDao extends SqlSessionDaoSupport {
    public boolean create(UserCost userCost){
        return getSqlSession().insert("UserCost.create", userCost) == 1;
    }

    public UserCost selectById(Long id){
        return getSqlSession().selectOne("UserCost.findById", id);
    }

    public boolean updateById(UserCost userCost){
        return getSqlSession().update("UserCost.update", userCost) == 1;
    }
}
