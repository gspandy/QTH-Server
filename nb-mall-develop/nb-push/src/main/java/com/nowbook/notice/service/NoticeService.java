package com.nowbook.notice.service;



import com.nowbook.notice.model.Express;
import com.nowbook.notice.model.Notice;
import com.nowbook.notice.model.NoticeReadTime;

import java.util.List;
import java.util.Map;

/**
 * @author dpzh
 * @create 2017-08-02 11:45
 * @description: 通知
 **/
public interface NoticeService {

    /**
     * @description: 存储通知消息
     * @author dpzh
     * @create 2017/8/2 15:27
     * @param notice 消息对象
     * @return: 返回该条消息的id
     **/
    public Long create(Notice notice);

    /**
     * @description: 通过id获取消息
     * @author dpzh
     * @create 2017/8/2 15:28
     * @param id 通知id
     * @return: Notice 通知对象
     **/
    public Notice get(Long id);

    /**
     * @description: 通过用户id获取该用户的所有通知
     * @author dpzh
     * @create 2017/8/2 15:30
     * @param notice 用户id
     * @return:java.util.List<com.nowbook.pushNotice.model.Notice>
     **/
    public List<Notice> findByToUserId(Notice notice);

    /**
     * @description: 删除通知
     * @author dpzh
     * @create 2017/8/2 15:31
     * @param id 用户id
     * @return:boolean
     **/
    public boolean delete(Long id);

    /**
     * @description: 更新通知
     * @author dpzh
     * @create 2017/8/2 15:32
     * @param notice 通知对象
     * @return:java.lang.Boolean
     **/
    public Boolean update(Notice notice);

    /**
     * @description: 批量删除
     * @author dpzh
     * @create 2017/8/2 15:32
     * @param ids 通知id  多个id时，以“_”间隔
     * @return: Integer
     **/
    public Integer batchDelete(String ids);



    /**
     * @description: 信息已读时间
     * @author dpzh
     * @create 2017/8/9 10:50
     * @param noticeReadTime
     * @return:
     **/
    public void readTime(NoticeReadTime noticeReadTime);

    /**
     * @description: 更新清空时间
     * @author dpzh
     * @create 2017/8/9 10:50
     * @param noticeReadTime
     * @return:
     **/
    public void clearanceTime(NoticeReadTime noticeReadTime);

    /**
     * @description: 消息列表（分类）
     * @author dpzh
     * @create 2017/8/9 13:30
     * @param userId 用户id
     * @return:
     **/
    public List<Map<String,String>>  countNotices(Long userId);


    /**
     * @description: 首页显示消息数
     * @author dpzh
     * @create 2017/8/9 14:11
     * @param userId 用户id
     * @return:
     **/
    public String homeCount(Long userId);



    /**
     * @description: 发出推送
     * @author dpzh
     * @create 2017/8/18 11:11
     * @return:
     **/
    public void pushNotice(Notice notice);

    /**
     * @description: 快递100（Express100）回调之后数据处理
     * @author dpzh
     * @create 2017/8/18 11:11
     * @return:
     **/
    public void pushExpress(Express express);




}
