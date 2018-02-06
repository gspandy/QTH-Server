package com.nowbook.sdp.service;

import com.nowbook.common.model.PageInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.dao.PaymentDetailDayDao;
import com.nowbook.sdp.model.PaymentDetailDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author dpzh
 * @create 2017-07-26 10:18
 * @description: levelService
 **/
@Service
public class PaymentDetailDayServiceImpl implements PaymentDetailDayService{

    @Autowired
    private PaymentDetailDayDao paymentDetailDayDao;

    @Override
    public Response<Paging<PaymentDetailDay>> findBy(String createStartAt, String createEndAt, Integer pageNo, Integer pageSize) throws ParseException {
        Response<Paging<PaymentDetailDay>> result = new Response<Paging<PaymentDetailDay>>();
        PageInfo page = new PageInfo(pageNo, pageSize);
        PaymentDetailDay paymentDetailDay = new PaymentDetailDay();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if (createStartAt != null && !createStartAt.equals("")) {
            paymentDetailDay.setCreateStartAt(format.parse(createStartAt));
        }
        if (createEndAt != null && !createEndAt.equals("")) {
            Date date = format.parse(createEndAt);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 1);
            paymentDetailDay.setCreateEndAt(cal.getTime());
        }
        paymentDetailDay.setOffset(page.getOffset());
        paymentDetailDay.setLimit(page.getLimit());
        Paging<PaymentDetailDay> list = paymentDetailDayDao.paymentDetailDayPaging(paymentDetailDay);
        result.setResult(list);
        return result;
    }

    @Override
    public List<PaymentDetailDay> select(PaymentDetailDay paymentDetailDay) {
        List<PaymentDetailDay> result = paymentDetailDayDao.findBy(paymentDetailDay);
        return result;
    }

    @Override
    public void update(PaymentDetailDay paymentDetailDay){
        paymentDetailDayDao.update(paymentDetailDay);
    }
}
