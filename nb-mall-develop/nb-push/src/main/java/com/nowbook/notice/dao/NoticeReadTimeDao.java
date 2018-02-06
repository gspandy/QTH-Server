package com.nowbook.notice.dao;


import com.nowbook.notice.model.NoticeReadTime;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author dpzh
 * @create 2017-08-02 11:46
 * @description: 通知
 **/

@Repository
public class NoticeReadTimeDao extends SqlSessionDaoSupport {

    /**
     * @description: 创建消息已读时间
     * @author dpzh
     * @create 2017/8/9 10:18
     * @param noticeReadTime  传入的已读时间
     * @return:java.lang.Long
     **/
    public Long create(NoticeReadTime noticeReadTime){
        getSqlSession().insert("NoticeReadTimeMapper.create", noticeReadTime);
        return noticeReadTime.getId();
    }

    /**
     * @description: 根据userId和type进行更新阅读时间或者清空时间
     * @author dpzh
     * @create 2017/8/9 10:20
     * @param noticeReadTime <描述>
     * @return:java.lang.Boolean
     **/
    public Boolean updateByUserId(NoticeReadTime noticeReadTime){

        return getSqlSession().update("NoticeReadTimeMapper.updateByUserId", noticeReadTime) == 1;
    }

    /**
     * @description: 根据传入的userId和type查询已读未读时间
     * @author dpzh
     * @create 2017/8/9 10:21
     * @param noticeReadTime <描述>
     * @return:java.util.List<com.nowbook.pushNotice.model.NoticeReadTime>
     **/
    public List<NoticeReadTime> findByToUserId(NoticeReadTime noticeReadTime){
        return getSqlSession().selectList("NoticeReadTimeMapper.findByToUserId",noticeReadTime);

    }

    public List<NoticeReadTime>  findGroupByUserId(NoticeReadTime noticeReadTime){
        return getSqlSession().selectList("NoticeReadTimeMapper.findGroupByUserId",noticeReadTime);
    }


}
