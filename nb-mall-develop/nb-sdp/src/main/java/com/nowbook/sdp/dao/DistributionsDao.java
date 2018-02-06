package com.nowbook.sdp.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.nowbook.sdp.model.DistributionInfo;
import com.nowbook.sdp.model.DistributionInfoForQuery;
import com.nowbook.sdp.model.Distributions;
import com.nowbook.sdp.model.Summary;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;
import com.nowbook.common.model.Paging;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhum01 on 2014/7/30.
 */
@Repository
public class DistributionsDao extends SqlSessionDaoSupport{

    public Paging<Distributions> distributionsAll(Integer offset, Integer limit) {
        Long total = getSqlSession().selectOne("Distributions.countLikeName");
        if(total == 0) {
            return new Paging<Distributions>(0L, Collections.<Distributions>emptyList());
        }
        List<Distributions> disAllPage = getSqlSession().selectList("Distributions.disAllByName", ImmutableMap.of("offset", offset, "limit", limit));
        return new Paging<Distributions>(total, disAllPage);
    }

    public Paging<Distributions> distributionsSearch(String shopName,String openStatus,String auditStatus,Integer offset, Integer limit) {
        Map<String, Object> n = Maps.newHashMap();
        n.put("shopName", shopName);
        n.put("openStatus", openStatus);
        n.put("auditStatus", auditStatus);
        Long total = getSqlSession().selectOne("Distributions.countLikeName", n);
        if (total==0) {
            return new Paging<Distributions>(0L, Collections.<Distributions>emptyList());
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("shopName", shopName);
        params.put("openStatus", openStatus);
        params.put("auditStatus", auditStatus);
        params.put("offset", offset);
        params.put("limit", limit);
        List<Distributions> data = getSqlSession().selectList("Distributions.disAllByName", params);
        return new Paging<Distributions>(total, data);
    }

    public void updateOpenStatus(Long id, String openStatus) {
        getSqlSession().update("Distributions.updateOpenStatus", ImmutableMap.of(
                "id", id, "openStatus", openStatus
        ));
    }

    public Boolean distributionsUpdate(Distributions distributions) {
        getSqlSession().update("Distributions.updateRealnameStatus",distributions);
        getSqlSession().update("Distributions.disMobileUpdate", distributions);
        return getSqlSession().update("Distributions.disUpdate",distributions)==1;
    }

    public Long distributionsCreat(Distributions distributions) {
        getSqlSession().insert("Distributions.disCreat", distributions);

        return distributions.getId();
    }

    public DistributionInfo selectByPrimaryKey(Long id){
        return getSqlSession().selectOne("Distributions.selectByPrimaryKey", id);
    }

    public DistributionInfoForQuery selectDistributionByKey(Long id){
        return getSqlSession().selectOne("Distributions.selectDistributionByKey", id);
    }

    public List<Summary> selectSummaryByKey(Long id){
        return getSqlSession().selectList("Distributions.selectSummaryByKey", id);
    }
    public DistributionInfoForQuery selectDistributionByUserId(Long userId){
        return getSqlSession().selectOne("Distributions.selectDistributionByUserId", userId);
    }

    public String getParentIds(Long id){
        return getSqlSession().selectOne("Distributions.getParentIds", id);
    }
    public String getParentIdsByuserId(Long userId){
        return getSqlSession().selectOne("Distributions.getParentIdsByuserId", userId);
    }

    public Paging<DistributionInfo> getDistributionByLevel(Long id,String level,Integer offset, Integer limit){
        Long total = getSqlSession().selectOne("Distributions.getDistributionByLevelCount", ImmutableMap.of("id", id, "level", level));
        if(total == 0) {
            return new Paging<DistributionInfo>(0L, Collections.<DistributionInfo>emptyList());
        }
        List<DistributionInfo> distributionInfos  = getSqlSession().selectList("Distributions.getDistributionByLevel", ImmutableMap.of("id", id, "level", level, "offset", offset, "limit", limit));
        return new Paging<DistributionInfo>(total, distributionInfos);
    }

    public void bindSdp(Long orderId, Long ditrabutorId) {
         getSqlSession().insert("Distributions.bindSdp",ImmutableMap.of("orderId", orderId, "ditrabutorId", ditrabutorId));
    }

    public String getParentsByOrderId(Long orderId) {
        return getSqlSession().selectOne("Distributions.getParentsByOrderId",ImmutableMap.of("orderId", orderId));
    }
    public String getOpenIdByDistributorId(Long DistributorId) {
        return getSqlSession().selectOne("Distributions.getOpenIdByDistributorId",ImmutableMap.of("DistributorId", DistributorId));
    }

    public String getConsumption(Long distributorId) {
        return getSqlSession().selectOne("Distributions.getConsumption",ImmutableMap.of("distributorId", distributorId));
    }
    public String withdrawalsEd(Long distributorId) {
        return getSqlSession().selectOne("Distributions.withdrawalsEd",ImmutableMap.of("distributorId", distributorId));
    }

    public String amountCount(Long distributorId) {
        return getSqlSession().selectOne("Distributions.amountCount",ImmutableMap.of("distributorId", distributorId));
    }
    public String getIds(Long userId){
        return getSqlSession().selectOne("Distributions.getIds",ImmutableMap.of("userId", userId));
    }
    public String getAvatar(Long userId){
        return getSqlSession().selectOne("Distributions.selectAvatar",ImmutableMap.of("userId", userId));
    }
    public String selectDistributorId(String orderId){
        return getSqlSession().selectOne("Distributions.selectDistributorId",ImmutableMap.of("orderId", orderId));
    }

    public Boolean setQr(Distributions distributions){
        return getSqlSession().update("Distributions.setQr",distributions) == 1;
    }
    public Boolean updateDistributor(Distributions distribution) {
        return getSqlSession().update("Distributions.updateDistributor",distribution)==1;
    }
}
