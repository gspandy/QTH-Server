package com.nowbook.restful.controller;

import com.google.common.base.Splitter;
import com.nowbook.notice.model.Express;
import com.nowbook.notice.model.Notice;
import com.nowbook.notice.model.NoticeReadTime;
import com.nowbook.notice.service.NoticeService;
import com.nowbook.restful.controller.testPostOrder.bin.demo.src.demo.JacksonHelper;
import com.nowbook.restful.controller.testPostOrder.bin.demo.src.pojo.NoticeResponse;
import com.nowbook.restful.dto.NbResponse;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import com.nowbook.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author dpzh
 * @create 2017-08-03 13:25
 * @description: 通知接口
 **/

@Controller
@Slf4j
@RequestMapping("/api/extend/notice")
public class NSNotice {

    private final static Splitter splitter = Splitter.on("_").omitEmptyStrings().trimResults();

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private MessageSources messageSources;

    /**
     * @description: 查询所有消息
     * @author dpzh
     * @create 2017/8/4 9:30
     * @param type
     * @param pageNo   页码
     * @param pageSize 每页条数
     * @return: NbResponse<List<Notice>> 返回消息list
     **/
    @RequestMapping(value = "/list", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<List<Notice>> list(@RequestParam(value = "type") Integer type,
                                         @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                         @RequestParam(value = "pageSize", required = false) Integer pageSize){
        NbResponse<List<Notice>> result=new NbResponse<List<Notice>>();
        BaseUser baseUser=new BaseUser();
        baseUser= UserUtil.getCurrentUser();
        try {
            if(baseUser!=null){
                Long userId=baseUser.getId();
                Notice notice=new Notice();
                notice.setToUser(userId);
                notice.setType(type);
                List<Notice> noticeList=noticeService.findByToUserId(notice);
                result.setResult(noticeList);
                log.info("Notice query successfully",userId);
            }else {
                result.setError(messageSources.get("notice.getId.is.null"));
            }
        }catch (Exception e){
            log.error("Notice query exception userId:{}",baseUser.getId(), e);
            result.setError(messageSources.get("notice.query.fail"));
        }
        return result;

    }

    /**
     * @description: 清空通知
     * @author dpzh
     * @create 2017/8/4 14:29
     * @return: NbResponse<String> 成功返回success 失败返回error
     **/
    @RequestMapping(value = "/clearNotice", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<String> clearNotice(@RequestParam(value = "type") Integer type){
        NbResponse<String> result=new NbResponse<String>();
        BaseUser baseUser=new BaseUser();
        try {
            baseUser= UserUtil.getCurrentUser();
            if (baseUser!=null){
                NoticeReadTime nrt=new NoticeReadTime();
                nrt.setUserId(baseUser.getId());
                nrt.setType(type);
                noticeService.clearanceTime(nrt);
                log.info("Notice status delete successfully",baseUser.getId());
                result.setResult("success");
           }else {
            result.setError(messageSources.get("notice.getId.is.null"));
        }
        }catch (Exception e){
            log.error("Notice status delete exception",  e);
            result.setError(messageSources.get("notice.delete.fail"));  //清空通知失败
        }
        return result;
    }

    /**
     * @description: 各类消息数及第一条消息
     * @author dpzh
     * @create 2017/8/9 14:51
     * @return:com.nowbook.restful.dto.NbResponse<java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>>>
     **/
    @RequestMapping(value = "/noticeCounts", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<List<Map<String,String>>> noticeIfication(){
        NbResponse<List<Map<String,String>>> result=new NbResponse<List<Map<String,String>>>();
        BaseUser baseUser=new BaseUser();
        try {
            baseUser= UserUtil.getCurrentUser();
            if (baseUser!=null){
                List<Map<String,String>> list=noticeService.countNotices(baseUser.getId());
                result.setResult(list);
            }else {
                result.setError(messageSources.get("notice.getId.is.null"));
            }
        }catch (Exception e){
            log.error("Notice query exception",  e);
            result.setError(messageSources.get("notice.query.fail"));
        }
        return result;
    }

    /**
     * @description: 首页显示的未读消息数
     * @author dpzh
     * @create 2017/8/9 14:52
     * @return:com.nowbook.restful.dto.NbResponse<java.util.Map<java.lang.String,java.lang.String>>
     **/
    @RequestMapping(value = "/homeCount", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Map<String,String>> homeCount(){
        NbResponse<Map<String,String>> result=new NbResponse<Map<String, String>>();
        BaseUser baseUser=new BaseUser();
        try {
            baseUser= UserUtil.getCurrentUser();
            if (baseUser!=null){
                Map<String,String> map=new HashMap<String, String>();
                String sum=noticeService.homeCount(baseUser.getId());
                map.put("sum",sum);
                result.setResult(map);

            }else {
                result.setError(messageSources.get("notice.getId.is.null"));
            }
        }catch (Exception e){
            log.error("Notice query exception",  e);
            result.setError(messageSources.get("notice.unread.query.fail"));
        }

        return result;
    }

    /**
     * @description: 快递100（Express100）回调接口
     * @author dpzh
     * @create 2017/8/22 14:52
     * @return:
     **/
    @RequestMapping(value = "/fromExpress100", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NoticeResponse testFromExpress100(@RequestParam(value = "param") String param,HttpServletRequest request){

        NoticeResponse req = new NoticeResponse();
        try {
            Express express=  JacksonHelper.fromJSON(param,Express.class);
            if(!("abort").equals(express.getStatus())){
                noticeService.pushExpress(express);
            }
            req.setMessage("成功");
            req.setResult(true);
            req.setReturnCode("200");
        }catch (Exception e){
            log.error("Express100 Callback exception",  e);
            req.setMessage("失败");
            req.setResult(false);
            req.setReturnCode("500");
            return req;
        }
        return req;


    }








}
