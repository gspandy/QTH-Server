package com.nowbook.restful.controller;

import com.nowbook.common.model.Response;
import com.nowbook.common.utils.JsonMapper;
import com.nowbook.exception.JsonResponseException;
import com.nowbook.restful.dto.NbResponse;
import com.nowbook.rlt.grid.service.GridService;
import com.nowbook.trade.dto.FatOrder;
import com.nowbook.trade.dto.UserFreightInfo;
import com.nowbook.trade.model.UserTradeInfo;
import com.nowbook.trade.service.FreightCountService;
import com.nowbook.trade.service.UserTradeInfoService;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import com.nowbook.user.model.Address;
import com.nowbook.user.service.AddressService;
import com.nowbook.web.misc.MessageSources;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.nowbook.common.utils.Arguments.notEmpty;
import static com.nowbook.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Date: 3/28/14
 * Time: 10:10
 * Author: 2014年 <a href="mailto:dong@nowbook.com">程程</a>
 */

@Controller
@Slf4j
@RequestMapping("/api/extend/region")
public class NSRegions {
    public final static JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();
    @Autowired
    AddressService addressService;

    @Autowired
    private MessageSources messageSources;
    @Autowired
    private  UserTradeInfoService userTradeInfoService;
    @Autowired
    private  GridService gridService;
    @Autowired
    private  FreightCountService freightCountService;


    @Value("#{app.restkey}")
    private String key;


    /**
     * 获取地址列表信息
     *
//     * @param channel   渠道, 必填
//     * @param sign      签名, 必填
     *
     * 获取省市区信息
     */
    @RequestMapping(value= "/select" ,method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<List<Address>> getAddresses(
//                                                        @RequestParam("channel") String channel,
//                                                     @RequestParam("sign") String sign,
                                                     HttpServletRequest request) {
        NbResponse<List<Address>> result = new NbResponse<List<Address>>();
        try {
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Response<List<Address>> addressGet = addressService.getTreeOf(0);
            checkState(addressGet.isSuccess(), addressGet.getError());
            result.setResult(addressGet.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to query addresses, error:{}", e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query addresses", e);
            result.setError(messageSources.get("address.list.query.fail"));
        }
        return result;
    }

    /**
     * 新增异步计算运费的逻辑
     * @param itemsInfo 商品信息（包含itemId，quantity,数据格式itemsInfo=["100:2","100:3","100:4"]）
     * @param request 用于获取区域cookie信息
     * @return  List
     * 返回一个
     */
    @RequestMapping(value = "/user/tradeInfos", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<UserFreightInfo> getTradeInfos(@RequestParam(value ="tradeId") Long tradeId,@RequestParam String itemsInfo, HttpServletRequest request) {
        Response<UserFreightInfo> response = new Response<UserFreightInfo>();
        BaseUser baseUser = UserUtil.getCurrentUser();
        Response<UserTradeInfo> result = userTradeInfoService.findById(tradeId);
        if (result.isSuccess()) {
            if(!result.getResult().getUserId().equals(baseUser.getId())){
                response.setError("此用户没有这个地址");
                return response;
            }
            //获取用户对应与该区域的物流地址
            Map<String, String> cookieKVs = Maps.newHashMap();
            cookieKVs.put("ammProvinceId", result.getResult().getProvinceCode().toString());

            //运费计算
            UserFreightInfo userFreightInfo = new UserFreightInfo();
            //当前只计算到省份
            Response<Integer> provinceRes = gridService.findProvinceFromCookie(cookieKVs);
            if(!provinceRes.isSuccess()){
                log.error("failed to find provinceId from cookie, error code={}", provinceRes.getError());
                throw new JsonResponseException(500, messageSources.get(provinceRes.getError()));
            }
            //如果地址不为空
            if(notNull(provinceRes.getResult())){
                List<FatOrder> fatOrders = JSON_MAPPER.fromJson(itemsInfo, JSON_MAPPER.createCollectionType(List.class, FatOrder.class));
                List<Map<String , String>> deliverFees = new ArrayList<Map<String, String>>();
                for(FatOrder fatOrder : fatOrders){
                    Map<Long, Integer> map = fatOrder.getSkuIdAndQuantity();
                    Map<String, String> deliverFee = Maps.newHashMap();
                    Long id;
                    Integer count;
                    Integer total =0;
                    //计算每个itemId:count的运费信息
                    for (Long  skuId : map.keySet()) {
                        //解析商品编号&购买数量的关系
                        id = skuId;
                        count = map.get(skuId);
                        total= total +freightCountService.countDefaultFee(provinceRes.getResult(), id, count).getResult();
                    }
                    deliverFee.put("sellerId",fatOrder.getSellerId().toString());
                    deliverFee.put("deliverFee",total.toString());
                    deliverFees.add(deliverFee);
                }
                userFreightInfo.setDeliverFees(deliverFees);
            }
            response.setResult(userFreightInfo);
            return response;
        } else {
            log.error("failed to query trade information for user:{},error code :{}", baseUser.getId(), result.getError());
            throw new JsonResponseException(500, result.getError());
        }
    }
}
