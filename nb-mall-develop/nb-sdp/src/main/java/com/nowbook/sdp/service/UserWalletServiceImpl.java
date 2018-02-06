package com.nowbook.sdp.service;



import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.enums.NoticeType;
import com.nowbook.event.PushEvent;
import com.nowbook.event.PushEventBus;
import com.nowbook.sdp.dao.*;
import com.nowbook.sdp.model.*;
import com.nowbook.user.model.User;
import com.nowbook.user.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wangdongchang on 16-04-24
 */
@Service
public class UserWalletServiceImpl implements UserWalletService {


    private final static Logger log = LoggerFactory.getLogger(UserWalletServiceImpl.class);
    @Autowired
    private UserWalletDao userWalletDao;

    @Autowired
    private UserWalletSummaryDao userWalletSummaryDao;

    @Autowired
    private UserTeamMemberDao userTeamMemberDao;

    @Autowired
    private UserTeamDao userTeamDao;

    @Autowired
    private UserLevelDao userLevelDao;

    @Autowired
    private LevelDao levelDao;

    @Autowired
    PushEventBus pushEventBus;

    @Autowired
    private UserEarningsBonusesDao userEarningsBonusesDao;

    @Autowired
    private UserTeamMemberSelectDao userTeamMemberSelectDao;

    @Override
    public Response<UserWallet> selectUserWallet(Long userId) {
        Response<UserWallet> result = new Response<UserWallet>();
        UserWallet userWallet = new UserWallet();
        userWallet.setUserId(userId);
        List<UserWallet> userWalletList = userWalletDao.selectBy(userWallet);
        if(userWalletList !=null && userWalletList.size() !=0){
            result.setResult(userWalletList.get(0));
        }else{
            result.setError("没找到此用户钱包。");
        }
        return result;
    }

    @Override
    public Response<Map<String,Object>> selectUserWalletSummary(UserWalletSummary userWalletSummary) {
        Response<Map<String,Object>> result = new Response<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String, Object>();
        if(userWalletSummary.getPageNo() ==null || userWalletSummary.getPageNo() <=0){
            userWalletSummary.setPageNo(1);
        }
        if(userWalletSummary.getPageSize() ==null || userWalletSummary.getPageSize() <=0){
            userWalletSummary.setPageSize(10);
        }

        userWalletSummary.setOffset((userWalletSummary.getPageNo()-1)*userWalletSummary.getPageSize());
        userWalletSummary.setLimit(userWalletSummary.getPageSize());

        List<UserWalletSummary> userWalletSummaryList = userWalletSummaryDao.selectBy(userWalletSummary);


        List<Object> newUserWalletList1 = new ArrayList<Object>();
        List<UserWalletSummary> newUserWalletList2 = new ArrayList<UserWalletSummary>();
        Map<String,Object> uwsMap = new HashMap<String,Object>();
        Integer year = 0;
        Integer num = 1;
        for(UserWalletSummary uws : userWalletSummaryList){
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(uws.getCreateAt());
            Integer uwsYear = calendar1.get(calendar1.YEAR);

            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(new Date());
            Integer nowYear = calendar2.get(calendar2.YEAR);


            SimpleDateFormat format1= new SimpleDateFormat("MM-dd");
            uws.setDay(format1.format(uws.getCreateAt()));
            SimpleDateFormat format2= new SimpleDateFormat("HH:mm");
            uws.setTime(format2.format(uws.getCreateAt()));

            if(uws.getRealName() !=null && !uws.getRealName().equals("")){
                uws.setOtherName(uws.getRealName());
            }else if(uws.getNick() !=null && !uws.getNick().equals("")){
                uws.setOtherName(uws.getNick());
            }else if(uws.getMobile() !=null && !uws.getMobile().equals("")){
                uws.setOtherName(uws.getMobile());
            }else {
                uws.setOtherName("");
            }

            uws.makeMessage();

            if(uwsYear.equals(year)){
                newUserWalletList2.add(uws);
            }else{
                if(year !=0){
                    if(year.equals(nowYear)){
                        uwsMap.put("year","now");
                    }else {
                        uwsMap.put("year",year);
                    }
                    uwsMap.put("data",newUserWalletList2);
                    newUserWalletList1.add(uwsMap);

                    uwsMap = new HashMap<String,Object>();
                    newUserWalletList2 = new ArrayList<UserWalletSummary>();
                }
                year = uwsYear;
                newUserWalletList2.add(uws);
            }


            if(num == userWalletSummaryList.size()){
                if(year.equals(nowYear)){
                    uwsMap.put("year","now");
                }else {
                    uwsMap.put("year",year);
                }
                uwsMap.put("data",newUserWalletList2);
                newUserWalletList1.add(uwsMap);

                uwsMap = new HashMap<String,Object>();
                newUserWalletList2 = new ArrayList<UserWalletSummary>();
            }
            num++;
        }

        map.put("userWalletSummary",newUserWalletList1);

        UserWalletSummary userWalletSummaryNum = userWalletSummaryDao.selectNum(userWalletSummary);
        map.put("total",userWalletSummaryNum.getTotal());

        map.put("pageNo",userWalletSummary.getPageNo());
        map.put("pageSize",userWalletSummary.getPageSize());

        result.setResult(map);
        return result;
    }

