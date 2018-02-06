package com.nowbook.sms.exception;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-05-23
 */
public class SmsException extends RuntimeException {
    public SmsException() {
    }

    public SmsException(String s) {
        super(s);
    }

    public SmsException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SmsException(Throwable throwable) {
        super(throwable);
    }
}
