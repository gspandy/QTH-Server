package com.nowbook.sdp.service;

import com.nowbook.category.dao.RedisFrontCategoryDao;
import com.nowbook.category.model.FrontCategory;
import com.nowbook.common.model.PageInfo;
import com.nowbook.common.model.Response;
import com.nowbook.common.model.Paging;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.nowbook.sdp.dao.*;
import com.nowbook.sdp.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

/**
 * Created by mark on 2014/7/11.
 */
@Service
public class DistributionsServiceImpl implements DistributionsService {

    private final static Logger log = LoggerFactory.getLogger(DistributionsServiceImpl.class);

    @Autowired
    private DistributionsDao distributionsDao;

    @Autowired
    private DistributorsAuditDao distributorsAuditDao;

    @Autowired
    private DistributorsRelationDao distributorsRelationDao;

    @Autowired
    private DistributorRedisDao distributorRedisDao;
    @Autowired
    private RedisFrontCategoryDao frontCategoryDao;
    @Autowired
    private ConcernMemberDao concernMemberDao;
    /**
     * 检索分销商
     **/
    @Override
    public Response<Paging<Distributions>> distributionsAll(String openStatus, String auditStatus, String shopName, Integer pageNo, Integer size) {
        PageInfo page = new PageInfo(pageNo, size);
        Response<Paging<Distributions>> result = new Response<Paging<Distributions>>();

        HashMap<String, String> map = distributorRedisDao.getAllDistributionConfKey();

        Paging<Distributions> distributionsPaging;

        try {
            if (Strings.isNullOrEmpty(shopName) && Strings.isNullOrEmpty(openStatus) && Strings.isNullOrEmpty(auditStatus)) {
                distributionsPaging = distributionsDao.distributionsAll(page.getOffset(), page.getLimit());
            } else {
                distributionsPaging = distributionsDao.distributionsSearch(shopName.trim(), openStatus.trim(), auditStatus.trim(), page.getOffset(), page.getLimit());
            }
            result.setResult(distributionsPaging);
            return result;
        } catch (Exception e) {
            log.error("failed to find all distribution, cause:", e);
            result.setError("brand.query.fail");
            return result;
        }
    }

    /**
     * 更新店铺状态
     **/
    @Override
    public Response<Boolean> updateOpenStatus(Long id, String openStatus) {
        Response<Boolean> result = new Response<Boolean>();

        if (id == null || openStatus == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            distributionsDao.updateOpenStatus(id, openStatus);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("fail to update distributors method openStatus by id={}, openStatus={},cause:{}",
                    id, openStatus, Throwables.getStackTraceAsString(e));
            result.setError("distributors.method.update.fail");
            return result;
        }
    }

