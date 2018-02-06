package com.nowbook.item.service;

import com.nowbook.item.dto.RichItem;
import com.nowbook.item.model.Item;
import com.nowbook.item.model.Sku;
import com.nowbook.sdp.dao.LevelDao;
import com.nowbook.sdp.model.Level;
import com.nowbook.sdp.service.LevelService;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dpzh
 * @create 2017-09-27 9:26
 * @description:<类文件描述>
 **/
@Service
public class LevelPriceServiceImpl implements LevelPriceService {


    @Autowired
    private LevelService levelService;


    @Autowired
    private LevelDao levelDao;


    /**
     * @description: 获取item对应会员等级价格
     * @author dpzh
     * @create 2017/9/27 10:13
     * @param item
     * @return: 返回该会员对应的商品价格
     **/
    public Integer getUserLevelPrice(RichItem item) {

        BaseUser user = UserUtil.getCurrentUser();
        Integer price=item.getPrice();
        Integer level=0;
        Integer discount=100;
        if (user != null) {
            List<Level> levels=levelService.selectByUserId(user.getId()); //根据用户id获取用户的会员等级
            if(levels.size()>0){
                for(Level lev:levels){
                    level=lev.getLevel();
                    discount=lev.getDiscount();
                }
            }
            if(item.getPriceType()==1){
                price=price*discount/100;
            }else  if(item.getPriceType()==2){
                price=(item.getSellingPrice()-item.getPurchasePrice())*discount/100+item.getPurchasePrice();
            } else if(item.getPriceType()==3){
                switch (level){
                    case 0:
                        price=item.getQuasiAngelPrice();
                        break;
                    case 1:
                        price=item.getAngelPrice();
                        break;
                    case 2:
                        price=item.getGoldPrice();
                        break;
                    case 3:
                        price=item.getPlatinumPrice();
                        break;
                    case 4:
                        price=item.getBlackPrice();
                        break;
                    case 5:
                        price=item.getPartnerPrice();
                        break;
                    case 6:
                        price=item.getComPrice();
                        break;
                }
            }
        }else {
            if(item.getPriceType()==2){
                price=item.getSellingPrice();
            }else if(item.getPriceType()==3) {
                price=item.getCustomPrice();
            }
        }
        return price;
    }


    /**
     * @description: 获取SKU对应会员等级价格
     * @author dpzh
     * @create 2017/9/27 10:13
     * @param sku
     * @return: 返回该会员对应的SKU价格
     **/
    public Integer getUserLevelPrice(Sku sku) {
        BaseUser user = UserUtil.getCurrentUser();
        Integer price=sku.getPrice();
        Integer level=0;
        Integer discount=100;
        if (user != null) {
            List<Level> levels=levelService.selectByUserId(user.getId()); //根据用户id获取用户的会员等级
            if(levels.size()>0){
                for(Level lev:levels){
                    level=lev.getLevel();
                    discount=lev.getDiscount();
                }
            }
            if(sku.getPriceType()==1){
                price=price*discount/100;
            }else  if(sku.getPriceType()==2){
                price=(sku.getSellingPrice()-sku.getPurchasePrice())*discount/100+sku.getPurchasePrice();
            } else if(sku.getPriceType()==3){
                switch (level){
                    case 0:
                        price=sku.getQuasiAngelPrice();
                        break;
                    case 1:
                        price=sku.getAngelPrice();
                        break;
                    case 2:
                        price=sku.getGoldPrice();
                        break;
                    case 3:
                        price=sku.getPlatinumPrice();
                        break;
                    case 4:
                        price=sku.getBlackPrice();
                        break;
                    case 5:
                        price=sku.getPartnerPrice();
                        break;
                    case 6:
                        price=sku.getComPrice();
                        break;
                }
            }
        }else {
            if(sku.getPriceType()==2){
                price=sku.getSellingPrice();
            }else if(sku.getPriceType()==3) {
                price=sku.getCustomPrice();
            }
        }
        return price;
    }

    /**
     * @description: 获取item对应会员等级价格
     * @author dpzh
     * @create 2017/9/27 10:13
     * @param item
     * @return: 返回该会员对应的商品价格
     **/
    public Integer getUserLevelPrice(Item item) {
        BaseUser user = UserUtil.getCurrentUser();
        Integer price=item.getPrice();
        Integer level=0;
        Integer discount=100;
        if (user != null) {
            List<Level> levels=levelService.selectByUserId(user.getId()); //根据用户id获取用户的会员等级
            if(levels.size()>0){
                for(Level lev:levels){
                    level=lev.getLevel();
                    discount=lev.getDiscount();
                }
            }
            if(item.getPriceType()==1){
                price=price*discount/100;
            }else  if(item.getPriceType()==2){
                price=(item.getSellingPrice()-item.getPurchasePrice())*discount/100+item.getPurchasePrice();
            } else if(item.getPriceType()==3){
                switch (level){
                    case 0:
                        price=item.getQuasiAngelPrice();
                        break;
                    case 1:
                        price=item.getAngelPrice();
                        break;
                    case 2:
                        price=item.getGoldPrice();
                        break;
                    case 3:
                        price=item.getPlatinumPrice();
                        break;
                    case 4:
                        price=item.getBlackPrice();
                        break;
                    case 5:
                        price=item.getPartnerPrice();
                        break;
                    case 6:
                        price=item.getComPrice();
                        break;
                }
            }
        }else {
            if(item.getPriceType()==2){
                price=item.getSellingPrice();
            }else if(item.getPriceType()==3) {
                price=item.getCustomPrice();
            }
        }
        return price;
    }

