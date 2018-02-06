package com.nowbook.admin.web.jobs;

import com.nowbook.admin.service.OrderCommentJobService;
import com.nowbook.sdp.service.UserLevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Date: 14-2-26
 * Time: PM12:56
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */

@Component
public class UserLevelUpJobs {
    private final static Logger log = LoggerFactory.getLogger(UserLevelUpJobs.class);

    private final AdminLeader adminLeader;

    private final UserLevelService userLevelService;


    @Autowired
    public UserLevelUpJobs(AdminLeader adminLeader,
                           UserLevelService userLevelService) {
        this.adminLeader = adminLeader;
        this.userLevelService = userLevelService;
    }

    /**
     * run every midnight 0:00
     * calculate shop's average score
     */
//    @Scheduled(cron = "0/15 * * * * *")
//    @Scheduled(cron = "0 0 0 * * *")
    public void userLevelUp() {

        log.info("exect calculateShopExtraScores begin");
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        log.info("[userLevelUp] userLevelUp begin");
        userLevelService.userLevelUp();
        log.info("[userLevelUp] userLevelUp end");
    }
}
