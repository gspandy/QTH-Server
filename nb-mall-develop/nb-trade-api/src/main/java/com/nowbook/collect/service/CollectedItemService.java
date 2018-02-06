package com.nowbook.collect.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.collect.dto.CollectedBar;
import com.nowbook.collect.dto.CollectedItemInfo;
import com.nowbook.collect.dto.CollectedSummary;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-10-10 5:50 PM  <br>
 * Author:cheng
 */
public interface CollectedItemService {

    /**
     * 添加商品收藏记录
     *
     * @param userId    用户id
     * @param itemId    商品id
     */
    Response<CollectedSummary> create(Long userId, Long itemId);


    /**
     * 删除商品收藏记录
     *
     * @param userId    用户id
     * @param itemId    店铺id
     * @return  操作是否成功
     */
    Response<Boolean> delete(Long userId, Long itemId);

    /**
     * 查询用户收藏的商品
     *
     * @param itemName  商品名称，选填，模糊匹配
     * @param pageNo    页码
     * @param size      分页大小
     * @param baseUser  查询用户
     * @return  收藏商品分页
     */
    Response<Paging<CollectedItemInfo>> findBy(@ParamInfo("itemName") @Nullable String itemName,
                                           @ParamInfo("pageNo") @Nullable Integer pageNo,
                                           @ParamInfo("size") @Nullable Integer size,
                                           @ParamInfo("baseUser") BaseUser baseUser);



    /**
     *
     * 批量删除商品收藏记录
     *
     * @param userId    用户id
     * @param itemIds   商品id列表
     * @return  操作是否成功
     */
    Response<Boolean> bulkDelete(Long userId, List<Long> itemIds);


    /**
     * 商品收藏组件(显示在商品详情页)
     *
     * @param itemId    商品id
     * @return 商品id
     */
    @SuppressWarnings("unused")
    Response<Long> getBarOfItem(@ParamInfo("itemId") Long itemId);


    /**
     * 判断商品是否已收藏
     *
     * @param itemId    商品id
     * @param buyerId   买家id
     * @return  用户是否已经收藏这个商品
     */
    Response<CollectedBar> collected(@ParamInfo("itemId") Long itemId,
                                     @ParamInfo("buyerId") Long buyerId);

}
