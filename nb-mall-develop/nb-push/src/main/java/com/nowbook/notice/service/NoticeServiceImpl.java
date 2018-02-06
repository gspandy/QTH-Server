package com.nowbook.notice.service;

import com.google.common.base.Splitter;

import com.nowbook.notice.dao.NoticeDao;
import com.nowbook.notice.dao.NoticeReadTimeDao;
import com.nowbook.notice.model.*;
import com.nowbook.push.jpush.model.PushBean;
import com.nowbook.push.pushserver.PushServer;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.model.LoginInfo;
import com.nowbook.user.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author dpzh
 * @create 2017-08-02 11:48
 * @description: 通知
 **/
@Service
public class NoticeServiceImpl implements NoticeService{

    @Autowired
    private NoticeDao noticeDao;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PushServer pushServer;

    @Autowired
    private NoticeReadTimeDao noticeReadTimeDao;

    private final static Splitter splitter = Splitter.on("_").omitEmptyStrings().trimResults();
    @Override
    public Long create(Notice notice){
        return noticeDao.create(notice);
    }

    @Override
    public Notice get(Long id) {
        return noticeDao.get(id);
    }


    /**
     * @description: 查询该用户的所有通知
     * @author dpzh
     * @create 2017/8/22 13:22
     * @param notice <描述>
     * @return:java.util.List<com.nowbook.notice.model.Notice>
     **/
    @Override
    public List<Notice> findByToUserId(Notice notice) {

        Integer pageNo=notice.getPageNo();
        Integer pageSize=notice.getPageSize();
        if(pageNo == null || pageNo <=0){
            pageNo =1;
        }
        if(pageSize == null || pageSize <=0){
            pageSize =10;
        }
        Integer offset = (pageNo-1)*pageSize;
        notice.setLimit(pageSize);
        notice.setOffset(offset);
        //查询清空时间，统计数据时只查清空时间之后的
        NoticeReadTime noticeReadTime=new NoticeReadTime();
        noticeReadTime.setType(notice.getType());
        noticeReadTime.setUserId(notice.getToUser());
        List<NoticeReadTime> nrtList=noticeReadTimeDao.findByToUserId(noticeReadTime);
        if(nrtList.size()>0){
            for(NoticeReadTime nrt:nrtList){
                if(nrt.getClearanceTime()!=null){
                    notice.setCreatedAt(nrt.getClearanceTime());
                }
            }
        }
        readTime(noticeReadTime);
        List<Notice> noticeList=noticeDao.findByToUserId(notice);
        List<Notice> list=new ArrayList<Notice>();
        if(noticeList.size()>0){
            for (Notice nt:noticeList){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String time= sdf.format(nt.getCreatedAt());
                nt.setCreatedTime(time);
                nt.setCreatedAt(null);
                nt.setUpdatedAt(null);
                if(notice.getType()==0){
                    BaseUser baseUser=new BaseUser();
                    baseUser.setId(nt.getToUser());
                    List<Notice> itemList=noticeDao.getItemImageByOrderId(nt.getBusinessId());
                    if(itemList.size()>0){
                        String itemImage=itemList.get(0).getItemImage();
                        Integer itemSize=itemList.size();
                        nt.setItemImage(itemImage);
                        nt.setItemSize(itemSize);
                    }

                }
                list.add(nt);
            }
            return list;
        }else {
            return noticeList ;
        }



    }

    //删除通知
    public boolean delete(Long id) {
        return noticeDao.delete(id);
    }

    //批量删除通知
    public Boolean update(Notice notice) {
        return noticeDao.update(notice);
    }



    //批量删除
    @Override
    public Integer batchDelete(String ids){
        Integer num=0;
        Notice notice=new Notice();
        List<String> idList = splitter.splitToList(ids);
        for(String id:idList){
            Long noticeId = Long.valueOf(id).longValue();
            noticeDao.delete(noticeId);
            num++;
        }
        return num;
    }



    //点击已读时间
    @Override
    public void readTime(NoticeReadTime noticeReadTime){
        List<NoticeReadTime> list=noticeReadTimeDao.findByToUserId(noticeReadTime);
        if(list.size()>0){
            noticeReadTime.setClickTime(new Date());
            noticeReadTimeDao.updateByUserId(noticeReadTime);
        }else {
            noticeReadTime.setClickTime(new Date());
            noticeReadTimeDao.create(noticeReadTime);
        }
    }

