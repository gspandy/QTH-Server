package com.nowbook.sdp.service;

import com.nowbook.sdp.model.Level;

import java.util.List;

/**
 * @author dpzh
 * @create 2017-07-26 10:22
 * @description: levelService
 **/
public interface LevelService {
    void updateById(Level level);
    public List<Level> selectByLevel(Level level);
    public List<Level> selectByUserId(Long userId);
}
