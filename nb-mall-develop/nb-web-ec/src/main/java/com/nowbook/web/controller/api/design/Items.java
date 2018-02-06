package com.nowbook.web.controller.api.design;

import com.nowbook.common.model.Response;
import com.nowbook.exception.JsonResponseException;
import com.nowbook.item.dao.redis.ItemDetaiImageDao;
import com.nowbook.item.model.Item;
import com.nowbook.item.service.ItemService;
import com.nowbook.site.service.ItemCustomService;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import com.nowbook.web.misc.MessageSources;
import com.nowbook.web.utils.SafeHtmlValidator;
import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 13-12-17
 */
@Controller("designItems")
@RequestMapping("/api/design")
public class Items {
    private final static Logger log = LoggerFactory.getLogger(Items.class);

    @Autowired
    private ItemDetaiImageDao itemDetaiImageDao;
    @Autowired
    private ItemCustomService itemCustomService;
    @Autowired
    private ItemService itemService;

    @Autowired
    private MessageSources messageSources;


    @RequestMapping(value = "/items/{itemId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void saveItemCustom(@PathVariable Long itemId, @RequestParam String template, @RequestParam String images) {
        if(template.length()>50*1024){
            log.error("template length is {},and long than 10k",template.length());
            throw new JsonResponseException(500, messageSources.get("content.too.long"));
        }
        if (SafeHtmlValidator.checkScriptAndEvent(template)){
            log.error("has invalid html content: template({})", template);
            throw new JsonResponseException(403, "invalid.html");
        }
        BaseUser user = UserUtil.getCurrentUser();
        Response<Item> itemR = itemService.findById(itemId);
        if(!itemR.isSuccess()){
            log.error("failed to find item(id={}) error code:{}", itemId, itemR.getError());
            throw new JsonResponseException(500,messageSources.get(itemR.getError()));
        }
       if (!Objects.equal(itemR.getResult().getUserId(), user.getId())) {
            throw new JsonResponseException(403, "item not belong to u");
        }
        Response<Boolean> saveR = itemCustomService.save(itemId, template);
        //详细页图片地址存储
        itemDetaiImageDao.setItemIdEvaluation(itemId,images);

        if(!saveR.isSuccess()){
            log.error("failed to save itemCustom of item(id={}) error code:{}", itemId, saveR.getError());
            throw new JsonResponseException(500,messageSources.get(saveR.getError()));
        }
    }

    @RequestMapping(value = "/item-templates/{spuId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void saveItemTemplate(@PathVariable Long spuId, @RequestParam String template, @RequestParam String images) {
        BaseUser.TYPE userType = UserUtil.getCurrentUser().getTypeEnum();
        if (userType != BaseUser.TYPE.ADMIN && userType != BaseUser.TYPE.SITE_OWNER  && userType!=BaseUser.TYPE.WHOLESALER) {
            throw new JsonResponseException(403, "no required auth");
        }
        if (SafeHtmlValidator.checkScriptAndEvent(template)){
            log.error("has invalid html content: template({})", template);
            throw new JsonResponseException(403, "invalid.html");
        }
        Response<Boolean> saveR = itemCustomService.saveTemplate(spuId, template);
        //模板详细页图片地址存储
        itemDetaiImageDao.setSpuIdEvaluation(spuId,images);

        if(!saveR.isSuccess()){
            log.error("failed to save item template of spu(id={}) error code:{}", spuId, saveR.getError());
            throw new JsonResponseException(500,messageSources.get(saveR.getError()));
        }
    }
}
