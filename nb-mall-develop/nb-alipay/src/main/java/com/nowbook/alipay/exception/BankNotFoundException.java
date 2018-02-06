package com.nowbook.alipay.exception;

/**
 * 未找到指定银行
 *
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-03-19 11:41 AM  <br>
 * Author:cheng
 */
public class BankNotFoundException extends RuntimeException{

    public BankNotFoundException(String s) {
        super(s);
    }
}
