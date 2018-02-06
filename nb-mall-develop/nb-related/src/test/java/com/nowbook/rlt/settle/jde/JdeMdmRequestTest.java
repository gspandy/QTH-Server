package com.nowbook.rlt.settle.jde;

import com.nowbook.rlt.jde.mdm.JdeMdmRequest;
import com.nowbook.rlt.jde.mdm.MdmPagingResponse;
import org.joda.time.DateTime;
import org.junit.Test;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-07-31 10:49 AM  <br>
 * Author:cheng
 */
public class JdeMdmRequestTest {

    @Test
    public void testLoad() {
        String url = "http://58.56.128.84:9001/EAI/RoutingProxyService/EAI_REST_POST_ServiceRoot?INT_CODE=temp_service_248";
        MdmPagingResponse result = JdeMdmRequest.build(url).startAt(DateTime.now().minusYears(1).toDate()).endAt(DateTime.now().toDate()).pageNo(1).load(10);
//        System.out.println(result);

        System.out.println(result.isSuccess());

    }

}
