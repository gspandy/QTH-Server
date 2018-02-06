package com.nowbook.sdp.service;

import com.nowbook.common.model.PageInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.dao.LevelDao;
import com.nowbook.sdp.dao.SystemConfRedisDao;
import com.nowbook.sdp.dao.UserEarningsBonusesDao;
import com.nowbook.sdp.dao.UserWalletDao;
import com.nowbook.sdp.model.DistributorSet;
import com.nowbook.sdp.model.Level;
import com.nowbook.sdp.model.UserEarningsBonuses;
import com.nowbook.sdp.model.UserWallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Romo on 2017/8/22.
 */
@Slf4j
@Service
public class SystemConfServiceImpl implements SystemConfService {

    @Autowired
    private SystemConfRedisDao systemConfRedisDao;

    @Autowired
    private LevelDao levelDao;

    @Autowired
    private UserEarningsBonusesDao userEarningsBonusesDao;

    @Autowired
    private UserWalletDao userWalletDao;

    private Properties prop1;

    private Properties prop2;

    // 获取目前所有返润设置
    @Override
    public Response<Map<String, Object>> getProfitDates() {
        Response<Map<String, Object>> result = new Response<Map<String, Object>>();
        Map<String, Object> resultMap = new HashMap<String, Object>();

        Map<String, String> map1 = systemConfRedisDao.getProfitDates();
        List<DistributorSet> list1 = new ArrayList<DistributorSet>();
        for (Map.Entry<String, String> entry : map1.entrySet()) {
            DistributorSet model = new DistributorSet();
            String key = entry.getKey();
            String value = entry.getValue();
            if (!"".equals(getDistributorCofig().getProperty(key))) {
                model.setKey(getDistributorCofig().getProperty(key));
            } else {
                model.setKey(key);
            }
            model.setValue(value);
            list1.add(model);
        }

        Map<String, String> map2 = systemConfRedisDao.getOthers();
        List<DistributorSet> list2 = new ArrayList<DistributorSet>();
        for (Map.Entry<String, String> entry : map2.entrySet()) {
            DistributorSet model = new DistributorSet();
            String key = entry.getKey();
            String value = entry.getValue();
            if (!"".equals(getOtherCofig().getProperty(key))) {
                model.setKey(getOtherCofig().getProperty(key));
            } else {
                model.setKey(key);
            }
            model.setValue(value);
            list2.add(model);
        }

        List<Level> levels = levelDao.selectByLevel(null);

        resultMap.put("profitConf", list1);
        resultMap.put("memberConf", levels);
        resultMap.put("otherConf", list2);
        result.setResult(resultMap);

        return result;
    }

    // 获取返润结算明细
    @Override
    public Response<Map<String, Object>> selectOrderDetail(String createTime,
                                                           String startAt,
                                                           String endAt,
                                                           String mobile,
                                                           Long orderItemId,
                                                           Integer pageNo,
                                                           Integer size) throws ParseException {
        Response<Map<String, Object>> result = new Response<Map<String, Object>>();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        PageInfo page = new PageInfo(pageNo, size);
        UserEarningsBonuses userEarningsBonuses = new UserEarningsBonuses();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        if (createTime != null && !createTime.equals("")) {
            Date endDate = format.parse(createTime);
            userEarningsBonuses.setEndAt(endDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.add(Calendar.DATE, -6);
            userEarningsBonuses.setStartAt(cal.getTime());
        }
        if (startAt != null && !startAt.equals("")) {
            Date startDate = format1.parse(startAt);
            userEarningsBonuses.setStartAt(startDate);
        }
        if (endAt != null && !endAt.equals("")) {
            Date endDate = format1.parse(endAt);
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.add(Calendar.DATE, 1);
            userEarningsBonuses.setEndAt(cal.getTime());
        }
        userEarningsBonuses.setMobile(mobile);
        userEarningsBonuses.setOrderItemId(orderItemId);
        userEarningsBonuses.setOffset(page.getOffset());
        userEarningsBonuses.setLimit(page.getLimit());
        Paging<UserEarningsBonuses> list = userEarningsBonusesDao.selectOrderDetail(userEarningsBonuses);
        resultMap.put("list", list);

        UserWallet userWallet = new UserWallet();
        if (mobile != null && !mobile.equals("")) {
            userWallet = userWalletDao.selectByMobile(mobile);
        }
        resultMap.put("userWallet", userWallet);

        String title = "付款明细";
        if (mobile != null && !mobile.equals("") && createTime != null && !createTime.equals("")) {
            title = title + "（" + format1.format(userEarningsBonuses.getStartAt()) +
                    " - " + format1.format(userEarningsBonuses.getEndAt()) + "）";
        }
        resultMap.put("title", title);

        result.setResult(resultMap);
        return result;
    }

    // 返润日期设置
    @Override
    public void updateProfitDate(Map<String, String> map) {
        systemConfRedisDao.updateProfitDate(map);
    }

    // 其他设置
    @Override
    public void updateOthers(Map<String, String> map) {
        systemConfRedisDao.updateOthers(map);
    }

    private Properties getDistributorCofig() {
        if (prop1 == null) {
            prop1 = new Properties();
            try {
                prop1.load(new InputStreamReader(SystemConfServiceImpl.class.getClassLoader().getResourceAsStream("profitConf.properties"), "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return prop1;
    }

    private Properties getOtherCofig() {
        if (prop2 == null) {
            prop2 = new Properties();
            try {
                prop2.load(new InputStreamReader(SystemConfServiceImpl.class.getClassLoader().getResourceAsStream("otherConf.properties"), "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return prop2;
    }

}
