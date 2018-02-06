/*
 * Copyright (c) 2012 大连锦霖科技有限公司
 */

package com.nowbook.admin;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2012-08-22
 */
public class MailClient {
    private final static Logger log = LoggerFactory.getLogger(MailClient.class);
    private final JavaMailSenderImpl javaMailSender;

    public MailClient(JavaMailSenderImpl javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void send(String subject, String html, String name, String to, String... ccList) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setTo(to);
            helper.setFrom(!Strings.isNullOrEmpty(name) ? name : javaMailSender.getUsername());
            if (ccList != null && ccList.length > 0) {
                helper.setCc(ccList);
            }

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("send mail to {} failed,cause:{}", to, e);
        }
    }
}
