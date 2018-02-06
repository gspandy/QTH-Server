package com.nowbook.admin.web.interceptors;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import com.nowbook.common.model.Response;
import com.nowbook.common.utils.CommonConstants;
import com.nowbook.restful.model.token.JwtSettings;
import com.nowbook.restful.security.jwt.JwtAuthenticationToken;
import com.nowbook.restful.security.jwt.extractor.ResponseUtil;
import com.nowbook.restful.security.jwt.extractor.TokenExtractor;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import com.nowbook.user.model.User;
import com.nowbook.user.service.AccountService;
import com.nowbook.user.service.TokenService;
import com.nowbook.user.util.LoginInfoUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

/*
 * Author: jl
 * Date: 2013-01-22
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {
    private final static Logger log = LoggerFactory.getLogger(LoginInterceptor.class);
    private final Pattern urlPattern;
    private final AccountService<User> accountService;
    private final Set<WhiteItem> whiteList;
    @Autowired
    public LoginInterceptor(AccountService<User> accountService,JwtSettings settings) throws Exception{
        this.settings=settings;
        //手机端api请求path 所有手机端的根path
        this.urlPattern = Pattern.compile("^" + settings.getApiPath() + "$");
        this.accountService = accountService;
        whiteList = Sets.newHashSet();
        //获取手机端API列表
        Resources.readLines(Resources.getResource("/api_white_list"), Charsets.UTF_8, new LineProcessor<Void>() {
            @Override
            public boolean processLine(String line) throws IOException {
                if (!nullOrComment(line)) {
                    line = Splitter.on("#").trimResults().splitToList(line).get(0);
                    List<String> parts = Splitter.on(':').trimResults().splitToList(line);
                    checkState(parts.size() == 2, "illegal white_list configuration [%s]", line);
                    Pattern urlPattern = Pattern.compile("^" + parts.get(0) + "$");
                    String methods = parts.get(1).toLowerCase();
                    ImmutableSet.Builder<String> httpMethods = ImmutableSet.builder();
                    for (String method : Splitter.on(',').omitEmptyStrings().trimResults().split(methods)) {
                        httpMethods.add(method);
                    }
                    whiteList.add(new WhiteItem(urlPattern, httpMethods.build()));

                }
                return true;
            }

            @Override
            public Void getResult() {
                return null;
            }
        });
    }

    @Autowired
    private TokenExtractor tokenExtractor;
    @Autowired
    private TokenService tokenService;

    private JwtSettings settings;


    private boolean nullOrComment(String line) {
        return (Strings.isNullOrEmpty(line) || line.startsWith("#"));
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        String requestURI = request.getRequestURI().substring(request.getContextPath().length());

        Object userId = null;
        //从session中取用户id 并且不是手机端API请求
        if (session != null&&!urlPattern.matcher(requestURI).matches()) {
            userId = session.getAttribute(CommonConstants.SESSION_USER_ID);

        } else if(urlPattern.matcher(requestURI).matches()) {

            try {
                //从token中取用户id
                userId = getToken(request);

            } catch (IllegalArgumentException e) {
                String method = request.getMethod().toLowerCase();
                for (WhiteItem whiteItem : whiteList) {  //method and uri matches with white list, ok
                    if (whiteItem.httpMethods.contains(method) && whiteItem.pattern.matcher(requestURI).matches()) {
                        return true;
                    }
                }
                log.error("fail to get token  with  error:{}", e.getMessage());

                ResponseUtil.renderErrorString(response, e.getMessage());
                return false;
            }
        }
        if (userId != null) {

            Response<User> result = accountService.findUserById(Long.parseLong(userId.toString()));
            if (!result.isSuccess()) {
                log.error("failed to find user where id={},error code:{}", userId, result.getError());
                return false;
            }
            User user = result.getResult();
            putCurrentUser(user);
            return true;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserUtil.removeUser();
        LoginInfoUtil.removeLoginInfo();
    }

    private String getToken(HttpServletRequest request) {
        //获取存在的token
        String tokenPayload = tokenExtractor.extract(request.getHeader(settings.getTokenHeader()));
        //如果token有效或者未过期利用现有token生成
        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(tokenPayload);
        Jws<Claims> claims = jwtAuthenticationToken.parseClaims(settings.getTokenSigningKey());
        String subject=claims.getBody().getSubject();
        int type=(Integer)claims.getBody().get("type");
        String deviceId=(String)claims.getBody().get("deviceId");
        int deviceType=(Integer)claims.getBody().get("deviceType");
        //验证token是否存在
        tokenService.verifyToken(tokenPayload,Long.parseLong(subject),deviceId, deviceType);
        LoginInfoUtil.putLoginInfo(tokenService.getRedisToken(Long.parseLong(subject),deviceId, deviceType));
        return subject;

    }

    private void putCurrentUser(User user) {
        BaseUser baseUser = new BaseUser(user.getId(), user.getName(), user.getType());
        baseUser.setParentId(user.getParentId());
        UserUtil.putCurrentUser(baseUser);
    }

}
