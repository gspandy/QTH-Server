package com.nowbook.sdp.service;

import com.nowbook.common.model.Response;
import com.nowbook.sdp.dao.LevelDao;
import com.nowbook.sdp.dao.UserBankDao;
import com.nowbook.sdp.model.Level;
import com.nowbook.sdp.model.UserBank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author dpzh
 * @create 2017-07-26 10:18
 * @description: levelService
 **/
@Service
public class UserBankServiceImpl implements UserBankService{
    @Autowired
    private UserBankDao userBankDao;

    @Override
    public Response<UserBank> viewBank(Long userId) {
        Response<UserBank> result = new Response<UserBank>();
        UserBank userBank = new UserBank();
        userBank.setUserId(userId);
        List<UserBank> userBankList = userBankDao.selectByUserId(userBank);
        if(userBankList != null && userBankList.size()>0){
            String bankCardNo = userBankList.get(0).getBankCardNo();
            if(bankCardNo !=null && bankCardNo.length()>4){
                bankCardNo = bankCardNo.substring(bankCardNo.length()-4,bankCardNo.length());
            }
            userBankList.get(0).setBankCardNo(bankCardNo);
            result.setResult(userBankList.get(0));
        }else{
            result.setResult(new UserBank());
        }
        return result;
    }

    @Override
    public Response<UserBank> viewBankForPayment(Long userId) {
        Response<UserBank> result = new Response<UserBank>();
        UserBank userBank = new UserBank();
        userBank.setUserId(userId);
        List<UserBank> userBankList = userBankDao.selectByUserId(userBank);
        if(userBankList != null && userBankList.size()>0){
            result.setResult(userBankList.get(0));
        }else{
            result.setResult(new UserBank());
        }
        return result;
    }

    @Override
    public Response<Boolean> bindingBank(UserBank userBank) {
        Response<Boolean> result = new Response<Boolean>();
        List<UserBank> userBankList = userBankDao.selectByUserId(userBank);
        if(userBankList != null && userBankList.size()>0){
            result.setError("has binding");
        }else{
            userBankDao.insert(userBank);
            result.setResult(true);
        }
        return result;
    }

    @Override
    public Response<Boolean> unBindingBank(UserBank userBank) {
        Response<Boolean> result = new Response<Boolean>();
        result.setResult(false);
        List<UserBank> userBankList = userBankDao.selectByUserId(userBank);
        if(userBankList != null && userBankList.size()>0){
            if(userBankList.get(0).getId().equals(userBank.getId())){
                userBankDao.deleteById(userBank);
                result.setResult(true);
            }
        }
        return result;
    }
}
