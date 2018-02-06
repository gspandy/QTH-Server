package com.nowbook.admin.web.controller;

import com.nowbook.common.model.Response;
import com.nowbook.rlt.popularizeurl.service.PopularizeUrlService;
import com.nowbook.site.exception.NotFound404Exception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nowbook.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * WxUser: yangzefeng
 * Date: 13-11-6
 * Time: 下午2:26
 */
@Controller
@RequestMapping("/api/admin/popUrl")
public class PopUrlController {

    @Autowired
    PopularizeUrlService popularizeUrlService;

    /*
    *推广端连接
     */
    @RequestMapping(value = "/createUrl")
    public void getPopularizeUrl(HttpServletRequest request, HttpServletResponse response,
                                 @RequestHeader("Host") String domain,
                                 @RequestParam("popUrlCode") String popUrlCode, @RequestParam("url") String url) {

        try {

            Response<Boolean> urlR = popularizeUrlService.createPopUrl(popUrlCode, url);

            if (!urlR.isSuccess()) {

                throw new NotFound404Exception(urlR.getError());
            }

        } catch (Exception e) {
        }
    }
}
