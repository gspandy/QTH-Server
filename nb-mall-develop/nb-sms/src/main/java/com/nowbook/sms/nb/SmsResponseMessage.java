package com.nowbook.sms.nb;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;


/**
 * Author:  <a href="mailto:cheng@nowbook.com">xiao</a>
 * Date: 2013-12-18
 */
@XStreamAlias("Message")
public class SmsResponseMessage {

    @Getter
    @Setter
    @XStreamAlias("DesMobile")
    private String desMobile;
    @Getter
    @Setter
    @XStreamAlias("SMSID")
    private String smsId;


    public SmsResponseMessage(String desMobile, String smsId) {
        this.desMobile = desMobile;
        this.smsId = smsId;
    }
}
