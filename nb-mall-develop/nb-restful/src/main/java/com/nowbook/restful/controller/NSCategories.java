package com.nowbook.restful.controller;

import com.nowbook.category.dto.FrontCategoryNav;
import com.nowbook.category.dto.RichCategory;
import com.nowbook.category.model.FrontCategory;
import com.nowbook.category.service.BackCategoryService;
import com.nowbook.category.service.FrontCategoryService;
import com.nowbook.common.model.Response;
import com.nowbook.exception.JsonResponseException;
import com.nowbook.item.dto.FacetSearchResult;
import com.nowbook.item.dto.ItemsWithTagFacets;
import com.nowbook.item.service.ItemSearchService;
import com.nowbook.redis.utils.JedisTemplate;
import com.nowbook.restful.dto.NbResponse;
import com.nowbook.sdp.service.DistributionsService;
import com.nowbook.site.dao.ComponentDao;
import com.nowbook.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * Date: 4/8/14
 * Time: 14:20
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */

@Controller
@Slf4j
@RequestMapping("/api/extend/categories")
public class NSCategories {
    @Autowired
    ItemSearchService itemSearchService;
    @Autowired
    BackCategoryService backCategoryService;

    @Autowired
    MessageSources messageSources;

    @Autowired
    private FrontCategoryService frontCategoryService;

    @Autowired
    private DistributionsService distributionsService;

    @Autowired
    private ComponentDao componentDao;

    @Autowired
    public JedisTemplate jedisTemplate;

    @Value("#{app.restkey}")
    private String key;

    /**
//     * @param channel  渠道, 必填
//     * @param sign     签名, 必填
     *
     *
     * 返回商场所有商品类目
     */
    @RequestMapping(value = "/all", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<List<RichCategory>> list(
//            @RequestParam("channel") String channel,
//                                                  @RequestParam("sign") String sign,
                                                  HttpServletRequest request) {
        NbResponse<List<RichCategory>> result = new NbResponse<List<RichCategory>>();

        try {
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            Response<List<RichCategory>> categoriesGetResult = backCategoryService.getTreeOf(0);
            checkState(categoriesGetResult.isSuccess(), categoriesGetResult.getError());
            result.setResult(categoriesGetResult.getResult(), key);
                    return result;

        } catch (IllegalArgumentException e) {
            log.error("fail to get back-categories, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get back-categories, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get back-categories", e);
            result.setError(messageSources.get("back.category.query.fail"));
        }

        return result;
    }
    @RequestMapping(value = "/children", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<FrontCategoryNav>> childrenOf(@RequestParam("categoryId") Long categoryId) {
        Response<List<FrontCategoryNav>> result = frontCategoryService.findSecondAndThirdLevel(categoryId);
        if (result.isSuccess()) {
            return result;
        } else {
            log.error("failed to load sub front categories of {},error code :{}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/secondLevelChildren", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<FrontCategory> getAllSecondLevelFrontCategories() {
        Response<List<FrontCategory>> result = distributionsService.findAllSecondLevel();
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to load sub front categories,error code :{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/shopSearch", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<ItemsWithTagFacets> shopSearch(@RequestParam("pageNo") Integer pageNo,
                                            @RequestParam("size") Integer size,
                                            @RequestParam Map<String, String> context) {
        Response<ItemsWithTagFacets> result = itemSearchService.searchOnShelfItemsInShop(pageNo,size,context);
        return result;
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<FacetSearchResult> search(@RequestParam("pageNo") Integer pageNo,
                                                   @RequestParam("size") Integer size,
                                                   @RequestParam Map<String, String> context) {

        Response<FacetSearchResult> result = itemSearchService.facetSearchItem(pageNo,size,context);
        return result;
    }

    // 获取首页一级分类
    @RequestMapping(value = "/firstLevel", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<JSONArray> getCarouselUrl() {
        Response<JSONArray> response = new Response<JSONArray>();
        JSONArray list = new JSONArray();
        String classes = componentDao.getData("common/nav_bar:release");
        JSONObject jo = JSONObject.fromObject(classes);
        if (jo.size() != 0) {
            JSONArray ja = JSONArray.fromObject(jo.get("data"));
            for (Object o : ja) {
                JSONObject j = JSONObject.fromObject(o);
                if (j.get("id") != null && !j.get("id").equals("")) {
                    final String categoryId = j.get("id").toString();
                    String name = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
                        @Override
                        public String action(Jedis jedis) {
                            return jedis.hget("front-category:" + categoryId, "name");
                        }
                    });
                    if (name != null) {
                        j.put("name", name);
                    }
                    String linkId = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
                        @Override
                        public String action(Jedis jedis) {
                            return jedis.hget("front-category:" + categoryId, "linkId");
                        }
                    });
                    if (linkId != null) {
                        j.put("linkId", linkId);
                    }
                }
                list.add(j);
            }
        }
        response.setResult(list);
        return response;
    }

    // 获取所有一级分类
    @RequestMapping(value = "/allFirstLevel", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<Map<String, String>>> allFirstLevel() {
        Response<List<Map<String, String>>> result = new Response<List<Map<String, String>>>();
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Response<List<RichCategory>> response = frontCategoryService.childrenOfNoCache(0L);
        List<RichCategory> l = response.getResult();
        if (l != null && l.size() != 0) {
            for (RichCategory r : l) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("id", r.getId().toString());
                map.put("link", r.getImageUrl());
                final String categoryId = r.getId().toString();
                String name = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
                    @Override
                    public String action(Jedis jedis) {
                        return jedis.hget("front-category:" + categoryId, "name");
                    }
                });
                if (name != null) {
                    map.put("name", name);
                }
                String linkId = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
                    @Override
                    public String action(Jedis jedis) {
                        return jedis.hget("front-category:" + categoryId, "linkId");
                    }
                });
                if (linkId != null) {
                    map.put("linkId", linkId);
                }
                list.add(map);
            }
        }
        result.setResult(list);
        return result;
    }

    // 获取轮播图
    @RequestMapping(value = "/carousel", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<JSONArray> carousel() {
        Response<JSONArray> response = new Response<JSONArray>();
        String carousels = componentDao.getData("common/carousel:release");
        JSONObject jo = JSONObject.fromObject(carousels);
        if (jo.size() != 0) {
            JSONArray ja = JSONArray.fromObject(jo.get("images"));
            response.setResult(ja);
        } else {
            response.setResult(new JSONArray());
        }
        return response;
    }

}