    /**
     * @description: 获取item对应会员等级价格
     * @author dpzh
     * @create 2017/9/27 10:13
     * @param item
     * @return: 返回该会员对应的商品价格
     **/
    public Integer getUserLevelPriceForCustomer(Item item,Long userId) {
        Integer price=item.getPrice();
        Integer level=0;
        Integer discount=100;
        if (userId != null) {
            List<Level> levels=levelService.selectByUserId(userId); //根据用户id获取用户的会员等级
            if(levels.size()>0){
                for(Level lev:levels){
                    level=lev.getLevel();
                    discount=lev.getDiscount();
                }
            }
            if(item.getPriceType()==1){
                price=price*discount/100;
            }else  if(item.getPriceType()==2){
                price=(item.getSellingPrice()-item.getPurchasePrice())*discount/100+item.getPurchasePrice();
            } else if(item.getPriceType()==3){
                switch (level){
                    case 0:
                        price=item.getQuasiAngelPrice();
                        break;
                    case 1:
                        price=item.getAngelPrice();
                        break;
                    case 2:
                        price=item.getGoldPrice();
                        break;
                    case 3:
                        price=item.getPlatinumPrice();
                        break;
                    case 4:
                        price=item.getBlackPrice();
                        break;
                    case 5:
                        price=item.getPartnerPrice();
                        break;
                    case 6:
                        price=item.getComPrice();
                        break;
                }
            }
        }else {
            if(item.getPriceType()==2){
                price=item.getSellingPrice();
            }else if(item.getPriceType()==3) {
                price=item.getCustomPrice();
            }
        }
        return price;
    }

    /**
     * @description: 获取item各个会员等级价格
     * @author dpzh
     * @create 2017/9/27 10:13
     * @param item
     * @return: 返回含有各个等级价格商品信息
     **/
    public Item getItemLevelPrice(Item item){
        Integer type=item.getPriceType();
        Integer price1=item.getPurchasePrice();
        Integer price2=item.getSellingPrice();
        //判断变更后的等级是否设置，没设置的话返回
        Level newLevelInfo = new Level();
        List<Level> levelInfoList = levelDao.selectByLevel(newLevelInfo);
        Map<Integer,Integer> map=new HashMap<Integer, Integer>();
        for(Level level:levelInfoList){
            map.put(level.getLevel(),level.getDiscount());
        }
        if(type==1){
            if(price2==null){

            }
            price2=item.getPrice();
            for (Integer key : map.keySet()) {
                Integer price=price2*map.get(key)/100;
                //通过循环等级确定各个等级的价格
                switch (key){
                    case 0:
                        item.setQuasiAngelPrice(price);  //准天使——价格
                        break;
                    case 1:
                        item.setAngelPrice(price);      //天使——价格
                        break;
                    case 2:
                        item.setGoldPrice(price);       //金卡会员——价格
                        break;
                    case 3:
                        item.setPlatinumPrice(price);   //铂金会员——价格
                        break;
                    case 4:
                        item.setBlackPrice(price);      //黑卡会员——价格
                        break;
                    case 5:
                        item.setPartnerPrice(price);    //合伙人——价格
                        break;
                    case 6:
                        item.setComPrice(price);        //公司——价格
                        break;
                }
            }

        }else if(type==2){
            if(price1==null||price2==null){

            }
            for (Integer key : map.keySet()) {
                Integer price=   (price2-price1)*map.get(key)/100+price1;
                switch (key){
                    case 0:
                        item.setQuasiAngelPrice(price);      //准天使——价格
                        break;
                    case 1:
                        item.setAngelPrice(price);           //天使——价格
                        break;
                    case 2:
                        item.setGoldPrice(price);           //金卡会员——价格
                        break;
                    case 3:
                        item.setPlatinumPrice(price);         //铂金会员——价格
                        break;
                    case 4:
                        item.setBlackPrice(price);          //黑卡会员——价格
                        break;
                    case 5:
                        item.setPartnerPrice(price);        //合伙人——价格
                        break;
                    case 6:
                        item.setComPrice(price);            //公司——价格
                        break;
                }


            }
        }

        return item;
    }





}