    @Override
    public Response<Map<String, Object>> selectUserWalletSummaryForAdmin(@ParamInfo("userId") @Nullable Long userId, @ParamInfo("type") @Nullable String type, @ParamInfo("pageNo") @Nullable Integer pageNo, @ParamInfo("size") @Nullable Integer pageSize) {

        if(userId ==null || userId.equals(0L)){
            return null;
        }
        Response<Map<String, Object>> response = new Response<Map<String, Object>>();
        UserWalletSummary userWalletSummary = new UserWalletSummary();
        userWalletSummary.setUserId(userId);
        if(type ==null || type.equals("")){
            userWalletSummary.setType1(1);
            userWalletSummary.setType2(56);
        }else if(type.equals("0")){
            userWalletSummary.setType1(1);
            userWalletSummary.setType2(12);
        }else if(type.equals("1")){
            userWalletSummary.setType1(21);
            userWalletSummary.setType2(26);
        }else if(type.equals("2")){
            userWalletSummary.setType1(31);
            userWalletSummary.setType2(34);
        }else if(type.equals("3")){
            userWalletSummary.setType1(41);
            userWalletSummary.setType2(46);
        }else if(type.equals("4")){
            userWalletSummary.setType1(51);
            userWalletSummary.setType2(56);
        }
        userWalletSummary.setPageNo(pageNo);
        userWalletSummary.setPageSize(pageSize);
        if(userWalletSummary.getPageNo() ==null || userWalletSummary.getPageNo() <=0){
            userWalletSummary.setPageNo(1);
        }
        if(userWalletSummary.getPageSize() ==null || userWalletSummary.getPageSize() <=0){
            userWalletSummary.setPageSize(10);
        }

        userWalletSummary.setOffset((userWalletSummary.getPageNo()-1)*userWalletSummary.getPageSize());
        userWalletSummary.setLimit(userWalletSummary.getPageSize());

        List<UserWalletSummary> userWalletSummaryList = userWalletSummaryDao.selectBy(userWalletSummary);
        for(UserWalletSummary uws :userWalletSummaryList){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time= sdf.format(uws.getCreateAt());
            uws.setCreateTime(time);

            if(uws.getRealName() !=null && !uws.getRealName().equals("")){
                uws.setOtherName(uws.getRealName());
            }else if(uws.getNick() !=null && !uws.getNick().equals("")){
                uws.setOtherName(uws.getNick());
            }else if(uws.getMobile() !=null && !uws.getMobile().equals("")){
                uws.setOtherName(uws.getMobile());
            }else {
                uws.setOtherName("");
            }

            uws.makeMessage();
        }
        response.setResult(new HashMap<String, Object>());
        response.getResult().put("userWalletSummary",userWalletSummaryList);

        UserWalletSummary userWalletSummaryNum = userWalletSummaryDao.selectNum(userWalletSummary);
        response.getResult().put("total",userWalletSummaryNum.getTotal());
        response.getResult().put("pageNo",userWalletSummary.getPageNo());
        response.getResult().put("pageSize",userWalletSummary.getPageSize());


        Response<UserWallet> response1 =selectUserWallet(userId);
        response.getResult().put("userWallet",response1.getResult());
        UserTeamMemberSelect userTeamMemberSelect = new UserTeamMemberSelect();
        userTeamMemberSelect.setUserId(userId);
        userTeamMemberSelect.setLimit(20);
        userTeamMemberSelect.setOffset(0);
        List<UserTeamMemberSelect> userTeamMemberSelects = userTeamMemberSelectDao.selectUser(userTeamMemberSelect);
        response.getResult().put("userTeamMemberSelect",userTeamMemberSelects.get(0));
        return  response;
    }

