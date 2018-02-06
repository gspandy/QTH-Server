package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.DistributorsRelation;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class DistributorsRelationDao extends SqlSessionDaoSupport {
    public void deleteByPrimaryKey(Long id){
        getSqlSession().delete("DistributorsRelationMapper.deleteByPrimaryKey", id);
    }

    public void insert(DistributorsRelation record){
        getSqlSession().insert("DistributorsRelationMapper.insert", record);
    }

    public void insertSelective(DistributorsRelation record){
        getSqlSession().insert("DistributorsRelationMapper.insertSelective", record);
    }

    public DistributorsRelation selectByPrimaryKey(Long id){
        return getSqlSession().selectOne("DistributorsRelationMapper.selectByPrimaryKey", id);
    }

    public void updateByPrimaryKeySelective(DistributorsRelation record){
        getSqlSession().update("DistributorsRelationMapper.updateByPrimaryKeySelective", record);
    }

    public void updateByPrimaryKey(DistributorsRelation record){
        getSqlSession().update("DistributorsRelationMapper.updateByPrimaryKey", record);
    }
}