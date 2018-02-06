package com.nowbook.web.controller.api.design;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.nowbook.common.model.Response;
import com.nowbook.exception.JsonResponseException;
import com.nowbook.redis.utils.JedisTemplate;
import com.nowbook.site.container.RenderConstants;
import com.nowbook.site.exception.Server500Exception;
import com.nowbook.site.handlebars.HandlebarEngine;
import com.nowbook.site.model.Component;
import com.nowbook.site.model.ComponentCategory;
import com.nowbook.site.service.ComponentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 13-11-28
 */
@Controller
@RequestMapping("/api/design/components")
public class Components {
    @Autowired
    private HandlebarEngine handlebarEngine;
    @Autowired
    private ComponentService componentService;
    @Autowired
    private JedisTemplate template;

    @RequestMapping(value = "render", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String render(@RequestParam("template") String template,
                         @RequestParam(required = false) String gdata,
                         @RequestParam Map<String, Object> context) {
        context.remove("template");
        context.put(RenderConstants.DESIGN_MODE, true);
        if (!Strings.isNullOrEmpty(gdata)) {
            context.remove("gdata");
            context.put(RenderConstants.DESIGN_GDATA, gdata);
        }
        String html = handlebarEngine.execInline(template, context);
        if (StringUtils.isBlank(html)) {
            throw new JsonResponseException(404, "组件未找到或者渲染出错");
        }
        return html;
    }

    @RequestMapping(value = "/categories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> listCategory(@RequestParam("mode") String modeName) {
        modeName = modeName.toUpperCase();
        ComponentCategory.Mode mode = ComponentCategory.Mode.valueOf(modeName);
        Map<String, Object> categoryMap = Maps.newHashMap();
        for (ComponentCategory category : ComponentCategory.listByMode(mode)) {
            categoryMap.put(category.name(), category.getName());
        }
        return categoryMap;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Component> list(@RequestParam("category") String categoryName) {
        categoryName = categoryName.toUpperCase();
        ComponentCategory category = ComponentCategory.valueOf(categoryName);
        if (category == null) {
            throw new JsonResponseException(400, "category not found");
        }
        if (category.getSuitableModes() == null) {
            throw new JsonResponseException(400, "category isnt designable");
        }
        Response<List<Component>> componentsR = componentService.findByCategory(category.name());
        Server500Exception.failToThrow(componentsR);
        return componentsR.getResult();
    }

    // 保存楼层 2017/8/31
    @RequestMapping(value = "/saveFloors", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void saveFloors(final String floors) {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.set("component-data:common/floors", floors);
            }
        });
    }

}
