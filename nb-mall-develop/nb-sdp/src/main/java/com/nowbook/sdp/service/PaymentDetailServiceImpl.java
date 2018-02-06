package com.nowbook.sdp.service;

import com.nowbook.common.model.PageInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.dao.PaymentDetailDao;
import com.nowbook.sdp.model.PaymentDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Romo on 2017/8/22.
 */
@Slf4j
@Service
public class PaymentDetailServiceImpl implements PaymentDetailService {

    @Autowired
    private PaymentDetailDao paymentDetailDao;

    // 获取返润结算明细
    @Override
    public Response<Map<String, Object>> paymentDetailList(String createTime,
                                                           String startAt,
                                                           String endAt,
                                                           String payStartAt,
                                                           String payEndAt,
                                                           String mobile,
                                                           Integer payType,
                                                           Integer payStatus,
                                                           Integer payResult,
                                                           Integer pageNo,
                                                           Integer size) throws ParseException {
        Response<Map<String, Object>> result = new Response<Map<String, Object>>();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        PageInfo page = new PageInfo(pageNo, size);
        PaymentDetail paymentDetail = new PaymentDetail();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        if (createTime != null && !createTime.equals("")) {
            Date endDate = format.parse(createTime);
            paymentDetail.setCreateEndAt(endDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.add(Calendar.DATE, -6);
            paymentDetail.setCreateStartAt(cal.getTime());
        }
        if (startAt != null && !startAt.equals("")) {
            Date startDate = format1.parse(startAt);
            paymentDetail.setCreateStartAt(startDate);
        }
        if (endAt != null && !endAt.equals("")) {
            Date endDate = format1.parse(endAt);
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.add(Calendar.DATE, 1);
            paymentDetail.setCreateEndAt(cal.getTime());
        }
        if (payStartAt != null && !payStartAt.equals("")) {
            Date startDate = format1.parse(payStartAt);
            paymentDetail.setPayStartAt(startDate);
        }
        if (payEndAt != null && !payEndAt.equals("")) {
            Date endDate = format1.parse(payEndAt);
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.add(Calendar.DATE, 1);
            paymentDetail.setPayEndAt(cal.getTime());
        }
        paymentDetail.setMobile(mobile);
        paymentDetail.setPayType(payType);
        paymentDetail.setPayResult(payResult);
        paymentDetail.setPayStatus(payStatus);
        paymentDetail.setOffset(page.getOffset());
        paymentDetail.setLimit(page.getLimit());
        Paging<PaymentDetail> list = paymentDetailDao.paymentDetailPaging(paymentDetail);
        resultMap.put("list", list);

        String title = "付款明细";
        if (createTime != null && !createTime.equals("")) {
            title = title + "（" + format1.format(paymentDetail.getCreateStartAt()) +
                    " - " + format1.format(paymentDetail.getCreateEndAt()) + "）";
        }
        resultMap.put("title", title);

        result.setResult(resultMap);
        return result;
    }

    @Override
    public List<PaymentDetail> select(PaymentDetail paymentDetail) {
        List<PaymentDetail> result = paymentDetailDao.findBy(paymentDetail);
        return result;
    }

    @Override
    public void update(PaymentDetail paymentDetail){
        paymentDetailDao.update(paymentDetail);
    }

}
