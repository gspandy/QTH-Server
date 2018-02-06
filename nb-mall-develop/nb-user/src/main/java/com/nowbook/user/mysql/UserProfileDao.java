package com.nowbook.user.mysql;

import com.nowbook.user.model.UserProfile;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-12
 */
@Repository
public class UserProfileDao extends SqlSessionDaoSupport {

    @Nullable
    public UserProfile findByUserId(@Nonnull Long userId) {
        return getSqlSession().selectOne("UserProfile.findByUserId", userId);
    }

    public void create(@Nonnull UserProfile userProfile) {
        getSqlSession().insert("UserProfile.create", userProfile);
    }

    public void updateByUserId(@Nonnull UserProfile userProfile) {
        getSqlSession().update("UserProfile.updateByUserId", userProfile);
    }

    public void deleteByUserId(@Nonnull Long userId) {
        getSqlSession().delete("UserProfile.deleteByUserId", userId);
    }

    public List<UserProfile> findByUserIds(List<Long> userIds) {
        return getSqlSession().selectList("UserProfile.findByUserIds", userIds);
    }

}
