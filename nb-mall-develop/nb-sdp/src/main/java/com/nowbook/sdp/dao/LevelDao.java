package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.Level;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LevelDao extends SqlSessionDaoSupport {

    public void insert(Level level) {
        getSqlSession().insert("LevelMapper.insert", level);
    }

    public void updateById(Level level) {
        getSqlSession().update("LevelMapper.updateById", level);
    }

    public void updateByUserId(Level level) {
        getSqlSession().update("LevelMapper.updateByUserId", level);
    }

    public List<Level> selectByLevel(Level level) {
        return getSqlSession().selectList("LevelMapper.selectByLevel", level);
    }
    //根据传入的userId查询该用户的会员等级
    public List<Level> selectByUserId(Long userId){
        return getSqlSession().selectList("LevelMapper.selectByUserId", userId);

    }
}