package com.nowbook.web.controller.api.userEvent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-06-09 5:38 PM  <br>
 * Author:cheng
 */
@ToString
public class RegisterEvent extends SessionEvent {

    @Getter
    @Setter
    private String userName;



    public RegisterEvent(long userId, String userName, HttpServletRequest request, HttpServletResponse response) {
        super(userId, request, response);
        this.userName = userName;
    }
}
