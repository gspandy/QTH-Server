package com.nowbook.item.manager;

import com.nowbook.item.dao.mysql.ItemDao;
import com.nowbook.item.service.RichItems;
import com.nowbook.search.ESClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-11-15
 */
abstract class ItemBaseIndexer {

    @Autowired
    protected ESClient esClient;

    @Autowired
    protected RichItems richItems;

    @Autowired
    protected ItemDao itemDao;
}