    //更新清空时间
    @Override
    public void clearanceTime(NoticeReadTime noticeReadTime){
        noticeReadTime.setClearanceTime(new Date());
        noticeReadTimeDao.updateByUserId(noticeReadTime);
    }

    @Override
    public String homeCount(Long userId){
        Notice notice=new Notice();
        notice.setToUser(userId);
        NoticeReadTime noticeReadTime=new NoticeReadTime();
        noticeReadTime.setUserId(userId);
        List<NoticeReadTime> readTimeList=noticeReadTimeDao.findGroupByUserId(noticeReadTime);
        String countNotice="";
        String countWallet="";
        String countOrder="";
        Date clearNoticeTime=null;
        Date clearWalletTime=null;
        Date clearOrderTime=null;
        for(NoticeReadTime nrt:readTimeList){
            if(nrt.getType()==0){
                clearNoticeTime=nrt.getClickTime();
            }else if (nrt.getType()==1){
                clearWalletTime=nrt.getClickTime();
            }else if (nrt.getType()==2){
                clearOrderTime=nrt.getClickTime();
            }
        }
        notice.setType(0);
        notice.setCreatedAt(clearNoticeTime);
        countNotice=noticeDao.countAll(notice);
        notice.setCreatedAt(clearWalletTime);
        notice.setType(1);
        countWallet=noticeDao.countAll(notice);
        notice.setCreatedAt(clearOrderTime);
        notice.setType(2);
        countOrder=noticeDao.countAll(notice);
        Integer sum=0;
        Integer cn=Integer.valueOf(countNotice);
        Integer cw=Integer.valueOf(countWallet);
        Integer co=Integer.valueOf(countOrder);
        sum=cn+cw+co;
        return sum.toString();
    }