    @Override
    public Response<Boolean> updateUserWallet(UserWalletSummary userWalletSummary) {
        Response<Boolean> result = new Response<Boolean>();

        if(userWalletSummary.getUserId() ==null || userWalletSummary.getType() == null || userWalletSummary.getMoney() ==null){
            result.setError("基础数据不存在");
            return result;
        }

        if(userWalletSummary.getMoney().equals(0L)){
            result.setError("钱数不能为0");
            return result;
        }

        Long userId = userWalletSummary.getUserId();

        UserWallet userWallet = new UserWallet();
        userWallet.setUserId(userId);
        List<UserWallet> userWalletList = userWalletDao.selectBy(userWallet);
        if(userWalletList == null || userWalletList.size() == 0){
            result.setError("用户钱包不存在");
            return result;
        }

        Long money = userWalletSummary.getMoney();

        UserWallet newUserWallet = new UserWallet();
        switch (userWalletSummary.getType()){
            //余额
            case 1:
                //充值升级时余额增加，需要字段userId,type,money,level
            case 3:
                //退款时余额增加，需要字段userId,type,money
            case 5:
                //退款时余额增加，并伴随着升级，需要字段userId,type,money,level
            case 6:
                //自己消费商品时余额减少，需要字段userId,type,money
            case 8:
                //自己升级时余额换算时余额减少，需要字段userId,type,money,level
            case 10:
                //下级升级向你进货时余额减少，需要字段userId,type,money,otherId,otherMoney,otherLevel
            case 12:
                //下级购买商品向你进货时余额减少，需要字段userId,type,money,otherId,otherMoney
                Long balance = userWalletList.get(0).getBalance();
                Long newBalance = balance+money;

                newUserWallet.setUserId(userId);
                newUserWallet.setBalance(newBalance);
                userWalletDao.update(newUserWallet);

                updateUserWalletSummary(userWalletSummary);

                break;
            //预存款
            case 21:
                //自己充值时预存款增加，需要字段userId,type,money
            case 23:
                //自己充值升级时预存款增加，需要字段userId,type,money,level
            case 25:
                //退款时预存款增加,需要字段userId,type,money
            case 26:
                //自己消费时预存款减少，需要字段userId,type,money
                Long advance = userWalletList.get(0).getAdvance();
                Long newAdvance = advance+money;

                newUserWallet.setUserId(userId);
                newUserWallet.setAdvance(newAdvance);
                userWalletDao.update(newUserWallet);

                updateUserWalletSummary(userWalletSummary);

                break;
            //保证金
            case 31:
                //自己升级时保证金增加，需要字段userId,type,money,level
            case 33:
                //合伙人每期奖励使保证金增加，需要字段userId,type,money
            case 34:
                //自己退会时保证金减少，需要字段userId,type,money
                Long deposit = userWalletList.get(0).getDeposit();
                Long newDeposit = deposit+money;

                newUserWallet.setUserId(userId);
                newUserWallet.setDeposit(newDeposit);
                userWalletDao.update(newUserWallet);

                updateUserWalletSummary(userWalletSummary);

                break;
            //待发放收益
            case 41:
                //下级升级进货时待发放收益增加，需要字段userId,type,money,otherId,otherMoney,otherLevel
            case 43:
                //下级购买商品时待发放收益增加，需要字段userId,type,money,otherId,otherMoney
            case 44:
                //下级退货时待发放收益减少，需要字段userId,type,money,otherId,otherMoney
            case 46:
                //结算过后待发放收益减少，需要字段userId,type,money
                Long pendingEarnings = userWalletList.get(0).getPendingEarnings();
                Long newPendingEarnings = pendingEarnings+money;

                newUserWallet.setUserId(userId);
                newUserWallet.setPendingEarnings(newPendingEarnings);
                userWalletDao.update(newUserWallet);

                updateUserWalletSummary(userWalletSummary);


                break;
            //待发放奖金
            case 51:
                //被你推荐的人升级进货时待发放奖金增加，需要字段userId,type,money,otherId,otherMoney,otherLevel
            case 53:
                //被你推荐的人购买商品时待发放奖金增加，需要字段userId,type,money,otherId,otherMoney
            case 54:
                //被你推荐的人退货时待发放奖金减少，需要字段userId,type,money,otherId,otherMoney
            case 56:
                //结算后待发放奖金减少，需要字段userId,type,money
                Long pendingBonuses = userWalletList.get(0).getPendingBonuses();
                Long newPendingBonuses = pendingBonuses+money;

                newUserWallet.setUserId(userId);
                newUserWallet.setPendingBonuses(newPendingBonuses);
                userWalletDao.update(newUserWallet);

                updateUserWalletSummary(userWalletSummary);


                break;
            //待支付邮费
            case 61:
                //自己购买商品时待支付邮费增加，需要字段userId,type,money
            case 62:
                //结算支付时待支付邮费减少，需要字段userId,type,money
                Long unpaidDeliverFee2 = userWalletList.get(0).getPendingBonuses();
                Long newUnpaidDeliverFee2 = unpaidDeliverFee2+money;

                newUserWallet.setUserId(userId);
                newUserWallet.setUnpaidDeliverFee(newUnpaidDeliverFee2);
                userWalletDao.update(newUserWallet);

                updateUserWalletSummary(userWalletSummary);
                break;
            //已结算收益
            case 71:
                //结算后已结算收益增加，需要字段userId,type,money
                Long totalEarnings = userWalletList.get(0).getTotalEarnings();
                Long newTotalEarnings = totalEarnings+money;

                newUserWallet.setUserId(userId);
                newUserWallet.setTotalEarnings(newTotalEarnings);
                userWalletDao.update(newUserWallet);

                updateUserWalletSummary(userWalletSummary);
                break;
            //已结算奖金
            case 81:
                //结算后已结算奖金增加，需要字段userId,type,money
                Long totalBonuses = userWalletList.get(0).getTotalBonuses();
                Long newTotalBonuses = totalBonuses+money;

                newUserWallet.setUserId(userId);
                newUserWallet.setTotalBonuses(newTotalBonuses);
                userWalletDao.update(newUserWallet);

                updateUserWalletSummary(userWalletSummary);
                break;
            //已支付邮费
            case 91:
                //结算后已支付邮费增加，需要字段userId,type,money
                Long totalDeliverFee = userWalletList.get(0).getTotalDeliverFee();
                Long newTotalDeliverFee = totalDeliverFee+money;

                newUserWallet.setUserId(userId);
                newUserWallet.setTotalDeliverFee(newTotalDeliverFee);
                userWalletDao.update(newUserWallet);

                updateUserWalletSummary(userWalletSummary);
                break;
        }
        result.setResult(true);
        return result;
    }

    @Override
    public Response<UserWalletSummary> selectByPayCode(UserWalletSummary userWalletSummary) {
        Response<UserWalletSummary> result = new Response<UserWalletSummary>();
        List<UserWalletSummary> userWalletSummaryList = userWalletSummaryDao.selectByPayCode(userWalletSummary);
        if(userWalletSummaryList !=null && userWalletSummaryList.size()>0){
            result.setResult(userWalletSummaryList.get(0));
        }else{
            result.setResult(null);
        }
        return result;
    }