    /**
     * 更新分销商信息
     **/
    @Override
    public Response<Boolean> distributionsUpdate(Distributions distributions) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            Boolean disUpdate = distributionsDao.distributionsUpdate(distributions);
            result.setResult(disUpdate);
            return result;
        } catch (Exception e) {
            log.error("failed to find all distribution, cause:", e);
            result.setError("api.distributor.createDistribution.fail");
            return result;
        }
    }

    /**
     * 创建分销商
     **/
    @Override
    public Response<Long> distributorsCreateInteger(Distributions distributions) {
        Response<Long> result = new Response<Long>();
        try {
            Long parentId = distributions.getParentId();
            String parentIds = distributionsDao.getParentIds(parentId);
            if(!"null".equals(parentId)&&parentId!=null&&!"".equals(parentId)){
                if("null".equals(parentIds)||parentIds==null||"".equals(parentIds)){
                    parentIds = parentId+"";
                }else{
                    parentIds = parentIds + "," + parentId;
                }
            }
            if("null".equals(parentIds)||parentIds==null||"".equals(parentIds)){
                parentIds = "";
            }
            distributions.setParentIds(parentIds);
            Long distributionsCreat = distributionsDao.distributionsCreat(distributions);

            //建立分销商和被推荐人的关系
            if (!"".equals(parentId) && parentId != null) {
                ConcernMember concernMember = new ConcernMember();
                concernMember.setDistributorId(parentId);
                concernMember.setUserId(distributions.getUserId());
                concernMember.setOperTime(new Date());
                concernMemberDao.insertConcernMember(concernMember);
            }

            boolean distributorsAuditCreat = distributorsAuditDao.distributorsAuditCreat(distributionsCreat);
            String[] stringArr = parentIds.split(",");
            distributions.setParentIds(parentIds);
            DistributorsRelation distributorsRelation = new DistributorsRelation();
            int length = stringArr.length;
            for (int i = 0; i < length; i++) {
                if(!"".equals(stringArr[i])){//如果parentId为空，那么不保存分销商关系。
                distributorsRelation.setDistributorsId(distributions.getId());
                distributorsRelation.setParentId(stringArr[i]);
                distributorsRelation.setDistributionLevel((length - i) + "");
                distributorsRelationDao.insert(distributorsRelation);
                }

            }
            result.setSuccess(true);
            result.setResult(distributionsCreat);
            return result;
        } catch (Exception e) {
            log.error("failed to find all distribution, cause:", e);
            result.setError("api.distributor.createDistribution.fail");
            return result;
        }
    }

    /**
     * 创建分销商
     **/
    @Override
    public Response<Boolean> distributorsCreate(Distributions distributions) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            Long distributionsCreat = distributionsDao.distributionsCreat(distributions);
            boolean distributorsAuditCreat = distributorsAuditDao.distributorsAuditCreat(distributionsCreat);
            result.setResult(distributorsAuditCreat);
            return result;
        } catch (Exception e) {
            log.error("failed to find all distribution, cause:", e);
            result.setError("api.distributor.createDistribution.fail");
            return result;
        }
    }

    @Override
    public Response<DistributionInfo> selectByPrimaryKey(Long id) {
        Response<DistributionInfo> result = new Response<DistributionInfo>();
        DistributionInfo distributionInfo = distributionsDao.selectByPrimaryKey(id);
        result.setResult(distributionInfo);
        return result;
    }

    @Override
    public Response<DistributionInfoForQuery> selectSummaryByKey(Long id) {
        Response<DistributionInfoForQuery> result = new Response<DistributionInfoForQuery>();
        DistributionInfoForQuery distributionInfo = distributionsDao.selectDistributionByKey(id);
        List<Summary> list = distributionsDao.selectSummaryByKey(id);
        HashMap<String, String> map = new HashMap<String, String>();
        for (Summary summary : list) {
            map.put(summary.getKey(), summary.getValue());
        }
        distributionInfo.setMap(map);
        result.setResult(distributionInfo);
        return result;
    }
    @Override
    public Response<DistributionInfoForQuery> selectDistributionByUserId(Long userId) {
        Response<DistributionInfoForQuery> result = new Response<DistributionInfoForQuery>();
        DistributionInfoForQuery distributionInfo = distributionsDao.selectDistributionByUserId(userId);

        result.setResult(distributionInfo);
        return result;
    }
    @Override
    public Response<DistributionInfoForQuery> selectDistributionByDistributionId(Long id){
        Response<DistributionInfoForQuery> result = new Response<DistributionInfoForQuery>();
        DistributionInfoForQuery distributionInfo = distributionsDao.selectDistributionByKey(id);

        result.setResult(distributionInfo);
        return result;
    }

    @Override
    public String getAvatar(Long userId) {
       String result =distributionsDao.getAvatar(userId);
        return result;
    }

    @Override
    public Response<Boolean> updateQr(Distributions distributions) {
        Response<Boolean> result = new Response<Boolean>();
        distributionsDao.setQr(distributions);
        result.setResult(Boolean.TRUE);
        return result;
    }

    @Override
    public String selectDistributorId(String orderId) {
        return distributionsDao.selectDistributorId(orderId);
    }

    @Override
    public Response<Boolean> bindSdp(Long orderId, Long ditrabutorId) {
        Response<Boolean> result = new Response<Boolean>();
        distributionsDao.bindSdp(orderId, ditrabutorId);
        result.setResult(Boolean.TRUE);
        return result;
    }

    @Override
    public Response<String> getParentsByOrderId(Long orderId) {
        Response<String> result = new Response<String>();
        String s = distributionsDao.getParentsByOrderId(orderId);
        result.setResult(s);
        return result;
    }

    @Override
    public Response<Paging<DistributionInfo>> getDistributionByLevel(Long id, String level, Integer pageNo, Integer size) {
        PageInfo page = new PageInfo(pageNo, size);
        Response<Paging<DistributionInfo>> result = new Response<Paging<DistributionInfo>>();
        Paging<DistributionInfo> distributionInfo = distributionsDao.getDistributionByLevel(id, level, page.getOffset(), page.getLimit());
        result.setResult(distributionInfo);
        return result;
    }

    @Override
    public Response<List<FrontCategory>> findAllSecondLevel() {
        Response result = new Response();
        try {
            List<FrontCategory> secondLevels = this.frontCategoryDao.findChildrenById(new Long(130));
            secondLevels.addAll(this.frontCategoryDao.findChildrenById(new Long(178)));
            secondLevels.addAll(this.frontCategoryDao.findChildrenById(new Long(231)));
            secondLevels.addAll(this.frontCategoryDao.findChildrenById(new Long(290)));
            secondLevels.addAll(this.frontCategoryDao.findChildrenById(new Long(337)));
            result.setResult(secondLevels);

            return result;
        } catch (Exception var9) {
            log.error("failed to find second  level category by first level,cause:{}", Throwables.getStackTraceAsString(var9));
            result.setError("frontCategory.query.fail");
            return result;
        }

    }

    @Override
    public Response<HashMap<String,String>> withdrawalsPreconditions(Long distributorId,Double money) {
        Response<HashMap<String,String>> result = new Response<HashMap<String,String>>();
        HashMap<String,String> map = new  HashMap<String,String>();
        map.put("retCode", "0");
        map.put("retMsg","");
        if (distributorId == null ) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            //结算时间（分销商下单并付款之后经过结算时间天数才可以对销售订单进行分成）
            //settlement_days
            //String settlementDays =  distributorRedisDao.getDistributionConfKey("settlement_days");


            //提现标准（分销商申请提现时，少于该值无法提现）；
            //withdrawal_standard

            String withdrawalStandard =  distributorRedisDao.getDistributionConfKey("withdrawal_standard");
            if(money > Double.valueOf(withdrawalStandard)){
                map.put("retCode", "1");
                map.put("retMsg","本次提现金额大于体现标准;");
            }
            //提现需消费金额（如果消费金额为0的话，按照提现标准进行判断，满足提现标准时可以提现，如果消费金额不为0的话，分销商必须达到提现需消费的金额时才能进行提现操作）；
            //withdrawal_consumption_amount
            String consumption =  distributorRedisDao.getDistributionConfKey("withdrawal_consumption_amount");
            String selfConsumption = distributionsDao.getConsumption(distributorId);
            if(Double.valueOf(selfConsumption)<Double.valueOf(consumption)){
                map.put("retCode", "1");
                map.put("retMsg",map.get("retMsg")+"你的消费金额不足;");
            }
            //提现需消费金额比例（当设置此项时，提现的金额会根据消费的比例来计算提现标准，比如如果设置消费金额比例为0.5，那么分销商如果有1万元钱需要提现的金额时，分销商必须消费5000元钱才能提出1万元钱。如果为0的话，提现金额没有消费金额比例限制，按照提现需消费金额来进行判断。）
            //withdrawal_consumption_amount_proportion
            String proportion =  distributorRedisDao.getDistributionConfKey("withdrawal_consumption_amount_proportion");
            //已提现+未提现 = 可提现金额
            String amountCount = distributionsDao.amountCount(distributorId);
            //已消费比例 = 已消费/(已提现 + 未提现)
            Double ratio = Double.valueOf(selfConsumption)/Double.valueOf(amountCount);
            if(ratio< Double.valueOf(proportion)){
                map.put("retCode", "1");
                map.put("retMsg",map.get("retMsg")+"你的消费金额比例不足;");
            }
            //可提现金额不能小于本次提现金额
            if(Double.valueOf(amountCount)<money){
                map.put("retCode", "1");
                map.put("retMsg","本次提现金额大于可提现金额;");
            }
            result.setResult(map);
            return result;
        } catch (Exception e) {
            log.error("fail to get withdrawalsPreconditions method openStatus by distributorId={}, money={},cause:{}",
                    distributorId, money, Throwables.getStackTraceAsString(e));
            result.setError("withdrawalsPreconditions.method.update.fail");
            map.put("retCode", "1");
            map.put("retMsg", e.getMessage());
            return result;
        }


    }




    @Override
    public Response<String> withdrawalsTimeIntval(){
        Response<String> result = new Response<String>();
        String settlementDays =  distributorRedisDao.getDistributionConfKey("settlement_days");
        result.setResult(settlementDays);
        return result;
    }

    @Override
    public Response<String> getId(Long userId){
        Response<String> result = new Response<String>();
        String id = distributionsDao.getIds(userId);
        result.setResult(id);
        return result;
    }

    @Override
    public Response<Boolean> updateDistributor(Distributions distribution) {
        Response<Boolean> result = new Response<Boolean>();
        boolean returnFlag =  distributionsDao.updateDistributor(distribution);
        result.setResult(returnFlag);
        return result;
    }

    @Override
    public Response<String> getDistributionConfKey(String key) {
        Response<String> result = new Response<String>();
        String proportion =  distributorRedisDao.getDistributionConfKey(key);
        result.setResult(proportion);
        return result;
    }

}