    @Override
    public List<Map<String,String>>  countNotices(Long userId){
        List<Map<String,String>> list=new ArrayList<Map<String, String>>();
        NoticeReadTime noticeReadTime=new NoticeReadTime();
        noticeReadTime.setUserId(userId);
        List<NoticeReadTime> readTimeList=noticeReadTimeDao.findGroupByUserId(noticeReadTime);
        Date clickNoticeTime=null;        //通知消息已读时间
        Date clearNoticeTime=null;        //通知消息清空时间

        Date clickWalletTime=null;        //资产消息已读时间
        Date clearWalletTime=null;        //资产消息清空时间

        Date clickOrderTime=null;        //订单消息已读时间
        Date clearOrderTime=null;        //订单消息清空时间

        for(NoticeReadTime nrt:readTimeList){
            if(nrt.getType()==0){
                clickNoticeTime=nrt.getClickTime();
                clearNoticeTime=nrt.getClearanceTime();
            }else if (nrt.getType()==1){
                clickWalletTime=nrt.getClickTime();
                clearWalletTime=nrt.getClearanceTime();
            }else if (nrt.getType()==2){
                clickOrderTime=nrt.getClickTime();
                clearOrderTime=nrt.getClearanceTime();
            }
        }

        //物流通知
        NoticeUnread nuNotice=firstNotice(userId,clearNoticeTime,clickNoticeTime,0);
        Map<String,String> mapNotice=new HashMap<String, String>();
        if(nuNotice.getFirstNotice()!=null){
            mapNotice.put("count",nuNotice.getCount());
            mapNotice.put("type",nuNotice.getType().toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String time= sdf.format(nuNotice.getTime());
            mapNotice.put("time",time);
            mapNotice.put("firstNotice",nuNotice.getFirstNotice());
            list.add(mapNotice);
        }

        Map<String,String> mapWallet=new HashMap<String, String>();
        NoticeUnread nuWallet=firstNotice(userId,clearWalletTime,clickWalletTime,1);
        if(nuWallet.getFirstNotice()!=null){
            mapWallet.put("count",nuWallet.getCount());
            mapWallet.put("type","1");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String time= sdf.format(nuWallet.getTime());
            mapWallet.put("time",time);
            mapWallet.put("firstNotice",nuWallet.getFirstNotice());

            list.add(mapWallet);
        }


        Map<String,String> mapOrderItem=new HashMap<String, String>();
        NoticeUnread nuOrder=firstNotice(userId,clearOrderTime,clickOrderTime,2);
        if(nuOrder.getFirstNotice()!=null){
            mapOrderItem.put("count",nuOrder.getCount());
            mapOrderItem.put("type","2");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String time= sdf.format(nuOrder.getTime());
            mapOrderItem.put("time",time);
            mapOrderItem.put("firstNotice",nuOrder.getFirstNotice());

            list.add(mapOrderItem);
        }


        return list;
    }

    //获取列表最新一条消息
    public NoticeUnread firstNotice(Long userId,Date clearNoticeTime,Date clickNoticeTime,Integer type){
        NoticeUnread noticeUnread=new NoticeUnread();
        String countNotice="";
        Notice firstNotice=new Notice();
        Notice notice=new Notice();
        notice.setToUser(userId);
        notice.setType(type);
        notice.setCreatedAt(clickNoticeTime);
        countNotice=noticeDao.countAll(notice);
        notice.setCreatedAt(clearNoticeTime);
        notice.setLimit(10);
        notice.setOffset(0);
        List<Notice> noticeList=noticeDao.findByToUserId(notice);
        if(noticeList.size()>0){
            firstNotice=noticeList.get(0);
        }
        noticeUnread.setCount(countNotice);
        noticeUnread.setTime(firstNotice.getCreatedAt());
        noticeUnread.setFirstNotice(firstNotice.getContent());
        noticeUnread.setType(0);
        return noticeUnread;
    }

    /**
     * @description: 发出推送消息
     * @author dpzh
     * @create 2017/8/22 13:29
     * @param notice 消息对象
     * @return:void
     **/
    public void pushNotice(Notice notice){
        PushBean pushBean=new PushBean();
        pushBean.setContent(notice.getContent());
        pushBean.setType(notice.getType());
        pushBean.setBusinessId(notice.getBusinessId());
        pushBean.setNoticeId(notice.getId());
        pushBean.setSubType(notice.getSubType());
        pushBean.setItemSize(notice.getItemSize());
        pushBean.setItemImage(notice.getItemImage());
        //通过userId从redis里获取设备号
        List<LoginInfo> infoList=tokenService.getRedisTokenByUserId(notice.getToUser());
        Set<String> setIos=new HashSet<String>();
        Set<String> setAndroid=new HashSet<String>();
        if(infoList.size()>0){
            for(LoginInfo lf:infoList){
                if(lf.getDeviceType()==1){
                    setIos.add(lf.getPushDeviceId());
                }else if(lf.getDeviceType()==2){
                   setAndroid.add(lf.getPushDeviceId());
                }
            }
            if(setIos.size()>0){
                for(String str:setIos){
                    pushBean.setDeviceId(str);
                    pushBean.setDeviceType(1);
                    pushServer.pushSingleApp(pushBean);
                }
            }
            if(setAndroid.size()>0){
                for(String str:setAndroid){
                    pushBean.setDeviceId(str);
                    pushBean.setDeviceType(2);
                    pushServer.pushSingleApp(pushBean);
                }
            }
        }
    }

    /**
     * @description: 快递100（Express100）回调之后数据处理
     * @author dpzh
     * @create 2017/8/18 11:11
     * @return:
     **/
    public void pushExpress(Express express){
        Notice notice=new Notice();
        LastResult lr=express.getLastResult();
        String content=lr.getData().get(0).getContext();  //最新物流消息
        String num=lr.getNu();                             //运单号
        String state=lr.getState(); //快递单当前签收状态，包括0在途中、1已揽收、2疑难、3已签收、4退签
        Integer subType=Integer.valueOf(state);
        notice.setType(0);
        notice.setSubType(subType);
        notice.setExpressNo(num);
        List<Notice> list=noticeDao.getNoticeByExpressNo(notice);
        if(list.size()==0){
            long number = Long.parseLong(num);
            Notice ntUser=noticeDao.getUserByOrderId(number);
            if(ntUser!=null&&ntUser.getToUser()!=null){
                notice.setContent(content);
                notice.setFromUser(0l);
                notice.setBusinessId(ntUser.getBusinessId());
                notice.setToUser(ntUser.getToUser());
                noticeDao.create(notice);
                pushNotice(notice);
            }
        }

    }



}
