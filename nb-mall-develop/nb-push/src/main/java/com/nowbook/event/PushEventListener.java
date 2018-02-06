package com.nowbook.event;

import com.google.common.eventbus.Subscribe;
import com.nowbook.enums.NoticeType;
import com.nowbook.notice.model.Notice;
import com.nowbook.notice.service.NoticeService;
import com.nowbook.push.jpush.model.PushBean;
import com.nowbook.push.pushserver.PushServer;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.model.LoginInfo;
import com.nowbook.user.model.User;
import com.nowbook.user.service.AccountService;
import com.nowbook.user.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dpzh
 * @create 2017-07-27 14:32
 * @description:<类文件描述>
 **/

@Slf4j
@Component
public class PushEventListener {

    private final PushEventBus eventBus;

    //帐户服务
    private final AccountService<User> accountService;

    private NoticeService noticeService;

    private PushServer pushServer;

    private TokenService tokenService;

    @Autowired
    public PushEventListener(PushEventBus eventBus,
                             AccountService<User> accountService,
                             NoticeService noticeService,
                             PushServer pushServer,
                             TokenService tokenService
                                                    ) {
        this.eventBus = eventBus;
        this.accountService = accountService;
        this.noticeService=noticeService;
        this.pushServer=pushServer;
        this.tokenService=tokenService;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    @Subscribe
    public void sendPushContent(PushEvent pushEvent) {
        String type=pushEvent.getType();
       if((NoticeType.ASSETS_FLUCTUATION.getNoticeType()).equals(type)){
            sendAssetMessage(pushEvent,1 );

        }
    }
    //资金变动发送消息
    private void sendAssetMessage(PushEvent pushEvent, Integer type){
        Notice notice=new Notice();
        notice.setContent(pushEvent.getContent());
        notice.setBusinessId(pushEvent.getBusinessId());
        notice.setSubType(pushEvent.getSubType());
        notice.setToUser(pushEvent.getUserId());
        notice.setType(type);
        notice.setFromUser(0l);
        Long noticeId= noticeService.create(notice);
        if(noticeId!=0){
            try {
               noticeService.pushNotice(notice);
            } catch (Exception e) {
                log.error("Push notification failed", e);
            }
        }


    }

}
