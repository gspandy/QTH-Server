package com.nowbook.item.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.item.model.SpuBundle;
import com.nowbook.user.base.BaseUser;

import java.util.List;
import java.util.Map;

/**
 * 组合套餐模版服务接口
 * CREATED BY: IntelliJ IDEA
 * AUTHOR: haolin
 * ON: 14-4-21
 */
public interface SpuBundleService {
    /**
     * 创建套餐模版
     * @param sb 套餐模版对象
     * @return Response对象
     */
    Response<Long> create(SpuBundle sb);

    /**
     * 更新套餐模版
     * @param sb 套餐模版对象
     * @return Response对象
     */
    Response<Boolean> update(SpuBundle sb);

    /**
     * 物理删除套餐模版
     * @param sbId 套餐模版对象
     * @return Response对象
     */
    Response<Boolean> delete(Long sbId);

    /**
     * 上架或下架套餐模版
     * @param sbId 套餐模版主键
     * @param status 套餐模版状态
     * @return Response对象
     */
    Response<Boolean> onOff(Long sbId, SpuBundle.Status status);

    /**
     * 增加套餐模版使用次数
     * @param sbId 套餐模版主键
     * @return Response对象
     */
    Response<Boolean> incrUsedCount(Long sbId);

    /**
     * 根据主键查找套餐模版
     * @param sbId 套餐模版主键
     * @return 套餐模版
     */
    Response<SpuBundle> findById(Long sbId);

    /**
     * 分页查询套餐模版
     * @param criterion 查询条件
     * @param pageNo 页数
     * @param pageSize 分页大小
     * @return 套餐模版分页对象
     */
    Response<Paging<SpuBundle>> finds(@ParamInfo("baseUser") BaseUser user,
                                      @ParamInfo("criterion") Map<String, Object> criterion,
                                      @ParamInfo("pageNo") Integer pageNo,
                                      @ParamInfo("pageSize") Integer pageSize);

    /**
     * 根据用户id列表 找用户模板
     * @param userIds  用户id列表
     * @param pageNo   页码
     * @param pageSize 分页大小
     * @return  套餐模板分页对象
     */
    Response<Paging<SpuBundle>> findByUserIds(List<Long> userIds, Integer pageNo, Integer pageSize);
}
