package com.nowbook.sdp.service;

import com.nowbook.common.model.Response;

public interface DistributorsAuditService {

    /**
     * 更新审核状态
     * @param id 配送方式id
     * @param auditStatus 审核更新状态（1通过，0未通过）
     * @return 是否成功
     */
    public Response<Boolean> updateAuditStatus(Long id, String auditStatus);

}
