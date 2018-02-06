package com.nowbook.sdp.service;


import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.AmountDetail;
import com.nowbook.sdp.model.UserLevel;
import com.nowbook.sdp.model.UserLevelWait;
import com.nowbook.sdp.model.UserTeamMemberSelect;
import com.nowbook.user.model.User;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

//用户等级关系
public interface UserLevelService {

    Response<Boolean> update();


    Response<UserTeamMemberSelect> selectUser(Long userId);

    Response<Paging<UserTeamMemberSelect>> selectUserLevel(@ParamInfo("userId") @Nullable Long userId,
                                                           @ParamInfo("mobile") @Nullable String mobile,
                                                           @ParamInfo("level") @Nullable Integer level,
                                                           @ParamInfo("type") @Nullable Integer type,
                                                           @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                           @ParamInfo("size") @Nullable Integer pageSize);

    Response<Map<String,Object>> selectUserTeamMember(@ParamInfo("userId") @Nullable Long userId,
                                                                @ParamInfo("level") @Nullable Integer level,
                                                                @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                                @ParamInfo("size") @Nullable Integer pageSize);

    Response<Map<String,Object>> selectUserInviter(@ParamInfo("userId") @Nullable Long userId,
                                                      @ParamInfo("level") @Nullable Integer level,
                                                      @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                      @ParamInfo("size") @Nullable Integer pageSize);
    /**
     * 入会时，通过推荐人来确定自己位置
     * @param userLevel 用户等级信息，必须字段：userId,inviter,level,levelUpAt
     * @return 返回成功还是失败
     */
    Response<Boolean> initiation(UserLevel userLevel);

    /**
     * 升级时，更换团队，并换算钱包里的余额。黑卡升级到合伙人时同时触发黑卡回填机制。
     * @param userLevel 等级用户等级信息，必须字段：userId,level,levelUpAt  level为新的等级
     * @param type 升级类型1：缴费 2：推荐
     * @param userIds 当黑卡升级到合伙人时，这里选3个人来做黑卡回填机制
     * @return 返回成功还是失败
     */
    Response<Boolean> levelUp(UserLevel userLevel, Integer type, String userIds);

    /**
     * 查找自己所推荐的人
     * @param userId 用户Id
     * @param type 查找类型 1：升级时用，查看推荐过同级或同级以上并且入会时间比你升级时间晚的人与总数 2：所有推荐过的人与总数，可以用level确定等级
     * @return 返回黑卡信息
     */
    Response<Map<String,Object>> selectMyInviter(Long userId,Integer level,Integer type,Integer pageNo,Integer pageSize);

    /**
     * 查找自己不同等级的下级
     * @param userId 用户Id
     * @param level 级别
     * @return 返回黑卡信息
     */
    Response<Map<String,Object>> selectTeamMember(Long userId , Integer level, Integer pageNo, Integer pageSize);

    /**
     * 黑卡回填机制，当升级到合伙人时，需要选择被选定3个人所有推荐过的黑卡以及这些黑卡再推荐过的黑卡直到没有为止的总数的一半，变为该合伙人的直属黑卡
     * @param userId 升级到合伙人的用户Id
     * @param userIdList 选择的3个人的Id集合
     * @return 返回成功与失败
     */
    Response<Boolean> backFill(Long userId,List<Long> userIdList);

    /**
     * 查询自己当前等级信息
     * @param userId 用户Id
     * @return 返回用户及各等级信息
     */
    Response<Map<String,Object>> selectMyLevel(Long userId);

    /**
     * 按payCode和payType查询
     * @return 返回是否可以升级
     */
    Response<UserLevelWait> selectByPayCode(UserLevelWait userLevelWait);

    /**
     * 申请升级，并查询是否已提交过申请
     * @param userLevelWait 用户升级信息
     * @return 返回是否可以升级
     */
    Response<String> applyLevelUp(UserLevelWait userLevelWait);

    /**
     * 查询是否满足推荐升级条件
     * @param userId 用户Id
     * @return 返回是否可以升级
     */
    Response<Map<String,Object>> selectIsContentLevelUp(Long userId);

    /**
     * 升级！
     */
    void userLevelUp();
}