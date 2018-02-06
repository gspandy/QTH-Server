package com.nowbook.web.controller;


import com.nowbook.common.utils.CommonConstants;
import com.nowbook.user.model.User;
import com.nowbook.third.model.ThirdUser;
import com.nowbook.third.service.ThirdPartLoginService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by zhaop01 on 2014/9/2.
 */
@Log
@Controller
@RequestMapping("/api/thridPart")
public class ThirdPartLoginController {

    @Autowired
    private ThirdPartLoginService thirdPartLoginService;

    @Value("#{app.thirdPartLogin_env}")
    private String env;
    /**
     * 第三方锦霖商城系统对接
     */
    @RequestMapping(value = "/auth", method = RequestMethod.GET)
    @ResponseBody
    public void auth(@RequestParam String source ,
                     @RequestParam String thirdPartToken,
                     @RequestParam String targetUrl,
                     HttpServletResponse response,
                     HttpSession session) {
        // Response res = new Response();
        try {
            String username = this.thirdPartLoginService.process(source,env,thirdPartToken,session.getId());
            if(username==null){
                log.info("invoke nb or enb interface has a error!");
                // res.setError("get user info has a failure!");
                // res.setSuccess(false);
                // return res;
            }else {
                if (this.thirdPartLoginService.isExistsByUserName(username)) {
                    // 如果存在则直接登录
                    ThirdUser tu = this.thirdPartLoginService.findBySourceName(username);
                    User user = this.thirdPartLoginService.findByName(tu.getNbUserName());
                    session.setAttribute(CommonConstants.SESSION_USER_ID, user.getId());
                    //res.setSuccess(true);
                    //res.setResult("已经登录NB系统");
                    // return res;
                } else {
                    // 如果不存在则进行保存
                    User user = new User();
                    user.setName(username);
                    this.thirdPartLoginService.saveUser(user, source);
                    ThirdUser tu = this.thirdPartLoginService.findBySourceName(username);
                    User tempUser = this.thirdPartLoginService.findByName(tu.getNbUserName());
                    session.setAttribute(CommonConstants.SESSION_USER_ID, tempUser.getId());
                    //res.setSuccess(true);
                    //res.setResult("已经登录NB系统");
                    // return res;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // return res;
    }

}
