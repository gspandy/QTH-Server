package com.nowbook.sdp.service;

import com.nowbook.common.model.Response;
import com.nowbook.sdp.dao.DistributorsAuditDao;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by mark on 2014/7/11.
 */
@Service
public class DistributorsAuditServiceImpl implements DistributorsAuditService {

    private final static Logger log = LoggerFactory.getLogger(DistributorsAuditServiceImpl.class);

   @Autowired
   private DistributorsAuditDao distributorsAuditDao;

    @Override
    public Response<Boolean> updateAuditStatus(Long id, String auditStatus) {
        Response<Boolean> result = new Response<Boolean>();

        if(id == null || auditStatus == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            distributorsAuditDao.updateAuditStatus(id, auditStatus);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update distributorsAudit method auditStatus by id={}, auditStatus={},cause:{}",
                    id, auditStatus, Throwables.getStackTraceAsString(e));
            result.setError("distributorsAudit.method.update.fail");
            return result;
        }
    }
}