    @Override
    public Response<Boolean> profit(UserWalletSummary userWalletSummary) {
        Response<Boolean> result = new Response<Boolean>();

        if(userWalletSummary.getUserId() == null || userWalletSummary.getMoney() == null || userWalletSummary.getMoneyType() == null){
            result.setError("基础数据不全。");
            return result;
        }
        if(userWalletSummary.getMoney() <=0){
            result.setError("钱不能为负数。");
            return result;
        }

        //先查找这个用户的等级
        UserLevel userLevel = new UserLevel();
        userLevel.setUserId(userWalletSummary.getUserId());
        List<UserLevel> userLevelList = userLevelDao.selectByUserId(userLevel);
        if(userLevelList ==null || userLevelList.size() ==0){
            result.setError("此用户不存在。");
            return result;
        }

        //查找下此用户的等级信息
        Level level = new Level();
        level.setLevel(userLevelList.get(0).getLevel());
        List<Level> levelList = levelDao.selectByLevel(level);
        if(levelList == null || levelList.size() == 0){
            result.setError("等级没有设置。");
            return result;
        }

        Long member = userWalletSummary.getUserId();
        //实际消费
        Long money = userWalletSummary.getMoney();
        //商品全价
        Long realMoney = userWalletSummary.getRealMoney();

        //查找推荐人等级
        UserLevel inviterUserLevel = new UserLevel();
        inviterUserLevel.setUserId(userLevelList.get(0).getInviter());
        List<UserLevel> inviterUserLevelList = userLevelDao.selectByUserId(inviterUserLevel);
        if(inviterUserLevelList ==null || inviterUserLevelList.size() ==0){
            result.setError("推荐人不存在。");
            return result;
        }

        //查找黑卡以上推荐人等级
        UserLevel blackInviterUserLevel = new UserLevel();
        blackInviterUserLevel.setUserId(userLevelList.get(0).getBlackInviter());
        List<UserLevel> blackInviterUserLevelList = userLevelDao.selectByUserId(blackInviterUserLevel);

        if(userWalletSummary.getMoneyType().equals(1) || userWalletSummary.getMoneyType().equals(4) ){
            //购买自营区或者下级升级时
            //奖金
            Long bonuses = 0L;
            Long blackBonuses = 0L;
            //根据被推荐人不同等级来计算奖金
            if(userLevelList.get(0).getLevel() <4){
                //被推荐人为黑卡以下
                if(inviterUserLevelList.get(0).getLevel()>0 && inviterUserLevelList.get(0).getLevel()<6){
                    //只要是非天使以上公司以下，就分到10%（可配置）的推荐奖
                    Level inviterLevel = new Level();
                    inviterLevel.setLevel(inviterUserLevelList.get(0).getLevel());
                    List<Level> inviterLevelList = levelDao.selectByLevel(level);
                    if(inviterLevelList == null || inviterLevelList.size() == 0){
                        result.setError("等级没有设置。");
                        return result;
                    }
                    bonuses = money * inviterLevelList.get(0).getBonusDiscount()/100;
                }
            }else if(userLevelList.get(0).getLevel().equals(4)){
                //被推荐人是黑卡
                Level inviterLevel = new Level();
                inviterLevel.setLevel(inviterUserLevelList.get(0).getLevel());
                List<Level> inviterLevelList = levelDao.selectByLevel(inviterLevel);
                if(inviterLevelList == null || inviterLevelList.size() == 0){
                    result.setError("等级没有设置。");
                    return result;
                }
                if(inviterUserLevelList.get(0).getLevel()>=1 && inviterUserLevelList.get(0).getLevel() <4){
                    //推荐人为天使到白金之间，就分到10%（可配置）的推荐奖，同时他的黑卡上级分到另外10%（可配置）的推荐奖。
                    if(blackInviterUserLevelList ==null || blackInviterUserLevelList.size() ==0){
                        result.setError("黑卡以上推荐人不存在。");
                        return result;
                    }

                    Level blackInviterLevel = new Level();
                    blackInviterLevel.setLevel(blackInviterUserLevelList.get(0).getLevel());
                    List<Level> blackInviterLevelList = levelDao.selectByLevel(blackInviterLevel);
                    if(blackInviterLevelList == null || blackInviterLevelList.size() == 0){
                        result.setError("等级没有设置。");
                        return result;
                    }
                    bonuses = money * inviterLevelList.get(0).getBonusDiscount()/100;
                    blackBonuses = money * blackInviterLevelList.get(0).getBonusDiscount()/100;


                }else if(inviterUserLevelList.get(0).getLevel()>=4 && inviterUserLevelList.get(0).getLevel()<6){
                    //推荐人为黑卡和合伙人时，就分到20%（可配置）的推荐奖。
                    bonuses = money * inviterLevelList.get(0).getBonusDiscount()/100;

                    Level otherInviterLevel = new Level();
                    otherInviterLevel.setLevel(3);
                    List<Level> otherInviterLevelList = levelDao.selectByLevel(otherInviterLevel);
                    if(otherInviterLevelList == null || otherInviterLevelList.size() == 0){
                        result.setError("等级没有设置。");
                        return result;
                    }
                    Long otherBonuses = money * otherInviterLevelList.get(0).getBonusDiscount()/100;
                    bonuses = bonuses + otherBonuses;
                }
            }else if(userLevelList.get(0).getLevel().equals(5)){
                //被推荐人为合伙人
                if(inviterUserLevelList.get(0).getLevel()>=4 && inviterUserLevelList.get(0).getLevel()<6){
                    Level inviterLevel = new Level();
                    inviterLevel.setLevel(inviterUserLevelList.get(0).getLevel());
                    List<Level> inviterLevelList = levelDao.selectByLevel(inviterLevel);
                    if(levelList == null || levelList.size() == 0){
                        result.setError("等级没有设置。");
                        return result;
                    }
                    bonuses = money * inviterLevelList.get(0).getTalentDiscount()/100;

                }else{
                    result.setError("此合伙人推荐人不是黑卡以上。");
                    return result;
                }
            }

            //开始创建奖金明细
            UserWalletSummary newUserWalletSummary  = new UserWalletSummary();
            newUserWalletSummary.setUserId(userLevelList.get(0).getInviter());
            if(userWalletSummary.getMoneyType().equals(3)){
                //推荐人升级
                newUserWalletSummary.setType(51);
                newUserWalletSummary.setOtherLevel(userWalletSummary.getLevel());
            }else{
                //购买商品
                if(userLevelList.get(0).getLevel().equals(5)){
                    newUserWalletSummary.setType(57);
                }else{
                    newUserWalletSummary.setType(53);
                }
            }
            newUserWalletSummary.setMoney(bonuses);
            newUserWalletSummary.setOtherId(userWalletSummary.getUserId());
            newUserWalletSummary.setOtherMoney(money);
            updateUserWallet(newUserWalletSummary);


            UserEarningsBonuses userEarningsBonuses = new UserEarningsBonuses();
            userEarningsBonuses.setUserId(userLevelList.get(0).getInviter());
            if(userWalletSummary.getMoneyType().equals(3)) {
                //推荐人升级
                userEarningsBonuses.setType(51);
            }else{
                //购买商品
                if(userLevelList.get(0).getLevel().equals(5)){
                    newUserWalletSummary.setType(57);
                }else{
                    newUserWalletSummary.setType(53);
                }
            }
            userEarningsBonuses.setMoneyType(2);
            userEarningsBonuses.setMoney(bonuses);
            userEarningsBonuses.setStatus(1);
            userEarningsBonuses.setFromId(userWalletSummary.getUserId());
            userEarningsBonuses.setOrderItemId(userWalletSummary.getOrderItemId());
            if(bonuses>0){
                userEarningsBonusesDao.insert(userEarningsBonuses);
            }

            if(blackBonuses >0){
                UserWalletSummary newBlackUserWalletSummary  = new UserWalletSummary();
                newBlackUserWalletSummary.setUserId(userLevelList.get(0).getBlackInviter());
                if(userWalletSummary.getMoneyType().equals(4)) {
                    newBlackUserWalletSummary.setType(51);
                    newBlackUserWalletSummary.setOtherLevel(userWalletSummary.getLevel());
                }else{
                    newBlackUserWalletSummary.setType(53);
                }
                newBlackUserWalletSummary.setMoney(blackBonuses);
                newBlackUserWalletSummary.setOtherId(userWalletSummary.getUserId());
                newBlackUserWalletSummary.setOtherMoney(money);
                updateUserWallet(newBlackUserWalletSummary);

                UserEarningsBonuses blackUserEarningsBonuses = new UserEarningsBonuses();
                blackUserEarningsBonuses.setUserId(userLevelList.get(0).getBlackInviter());
                if(userWalletSummary.getMoneyType().equals(4)) {
                    blackUserEarningsBonuses.setType(51);
                }else{
                    blackUserEarningsBonuses.setType(53);
                    blackUserEarningsBonuses.setOrderItemId(userWalletSummary.getOrderItemId());
                }
                blackUserEarningsBonuses.setMoneyType(2);
                blackUserEarningsBonuses.setMoney(blackBonuses);
                blackUserEarningsBonuses.setStatus(1);
                blackUserEarningsBonuses.setFromId(userWalletSummary.getUserId());
                if(blackBonuses>0){
                    userEarningsBonusesDao.insert(blackUserEarningsBonuses);
                }
            }

            Integer num = 0;
            //上级折扣
            Integer highDiscount = 0;
            //下级折扣
            Integer lowDiscount = levelList.get(0).getDiscount();
            //待分余额
            Long waitBalance = money;

            //收益
            while (num<10){
                //查找所属团队
                UserTeamMember userTeamMember = new UserTeamMember();
                userTeamMember.setMember(member);
                userTeamMember.setRole("MEMBER");
                List<UserTeamMember> userTeamMemberList = userTeamMemberDao.selectBy(userTeamMember);
                if (userTeamMemberList == null || userTeamMemberList.size() == 0) {
                    result.setError("团队不存在。");
                    return result;
                }

                //查找所属团队的领队
                Long teamId = userTeamMemberList.get(0).getTeam();
                UserTeam userTeam = new UserTeam();
                userTeam.setId(teamId);
                List<UserTeam> leaderUserTeamList = userTeamDao.selectBy(userTeam);
                if (leaderUserTeamList == null || leaderUserTeamList.size() == 0) {
                    result.setError("团队领队不存在。");
                    return result;
                }

                //查找领队等级
                UserLevel leaderUserLevel = new UserLevel();
                leaderUserLevel.setUserId(leaderUserTeamList.get(0).getLeader());
                List<UserLevel> leaderUserLevelList = userLevelDao.selectByUserId(leaderUserLevel);
                if (leaderUserLevelList == null || leaderUserLevelList.size() == 0) {
                    result.setError("上级等级不存在。");
                    return result;
                }

                //如果等级大于合伙人，直接结束分润
                if(leaderUserLevelList.get(0).getLevel()>5){
                    break;
                }

                //查找领队等级信息
                Level leaderLevel = new Level();
                leaderLevel.setLevel(leaderUserLevelList.get(0).getLevel());
                List<Level>  leaderLevelList= levelDao.selectByLevel(leaderLevel);
                if (leaderLevelList == null || leaderLevelList.size() == 0) {
                    result.setError("上级等级信息不存在。");
                    return result;
                }

                highDiscount = leaderLevelList.get(0).getDiscount();

                //余额收益
                Long balanceEarnings = 0L;
                //管理收益
                Long manageEarnings = 0L;
                //总收益
                Long totalEarnings = 0L;
                //扣除的余额
                Long deductionBalance = 0L;
                //计算余额收益
                if(waitBalance >0){
                    //待分余额大于0时开始分余额收益
                    if(leaderUserLevelList.get(0).getLevel().equals(5)){
                        //上级为合伙人时，不参与余额收益计算
                        balanceEarnings = 0L;
                        Long leaderMoney = waitBalance*highDiscount/lowDiscount;
                        //查找合伙人推荐人
                        UserLevel partnerInviterUserLevel = new UserLevel();
                        partnerInviterUserLevel.setUserId(userLevelList.get(0).getInviter());
                        List<UserLevel> partnerInviterUserLevelList = userLevelDao.selectByUserId(partnerInviterUserLevel);
                        if(partnerInviterUserLevelList ==null || partnerInviterUserLevelList.size() ==0){
                            result.setError("推荐人不存在。");
                            return result;
                        }
                        if(partnerInviterUserLevelList.get(0).getLevel()>=4 && partnerInviterUserLevelList.get(0).getLevel()<6){
                            Level partnerInviterLevel = new Level();
                            partnerInviterLevel.setLevel(partnerInviterUserLevelList.get(0).getLevel());
                            List<Level> partnerInviterLevelList = levelDao.selectByLevel(partnerInviterLevel);
                            if(partnerInviterLevelList == null || partnerInviterLevelList.size() == 0){
                                result.setError("等级没有设置。");
                                return result;
                            }
                            Long talentBonuses = money * partnerInviterLevelList.get(0).getTalentDiscount()/100;

                            //开始创建收益明细
                            UserWalletSummary newUserWalletSummaryForTalentBonuses  = new UserWalletSummary();
                            newUserWalletSummaryForTalentBonuses.setUserId(partnerInviterUserLevelList.get(0).getUserId());
                            newUserWalletSummaryForTalentBonuses.setType(55);
                            newUserWalletSummaryForTalentBonuses.setMoney(talentBonuses);
                            newUserWalletSummaryForTalentBonuses.setOtherId(leaderUserLevelList.get(0).getUserId());
                            newUserWalletSummaryForTalentBonuses.setOtherMoney(leaderMoney);

                            updateUserWallet(newUserWalletSummaryForTalentBonuses);


                            UserEarningsBonuses talentEarningsBonuses = new UserEarningsBonuses();
                            talentEarningsBonuses.setUserId(partnerInviterUserLevelList.get(0).getUserId());
                            talentEarningsBonuses.setType(55);
                            talentEarningsBonuses.setMoneyType(2);
                            talentEarningsBonuses.setMoney(talentBonuses);
                            talentEarningsBonuses.setStatus(1);
                            talentEarningsBonuses.setFromId(leaderUserLevelList.get(0).getUserId());
                            if(totalEarnings>0){
                                userEarningsBonusesDao.insert(talentEarningsBonuses);
                            }
                        }
                    }

                    //金卡到黑卡之间，计算余额收益
                    if(leaderUserLevelList.get(0).getLevel()<=4 && leaderUserLevelList.get(0).getLevel()>=2){
                        //先查找上级用户钱包
                        UserWallet leaderUserWallet = new UserWallet();
                        leaderUserWallet.setUserId(leaderUserLevelList.get(0).getUserId());
                        List<UserWallet> leaderUserWalletList = userWalletDao.selectBy(leaderUserWallet);
                        if(leaderUserWalletList ==null && leaderUserWalletList.size() ==0) {
                            result.setError("上级钱包不存在。");
                            return result;
                        }

                        //上级原余额
                        Long leaderBalance = leaderUserWalletList.get(0).getBalance();
                        //上级商品价格
                        Long leaderMoney = waitBalance*highDiscount/lowDiscount;
                        if(leaderBalance >= leaderMoney){
                            deductionBalance = leaderMoney;
                            balanceEarnings = leaderMoney;
                            waitBalance = 0L;

                        }else {
                            deductionBalance = leaderBalance;
                            balanceEarnings = leaderBalance;
                            waitBalance = leaderMoney - leaderBalance;
                        }

                    }
                }

                //计算管理收益
                manageEarnings = realMoney * (lowDiscount - highDiscount)/100;

                if(num.equals(0)){
                    //需要跟推荐人分收益
                    totalEarnings = manageEarnings + balanceEarnings - bonuses - blackBonuses ;
                }else{
                    totalEarnings = manageEarnings + balanceEarnings;
                }

                //开始创建余额明细
                if(deductionBalance >0){
                    UserWalletSummary newUserWalletSummaryForBalance  = new UserWalletSummary();
                    newUserWalletSummaryForBalance.setUserId(leaderUserTeamList.get(0).getLeader());
                    if(userWalletSummary.getMoneyType().equals(4)){
                        //下级升级
                        newUserWalletSummaryForBalance.setType(10);
                        newUserWalletSummaryForBalance.setOtherLevel(userWalletSummary.getLevel());
                    }else{
                        //购买商品
                        newUserWalletSummaryForBalance.setType(12);
                    }
                    newUserWalletSummaryForBalance.setMoney(-deductionBalance);
                    newUserWalletSummaryForBalance.setOtherId(userWalletSummary.getUserId());
                    newUserWalletSummaryForBalance.setOtherMoney(money);
                    updateUserWallet(newUserWalletSummaryForBalance);
                }

                //开始创建收益明细
                UserWalletSummary newUserWalletSummaryForEarnings  = new UserWalletSummary();
                newUserWalletSummaryForEarnings.setUserId(leaderUserTeamList.get(0).getLeader());
                if(userWalletSummary.getMoneyType().equals(4)){
                    //下级升级
                    newUserWalletSummaryForEarnings.setType(41);
                    newUserWalletSummaryForEarnings.setOtherLevel(userWalletSummary.getLevel());
                }else{
                    //购买商品
                    newUserWalletSummaryForEarnings.setType(43);
                }
                newUserWalletSummaryForEarnings.setMoney(totalEarnings);
                newUserWalletSummaryForEarnings.setOtherId(userWalletSummary.getUserId());
                newUserWalletSummaryForEarnings.setOtherMoney(money);

                updateUserWallet(newUserWalletSummaryForEarnings);


                UserEarningsBonuses leaderUserEarningsBonuses = new UserEarningsBonuses();
                leaderUserEarningsBonuses.setUserId(leaderUserTeamList.get(0).getLeader());
                if(userWalletSummary.getMoneyType().equals(4)){
                    leaderUserEarningsBonuses.setType(41);
                }else{
                    leaderUserEarningsBonuses.setType(43);
                    leaderUserEarningsBonuses.setOrderItemId(userWalletSummary.getOrderItemId());
                }
                leaderUserEarningsBonuses.setMoneyType(1);
                leaderUserEarningsBonuses.setMoney(totalEarnings);
                leaderUserEarningsBonuses.setStatus(1);
                leaderUserEarningsBonuses.setFromId(userWalletSummary.getUserId());
                if(totalEarnings>0){
                    userEarningsBonusesDao.insert(leaderUserEarningsBonuses);
                }

                lowDiscount = leaderLevelList.get(0).getDiscount();
                member = leaderUserTeamList.get(0).getLeader();
                num++;
            }
        }else if(userWalletSummary.getMoneyType().equals(2) || userWalletSummary.getMoneyType().equals(3)){
            //购买优选区商品
            if(userLevelList.get(0).getLevel().equals(1)){
                //如果天使，只有推荐人是天使的时候分润
                if(inviterUserLevelList.get(0).getLevel().equals(1)){
                    Long bonuses = 0L;
                    if(userWalletSummary.getMoneyType().equals(2)){
                        bonuses = Long.valueOf(userWalletSummary.getJson().get("angelRecommendPrice").toString())*Long.valueOf(userWalletSummary.getJson().get("num").toString())*Long.valueOf(userWalletSummary.getJson().get("selfSupportDiscount").toString())/100;
                    }else if(userWalletSummary.getMoneyType().equals(3)){
                        bonuses = Long.valueOf(userWalletSummary.getJson().get("angelRecommendPrice").toString())*Long.valueOf(userWalletSummary.getJson().get("num").toString());
                    }
                    UserWalletSummary newUserWalletSummary  = new UserWalletSummary();
                    newUserWalletSummary.setUserId(userLevelList.get(0).getInviter());
                    newUserWalletSummary.setType(53);
                    newUserWalletSummary.setMoney(bonuses);
                    newUserWalletSummary.setOtherId(userWalletSummary.getUserId());
                    newUserWalletSummary.setOtherMoney(money);
                    updateUserWallet(newUserWalletSummary);


                    UserEarningsBonuses userEarningsBonuses = new UserEarningsBonuses();
                    userEarningsBonuses.setUserId(userLevelList.get(0).getInviter());
                    userEarningsBonuses.setType(53);
                    userEarningsBonuses.setMoneyType(2);
                    userEarningsBonuses.setMoney(bonuses);
                    userEarningsBonuses.setStatus(1);
                    userEarningsBonuses.setFromId(userWalletSummary.getUserId());
                    userEarningsBonuses.setOrderItemId(userWalletSummary.getOrderItemId());
                    if(bonuses>0){
                        userEarningsBonusesDao.insert(userEarningsBonuses);
                    }
                }
            }else if (userLevelList.get(0).getLevel()>=2 && userLevelList.get(0).getLevel()<=4){
                //只有金卡到黑卡之间给上级分钱
                Integer num = 0;
                //上级折扣
                Integer highDiscount = 0;
                //下级折扣
                Integer lowDiscount = levelList.get(0).getDiscount();
                Integer lowLevel = levelList.get(0).getLevel();
                Integer highLevel = 0;
                //收益
                while (num<10){
                    //查找所属团队
                    UserTeamMember userTeamMember = new UserTeamMember();
                    userTeamMember.setMember(member);
                    userTeamMember.setRole("MEMBER");
                    List<UserTeamMember> userTeamMemberList = userTeamMemberDao.selectBy(userTeamMember);
                    if (userTeamMemberList == null || userTeamMemberList.size() == 0) {
                        result.setError("团队不存在。");
                        return result;
                    }

                    //查找所属团队的领队
                    Long teamId = userTeamMemberList.get(0).getTeam();
                    UserTeam userTeam = new UserTeam();
                    userTeam.setId(teamId);
                    List<UserTeam> leaderUserTeamList = userTeamDao.selectBy(userTeam);
                    if (leaderUserTeamList == null || leaderUserTeamList.size() == 0) {
                        result.setError("团队领队不存在。");
                        return result;
                    }

                    //查找领队等级
                    UserLevel leaderUserLevel = new UserLevel();
                    leaderUserLevel.setUserId(leaderUserTeamList.get(0).getLeader());
                    List<UserLevel> leaderUserLevelList = userLevelDao.selectByUserId(leaderUserLevel);
                    if (leaderUserLevelList == null || leaderUserLevelList.size() == 0) {
                        result.setError("上级等级不存在。");
                        return result;
                    }

                    //如果等级大于合伙人，直接结束分润
                    if(leaderUserLevelList.get(0).getLevel()>5){
                        break;
                    }

                    //查找领队等级信息
                    Level leaderLevel = new Level();
                    leaderLevel.setLevel(leaderUserLevelList.get(0).getLevel());
                    List<Level>  leaderLevelList= levelDao.selectByLevel(leaderLevel);
                    if (leaderLevelList == null || leaderLevelList.size() == 0) {
                        result.setError("上级等级信息不存在。");
                        return result;
                    }

                    highDiscount = leaderLevelList.get(0).getDiscount();
                    highLevel = leaderLevelList.get(0).getLevel();
                    //管理收益
                    Long manageEarnings = 0L;

                    //计算管理收益
                    if(userWalletSummary.getMoneyType().equals(2)){
                        Long lowMoney = ((Long.valueOf(userWalletSummary.getJson().get("sellingPrice").toString())-Long.valueOf(userWalletSummary.getJson().get("purchasePrice").toString()))*lowDiscount/100*Long.valueOf(userWalletSummary.getJson().get("selfSupportDiscount").toString())/100+Long.valueOf(userWalletSummary.getJson().get("purchasePrice").toString())-Long.valueOf(userWalletSummary.getJson().get("discount").toString()))*Long.valueOf(userWalletSummary.getJson().get("num").toString());
                        Long highMoney = ((Long.valueOf(userWalletSummary.getJson().get("sellingPrice").toString())-Long.valueOf(userWalletSummary.getJson().get("purchasePrice").toString()))*highDiscount/100*Long.valueOf(userWalletSummary.getJson().get("selfSupportDiscount").toString())/100+Long.valueOf(userWalletSummary.getJson().get("purchasePrice").toString())-Long.valueOf(userWalletSummary.getJson().get("discount").toString()))*Long.valueOf(userWalletSummary.getJson().get("num").toString());
                        manageEarnings = lowMoney-highMoney;
                    }else if(userWalletSummary.getMoneyType().equals(3)){
                        Long lowMoney = Long.valueOf(userWalletSummary.getJson().get(lowLevel.toString()).toString())*Long.valueOf(userWalletSummary.getJson().get("num").toString());
                        Long highMoney = Long.valueOf(userWalletSummary.getJson().get(highLevel.toString()).toString())*Long.valueOf(userWalletSummary.getJson().get("num").toString());
                        manageEarnings = lowMoney-highMoney;
                    }

                    //开始创建收益明细
                    UserWalletSummary newUserWalletSummaryForEarnings  = new UserWalletSummary();
                    newUserWalletSummaryForEarnings.setUserId(leaderUserTeamList.get(0).getLeader());
                    newUserWalletSummaryForEarnings.setType(43);
                    newUserWalletSummaryForEarnings.setMoney(manageEarnings);
                    newUserWalletSummaryForEarnings.setOtherId(userWalletSummary.getUserId());
                    newUserWalletSummaryForEarnings.setOtherMoney(money);
                    updateUserWallet(newUserWalletSummaryForEarnings);

                    UserEarningsBonuses blackUserEarningsBonuses = new UserEarningsBonuses();
                    blackUserEarningsBonuses.setUserId(userLevelList.get(0).getBlackInviter());
                    blackUserEarningsBonuses.setType(43);
                    blackUserEarningsBonuses.setMoneyType(1);
                    blackUserEarningsBonuses.setMoney(manageEarnings);
                    blackUserEarningsBonuses.setStatus(1);
                    blackUserEarningsBonuses.setFromId(userWalletSummary.getUserId());
                    blackUserEarningsBonuses.setOrderItemId(userWalletSummary.getOrderItemId());
                    if(manageEarnings>0){
                        userEarningsBonusesDao.insert(blackUserEarningsBonuses);
                    }

                    lowDiscount = leaderLevelList.get(0).getDiscount();
                    lowLevel =  leaderLevelList.get(0).getLevel();
                    member = leaderUserTeamList.get(0).getLeader();
                    num++;
                }
            }
        }
        return null;
    }

