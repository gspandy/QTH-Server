/*
 * Copyright (c) 2013 大连锦霖科技有限公司
 */

package com.nowbook.web.controller.api.userEvent;

import com.nowbook.common.utils.CommonConstants;
import com.nowbook.common.utils.CookieBuilder;
import com.nowbook.trade.service.CartService;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-10
 */
@Component
public class CartHandler {

    private final UserEventBus eventBus;

    private final CartService cartService;

    private final CommonConstants commonConstants;

    @Autowired
    public CartHandler(UserEventBus eventBus, CartService cartService, CommonConstants commonConstants) {
        this.eventBus = eventBus;
        this.cartService = cartService;
        this.commonConstants = commonConstants;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    @Subscribe
    public void merge(LoginEvent loginEvent) {
        String value = loginEvent.getCookie(CommonConstants.COOKIE_SHOP_CART);
        if (Strings.isNullOrEmpty(value)) {
            return;
        }
        cartService.merge(value.trim(), loginEvent.getUserId());
    }

    @Subscribe
    public void delete(LogoutEvent logoutEvent) {
        HttpServletResponse response = logoutEvent.getResponse();
        Cookie cartCookie = CookieBuilder.from(CommonConstants.COOKIE_SHOP_CART, "deleteMe", commonConstants.getDomain())
                .maxAge(0).build();
        response.addCookie(cartCookie);
    }
}
