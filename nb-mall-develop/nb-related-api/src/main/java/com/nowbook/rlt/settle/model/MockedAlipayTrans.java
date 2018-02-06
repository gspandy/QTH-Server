package com.nowbook.rlt.settle.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-05-15 10:58 AM  <br>
 * Author:cheng
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class MockedAlipayTrans extends AlipayTrans implements Serializable {

    private static final long serialVersionUID = 6034070408462717534L;
}