    @Override
    public Response<Boolean> pay(Long userId, Integer type, Long money, Long deliverFee) {
        Response<Boolean> result = new Response<Boolean>();
        UserWallet userWallet = new UserWallet();
        userWallet.setUserId(userId);
        List<UserWallet> userWalletList = userWalletDao.selectBy(userWallet);
        if(userWalletList ==null || userWalletList.size() ==0){
            result.setError("用户钱包信息异常");
            return result;
        }
        if(type.equals(1)){
            log.info("balance:"+userWalletList.get(0).getBalance());
            log.info("money:"+money);
            if(userWalletList.get(0).getBalance()<money){
                result.setError("余额不足");
                return result;
            };
            UserWalletSummary userWalletSummary = new UserWalletSummary();
            userWalletSummary.setUserId(userId);
            userWalletSummary.setMoney(-money);
            userWalletSummary.setType(6);
            updateUserWallet(userWalletSummary);
        }else if(type.equals(2)){
            log.info("advance:"+userWalletList.get(0).getAdvance());
            log.info("money:"+money);
            if(userWalletList.get(0).getAdvance()<money){
                result.setError("预存款不足");
                return result;
            };
            UserWalletSummary userWalletSummary = new UserWalletSummary();
            userWalletSummary.setUserId(userId);
            userWalletSummary.setMoney(-money);
            userWalletSummary.setType(26);
            updateUserWallet(userWalletSummary);
        }
        if(deliverFee>0){
            UserWalletSummary userWalletSummaryForDeliverFee = new UserWalletSummary();
            userWalletSummaryForDeliverFee.setUserId(userId);
            userWalletSummaryForDeliverFee.setMoney(deliverFee);
            userWalletSummaryForDeliverFee.setType(91);
            updateUserWallet(userWalletSummaryForDeliverFee);
        }
        result.setResult(true);
        return result;
    }


