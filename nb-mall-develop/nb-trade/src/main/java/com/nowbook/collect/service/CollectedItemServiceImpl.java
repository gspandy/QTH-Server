package com.nowbook.collect.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.collect.dao.CollectedItemDao;
import com.nowbook.collect.dto.CollectedBar;
import com.nowbook.collect.dto.CollectedItemInfo;
import com.nowbook.collect.dto.CollectedSummary;
import com.nowbook.collect.manager.CollectedManager;
import com.nowbook.collect.model.CollectedItem;
import com.nowbook.common.model.PageInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.item.model.Item;
import com.nowbook.item.service.ItemService;
import com.nowbook.item.service.LevelPriceService;
import com.nowbook.sdp.model.Level;
import com.nowbook.sdp.service.LevelService;
import com.nowbook.user.base.BaseUser;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nowbook.user.base.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.nowbook.common.utils.Arguments.isNull;
import static com.nowbook.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-10-10 6:26 PM  <br>
 * Author:cheng
 */
@Slf4j
@Service
public class CollectedItemServiceImpl implements CollectedItemService {

    @Autowired
    private CollectedManager collectedManager;

    @Autowired
    private CollectedItemDao collectedItemDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private LevelService levelService;

    @Autowired
    private LevelPriceService levelPriceService;


    /**
     * 添加商品收藏记录
     *
     * @param userId 用户id
     * @param itemId 商品id
     * @return 操作是否成功
     */
    @Override
    public Response<CollectedSummary> create(Long userId, Long itemId) {
        Response<CollectedSummary> result = new Response<CollectedSummary>();

        try {
            CollectedItem collectedItem = new CollectedItem();
            collectedItem.setBuyerId(userId);
            collectedItem.setItemId(itemId);

            Response<Item> itemResponse = itemService.findById(itemId);
            checkState(itemResponse.isSuccess(), itemResponse.getError());

            Item item = itemResponse.getResult();
            collectedItem.setItemNameSnapshot(item.getName());

            collectedManager.createCollectedItem(collectedItem);

            Long total = collectedItemDao.countOf(userId);
            result.setResult(new CollectedSummary(collectedItem.getId(), total));

        } catch (IllegalArgumentException e) {
            log.error("fail to create collect item whit userId:{}, itemId:{}, error:{}",
                    userId, itemId, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to create collect item whit userId:{}, itemId:{}, error:{}",
                    userId, itemId, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to create collect item whit userId:{}, itemId:{}, cause:{}",
                    userId, itemId, Throwables.getStackTraceAsString(e));
            result.setError("collected.item.create.fail");
        }

        return result;
    }

    /**
     * 删除商品收藏记录
     *
     * @param userId 用户id
     * @param itemId 店铺id
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> delete(Long userId, Long itemId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            CollectedItem deleting = collectedItemDao.getByUserIdAndItemId(userId, itemId);
            if (notNull(deleting)) {
                collectedItemDao.delete(deleting.getId());
            }

            result.setResult(Boolean.TRUE);

        } catch (Exception e) {
            log.error("fail to delete collected item with userId:{}, itemId:{}, cause:{}",
                    userId, itemId, Throwables.getStackTraceAsString(e));
            result.setError("collected.item.delete.fail");
        }
        return result;
    }

    /**
     * 查询用户收藏的商品
     *
     * @param itemName 商品名称，选填，模糊匹配
     * @param baseUser 查询用户
     * @return 收藏商品分页
     */
    @Override
    public Response<Paging<CollectedItemInfo>> findBy(@ParamInfo("itemName") @Nullable String itemName,
                                                  @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                  @ParamInfo("size") @Nullable Integer size,
                                                  @ParamInfo("baseUser") BaseUser baseUser) {

        Response<Paging<CollectedItemInfo>> result = new Response<Paging<CollectedItemInfo>>();

        try {

            PageInfo pageInfo = new PageInfo(pageNo, size);

            CollectedItem criteria = new CollectedItem();
            criteria.setItemNameSnapshot(itemName);
            criteria.setBuyerId(baseUser.getId());
            Paging<CollectedItem> paging = collectedItemDao.findBy(criteria, pageInfo.offset, pageInfo.limit);
            result.setResult(appendItemDetail(paging));


        } catch (Exception e) {
            log.error("fail to query collect items by itemName:{}, pageNo:{}, size:{}, uid:{} cause:{}",
                    itemName, pageNo, size, baseUser.getId(), Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(CollectedItemInfo.class));
        }

        return result;
    }


