package com.nowbook.admin.web.controller;

import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import com.nowbook.web.misc.MessageSources;
import com.nowbook.brand.model.BrandRlView;
import com.nowbook.brand.model.BrandWRlView;
import com.nowbook.brand.service.BrandRlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by zhua02 on 2014/7/29.
 */

@Slf4j
@Controller
@RequestMapping("/brandRlView")
public class BrandRlViewController {

    @Autowired
    private BrandRlService brandRlService;

    @Autowired
    private MessageSources messageSources;

    @RequestMapping(value = "/queryRl", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<BrandRlView>> queryRl() {
        BaseUser baseUser=UserUtil.getCurrentUser();
        Response<List<BrandRlView>> result = brandRlService.findRlzj(baseUser,"");
        return result;
    }

    @RequestMapping(value = "/queryWRl", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<BrandWRlView>> queryWRl() {
        BaseUser baseUser=UserUtil.getCurrentUser();
        Response<List<BrandWRlView>> result = brandRlService.findWRl(baseUser,"");
        return result;
    }

    @RequestMapping(value = "/delRl", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delRl(@RequestParam("idlist") String[] idlist) {
        if(idlist.length>0){
            int[] ia = new int[idlist.length];
            for(int i=0;i<idlist.length;i++){
                ia[i]=Integer.parseInt(idlist[i]);
            }
            brandRlService.delRl_Key(ia);
        }
        return "Delete Success";
    }

    @RequestMapping(value = "/delRldl", method = RequestMethod.GET)
    public String delRl(@RequestParam("id") String id) {
        int[] ia = new int[1];
        ia[0]=Integer.parseInt(id);
        brandRlService.delRl_Key(ia);
        return "redirect:/operations/brandRl";
    }

    @RequestMapping(value = "/addRl", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String addRl(@RequestParam("shopidlist") String[] shopidlist) {
        BaseUser baseUser=UserUtil.getCurrentUser();
        int[] ia = new int[shopidlist.length];
        for(int i=0;i<shopidlist.length;i++){
            ia[i]=Integer.parseInt(shopidlist[i]);
        }
        brandRlService.addRl_Key(ia,baseUser);
        return "Add Success";
    }

    @RequestMapping(value = "/addRldl", method = RequestMethod.GET)
    public String addRl(@RequestParam("shopid") String shopid) {
        BaseUser baseUser=UserUtil.getCurrentUser();
        int[] ia = new int[1];
        ia[0]=Integer.parseInt(shopid);
        brandRlService.addRl_Key(ia,baseUser);
        return "redirect:/operations/brandWRl";
    }

}
