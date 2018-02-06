package com.nowbook.sms.inner;

import java.util.List;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-05-24
 */
public class SmsResponse {

    private String code;

    private List<SmsMessage> messages;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<SmsMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<SmsMessage> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "SmsResponse{" +
                "code='" + code + '\'' +
                ", messages=" + messages +
                '}';
    }
}
