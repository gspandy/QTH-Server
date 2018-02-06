package com.nowbook.alipay.request;

import com.nowbook.common.utils.JsonMapper;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.nowbook.common.utils.Arguments.notEmpty;
import static com.nowbook.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 *
 * 回调的url
 *
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-05-04 9:42 AM  <br>
 * Author:cheng
 */
public class CallBack {

    @Getter
    @Setter
    private String url;

    @Getter
    @Setter
    private Map<String, Object>  params = Maps.newTreeMap();

    public CallBack(String suffix) {
        this.url = suffix;
    }


    public void append(String key, String value) {
        checkArgument(notEmpty(key));
        params.put(key, value);
    }

    @Override
    public String toString() {
        checkArgument(notNull(url));

        if (params.size() == 0) {
            return url;
        }

        return url + "?" + "detail=" + JsonMapper.nonDefaultMapper().toJson(params);
    }

}
