package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.UserRelation;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRelationDao extends SqlSessionDaoSupport {
    /**
     * 添加上下级信息
     *
     * @param  userRelation   上下级信息
     */
    public void insert(UserRelation userRelation) {
        getSqlSession().insert("UserRelationMapper.insert", userRelation);
    }

    /**
     * 删除上下级信息
     *
     * @param  userRelation   上下级信息
     */
    public void delete(UserRelation userRelation) {
        getSqlSession().delete("UserRelationMapper.deleteByUserId", userRelation);
    }

    /**
     * 查找上下级信息
     *
     * @param  userRelation   上下级信息
     */
    public List<UserRelation> selectBy(UserRelation userRelation) {
        return getSqlSession().selectList("UserRelationMapper.selectBy", userRelation);
    }
}