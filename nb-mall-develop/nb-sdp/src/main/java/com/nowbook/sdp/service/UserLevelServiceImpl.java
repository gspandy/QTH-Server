package com.nowbook.sdp.service;



import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
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
public class UserLevelServiceImpl implements UserLevelService {

    private final static Logger log = LoggerFactory.getLogger(UserLevelServiceImpl.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserTeamMemberDao userTeamMemberDao;

    @Autowired
    private UserTeamDao userTeamDao;

    @Autowired
    private UserLevelDao userLevelDao;

    @Autowired
    private UserRelationDao userRelationDao;

    @Autowired
    private UserWalletDao userWalletDao;

    @Autowired
    private UserBankDao userBankDao;

    @Autowired
    private LevelDao levelDao;

    @Autowired
    private UserTeamHistoryDao userTeamHistoryDao;

    @Autowired
    private UserLevelHistoryDao userLevelHistoryDao;

    @Autowired
    private UserInviterHistoryDao userInviterHistoryDao;

    @Autowired
    private UserTeamMemberSelectDao userTeamMemberSelectDao;

    @Autowired
    private UserWalletService userWalletService;

    @Autowired
    private UserLevelWaitDao userLevelWaitDao;

    public Response<Boolean> update(){
        Integer size = 100;
        Integer pageNo = 1;
        while (true){
            Response<Paging<User>> all = accountService.findUser(new HashMap<String, String>(),pageNo,size);
            List<User> list = all.getResult().getData();
            for(User user : list){
                updateUserRelation(user.getId());
            }
            if(list.size() ==100){
                pageNo ++;
                log.info("user_relation update on pageNo="+pageNo);
            }else {
                break;
            }
        }
        log.info("user_relation update all right");
        return null;
    }

    @Override
    public Response<UserTeamMemberSelect> selectUser(Long userId) {
        Response<UserTeamMemberSelect> result = new Response<UserTeamMemberSelect>();
        UserTeamMemberSelect userTeamMemberSelect = new UserTeamMemberSelect();
        userTeamMemberSelect.setUserId(userId);
        List<UserTeamMemberSelect> userTeamMemberSelectList = userTeamMemberSelectDao.selectMyLevel(userTeamMemberSelect);
        if(userTeamMemberSelectList ==null || userTeamMemberSelectList.size() ==0){
            result.setError("用户不存在");
        }
        if(userTeamMemberSelectList.get(0).getIsHavePayPass() !=null && !userTeamMemberSelectList.get(0).getIsHavePayPass().equals("")){
            userTeamMemberSelectList.get(0).setIsHavePayPass("1");
        }else{
            userTeamMemberSelectList.get(0).setIsHavePayPass("0");
        }
        UserBank userBank = new UserBank();
        userBank.setUserId(userId);
        List<UserBank> userBankList = userBankDao.selectByUserId(userBank);
        if(userBankList !=null && userBankList.size()>0){
            userTeamMemberSelectList.get(0).setRealName(userBankList.get(0).getBankUser());
        }
        result.setResult(userTeamMemberSelectList.get(0));
        return result;
    }

    @Override
    public Response<Paging<UserTeamMemberSelect>> selectUserLevel(Long userId,String mobile,Integer level,Integer type,Integer pageNo,Integer pageSize) {
        Response<Paging<UserTeamMemberSelect>> result = new Response<Paging<UserTeamMemberSelect>>();
        if(pageNo==null){
            pageNo =1;
        }
        if(pageSize==null){
            pageSize =10;
        }
        UserTeamMemberSelect userTeamMemberSelect = new UserTeamMemberSelect();
        userTeamMemberSelect.setLimit(pageSize);
        userTeamMemberSelect.setOffset((pageNo-1)*pageSize);
        userTeamMemberSelect.setLevels(level);
        userTeamMemberSelect.setType(type);
        userTeamMemberSelect.setMobile(mobile);
        userTeamMemberSelect.setUserId(userId);
        List<UserTeamMemberSelect> userTeamMemberSelectList = userTeamMemberSelectDao.selectMyInviter(userTeamMemberSelect);
        UserTeamMemberSelect userTeamMemberSelectNum= userTeamMemberSelectDao.selectUserNum(userTeamMemberSelect);
        result.setResult(new Paging<UserTeamMemberSelect>(Long.valueOf(userTeamMemberSelectNum.getInviterNum()), userTeamMemberSelectList));
        return result;
    }

    @Override
    public Response<Map<String,Object>> selectUserTeamMember(@ParamInfo("userId") @Nullable Long userId, @ParamInfo("level") @Nullable Integer level, @ParamInfo("pageNo") @Nullable Integer pageNo, @ParamInfo("size") @Nullable Integer pageSize) {
        if(userId ==null || userId.equals(0L)){
            return null;
        }
        Response<Map<String,Object>> response = selectTeamMember(userId, level, pageNo, pageSize);
        UserRelation userRelation= new UserRelation();
        userRelation.setUserId(userId);
        userRelation.setOffset(0);
        userRelation.setLimit(10);
        List<UserRelation> relationList = userRelationDao.selectBy(userRelation);
        Map<String,String> map = new HashMap<String, String>();
        for(UserRelation ur : relationList){
            if(ur.getParentLevel().equals(2)){
                map.put("gold",ur.getParentMobile());
            }
            if(ur.getParentLevel().equals(3)){
                map.put("platinum",ur.getParentMobile());
            }
            if(ur.getParentLevel().equals(4)){
                map.put("black",ur.getParentMobile());
            }
            if(ur.getParentLevel().equals(5)){
                map.put("partner",ur.getParentMobile());
            }
        }
        response.getResult().put("relation",map);
        Response<UserTeamMemberSelect> userTeamMemberSelectResponse = selectUser(userId);
        response.getResult().put("userTeamMemberSelect",userTeamMemberSelectResponse.getResult());
        return response;
    }

    @Override
    public Response<Map<String, Object>> selectUserInviter(@ParamInfo("userId") @Nullable Long userId, @ParamInfo("level") @Nullable Integer level, @ParamInfo("pageNo") @Nullable Integer pageNo, @ParamInfo("size") @Nullable Integer pageSize) {
        if(userId ==null || userId.equals(0L)){
            return null;
        }
        Response<Map<String,Object>> response = selectMyInviter(userId, level,2, pageNo, pageSize);
        Response<UserTeamMemberSelect> userTeamMemberSelectResponse = selectUser(userId);
        response.getResult().put("userTeamMemberSelect",userTeamMemberSelectResponse.getResult());
        return response;
    }

    //入会时，通过推荐人来确定自己位置
    @Override
    public Response<Boolean> initiation(UserLevel userLevel){
        Response<Boolean> result = new Response<Boolean>();

        if(userLevel.getUserId() == null || userLevel.getInviter() == null || userLevel.getLevel() == null || userLevel.getLevelUpAt() == null){
            result.setError("基础数据不全");
            return result;
        }

        //判断新用户等级是否已经存在，存在的话直接返回
        List<UserLevel> userLevelList = userLevelDao.selectByUserId(userLevel);
        if(userLevelList != null && userLevelList.size() >0){
            result.setError("这个用户已经入过会。");
            return result;
        }

        //判断推荐人是否存在，不存在则直接返回
        UserLevel inviterUserLevel = new UserLevel();
        inviterUserLevel.setUserId(userLevel.getInviter());
        List<UserLevel> inviterUserLevelList = userLevelDao.selectByUserId(inviterUserLevel);
        if(inviterUserLevelList == null || inviterUserLevelList.size() == 0){
            result.setError("推荐人不存在。");
            return result;
        }

        Level level = new Level();
        level.setLevel(userLevel.getLevel());
        List<Level> levelList = levelDao.selectByLevel(level);
        if(levelList == null || levelList.size() == 0){
            result.setError("新用户选定的等级没有设置。");
            return result;
        }

        //创建钱包数据
        UserWallet userWallet = new UserWallet();
        userWallet.setUserId(userLevel.getUserId());
        userWallet.setBalance(0L);
        userWallet.setAdvance(0L);
        userWallet.setDeposit(0L);
        userWallet.setPendingEarnings(0L);
        userWallet.setPendingBonuses(0L);
        userWallet.setUnpaidDeliverFee(0L);
        userWallet.setTotalBonuses(0L);
        userWallet.setTotalEarnings(0L);
        userWallet.setTotalDeliverFee(0L);
        List<UserWallet> userWalletList = userWalletDao.selectBy(userWallet);
        if(userWalletList != null && userWalletList.size() >0){
            result.setError("这个用户已有钱包。");
            return result;
        }else{
            userWalletDao.insert(userWallet);
        }


        //如果推荐人是否是黑卡以上
        if(inviterUserLevelList.get(0).getLevel() < 4){
            //不是的话找到这个推荐人的黑卡或以上上级来当另一个推荐人
            UserTeam userTeam = selectParentTeam(userLevel.getInviter(),3);
            if(userTeam == null){
                result.setError("寻找黑卡团队失败。");
            }
            userLevel.setBlackInviter(userTeam.getLeader());
        }

        //如果等级是黑卡或以上，记录升级到黑卡或以上时间
        if(userLevel.getLevel() >= 4){
            userLevel.setLevelUpBlackAt(new Date());
        }


        //更改用户等级
        updateUserLevel(userLevel);

        //寻找新用户管理人
        if(inviterUserLevelList.get(0).getLevel()>userLevel.getLevel()){
            //如果推荐人等级大于新用户等级，则推荐人直接管理新用户。
            UserTeam userTeam = new UserTeam();
            userTeam.setLeader(userLevel.getInviter());
            List<UserTeam> leaderUserTeamList = userTeamDao.selectBy(userTeam);
            if(leaderUserTeamList ==null || leaderUserTeamList.size() == 0){
                result.setSuccess(false);
                result.setError("推荐人的团队不存在。");
                return result;
            }
            //更改用户上级团队
            UserTeamMember userTeamMember = new UserTeamMember();
            userTeamMember.setMember(userLevel.getUserId());
            userTeamMember.setTeam(leaderUserTeamList.get(0).getId());
            updateUserTeamMember(userTeamMember);
        }else{
            //如果推荐人等级不大于新用户等级，则找到推荐人的合适的管理人管理新用户。
            UserTeam userTeam = selectParentTeam(userLevel.getInviter(),userLevel.getLevel());
            if(userTeam == null){
                result.setError("寻找上级团队失败。");
            }
            //更改用户上级团队
            UserTeamMember newUserTeamMember = new UserTeamMember();
            newUserTeamMember.setMember(userLevel.getUserId());
            newUserTeamMember.setTeam(userTeam.getId());
            updateUserTeamMember(newUserTeamMember);
        }

        //创建团队数据
        UserTeam newUserTeam = new UserTeam();
        newUserTeam.setLeader(userLevel.getUserId());
        List<UserTeam> userTeamList = userTeamDao.selectBy(newUserTeam);
        if(userTeamList !=null && userTeamList.size() > 0){
            result.setSuccess(false);
            result.setError("团队已存在。");
            return result;
        }
        newUserTeam.setName("我的团队");
        userTeamDao.insert(newUserTeam);

        //余额变化
        UserWalletSummary userWalletSummaryForBalance = new UserWalletSummary();
        userWalletSummaryForBalance.setUserId(userLevel.getUserId());
        userWalletSummaryForBalance.setMoney(levelList.get(0).getBalance());
        userWalletSummaryForBalance.setType(1);
        userWalletSummaryForBalance.setLevel(userLevel.getLevel());
        userWalletSummaryForBalance.setRealMoney(levelList.get(0).getBalance()*100/levelList.get(0).getDiscount());
        userWalletSummaryForBalance.setMoneyType(4);
        userWalletService.updateUserWallet(userWalletSummaryForBalance);
        userWalletService.profit(userWalletSummaryForBalance);

        //预存款变化
        UserWalletSummary userWalletSummaryForAdvance = new UserWalletSummary();
        userWalletSummaryForAdvance.setUserId(userLevel.getUserId());
        userWalletSummaryForAdvance.setMoney(levelList.get(0).getAdvance());
        userWalletSummaryForAdvance.setType(23);
        userWalletSummaryForAdvance.setLevel(userLevel.getLevel());
        userWalletService.updateUserWallet(userWalletSummaryForAdvance);

        //保证金变化
        UserWalletSummary userWalletSummaryForDeposit = new UserWalletSummary();
        userWalletSummaryForDeposit.setUserId(userLevel.getUserId());
        userWalletSummaryForDeposit.setMoney(levelList.get(0).getDeposit());
        userWalletSummaryForDeposit.setType(31);
        userWalletSummaryForDeposit.setLevel(userLevel.getLevel());
        userWalletService.updateUserWallet(userWalletSummaryForAdvance);


        result.setSuccess(true);
        return result;
    };

    //升级时，更换团队，并换算钱包里的余额。黑卡升级到合伙人时同时触发黑卡回填机制。
    @Override
    public Response<Boolean> levelUp(UserLevel userLevel,Integer type, String userIds){
        Response<Boolean> result = new Response<Boolean>();

        if(userLevel.getUserId() == null ||  userLevel.getLevel() == null || userLevel.getLevelUpAt() == null || type == null){
            result.setError("基础数据不全");
            return result;
        }

        if(userLevel.getLevel()>=2 && userLevel.getLevel()<=4){
            if (userLevel.getMoney() ==null){
                result.setError("基础数据不全");
                return result;
            }
        }

        List<Long> userIdList = new ArrayList<Long>();
//        if(userLevel.getLevel().equals(5)){
//            if (userIds ==null){
//               result.setError("基础数据不全");
//               return result;
//            }
//            for(String userId : userIds.split(",")){
//                userIdList.add(Long.valueOf(userId));
//            }
//            if(userIdList.size() !=3){
//                result.setError("选择人数有误");
//                return result;
//            }
//        }



        //判断用户等级是否存在，不存在的话直接返回
        List<UserLevel> userLevelList = userLevelDao.selectByUserId(userLevel);
        if(userLevelList == null && userLevelList.size() ==0){
            result.setError("这个用户没有入过会。");
            return result;
        }

        Integer oldLevel = userLevelList.get(0).getLevel();

        //判断变更前的等级是否设置，没设置的话返回
        Level oldLevelInfo = new Level();
        oldLevelInfo.setLevel(oldLevel);
        List<Level> oldLevelInfoList = levelDao.selectByLevel(oldLevelInfo);
        if(oldLevelInfoList == null || oldLevelInfoList.size() == 0){
            result.setError("变更前的等级没有设置。");
            return result;
        }

        //判断变更后的等级是否设置，没设置的话返回
        Level newLevelInfo = new Level();
        newLevelInfo.setLevel(userLevel.getLevel());
        List<Level> newLevelInfoList = levelDao.selectByLevel(newLevelInfo);
        if(newLevelInfoList == null || newLevelInfoList.size() == 0){
            result.setError("变更后的等级没有设置。");
            return result;
        }

        //查找原所属团队
        UserTeamMember oldUserTeamMember = new UserTeamMember();
        oldUserTeamMember.setMember(userLevel.getUserId());
        oldUserTeamMember.setRole("MEMBER");
        List<UserTeamMember> oldUserTeamMemberList = userTeamMemberDao.selectBy(oldUserTeamMember);
        if (oldUserTeamMemberList == null || oldUserTeamMemberList.size() == 0) {
            result.setError("此用户没加入过团队。");
            return result;
        }

        //查找原所属团队的领队
        Long oldTeam = oldUserTeamMemberList.get(0).getTeam();
        UserTeam oldUserTeam = new UserTeam();
        oldUserTeam.setId(oldTeam);
        List<UserTeam> oldUserTeamList = userTeamDao.selectBy(oldUserTeam);
        if (oldUserTeamList == null || oldUserTeamList.size() == 0) {
            result.setError("原所属团队不存在。");
            return result;
        }

        //查找原所属团队的领队的等级
        Long oldLeader = oldUserTeamList.get(0).getLeader();
        UserLevel oldUserLevel = new UserLevel();
        oldUserLevel.setUserId(oldLeader);
        List<UserLevel> oldUserLevelList = userLevelDao.selectByUserId(oldUserLevel);
        if(oldUserLevelList == null && oldUserLevelList.size() ==0){
            result.setError("原所属团队不存在。");
            return result;
        }

        //查找钱包
        UserWallet userWallet = new UserWallet();
        userWallet.setUserId(userLevel.getUserId());
        List<UserWallet> userWalletList = userWalletDao.selectBy(userWallet);
        if(userWalletList ==null || userWalletList.size() ==0){
            result.setError("用户钱包不存在。");
            return result;
        }

        //如果升级到合伙人，判断其升级到黑卡时间是否在推荐人升级到黑卡之前？是的话要更换推荐人。
        if(userLevel.getLevel().equals(5)){
            //先判断一下推荐人是否是黑卡，不是的话用另一个推荐人代替他。
            UserLevel inviterUserLevel = new UserLevel();
            inviterUserLevel.setUserId(userLevelList.get(0).getInviter());
            List<UserLevel> inviterUserLevelList = userLevelDao.selectByUserId(inviterUserLevel);
            if(inviterUserLevelList ==null || inviterUserLevelList.size() ==0){
                result.setError("推荐人不存在。");
                return result;
            }
            if(inviterUserLevelList.get(0).getLevel() <4){
                UserLevel userLevelInviter = new UserLevel();
                userLevelInviter.setUserId(userLevel.getUserId());
                Long oldInviter = userLevelList.get(0).getInviter();
                userLevelInviter.setInviter(userLevelList.get(0).getBlackInviter());
                userLevelInviter.setBlackInviter(0L);
                updateUserInviter(userLevelInviter,1,oldInviter);
            }else if(inviterUserLevelList.get(0).getLevel() == 4 || inviterUserLevelList.get(0).getLevel() == 5){
                //推荐人升级到黑卡时间
                Date time1 =  inviterUserLevelList.get(0).getLevelUpBlackAt();

                //此用户升级到黑卡时间
                Date time2 = userLevelList.get(0).getLevelUpBlackAt();

                if(time1.getTime() > time2.getTime()){
                    //如果推荐人升级到黑卡时间比此用户升级到黑卡时间晚，那么推荐人换成另一个推荐人
                    UserLevel userLevelInviter = new UserLevel();
                    userLevelInviter.setUserId(userLevel.getUserId());
                    Long oldInviter = userLevelList.get(0).getInviter();
                    userLevelInviter.setInviter(userLevelList.get(0).getBlackInviter());
                    userLevelInviter.setBlackInviter(0L);
                    updateUserInviter(userLevelInviter,1,oldInviter);
                }
            }
        }

        //升级到黑卡要记录时间
        UserLevel userLevelLevelUp = new UserLevel();
        userLevelLevelUp.setUserId(userLevel.getUserId());
        userLevelLevelUp.setLevel(userLevel.getLevel());
        userLevelLevelUp.setLevelUpAt(new Date());
        if(userLevel.getLevel() == 4){
            userLevelLevelUp.setLevelUpBlackAt(userLevel.getLevelUpAt());
        }

        //更改用户等级
        updateUserLevel(userLevelLevelUp);

        //寻找新团队
        if(oldUserLevelList.get(0).getLevel() > userLevel.getLevel()){
            //如果老团队领队等级依然比此用户升级后的等级高，则团队不动，只需要更改user_relation表即可
            updateUserRelation(userLevel.getUserId());
        }else{
            //当老团队领队等级没有此用户升级后的等级高，更换团队，同时更改user_relation表
            UserTeam userTeam = selectParentTeam(userLevel.getUserId(),userLevel.getLevel());
            if(userTeam == null){
                result.setError("寻找上级团队失败。");
                return result;
            }
            //更改用户上级团队
            UserTeamMember newUserTeamMember = new UserTeamMember();
            newUserTeamMember.setMember(userLevel.getUserId());
            newUserTeamMember.setTeam(userTeam.getId());
            updateUserTeamMember(newUserTeamMember);
        }

        //黑卡升级到合伙人时触发回填
        if(userLevel.getLevel().equals(5)){
            backFill(userLevel.getUserId(),userIdList);
        }

        //换算余额
        Integer oldDiscount = oldLevelInfoList.get(0).getDiscount();
        Integer newDiscount = newLevelInfoList.get(0).getDiscount();

        if(userWalletList.get(0).getBalance()>0){
            Long changeBalance = userWalletList.get(0).getBalance()*newDiscount/oldDiscount -userWalletList.get(0).getBalance();
            UserWalletSummary userWalletSummaryForOldBalance = new UserWalletSummary();
            userWalletSummaryForOldBalance.setUserId(userLevel.getUserId());
            userWalletSummaryForOldBalance.setMoney(changeBalance);
            userWalletSummaryForOldBalance.setType(8);
            userWalletSummaryForOldBalance.setLevel(userLevel.getLevel());
            userWalletService.updateUserWallet(userWalletSummaryForOldBalance);
        }

        if(type.equals(1)){
            //余额变化
            UserWalletSummary userWalletSummaryForBalance = new UserWalletSummary();
            userWalletSummaryForBalance.setUserId(userLevel.getUserId());
            userWalletSummaryForBalance.setMoney(newLevelInfoList.get(0).getBalance());
            userWalletSummaryForBalance.setType(1);
            userWalletSummaryForBalance.setLevel(userLevel.getLevel());
            userWalletSummaryForBalance.setRealMoney(newLevelInfoList.get(0).getBalance()*100/newLevelInfoList.get(0).getDiscount());
            userWalletSummaryForBalance.setMoneyType(4);
            userWalletService.updateUserWallet(userWalletSummaryForBalance);
            userWalletService.profit(userWalletSummaryForBalance);

            //预存款变化
            UserWalletSummary userWalletSummaryForAdvance = new UserWalletSummary();
            userWalletSummaryForAdvance.setUserId(userLevel.getUserId());
            userWalletSummaryForAdvance.setMoney(newLevelInfoList.get(0).getAdvance());
            userWalletSummaryForAdvance.setType(23);
            userWalletSummaryForAdvance.setLevel(userLevel.getLevel());
            userWalletService.updateUserWallet(userWalletSummaryForAdvance);

            //保证金变化
            UserWalletSummary userWalletSummaryForDeposit = new UserWalletSummary();
            userWalletSummaryForDeposit.setUserId(userLevel.getUserId());
            userWalletSummaryForDeposit.setMoney(userLevel.getMoney() - newLevelInfoList.get(0).getBalance()- newLevelInfoList.get(0).getAdvance());
            userWalletSummaryForDeposit.setType(31);
            userWalletSummaryForDeposit.setLevel(userLevel.getLevel());
            userWalletService.updateUserWallet(userWalletSummaryForDeposit);
        }else if(type.equals(2)){
            //保证金变化
            UserWalletSummary userWalletSummaryForDeposit = new UserWalletSummary();
            userWalletSummaryForDeposit.setUserId(userLevel.getUserId());
            userWalletSummaryForDeposit.setMoney(userLevel.getMoney());
            userWalletSummaryForDeposit.setType(31);
            userWalletSummaryForDeposit.setLevel(userLevel.getLevel());
            userWalletService.updateUserWallet(userWalletSummaryForDeposit);
        }else if(type.equals(3)){
            //线下升级
            //余额变化
            if(userLevel.getBalance()>0){
                UserWalletSummary userWalletSummaryForBalance = new UserWalletSummary();
                userWalletSummaryForBalance.setUserId(userLevel.getUserId());
                userWalletSummaryForBalance.setMoney(userLevel.getBalance());
                userWalletSummaryForBalance.setType(1);
                userWalletSummaryForBalance.setLevel(userLevel.getLevel());
                userWalletSummaryForBalance.setRealMoney(userLevel.getBalance()*100/newLevelInfoList.get(0).getDiscount());
                userWalletSummaryForBalance.setMoneyType(4);
                userWalletService.updateUserWallet(userWalletSummaryForBalance);
                userWalletService.profit(userWalletSummaryForBalance);
            }
            //预存款变化
            if(userLevel.getAdvance()>0){
                UserWalletSummary userWalletSummaryForAdvance = new UserWalletSummary();
                userWalletSummaryForAdvance.setUserId(userLevel.getUserId());
                userWalletSummaryForAdvance.setMoney(userLevel.getAdvance());
                userWalletSummaryForAdvance.setType(23);
                userWalletSummaryForAdvance.setLevel(userLevel.getLevel());
                userWalletService.updateUserWallet(userWalletSummaryForAdvance);
            }
            //保证金变化
            if(userLevel.getDeposit()>0){
                UserWalletSummary userWalletSummaryForDeposit = new UserWalletSummary();
                userWalletSummaryForDeposit.setUserId(userLevel.getUserId());
                userWalletSummaryForDeposit.setMoney(userLevel.getDeposit());
                userWalletSummaryForDeposit.setType(31);
                userWalletSummaryForDeposit.setLevel(userLevel.getLevel());
                userWalletService.updateUserWallet(userWalletSummaryForDeposit);
            }
        }
        result.setResult(true);
        return result;
    };

    //查找自己所推荐的人
    public Response<Map<String,Object>> selectMyInviter(Long userId, Integer level,Integer type,Integer pageNo,Integer pageSize){
        Response<Map<String,Object>> result = new Response<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String, Object>();

        if(pageNo == null || pageNo <=0){
            pageNo =1;
        }
        if(pageSize == null || pageSize <=0){
            pageSize =10;
        }
        Integer offset = (pageNo-1)*pageSize;

        UserLevel userLevel = new UserLevel();
        userLevel.setUserId(userId);
        List<UserLevel> levelList = userLevelDao.selectByUserId(userLevel);
        if(levelList ==null || levelList.size() == 0){
            result.setSuccess(false);
            result.setError("此用户没有入会。");
            return result;
        }

        UserTeamMemberSelect userTeamMemberSelect = new UserTeamMemberSelect();
        userTeamMemberSelect.setInviter(userId);
        userTeamMemberSelect.setLimit(pageSize);
        userTeamMemberSelect.setOffset(offset);
        List<UserTeamMemberSelect> list = new ArrayList<UserTeamMemberSelect>();
        if(type == 1){
            userTeamMemberSelect.setLevel(levelList.get(0).getLevel());
            userTeamMemberSelect.setLevelUpAt(levelList.get(0).getLevelUpAt());
            if(levelList.get(0).getLevel() >=4){
                list = userTeamMemberSelectDao.selectMyInviterForBlack(userTeamMemberSelect);
            }else{
                list = userTeamMemberSelectDao.selectMyInviter(userTeamMemberSelect);
            }
        }else if(type == 2){
            userTeamMemberSelect.setLevels(level);
            if(levelList.get(0).getLevel() >=4){
                list = userTeamMemberSelectDao.selectMyInviterForBlack(userTeamMemberSelect);
            }else{
                list = userTeamMemberSelectDao.selectMyInviter(userTeamMemberSelect);
            }
        }

        if(list !=null && list.size() !=0){
            for(UserTeamMemberSelect utms : list){
                UserTeamMemberSelect u1 = new UserTeamMemberSelect();
                u1.setParent(utms.getUserId());
                u1 = userTeamMemberSelectDao.selectMemberNum(u1);
                utms.setMemberNum(u1.getMemberNum());

                u1.setParent(utms.getUserId());
                u1 = userTeamMemberSelectDao.selectInviterNum(u1);
                utms.setInviterNum(u1.getInviterNum());

                SimpleDateFormat format= new SimpleDateFormat("yyyy-MM-dd");
                if(utms.getCreateAt()!=null){
                    utms.setCreateTime(format.format(utms.getCreateAt()));
                }
                if(utms.getLevelUpAt()!=null){
                    utms.setCreateTime(format.format(utms.getLevelUpAt()));
                }
            }
        }
        map.put("user",list);

        UserTeamMemberSelect inviterNum = new UserTeamMemberSelect();
        if(levelList.get(0).getLevel() >=4){
            inviterNum =  userTeamMemberSelectDao.selectMyInviterNumForBlack(userTeamMemberSelect);
        }else {
            inviterNum = userTeamMemberSelectDao.selectMyInviterNum(userTeamMemberSelect);
        }
        map.put("total",inviterNum.getInviterNum());

        result.setResult(map);
        return result;
    };


    // 查找自己不同等级的下级
    public Response<Map<String,Object>> selectTeamMember(Long userId,Integer level,Integer pageNo,Integer pageSize){
        Response<Map<String,Object>> result = new Response<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String, Object>();
        if(pageNo == null || pageNo <=0){
            pageNo =1;
        }
        if(pageSize == null || pageSize <=0){
            pageSize =10;
        }

        Integer offset = (pageNo-1)*pageSize;

        UserTeamMemberSelect userTeamMemberSelect = new UserTeamMemberSelect();
        userTeamMemberSelect.setLimit(pageSize);
        userTeamMemberSelect.setOffset(offset);

        userTeamMemberSelect.setLevel(level);
        userTeamMemberSelect.setParent(userId);

        List<UserTeamMemberSelect> list = userTeamMemberSelectDao.selectUserTeamMember(userTeamMemberSelect);
        if(list !=null && list.size() !=0){
            for(UserTeamMemberSelect utms : list){
                UserTeamMemberSelect u1 = new UserTeamMemberSelect();
                u1.setParent(utms.getUserId());
                u1 = userTeamMemberSelectDao.selectMemberNum(u1);
                utms.setMemberNum(u1.getMemberNum());

                u1.setParent(utms.getUserId());
                u1 = userTeamMemberSelectDao.selectInviterNum(u1);
                utms.setInviterNum(u1.getInviterNum());

                SimpleDateFormat format= new SimpleDateFormat("yyyy-MM-dd");
                if(utms.getCreateAt()!=null){
                    utms.setCreateTime(format.format(utms.getCreateAt()));
                }
            }
        }
        map.put("user",list);
        map.put("pageNo",pageNo);
        map.put("pageSize",pageSize);

        UserTeamMemberSelect memberNum = userTeamMemberSelectDao.selectMemberNum(userTeamMemberSelect);
        map.put("total",memberNum.getMemberNum());

        result.setResult(map);
        return result;
    };

    //黑卡回填机制，当升级到合伙人时，需要选择被选定3个人所有推荐过的黑卡以及这些黑卡再推荐过的黑卡直到没有为止的总数的一半，变为该合伙人的直属黑卡
    public Response<Boolean> backFill(Long userId,List<Long> userIdList){
        Response<Boolean> result = new Response<Boolean>();

        //确定新合伙人的团队
        UserTeam userTeam = new UserTeam();
        userTeam.setLeader(userId);
        List<UserTeam> userTeamList = userTeamDao.selectBy(userTeam);
        if (userTeamList == null || userTeamList.size() == 0) {
            result.setError("没有找到该合伙人的团队。");
        }

        //先判断选的3个人是否都是黑卡以上
        List<UserLevel> userLevelList1 = new ArrayList<UserLevel>();
        List<UserLevel> userLevelList2 = new ArrayList<UserLevel>();
        for(Long user : userIdList){
            UserLevel userLevel = new UserLevel();
            userLevel.setUserId(user);
            List<UserLevel> userLevelArrayList = userLevelDao.selectByUserId(userLevel);
            if(userLevelArrayList !=null && userLevelArrayList.size()>0){
                for(UserLevel ul : userLevelArrayList){
                    if(ul.getLevel()==4){
                        userLevelList1.add(ul);
                    }else if(ul.getLevel() <4){
                        result.setError("选择的人不是黑卡以上。");
                        return result;
                    }
                }
            }else{
                result.setError("选择的人等级不存在。");
                return result;
            }
        }

        //寻找选的3个人
        List<Long> userIdArrayList = new ArrayList<Long>();
        while(userIdList.size()>0){
            for(Long user : userIdList){
                UserLevel userLevel = new UserLevel();
                userLevel.setInviter(user);
                userLevel.setLevel(4);
                List<UserLevel> userLevelArrayList = userLevelDao.selectByInviterAndLevel(userLevel);
                if(userLevelArrayList !=null && userLevelArrayList.size()>0){
                    for(UserLevel ul : userLevelArrayList){
                        userIdArrayList.add(ul.getId());
                        userLevelList2.add(ul);
                    }
                }
            }
            if(userIdArrayList.size()>0){
                userIdList = userIdArrayList;
                userIdArrayList = new ArrayList<Long>();
            }else {
                userIdList = new ArrayList<Long>();
            }
        }

        //更改团队
        //更改选的3个人
        for(UserLevel userLevel : userLevelList1){
            UserTeamMember userTeamMember = new UserTeamMember();
            userTeamMember.setMember(userLevel.getUserId());
            userTeamMember.setTeam(userTeamList.get(0).getId());
            updateUserTeamMember(userTeamMember);
        }
        //更改选的3个人推荐的黑卡
        Integer num = 1;
        for(UserLevel userLevel : userLevelList1){
            if(num %2 == 0){
                UserTeamMember userTeamMember = new UserTeamMember();
                userTeamMember.setMember(userLevel.getUserId());
                userTeamMember.setTeam(userTeamList.get(0).getId());
                updateUserTeamMember(userTeamMember);
            }
            num++;
        }

        return result;
    };

    @Override
    public Response<Map<String,Object>> selectMyLevel(Long userId){
        Response<Map<String,Object>> result = new Response<Map<String, Object>>();
        Map<String,Object> map = new HashMap<String, Object>();
        UserTeamMemberSelect userTeamMemberSelect = new UserTeamMemberSelect();
        userTeamMemberSelect.setUserId(userId);
        List<UserTeamMemberSelect> userTeamMemberSelectList = userTeamMemberSelectDao.selectMyLevel(userTeamMemberSelect);
        if(userTeamMemberSelectList == null || userTeamMemberSelectList.size() == 0){
            result.setError("没有此用户。");
            return result;
        }

        UserWallet userWallet = new UserWallet();
        userWallet.setUserId(userId);
        List<UserWallet> userWalletList = userWalletDao.selectBy(userWallet);
        if(userWalletList == null || userWalletList.size() == 0){
            result.setError("没有此用户的钱包。");
            return result;
        }

        List<Level> levelList = levelDao.selectByLevel(new Level());
        if(levelList == null || levelList.size() == 0){
            result.setError("没有设置等级。");
            return result;
        }

//        List<Level> newlevelList = new ArrayList<Level>();
        for(Level level : levelList){
//            if(level.getLevel()>userTeamMemberSelectList.get(0).getLevel()){
                Long needMoney = level.getBalance()+level.getAdvance()+level.getDeposit()-userWalletList.get(0).getDeposit();
                if (needMoney<0){
                    needMoney =0L;
                }
                level.setNeedMoney(needMoney);
//                newlevelList.add(level);
//            }
        }
        map.put("user",userTeamMemberSelectList.get(0));
        map.put("level",levelList);
        result.setResult(map);
        return result;
    }

    @Override
    public Response<UserLevelWait> selectByPayCode(UserLevelWait userLevelWait) {
        Response<UserLevelWait> result = new Response<UserLevelWait>();
        userLevelWait.setLimit(10);
        userLevelWait.setOffset(0);
        List<UserLevelWait> userLevelWaitList = userLevelWaitDao.selectBy(userLevelWait);
        if(userLevelWaitList !=null && userLevelWaitList.size()>0){
            result.setResult(userLevelWaitList.get(0));
        }else{
            result.setResult(null);
        }
        return result;
    }

    @Override
    public Response<String> applyLevelUp(UserLevelWait userLevelWait) {
        Response<String> result = new Response<String>();

        if(userLevelWait.getUserId() ==null || userLevelWait.getIsSelect() == null){
            result.setError("基本数据有误。");
            return result;
        }

        if(userLevelWait.getIsSelect().equals(1)){
            userLevelWait.setStatus(1);
            userLevelWait.setLimit(10);
            userLevelWait.setOffset(0);
            List<UserLevelWait> userLevelWaitList = userLevelWaitDao.selectBy(userLevelWait);
            if(userLevelWaitList != null && userLevelWaitList.size() > 0){
                result.setResult("noSubmitted");
                return result;
            }else{
                result.setResult("noSubmitted");
                return result;
            }
        }else if(userLevelWait.getIsSelect().equals(2)){
            if(userLevelWait.getLevel() ==null || userLevelWait.getType() == null){
                result.setError("基本数据有误。");
                return result;
            }

            if(userLevelWait.getLevel() <2 || userLevelWait.getLevel() >5){
                result.setError("等级信息有误。");
                return result;
            }

            if(userLevelWait.getLevel().equals(5)){
                if(!userLevelWait.getType().equals(3)){
                    if(userLevelWait.getUserIdList() ==null || userLevelWait.getUserIdList().size() !=3){
                        result.setError("选择人数有误。");
                        return result;
                    }
                }
            }

            if(!userLevelWait.getType().equals(1) && !userLevelWait.getType().equals(2) && !userLevelWait.getType().equals(3)){
                result.setError("没有设置此升级类型。");
                return result;
            }

            Level levels = new Level();
            levels.setLevel(userLevelWait.getLevel());
            List<Level> levelList = levelDao.selectByLevel(levels);
            if(levelList == null || levelList.size() == 0){
                result.setError("没有设置此等级。");
                return result;
            }

            UserTeamMemberSelect userTeamMemberSelect = new UserTeamMemberSelect();
            userTeamMemberSelect.setUserId(userLevelWait.getUserId());
            List<UserTeamMemberSelect> userTeamMemberSelectList = userTeamMemberSelectDao.selectMyLevel(userTeamMemberSelect);
            if(userTeamMemberSelectList == null || userTeamMemberSelectList.size() == 0){
                result.setError("没有此用户。");
                return result;
            }

            if(userTeamMemberSelectList.get(0).getLevel().equals(5)){
                result.setError("您已经是合伙人，无法继续升级。");
                return result;
            }

            if(userLevelWait.getLevel() <= userTeamMemberSelectList.get(0).getLevel()){
                result.setError("所升等级不能比原来等级低。");
                return result;
            }

            if(userLevelWait.getType().equals(2)){
                if(userLevelWait.getLevel().equals(2)){
                    result.setError("天使不能通过推荐升级。");
                    return result;
                }

                if(userLevelWait.getLevel() -  userTeamMemberSelectList.get(0).getLevel() !=1){
                    result.setError("推荐升级时不能跳着升级。");
                    return result;
                }
            }

            userLevelWait.setStatus(1);
            userLevelWait.setLimit(10);
            userLevelWait.setOffset(0);
//            List<UserLevelWait> userLevelWaitList = userLevelWaitDao.selectBy(userLevelWait);
//            if(userLevelWaitList != null && userLevelWaitList.size() > 0){
//                result.setError("您已提交过升级申请，请明天查看结果。");
//                return result;
//            }

            UserWallet userWallet = new UserWallet();
            userWallet.setUserId(userLevelWait.getUserId());
            List<UserWallet> userWalletList = userWalletDao.selectBy(userWallet);
            if(userWalletList == null || userWalletList.size() == 0){
                result.setError("没有此用户的钱包。");
                return result;
            }
            if(userLevelWait.getType().equals(1)){
                //缴费升级
                Long needMoney = levelList.get(0).getBalance()+levelList.get(0).getAdvance()+levelList.get(0).getDeposit()-userWalletList.get(0).getDeposit();
                userLevelWait.setMoney(needMoney);
                userLevelWait.setStatus(1);
                userLevelWaitDao.insert(userLevelWait);

                UserLevel userLevel = new UserLevel();
                userLevel.setUserId(userLevelWait.getUserId());
                userLevel.setLevel(userLevelWait.getLevel());
                userLevel.setLevelUpAt(new Date());
                userLevel.setMoney(needMoney);
                levelUp(userLevel,userLevelWait.getType(),userLevelWait.getUserIdListString());

//                UserLevelWait ult = new UserLevelWait();
//                ult.setUserId(userLevelWait.getUserId());
//                ult.setStatus(1);
//                ult.setLimit(10);
//                ult.setOffset(0);
//                List<UserLevelWait> ulwl = userLevelWaitDao.selectBy(ult);
//
//                UserLevelWait newUserLevelWait = new UserLevelWait();
//                newUserLevelWait.setId(ulwl.get(0).getId());
//                newUserLevelWait.setStatus(2);
//                userLevelWaitDao.update(newUserLevelWait);

            }else if(userLevelWait.getType().equals(2)){
                UserTeamMemberSelect inviterNum = new UserTeamMemberSelect();
                if(userLevelWait.getLevel().equals(5)){
                    inviterNum =  userTeamMemberSelectDao.selectMyInviterNumForBlack(userTeamMemberSelect);
                }else {
                    inviterNum = userTeamMemberSelectDao.selectMyInviterNum(userTeamMemberSelect);
                    Long needMoney = levelList.get(0).getDeposit()-userWalletList.get(0).getDeposit();
                    userLevelWait.setMoney(needMoney);
                }
                if(inviterNum.getInviterNum() < levelList.get(0).getNeedNum()){
                    result.setError("没有推荐足够的人数。");
                    return result;
                }
                userLevelWait.setNum(levelList.get(0).getNeedNum());
                userLevelWait.setStatus(1);
                userLevelWaitDao.insert(userLevelWait);

                UserLevel userLevel = new UserLevel();
                userLevel.setUserId(userLevelWait.getUserId());
                userLevel.setLevel(userLevelWait.getLevel());
                userLevel.setLevelUpAt(new Date());
                levelUp(userLevel,userLevelWait.getType(),userLevelWait.getUserIdListString());

//                UserLevelWait ult = new UserLevelWait();
//                ult.setUserId(userLevelWait.getUserId());
//                ult.setStatus(1);
//                ult.setLimit(10);
//                ult.setOffset(0);
//                List<UserLevelWait> ulwl = userLevelWaitDao.selectBy(ult);
//
//                UserLevelWait newUserLevelWait = new UserLevelWait();
//                newUserLevelWait.setId(ulwl.get(0).getId());
//                newUserLevelWait.setStatus(2);
//                userLevelWaitDao.update(newUserLevelWait);
            }if(userLevelWait.getType().equals(3)){
                //线下升级
                Long needMoney = userLevelWait.getBalance()+userLevelWait.getAdvance()+userLevelWait.getDeposit();
                userLevelWait.setMoney(needMoney);
                userLevelWait.setStatus(1);
                userLevelWaitDao.insert(userLevelWait);

                UserLevel userLevel = new UserLevel();
                userLevel.setUserId(userLevelWait.getUserId());
                userLevel.setLevel(userLevelWait.getLevel());
                userLevel.setLevelUpAt(new Date());
                userLevel.setMoney(needMoney);
                userLevel.setBalance(userLevelWait.getBalance());
                userLevel.setAdvance(userLevelWait.getAdvance());
                userLevel.setDeposit(userLevelWait.getDeposit());
                levelUp(userLevel,userLevelWait.getType(),userLevelWait.getUserIdListString());

//                UserLevelWait ult = new UserLevelWait();
//                ult.setUserId(userLevelWait.getUserId());
//                ult.setStatus(1);
//                ult.setLimit(10);
//                ult.setOffset(0);
//                List<UserLevelWait> ulwl = userLevelWaitDao.selectBy(ult);
//
//                UserLevelWait newUserLevelWait = new UserLevelWait();
//                newUserLevelWait.setId(ulwl.get(0).getId());
//                newUserLevelWait.setStatus(2);
//                userLevelWaitDao.update(newUserLevelWait);

            }
            result.setResult("提交申请成功，请明天查看结果。");
        }
        return result;
    }

    @Override
    public Response<Map<String,Object>> selectIsContentLevelUp(Long userId) {
        Response<Map<String,Object>> result = new Response<Map<String,Object>>();
        UserLevelWait userLevelWait = new UserLevelWait();
        userLevelWait.setUserId(userId);
        userLevelWait.setStatus(1);
        userLevelWait.setLimit(10);
        userLevelWait.setOffset(0);
        List<UserLevelWait> userLevelWaitList = userLevelWaitDao.selectBy(userLevelWait);
        if(userLevelWaitList != null && userLevelWaitList.size() > 0){
            result.setError("hasSubmitted");
            return result;
        }
        Map<String,Object> map = new HashMap<String, Object>();
        UserLevel userLevel = new UserLevel();
        userLevel.setUserId(userId);
        List<UserLevel> userLevelArrayList = userLevelDao.selectByUserId(userLevel);
        if(userLevelArrayList ==null || userLevelArrayList.size() == 0){
            result.setError("没有此用户");
            return result;
        }

        if(userLevelArrayList.get(0).getLevel()<2){
            result.setError("您等级不足金卡，无法使用推荐升级。");
            return result;
        }

        if(userLevelArrayList.get(0).getLevel()>=5){
            result.setError("您已经满级，无法继续升级。");
            return result;
        }

        Level level = new Level();
        level.setLevel(userLevelArrayList.get(0).getLevel());
        List<Level> levelList = levelDao.selectByLevel(level);
        if(levelList == null || levelList.size() == 0){
            result.setError("原等级没有设置。");
            return result;
        }

        Level nextLevel = new Level();
        nextLevel.setLevel(userLevelArrayList.get(0).getLevel()+1);
        List<Level> nextLevelList = levelDao.selectByLevel(nextLevel);
        if(nextLevelList == null || nextLevelList.size() == 0){
            result.setError("下一个等级没有设置。");
            return result;
        }

        UserTeamMemberSelect userTeamMemberSelect = new UserTeamMemberSelect();
        userTeamMemberSelect.setInviter(userId);
        userTeamMemberSelect.setLevel(userLevelArrayList.get(0).getLevel());
        userTeamMemberSelect.setLevelUpAt(userLevelArrayList.get(0).getLevelUpAt());
        Integer inviterNum = 0;
        if(userLevelArrayList.get(0).getLevel()>=4){
            inviterNum =  userTeamMemberSelectDao.selectMyInviterNumForBlack(userTeamMemberSelect).getInviterNum();
        }else {
            inviterNum = userTeamMemberSelectDao.selectMyInviterNum(userTeamMemberSelect).getInviterNum();
        }

        if(inviterNum >= nextLevelList.get(0).getNeedNum()){
            if(nextLevel.getLevel().equals(5)){
                result.setError("升级到合伙人无需补齐保证金。");
                return result;
            }
            UserWallet userWallet = new UserWallet();
            userWallet.setUserId(userId);
            List<UserWallet> userWalletList = userWalletDao.selectBy(userWallet);
            if(userWalletList == null || userWalletList.size() == 0){
                result.setError("没有此用户的钱包。");
                return result;
            }
            if(userWalletList.get(0).getDeposit() < nextLevelList.get(0).getDeposit()){
                Long needDeposit = nextLevelList.get(0).getDeposit() - userWalletList.get(0).getDeposit();
                map.put("nextLevel",nextLevelList.get(0).getLevel());
                map.put("needDeposit",needDeposit);
                result.setResult(map);
                return result;
            }else{
                result.setError("保证金足够。");
                return result;
            }
        }else{
            result.setError("推荐人数不足。");
            return result;
        }
    }

    @Override
    public void userLevelUp() {
        Integer size = 100;
        Integer pageNo = 1;
        while (true){
            UserLevelWait userLevelWait = new UserLevelWait();
            userLevelWait.setStatus(1);
            userLevelWait.setLimit(size);
            userLevelWait.setOffset((pageNo-1)*size);
            List<UserLevelWait> userLevelWaitList = userLevelWaitDao.selectBy(userLevelWait);
            if(userLevelWaitList !=null && userLevelWaitList.size() !=0){
                for(UserLevelWait ulw : userLevelWaitList){
                    UserLevel userLevel = new UserLevel();
                    userLevel.setUserId(ulw.getUserId());
                    userLevel.setLevel(ulw.getLevel());
                    userLevel.setLevelUpAt(ulw.getCreateAt());
                    userLevel.setMoney(ulw.getMoney());
                    Response<Boolean> result = levelUp(userLevel,ulw.getType(),ulw.getUserIdListString());

                    UserLevelWait newUserLevelWait = new UserLevelWait();
                    newUserLevelWait.setId(ulw.getId());
                    if(result.isSuccess()){
                        newUserLevelWait.setStatus(2);
                    }else{
                        newUserLevelWait.setReason(result.getError());
                    }
                    userLevelWaitDao.update(newUserLevelWait);
                }

                if(userLevelWaitList.size() == size){
                    pageNo++;
                }else{
                    break;
                }
            }else{
                break;
            }
        }
    }

    ;


    //寻找合适等级上级
    public UserTeam selectParentTeam(Long userId , Integer level){
        Long member = userId;
        Integer num = 0;
        //防止无限循环
        while (num<10) {
            //查找所属团队
            UserTeamMember userTeamMember = new UserTeamMember();
            userTeamMember.setMember(member);
            userTeamMember.setRole("MEMBER");
            List<UserTeamMember> userTeamMemberList = userTeamMemberDao.selectBy(userTeamMember);
            if (userTeamMemberList == null || userTeamMemberList.size() == 0) {
                return null;
            }

            //查找所属团队的领队
            Long teamId = userTeamMemberList.get(0).getTeam();
            UserTeam userTeam = new UserTeam();
            userTeam.setId(teamId);
            List<UserTeam> leaderUserTeamList = userTeamDao.selectBy(userTeam);
            if (leaderUserTeamList == null || leaderUserTeamList.size() == 0) {
                return null;
            }

            //查找领队等级
            UserLevel leaderLevel = new UserLevel();
            leaderLevel.setUserId(leaderUserTeamList.get(0).getLeader());
            List<UserLevel> leaderLevelList = userLevelDao.selectByUserId(leaderLevel);
            if (leaderLevelList == null || leaderLevelList.size() == 0) {
                return null;
            }

            //如果领队等级大于新用户等级，则管理新用户，否则继续寻找
            if(leaderLevelList.get(0).getLevel() > level){
                return leaderUserTeamList.get(0);
            }else{
                member = leaderUserTeamList.get(0).getLeader();
                num++;
            }
        }
        return null;
    }

    //更改推荐人，userId是需要更改的人，inviter是新的推荐人，type=1的时候是合伙人修改，type=2的时候是非天使修改
    public boolean updateUserInviter(UserLevel userLevel,Integer type,Long oldInviter){
        if(userLevel.getUserId() !=null && userLevel.getInviter() !=null && type !=null){
            List<UserLevel> userLevelList=userLevelDao.selectByUserId(userLevel);
            if(userLevelList != null && userLevelList.size() >0 && userLevelList.get(0).getInviter() !=null) {
                UserInviterHistory userInviterHistory = new UserInviterHistory();
                userInviterHistory.setUserId(userLevel.getUserId());
                userInviterHistory.setInviter(oldInviter);
                userInviterHistory.setType(type);
                userInviterHistoryDao.insert(userInviterHistory);

                userLevelDao.updateByUserId(userLevel);
            }
        }
        return true;
    }


    //更改等级，userId是需要更改的人，level是新的等级
    public boolean updateUserLevel(UserLevel userLevel){
        if(userLevel.getUserId() !=null && userLevel.getLevel() !=null){

            //先查看是否有老的等级
            List<UserLevel> userLevelList=userLevelDao.selectByUserId(userLevel);
            if(userLevelList != null && userLevelList.size() >0){
                //有老的等级，记录历史
                UserLevelHistory userLevelHistory = new UserLevelHistory();
                userLevelHistory.setUserId(userLevel.getUserId());
                userLevelHistory.setLevel(userLevelList.get(0).getLevel());
                userLevelHistoryDao.insert(userLevelHistory);

                userLevelDao.updateByUserId(userLevel);
            }else{
                //没有老的等级，直接添加
                userLevelDao.insert(userLevel);
            }
        }
        return true;
    }


    //更改团队，userId是需要更改的人，team是新的团队Id,
    public boolean updateUserTeamMember(UserTeamMember userTeamMember){
        if(userTeamMember.getMember() !=null && userTeamMember.getTeam() !=null){
            //先查看是否有老的团队
            userTeamMember.setRole("MEMBER");
            List<UserTeamMember> userTeamMemberList = userTeamMemberDao.selectBy(userTeamMember);
            if(userTeamMemberList !=null && userTeamMemberList.size() >0){
                //有老的团队，记录历史
                UserTeamHistory userTeamHistory = new UserTeamHistory();
                userTeamHistory.setTeam(userTeamMemberList.get(0).getTeam());
                userTeamHistory.setUserId(userTeamMember.getMember());
                userTeamHistoryDao.insert(userTeamHistory);

                userTeamMemberDao.updateByMember(userTeamMember);
            }else{
                //没有老的团队，直接添加
                userTeamMemberDao.insert(userTeamMember);
            }

            //更新user_relation表关系
            updateUserRelation(userTeamMember.getMember());
            return true;
        }else{
            return false;
        }
    }

    //更新user_relation表关系
    public boolean updateUserRelation (Long userId){
        Long nowId = userId;
        UserRelation userRelations = new UserRelation();
        userRelations.setUserId(userId);

        //删除原有关系
        userRelationDao.delete(userRelations);

        //判断需要更新用户的等级是否大于等于6（公司），是的话直接返回
        UserLevel userLevel = new UserLevel();
        userLevel.setUserId(userId);
        List<UserLevel> userLevelList = userLevelDao.selectByUserId(userLevel);
        if(userLevelList == null || userLevelList.size() == 0 || userLevelList.get(0).getLevel() >=6){
            return true;
        }

        //防止无限循环
        Integer num = 0;
        while (num<10){
            //查找被管理人等级
            UserLevel childrenLevel = new UserLevel();
            childrenLevel.setUserId(nowId);
            List<UserLevel> childrenLevelList = userLevelDao.selectByUserId(childrenLevel);
            if(childrenLevelList == null || childrenLevelList.size() == 0 || childrenLevelList.get(0).getLevel() >=6){
                break;
            }

            //查找被管理人所属团队
            UserTeamMember userTeamMember = new UserTeamMember();
            userTeamMember.setMember(nowId);
            userTeamMember.setRole("MEMBER");
            List<UserTeamMember> userTeamMemberList = userTeamMemberDao.selectBy(userTeamMember);
            if(userTeamMemberList ==null || userTeamMemberList.size() == 0){
                break;
            }

            //查找所属团队的领队
            Long teamId = userTeamMemberList.get(0).getTeam();
            UserTeam userTeam = new UserTeam();
            userTeam.setId(teamId);
            List<UserTeam> userTeamList = userTeamDao.selectBy(userTeam);
            if(userTeamList ==null || userTeamList.size() == 0){
                break;
            }

            //user_relation表记录关系
            Long leader = userTeamList.get(0).getLeader();
            UserRelation userRelation = new UserRelation();
            userRelation.setUserId(userId);
            userRelation.setLevel(userLevelList.get(0).getLevel());
            userRelation.setParent(leader);
            userRelationDao.insert(userRelation);

            //判断管理人的等级是否大于等于6（公司），是的话直接返回
            UserLevel parentLevel = new UserLevel();
            parentLevel.setUserId(leader);
            List<UserLevel> parentLevelList = userLevelDao.selectByUserId(parentLevel);
            if(parentLevelList == null || parentLevelList.size() == 0 || parentLevelList.get(0).getLevel()>=6){
                break;
            }else{
                nowId = leader;
            };
            num++;
        }

        if(num >=10){
            return false;
        }
        return true;
    }


}