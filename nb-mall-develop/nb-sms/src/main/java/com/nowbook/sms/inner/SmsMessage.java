package com.nowbook.sms.inner;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-05-24
 */
public class SmsMessage {
    private String desmobile;

    private String msgid;

    public String getDesmobile() {
        return desmobile;
    }

    public void setDesmobile(String desmobile) {
        this.desmobile = desmobile;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    @Override
    public String toString() {
        return "SmsMessage{" +
                "desmobile='" + desmobile + '\'' +
                ", msgid='" + msgid + '\'' +
                '}';
    }
}
