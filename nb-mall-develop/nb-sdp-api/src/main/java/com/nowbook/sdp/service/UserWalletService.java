package com.nowbook.sdp.service;


import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.UserWallet;
import com.nowbook.sdp.model.UserWalletSummary;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

//用户钱包关系
public interface UserWalletService {

    /**
     * 用户查看钱包数据
     * @param userId 用户ID
     * @return 返回成功还是失败
     */
    Response<UserWallet> selectUserWallet(Long userId);

    /**
     * 用户查看钱包明细
     * @param userWalletSummary 用户ID
     * @return 返回成功还是失败
     */
    Response<Map<String,Object>> selectUserWalletSummary(UserWalletSummary userWalletSummary);

    /**
     * 用户查看钱包明细
     * @return 返回成功还是失败
     */
    Response<Map<String,Object>> selectUserWalletSummaryForAdmin(@ParamInfo("userId") @Nullable Long userId,
                                                         @ParamInfo("type") @Nullable String type,
                                                         @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                         @ParamInfo("size") @Nullable Integer pageSize);

    /**
     * 用户更改钱包数据
     * @param userWalletSummary 收入支付类型，奇数全部都是增加，偶数全部都是减少。
     * @return 返回成功还是失败
     */
    Response<Boolean> updateUserWallet(UserWalletSummary userWalletSummary);

    /**
     * 按流水号查钱包明细
     * @param userWalletSummary 需要payCode 和 payType
     * @return 返回成功还是失败
     */
    Response<UserWalletSummary> selectByPayCode(UserWalletSummary userWalletSummary);

    /**
     * 用户收益和奖金的获得
     * @param userWalletSummary moneyType = 1：自营，type = 2：优选, type = 3: 自定义, type = 4: 入会或者升级。
     * @return
     */
    Response<Boolean> profit (UserWalletSummary userWalletSummary);

    /**
     * 用户支付接口
     * @param userId
     * @param type type 1余额  2预存款
     * @param money 订单总价，含邮费
     * @param deliverFee 邮费
     * @return
     */
    Response<Boolean> pay (Long userId ,Integer type ,Long money,Long deliverFee);
}