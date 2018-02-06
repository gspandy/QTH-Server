package com.nowbook.sdp.service;


import com.nowbook.common.model.Response;
import com.nowbook.sdp.dao.DistributorsRelationDao;
import com.nowbook.sdp.model.DistributorsRelation;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wangdongchang on 16-04-24
 */
@Service
public class DistributorsRelationServiceImpl implements DistributorsRelationService{
    private final static Logger log = LoggerFactory.getLogger(DistributorsRelationServiceImpl.class);
    @Autowired
    private DistributorsRelationDao distributorsRelationDao;
    @Override
    public  Response<Boolean> deleteByPrimaryKey(Long id){
        Response<Boolean> result = new Response<Boolean>();
        if (id == null) {
            log.error("id should be specified when deleted");
            result.setError("distributorsRelation.id.null");
            return result;
        }
        try {
            distributorsRelationDao.deleteByPrimaryKey(id);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to delete distributorsRelation(id={}),cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError(e.getMessage());
            return result;
        }
    }
    @Override
    public Response<Long> insert(DistributorsRelation record){
        Response<Long> result = new Response<Long>();
        if (record == null) {
            log.error("AmountDetail can not be null when create distributorsRelation");
            result.setError("illegal.param");
            return result;
        }
        if (record.getId() == null) {
            log.error("id can not be null when create distributorsRelation");
            result.setError("illegal.param");
            return result;
        }
        distributorsRelationDao.insert(record);
        result.setResult(record.getId());

        return result;
    }
    @Override
    public Response<Long> insertSelective(DistributorsRelation record){
        Response<Long> result = new Response<Long>();
        if (record == null) {
            log.error("distributorsRelation can not be null when create distributorsRelation");
            result.setError("illegal.param");
            return result;
        }
        if (record.getId() == null) {
            log.error("id can not be null when create distributorsRelation");
            result.setError("illegal.param");
            return result;
        }
        distributorsRelationDao.insertSelective(record);
        result.setResult(record.getId());

        return result;
    }
    @Override
    public Response<DistributorsRelation> selectByPrimaryKey(Long id){
        Response<DistributorsRelation> result = new Response<DistributorsRelation>();
        DistributorsRelation distributorsRelation = distributorsRelationDao.selectByPrimaryKey(id);
        result.setResult(distributorsRelation);
        return result;
    }
    @Override
    public  Response<Boolean> updateByPrimaryKeySelective(DistributorsRelation record){
        Response<Boolean> result = new Response<Boolean>();

        if(record.getId() == null ) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            distributorsRelationDao.updateByPrimaryKeySelective(record);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update distributorsRelation method openStatus by id={}, parentIds={},cause:{}",
                    record.getId(), record.getParentId(), Throwables.getStackTraceAsString(e));
            result.setError("distributorsRelation.method.update.fail");
            return result;
        }
    }
    @Override
    public  Response<Boolean> updateByPrimaryKey(DistributorsRelation record){
        Response<Boolean> result = new Response<Boolean>();

        if(record.getId() == null ) {
            log.error("distributorsRelation params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            distributorsRelationDao.updateByPrimaryKey(record);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update distributorsRelation method openStatus by id={}, parentIds={},cause:{}",
                    record.getId(), record.getParentId(), Throwables.getStackTraceAsString(e));
            result.setError("distributorsRelation.method.update.fail");
            return result;
        }
    }


}