    //记录钱包更新历史
    public boolean updateUserWalletSummary(UserWalletSummary userWalletSummary){
        if(!userWalletSummary.getMoney().equals(0L)){
            userWalletSummaryDao.insert(userWalletSummary);
            Integer type=userWalletSummary.getType();
            if(type<61){
                try{
                    pushAssetsNotice(userWalletSummary);
                }catch (Exception e){
                    log.error("Assets notice push fail userId:{},id:{}, error:{}",userWalletSummary.getUserId(),userWalletSummary.getId(),e);
                }
            }
        }
        return true;
    }

    private void pushAssetsNotice(UserWalletSummary userWalletSummary){
        if(userWalletSummary.getOtherName()==null&&userWalletSummary.getOtherId()!=null){
            UserTeamMemberSelect utms=new UserTeamMemberSelect();
            utms.setUserId(userWalletSummary.getOtherId());
            utms.setOffset(0);
            utms.setLimit(5);
            List<UserTeamMemberSelect> list=  userTeamMemberSelectDao.selectUser(utms);
            if(list.size()>0){
                UserTeamMemberSelect user=list.get(0);
                String mobile=user.getMobile();
                mobile=mobile.substring(mobile.length()-4,mobile.length());
                userWalletSummary.setOtherName(mobile);
                userWalletSummary.makePushMessage();
                pushEventBus.post(new PushEvent(userWalletSummary.getId(),userWalletSummary.getMessage(),userWalletSummary.getType(),userWalletSummary.getUserId(),NoticeType.ASSETS_FLUCTUATION.getNoticeType()));
            }
        }else {
            userWalletSummary.makePushMessage();
            pushEventBus.post(new PushEvent(userWalletSummary.getId(),userWalletSummary.getMessage(),userWalletSummary.getType(),userWalletSummary.getUserId(),NoticeType.ASSETS_FLUCTUATION.getNoticeType()));
        }


    }


}