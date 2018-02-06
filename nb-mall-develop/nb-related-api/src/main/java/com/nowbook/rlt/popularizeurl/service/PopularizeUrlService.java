package com.nowbook.rlt.popularizeurl.service;

import com.nowbook.common.model.Response;

/**
 * Created by 王猛 on 14-9-23
 */
public interface PopularizeUrlService {

    /**
     *
     * @param popUrl 推广url
     * @return 跳转url
     */
    Response<String> getUrl(String popUrl);

    /**
     * 创建推广链接
     * @param popUrl 推广url
     * @param url
     */
    Response<Boolean> createPopUrl(String popUrl, String url);
}
