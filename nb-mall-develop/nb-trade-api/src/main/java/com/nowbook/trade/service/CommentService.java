/*
 * Copyright (c) 2012 大连锦霖科技有限公司
 */

package com.nowbook.trade.service;

import com.nowbook.common.model.Paging;
import com.nowbook.trade.model.Comment;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2012-10-30
 */
public interface CommentService {

    void create(Comment comment);

    void update(Comment comment);

    void delete(Comment comment);

    Comment findById(Long id);

    Paging<Comment> findByTargetTypeAndTargetId(Integer offset, Integer limitN, Integer targetType, Long targetId);
}
