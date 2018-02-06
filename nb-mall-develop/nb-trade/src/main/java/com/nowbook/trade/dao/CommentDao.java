/*
 * Copyright (c) 2012 大连锦霖科技有限公司
 */

package com.nowbook.trade.dao;

import com.nowbook.trade.model.Comment;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2012-10-30
 */
@Repository
public class CommentDao extends SqlSessionDaoSupport {
    public Comment findById(Long id) {
        return getSqlSession().selectOne("Comment.findById", id);
    }

    public List<Comment> findByTargetTypeAndTargetId(Integer targetType, Long targetId, Integer offset, Integer limit) {
        return getSqlSession().selectList("Comment.findByTargetTypeAndTargetId", ImmutableMap.of("targetType", targetType,
                "targetId", targetId, "offset", offset, "limit", limit));
    }

    public Integer countOf(Integer targetType, Long targetId) {
        Integer count = getSqlSession().selectOne("Comment.countOf", ImmutableMap.of("targetType", targetType,
                "targetId", targetId));
        return Objects.firstNonNull(count, 0);
    }

    public void create(Comment comment) {
        getSqlSession().insert("Comment.create", comment);
    }

    public void delete(Long id) {
        getSqlSession().delete("Comment.delete", id);
    }

    public void update(Comment comment) {
        getSqlSession().update("Comment.update", comment);
    }
}
