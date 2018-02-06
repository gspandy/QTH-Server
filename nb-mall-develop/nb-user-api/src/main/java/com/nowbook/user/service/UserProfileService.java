package com.nowbook.user.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.dto.UserProfileDto;
import com.nowbook.user.model.UserProfile;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-07-17
 */
public interface UserProfileService {

    Response<Long> createUserProfile(UserProfile userProfile);

    Response<UserProfile> findUserProfileByUserId(Long userId);

    Response<UserProfileDto> findUserProfileByUser(@ParamInfo("baseUser") BaseUser baseUser);

    Response<Boolean> updateUserProfileByUserId(UserProfile userProfile);

    Response<Boolean> deleteUserProfileByUserId(Long userId);
}
