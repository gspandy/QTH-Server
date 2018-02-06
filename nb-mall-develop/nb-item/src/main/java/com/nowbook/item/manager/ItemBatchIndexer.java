package com.nowbook.item.manager;

import com.nowbook.item.dto.RichItem;
import com.nowbook.item.model.Item;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.nowbook.item.service.LevelPriceService;
import com.nowbook.sdp.dao.LevelDao;
import com.nowbook.sdp.model.Level;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 全量或者增量dump item
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-11-15
 */
@Component
public class ItemBatchIndexer extends ItemBaseIndexer {

    private static final Logger log = LoggerFactory.getLogger(ItemBatchIndexer.class);

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static final String ITEM_INDEX_NAME = "items";
    public static final String ITEM_INDEX_TYPE = "item";

    private static final int PAGE_SIZE = 200;
    private static final RichItem DUMB = new RichItem();


    @Autowired
    private LevelPriceService levelPriceService;

    /**
     * 全量dump搜索引擎
     */
    public void fullDump() {
        log.info("[FULL_DUMP_ITEM] full item refresh start");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Long lastId = itemDao.maxId() + 1;  //scan from maxId+1
        int returnSize = PAGE_SIZE;
        int handled = 0;
        while (returnSize == PAGE_SIZE) {
            List<Item> items = itemDao.forDump(lastId, PAGE_SIZE);
            final List<Long> invalidIds = Lists.newArrayList();
            if (!items.isEmpty()) {

                //handle all status except deleted items
                Iterable<RichItem> valid = filterValidItems(items, invalidIds);
                esClient.index(ITEM_INDEX_NAME,ITEM_INDEX_TYPE, valid);
                esClient.delete(ITEM_INDEX_NAME,ITEM_INDEX_TYPE, invalidIds);

                handled += items.size();
                lastId = items.get(items.size() - 1).getId();
                log.info("has indexed {} items,and last handled id is {}", handled, lastId);
                returnSize = items.size();
            } else {
                break;
            }
        }
        stopwatch.stop();
        log.info("[FULL_DUMP_ITEM] full item refresh end, took {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }


    public void deltaDump(Integer interval) {
        log.info("[DELTA_DUMP_ITEM] item delta dump start");

        String compared = DATE_TIME_FORMAT.print(new DateTime().minusMinutes(interval));
        Stopwatch stopwatch = Stopwatch.createStarted();
        Long lastId = itemDao.maxId() + 1;  //scan from maxId+1
        int returnSize = PAGE_SIZE;
        int handled = 0;
        while (returnSize == PAGE_SIZE) {
            List<Item> items = itemDao.forDeltaDump(lastId, compared, PAGE_SIZE);
            final List<Long> invalidIds = Lists.newArrayList();
            if (!items.isEmpty()) {
                //handle all status except deleted items
                Iterable<RichItem> valid = filterValidItems(items, invalidIds);
                esClient.index(ITEM_INDEX_NAME,ITEM_INDEX_TYPE, valid);
                esClient.delete(ITEM_INDEX_NAME,ITEM_INDEX_TYPE, invalidIds);

                handled += items.size();
                lastId = items.get(items.size() - 1).getId();
                log.info("has indexed {} items,and last handled id is {}", handled, lastId);
                returnSize = items.size();
            } else {
                break;
            }
        }
        stopwatch.stop();
        log.info("[DELTA_DUMP_ITEM] item delta finished,cost {} millis,handled {} items", stopwatch.elapsed(TimeUnit.MILLISECONDS), handled);
    }


    private Iterable<RichItem> filterValidItems(List<Item> items, final List<Long> invalidIds) {
        //上全文前，通过item获取对应的各个等级价格 2017-09-27 dpzh
        if(items.size()>0){
            for(Item item:items){
                if(item.getPriceType()==1||item.getPriceType()==2){
                    item=  levelPriceService.getItemLevelPrice(item);
                    if(item.getPriceType()==2){
                        item.setPrice(item.getSellingPrice());
                    }
                }else if(item.getPriceType()==3){
                    item.setQuasiAngelPrice(item.getAngelPrice());
                    item.setComPrice(item.getPartnerPrice());
                    item.setPrice(item.getCustomPrice());
                }
            }
        }

        return FluentIterable.from(items).filter(new Predicate<Item>() {
            @Override
            public boolean apply(Item input) {
                return Item.Status.DELETED != Item.Status.fromNumber(input.getStatus());
            }
        }).transform(new Function<Item, RichItem>() {
            @Override
            public RichItem apply(Item input) {
                try {
                    return richItems.make(input);
                } catch (Exception e) {
                    log.error("can not make rich item for item (id={}), cause:{}", input.getId(), Throwables.getStackTraceAsString(e));
                    invalidIds.add(input.getId());
                    return DUMB;
                }
            }
        }).filter(new Predicate<RichItem>() {
            @Override
            public boolean apply(RichItem input) {
                return input.getId() != null;
            }
        });
    }


}
