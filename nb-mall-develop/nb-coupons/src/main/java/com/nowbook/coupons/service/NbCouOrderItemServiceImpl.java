package com.nowbook.coupons.service;

import com.nowbook.common.model.Response;
import com.nowbook.coupons.dao.NbCouOrderItemDao;
import com.nowbook.coupons.model.NbCouOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2014/8/24.
 */
@Service
public class NbCouOrderItemServiceImpl implements NbCouOrderItemService {

    @Autowired
    private NbCouOrderItemDao nbCouOrderItemDao;

    @Override
    public Response<Boolean> saveCouOrderItem(NbCouOrderItem nbCouOrderItem) {
        Response<Boolean> result = new Response<Boolean>();
        try{
            Boolean istrue =  nbCouOrderItemDao.saveCouOrderItem(nbCouOrderItem);
            result.setResult(istrue);
            return result;
        }catch(Exception e){
            //log.error("failed to update brand, cause:", e);
            result.setError("brand.update.fail");
            return result;
        }
    }
}
