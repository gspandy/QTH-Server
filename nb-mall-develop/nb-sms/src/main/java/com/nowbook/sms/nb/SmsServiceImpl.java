package com.nowbook.sms.nb;


import com.nowbook.common.model.Response;
import com.nowbook.sms.SmsService;
import com.nowbook.sms.exception.SmsException;
import com.nowbook.web.misc.MessageSources;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

import static com.nowbook.common.utils.Arguments.equalWith;
import static com.nowbook.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.*;

/**
 * Author:  <a href="mailto:cheng@nowbook.com">xiao</a>
 * Date: 2013-12-18
 */
@Service
public class SmsServiceImpl implements SmsService {
    private final static Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);
    @Autowired
    private MessageSources messageSources;
    //短信长度阈值
    private static final int MSG_MAX_LEN = 600;

    private static final String DEFAULT_SMS_TYPE = "";

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"GBK\"?>\n";

    @Value("#{app.operId}")
    private String operId;
    @Value("#{app.operPass}")
    private String operPass;
    @Value("#{app.smsRequestUrl}")
    private String smsRequestUrl;
    @Value("#{app.taobaoClientUrl}")
    private String taobaoClientUrl;
    @Value("#{app.taobaoAppkey}")
    private String taobaoAppkey;
    @Value("#{app.taobaoSecret}")
    private String taobaoSecret;
    @Value("#{app.taobaoSmsFreeSignName}")
    private String taobaoSmsFreeSignName;
    @Value("#{app.taobaoSmsTemplateCode}")
    private String taobaoSmsTemplateCode;

    public SmsServiceImpl() {
    }

    //constructor for mock
    public SmsServiceImpl(String operId, String operPass, String smsRequestUrl) {
        this.operId = operId;
        this.operPass = operPass;
        this.smsRequestUrl = smsRequestUrl;
    }

    /**
     * 发送单条信息: <br/>
     * <p>
     * 普通短信与长短信是两种提交模式。<br/>
     * 普通短信目前长度阀值为67个字，超过该字数长度将被系统拆分，但发送到手机上仍然显示为一条完整短信；<br/>
     * 短信内容的最大长度无限制，但考虑到短信接收成功率问题，<br/>
     * 短信总长度按照长短信拆分后不要超过10条(长短信按67个字进行拆分)<br/>
     * 短信编码方式采用GBK
     * </p>
     *
     * @param from    发送方手机
     * @param to      接收方手机号码
     * @param message 消息体
     * @throws SmsException 异常
     */
    @Override
    public Response<Boolean> sendSingle(String from, String to, String message) {
        Response<Boolean> response = new Response<Boolean>();
        response.setSuccess(false);
        response.setResult(false);
        try {
            //检查入参
            checkNotNull(message, "message can not be null");
            checkNotNull(from, "message sender can not be null");
            checkNotNull(to, "message receiver can not be null");


            int messageLength = message.length();
            checkArgument(messageLength <= MSG_MAX_LEN, "message too long: %s", messageLength);

            String messageType = getMessageType(message);

            log.info("sender:{},receiver:{},message:{}", from, to, message);

            //发送短信
            AlibabaAliqinFcSmsNumSendResponse smsResponse = sendMessage(from, to, message, messageType);

            checkState(notNull(smsResponse), "sms.ack.empty");
            checkState(notNull(smsResponse.getBody()), "sms.ack.empty");

            log.info("sms to {} ack code [{}]", to, "" + smsResponse.getErrorCode());

            String code = smsResponse.getResult().getErrCode() == null ? "NULL" : smsResponse.getResult().getErrCode();
            checkState(equalWith(smsResponse.getResult().getErrCode(), "0"), "sms.ack.error:" + code);

            response.setSuccess(Boolean.TRUE);
            response.setResult(Boolean.TRUE);

        } catch (IllegalStateException e) {
            log.error("sms sendSingle raise exception {}", e.getMessage());
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("sms sendSingle raise exception {}", e);
            response.setError("sms.send.fail");
        }

        return response;
    }

    /**
     * 发送单条信息:阿里大鱼接口 <br/>
     * <p>
     * 普通短信与长短信是两种提交模式。<br/>
     * 普通短信目前长度阀值为67个字，超过该字数长度将被系统拆分，但发送到手机上仍然显示为一条完整短信；<br/>
     * 短信内容的最大长度无限制，但考虑到短信接收成功率问题，<br/>
     * 短信总长度按照长短信拆分后不要超过10条(长短信按67个字进行拆分)<br/>
     * 短信编码方式采用GBK
     * </p>
     *
     * @param from     发送方手机
     * @param to       接收方手机号码
     * @param template 模版
     * @param message  消息体
     * @throws SmsException 异常
     */
    @Override
    public Response<Boolean> sendSingle(String from, String to, String template, String message) {
        Response<Boolean> response = new Response<Boolean>();
        response.setSuccess(false);
        response.setResult(false);
        try {
            //检查入参
            checkNotNull(message, "message can not be null");
            checkNotNull(from, "message sender can not be null");
            checkNotNull(to, "message receiver can not be null");


            int messageLength = message.length();
            checkArgument(messageLength <= MSG_MAX_LEN, "message too long: %s", messageLength);

            String messageType = getMessageType(message);

            log.info("sender:{},receiver:{},message:{}", from, to, message);

            //发送短信
            AlibabaAliqinFcSmsNumSendResponse smsResponse = sendMessage(from, to, template, message, messageType);

            checkState(notNull(smsResponse), "sms.ack.empty");
            checkState(notNull(smsResponse.getBody()), "sms.ack.empty");

            log.info("sms to {} ack code [{}]", to, "" + smsResponse.getErrorCode());

            String code = smsResponse.getResult().getErrCode() == null ? "NULL" : smsResponse.getResult().getErrCode();
            checkState(equalWith(smsResponse.getResult().getErrCode(), "0"), "sms.ack.error:" + code);

            response.setSuccess(Boolean.TRUE);
            response.setResult(Boolean.TRUE);

        } catch (IllegalStateException e) {
            log.error("sms sendSingle raise exception {}", e.getMessage());
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("sms sendSingle raise exception {}", e);
            response.setError("sms.send.fail");
        }

        return response;
    }

    /**
     * 群发信息:<br/>
     * <p>
     * 群发短信单次请求无数量上限，但考虑到效率问题，建议群发短信单次请求数不要超过200条
     * </p>
     *
     * @param from    发送方手机
     * @param to      接受方手机号码列表
     * @param message 消息体
     */
    @Override
    public Response<Boolean> sendGroup(String from, Iterable<String> to, String message) {
        Response<Boolean> response = new Response<Boolean>();
        response.setSuccess(false);
        response.setResult(true);

        try {
            //检查入参
            checkNotNull(message, "message can not be null");
            checkNotNull(from, "message sender can not be null");
            checkNotNull(to, "message receiver can not be null");


            String messageType = getMessageType(message);

            if (log.isDebugEnabled()) {
                log.debug("receiver:{},message:{}", to, message);
            }

            //发送短信
            String receiver = Joiner.on(",").skipNulls().join(to);
            AlibabaAliqinFcSmsNumSendResponse smsResponse = sendMessage(from, receiver, message, messageType);

            checkState(notNull(smsResponse), "sms.ack.empty");
            checkState(notNull(smsResponse.getBody()), "sms.ack.empty");

            String code = smsResponse.getErrorCode() == null ? "NULL" : smsResponse.getErrorCode();
            checkState(equalWith(smsResponse.getErrorCode(), "0"), "sms.ack.error:" + code);

            response.setSuccess(Boolean.TRUE);
            response.setResult(Boolean.TRUE);

        } catch (IllegalStateException e) {
            log.error("sms sendSingle raise exception {}", e.getMessage());
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("sms sendSingle raise exception {}", e);
            response.setError("sms.send.fail");
        }

        return response;
    }

    //根据当前消息长度判断当前消息类型
    //大于67小于200为长消息，小于67为普通消息，否则为非法消息
    private String getMessageType(String message) {
        int messageLength = message.length();
        checkArgument(messageLength <= MSG_MAX_LEN, "message too long: %s", messageLength);

        //默认以普通短信发送,锦霖短信平台会自动根据长度调整消息类型
        return DEFAULT_SMS_TYPE;
    }


    //发送短信
    /*private SmsResponse sendMessage(String from, String to, String message, String messageType) throws UnsupportedEncodingException {

        //拼装短信报文
        SmsRequestMessage smsMsg = new SmsRequestMessage(to, message, messageType);

        SmsRequestBody smsBody = new SmsRequestBody("", Lists.newArrayList(smsMsg));
        SmsRequest request = new SmsRequest(operId, operPass, smsBody);
        String requestXml = XmlTranslator.toXML(request);

        requestXml = XML_HEADER + requestXml;
        byte[] content = requestXml.getBytes("GBK");

        log.debug("sms request: {}", requestXml);
        //发送短信
        HttpRequest httpRequest = HttpRequest.post(smsRequestUrl, false).send(content).connectTimeout(10000).readTimeout(10000);
        String responseXml = httpRequest.body();
        log.debug("sms ack: {}", responseXml);


        //接收应答请求
        SmsResponse response = XmlTranslator.fromXML(responseXml, SmsResponse.class);
        checkNotNull(response, "response is null");
        checkNotNull(response.getBody(), "response message body is null");

        if (!Objects.equal(response.getBody().getCode(), "0")) {
            log.error("failed to send message from {} to {},response:{}", from, to, responseXml);
        }
        return response;
    }*/

    private AlibabaAliqinFcSmsNumSendResponse sendMessage(String from, String to, String message, String messageType) throws UnsupportedEncodingException {

        //拼装短信报文

        TaobaoClient client = new DefaultTaobaoClient(taobaoClientUrl, taobaoAppkey, taobaoSecret);
        AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
        //  req.setExtend("123456");
        req.setSmsType("normal");
        req.setSmsFreeSignName(messageSources.get("smsBody"));
        req.setSmsParamString("{\"code\":\"" + message + "\",\"product\":\"alidayu\"}");
        req.setRecNum(to);
        req.setSmsTemplateCode(messageSources.get("sms.templates.signName"));

        log.debug("sms request: {}", req);
        //发送短信

        AlibabaAliqinFcSmsNumSendResponse rsp = null;
        try {
            rsp = client.execute(req);
            //接收应答请求

            checkNotNull(rsp, "response is null");
            checkNotNull(rsp.getBody(), "response message body is null");
            log.debug("sms ack: {}", rsp.getBody());
            if (!Objects.equal(rsp.getResult().getErrCode(), "0")) {
                log.error("failed to send message from {} to {},response:{}", from, to, rsp.getBody());
            }
        } catch (Exception e) {
            log.error("sms sendSingle raise exception {}", e);
            // rsp.setError("sms.send.fail");
        }


        return rsp;
    }

    private AlibabaAliqinFcSmsNumSendResponse sendMessage(String from, String to, String template, String message, String messageType) throws UnsupportedEncodingException {

        //拼装短信报文

        TaobaoClient client = new DefaultTaobaoClient(taobaoClientUrl, taobaoAppkey, taobaoSecret);
        AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
        //  req.setExtend("123456");
        req.setSmsType("normal");
        //  req.setSmsFreeSignName(messageSources.get("sms.templates.signName"));
        req.setSmsFreeSignName(messageSources.get("smsBody"));
        req.setSmsParamString(message);
        req.setRecNum(to);
        req.setSmsTemplateCode(template);

        log.debug("sms request: {}", req);
        //发送短信

        AlibabaAliqinFcSmsNumSendResponse rsp = null;
        try {
            rsp = client.execute(req);
            //接收应答请求

            checkNotNull(rsp, "response is null");
            checkNotNull(rsp.getBody(), "response message body is null");
            log.debug("sms ack: {}", rsp.getBody());
            if (!Objects.equal(rsp.getResult().getErrCode(), "0")) {
                log.error("failed to send message from {} to {},response:{}", from, to, rsp.getBody());
            }
        } catch (Exception e) {
            log.error("sms sendSingle raise exception {}", e);
            // rsp.setError("sms.send.fail");
        }


        return rsp;
    }

    /**
     * 查询剩余短信条数
     *
     * @return 剩余短信条数
     */
    @Override
    public Integer available() {
        return null;
    }
}