    private Paging<CollectedItemInfo> appendItemDetail(Paging<CollectedItem> paging) {
        List<CollectedItem> collectedItems = paging.getData();
        List<CollectedItemInfo> dtos = Lists.newArrayListWithCapacity(collectedItems.size());
        List<Long> ids = Lists.newArrayListWithCapacity(collectedItems.size());

        for (CollectedItem item : collectedItems) {
            ids.add(item.getItemId());
        }

        Response<List<Item>> itemRes = itemService.findByIds(ids);
        checkState(itemRes.isSuccess(), itemRes.getError());
        BaseUser user=new BaseUser();
        user= UserUtil.getCurrentUser();
        Integer level=0;
        if(user!=null){
            List<Level> levels=levelService.selectByUserId(user.getId()); //根据用户id获取用户的会员等级
            if(levels.size()>0){
                for(Level lev:levels){
                    level=lev.getLevel();
                }
            }
        }
        for(Item item:itemRes.getResult()){
            if(user!=null){
                //通过item获取对应的各个等级价格 2017-09-27 dpzh
                Integer price=levelPriceService.getUserLevelPrice(item);
                item .setSalePrice(price);
                if(item.getPriceType()==2){
                    item.setPrice(item.getSellingPrice());
                }if(item.getPriceType()==3){
                    item.setPrice(item.getCustomPrice());
                }

            }else {
                item.setSalePrice(item.getPrice());
            }
        }

        Map<Long, Item> mappedItems = convertToMappedItems(itemRes.getResult());

        for (CollectedItem collectedItem : collectedItems) {
            dtos.add(CollectedItemInfo.transform(collectedItem, mappedItems.get(collectedItem.getItemId())));
            ids.add(collectedItem.getId());
        }

        return new Paging<CollectedItemInfo>(paging.getTotal(), dtos);
    }


    private Map<Long, Item> convertToMappedItems(List<Item> items) {
        Map<Long, Item> mappedItems = Maps.newHashMap();
        for (Item item : items) {
            mappedItems.put(item.getId(), item);
        }

        return mappedItems;
    }


    /**
     * 批量删除商品收藏记录
     *
     * @param userId  用户id
     * @param itemIds 商品id列表
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> bulkDelete(Long userId, List<Long> itemIds) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            collectedManager.bulkDeleteCollectedItems(userId, itemIds);
            result.setResult(Boolean.TRUE);
        } catch (IllegalArgumentException e) {
            log.error("fail to delete collected items with userId:{}, itemIds:{}, error:{}",
                    userId, itemIds, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to delete collected items with userId:{}, itemIds:{}, error:{}",
                    userId, itemIds, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to delete collected items with userId:{}, itemIds:{}, cause:{}",
                    userId, itemIds, Throwables.getStackTraceAsString(e));
            result.setError("collected.item.delete.fail");
        }

        return result;
    }

    /**
     * 商品收藏组件(显示在商品详情页)
     *
     * @param itemId    商品id
     * @return 商品收藏组件所需信息
     */
    @Override
    public Response<Long> getBarOfItem(@ParamInfo("itemId") Long itemId) {
        Response<Long> result = new Response<Long>();

        try {
            result.setResult(itemId);
        } catch (Exception e) {
            log.error("fail to get bar with itemId:{},  cause:{}", itemId, Throwables.getStackTraceAsString(e));
        }
        return result;
    }


    @Override
    public Response<CollectedBar> collected(@ParamInfo("itemId") Long itemId,
                                            @ParamInfo("buyerId") Long buyerId) {
        Response<CollectedBar> result = new Response<CollectedBar>();

        try {
            if (isNull(buyerId)) {
                result.setResult(new CollectedBar());
                return result;
            }

            CollectedItem collectedItem = collectedItemDao.getByUserIdAndItemId(buyerId, itemId);
            Boolean hasCollected = notNull(collectedItem) ? Boolean.TRUE : Boolean.FALSE;
            result.setResult(new CollectedBar(itemId, hasCollected));

        } catch (Exception e) {
            log.error("fail to get bar with itemId:{}, buyer:{}, cause:{}",
                    itemId, buyerId, Throwables.getStackTraceAsString(e));
            // 加载失败不影响整个详情页面的渲染
            result.setResult(new CollectedBar());
        }

        return result;
    }
}
