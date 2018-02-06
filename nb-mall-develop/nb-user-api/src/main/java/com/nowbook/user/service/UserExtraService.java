package com.nowbook.user.service;

import com.nowbook.common.model.Response;
import com.nowbook.user.model.UserExtra;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * Created by yangzefeng on 14-3-4
 */
public interface UserExtraService {

    Response<UserExtra> findById(Long id);

    Response<UserExtra> findByUserId(Long userId);

    Response<Long> create(UserExtra userExtra);

    Response<Boolean> updateByUserId(UserExtra userExtra);

    Response<Long> increaseUserTradeInfo(Long buyerId, Long fee);

    public Response<Boolean> bulkInsertOrReplace(List<UserExtra> userExtras);

    /**
     * increment update. 加上原交易总数总额且 update
     * @param userExtras    传入将要升级的对象列表
     */
    public Response<Boolean> bulkInsertOrUpdate(List<UserExtra> userExtras);
    /**
     * @description:  上传头像
     * @param fileName    图片名字
     * @param  file        图片文件
     * @param  userId      用户id
     */
    Response<String> uploadUserAvatar(String fileName, MultipartFile file, Long userId);

}
