package com.nowbook.sdp.service;

import com.nowbook.sdp.dao.LevelDao;
import com.nowbook.sdp.model.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author dpzh
 * @create 2017-07-26 10:18
 * @description: levelService
 **/
@Service
public class LevelServiceImpl implements LevelService{

    @Autowired
    private LevelDao levelDao;

    // 修改会员设置
    @Override
    public void updateById(Level level) {
        levelDao.updateById(level);
    }

    /**
     * @description: 查询折扣
     * @author dpzh
     * @create 2017/7/26 10:20
     * @param level
     * @return:java.util.List<com.nowbook.sdp.model.Level>
     **/
    public List<Level> selectByLevel(Level level){
        return levelDao.selectByLevel(level);
    }
    /**
     * @description: 根据用户id查询折扣
     * @author dpzh
     * @create 2017/7/26 10:20
     * @param userId 用户id
     * @return:java.util.List<com.nowbook.sdp.model.Level>
     **/
    public List<Level> selectByUserId(Long userId){
        return levelDao.selectByUserId(userId);
    }

}
