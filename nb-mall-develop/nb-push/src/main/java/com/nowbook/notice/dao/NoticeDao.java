package com.nowbook.notice.dao;

import com.nowbook.notice.model.Notice;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author dpzh
 * @create 2017-08-02 11:46
 * @description: 通知
 **/

@Repository
public class NoticeDao extends SqlSessionDaoSupport {

    /**
     * @description: 存储通知消息
     * @author dpzh
     * @create 2017/8/2 15:27
     * @param notice 消息对象
     * @return: 返回该条消息的id
     **/
    public Long create(Notice notice){
        getSqlSession().insert("NoticeMapper.create", notice);
        return notice.getId();
    }

    /**
     * @description: 通过id获取消息
     * @author dpzh
     * @create 2017/8/2 15:28
     * @param id 通知id
     * @return: Notice 通知对象
     **/
    public Notice get(Long id) {
        return getSqlSession().selectOne("NoticeMapper.get", id);
    }

    /**
     * @description: 通过用户id获取该用户的所有通知
     * @author dpzh
     * @create 2017/8/2 15:30
     * @param notice
     * @return:java.util.List<com.nowbook.pushNotice.model.Notice>
     **/
    public List<Notice> findByToUserId(Notice notice) {
        return getSqlSession().selectList("NoticeMapper.findByToUserId",notice);
    }

    /**
     * @description: 删除通知
     * @author dpzh
     * @create 2017/8/2 15:31
     * @param id 用户id
     * @return:boolean
     **/
    public boolean delete(Long id) {
        return getSqlSession().delete("NoticeMapper.delete", id) == 1;
    }

    /**
     * @description: 更新通知
     * @author dpzh
     * @create 2017/8/2 15:32
     * @param notice 通知对象
     * @return:java.lang.Boolean
     **/

    public Boolean update(Notice notice) {
        notice.setUpdatedAt(new Date());
        return getSqlSession().update("NoticeMapper.update", notice) == 1;
    }


    /**
     * @description: 统计各个type状态的个数
     * @author dpzh
     * @create 2017/8/4 9:50
     * @return:java.util.List<com.nowbook.pushNotice.model.Notice>
     **/
    public List<Notice> countByType(Notice notice){

        return getSqlSession().selectList("NoticeMapper.countByType",notice);
    }

    /**
     * @description: 统计信息总条数
     * @author dpzh
     * @create 2017/8/4 9:50
     * @param notice 通知对象
     * @return:java.util.List<com.nowbook.pushNotice.model.Notice>
     **/
    public String countAll(Notice notice){
        Notice noti=getSqlSession().selectOne("NoticeMapper.countAll",notice);
        String count=noti.getCount();
        return count;
    }

    /**
     * @description: 获取钱包明显的未读消息
     * @author dpzh
     * @create 2017/8/9 11:24
     * @param notice 通知对象
     * @return:java.util.List<com.nowbook.pushNotice.model.Notice>
     **/
    public String countWalletByUserId(Notice notice){
        Notice noti=getSqlSession().selectOne("NoticeMapper.countWalletByUserId",notice);
        String count=noti.getCount();
        return count;
    }

    /**
     * @description: 通过大订单号获取该订单下面的小订单
     * @author dpzh
     * @create 2017/8/21 17:44
     * @param orderId <描述>
     * @return:java.util.List<com.nowbook.notice.model.Notice>
     **/
    public List<Notice> getItemImageByOrderId(Long orderId){
        return getSqlSession().selectList("NoticeMapper.getItemImageByOrderId",orderId);
    }

    public List<Notice> getNoticeByExpressNo(Notice notice){
        return getSqlSession().selectList("NoticeMapper.getNoticeByExpressNo",notice);
    }

    public Notice getUserByOrderId(Long expressNo){
        return getSqlSession().selectOne("NoticeMapper.getUserByOrderId", expressNo);
    }


}
