package com.nowbook.restful.model.token;


import com.nowbook.restful.dto.SimpleOrderInfo;
import com.nowbook.restful.security.jwt.JwtAuthenticationToken;
import com.nowbook.third.model.token.JwtToken;
import com.nowbook.trade.dto.FatOrder;
import com.nowbook.user.model.LoginInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkArgument;
import static com.nowbook.common.utils.Arguments.notEmpty;

/**
 * Factory class that should be always used to create {@link JwtAuthenticationToken}.
 *
 * @author vladimir.stankovic
 *
 * May 31, 2016
 */
@Component
public class JwtTokenFactory {

    private final JwtSettings settings;

    @Autowired
    public JwtTokenFactory(JwtSettings settings) {
        this.settings = settings;
    }

    /**
     * Factory method for issuing new JWT Tokens.
     *
     * @param loginInfo
     *
     * @return
     */
    public JwtAuthenticationToken createAccessJwtToken(LoginInfo loginInfo) {
        String token =createJwtToken(loginInfo);
        String refreshJwtToken=createRefreshJwtToken(loginInfo);
        return new JwtAuthenticationToken(loginInfo,token,refreshJwtToken);
    }

    /**
     * Factory method for issuing new JWT Tokens.
     *
     * @param loginInfo
     *
     * @return token
     */
    public String createJwtToken(LoginInfo loginInfo) {
        checkArgument(notEmpty(loginInfo.getId()), "cannot.create.token.without.username");
        Claims claims = Jwts.claims().setSubject(loginInfo.getId());
        claims.put("deviceId",loginInfo.getDeviceId());
        claims.put("type",loginInfo.getType());
        claims.put("deviceType",loginInfo.getDeviceType());

        DateTime currentTime = new DateTime();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(settings.getTokenIssuer())
                .setIssuedAt(currentTime.toDate())
                .setExpiration(currentTime.plusDays(settings.getTokenExpirationTime()).toDate())
                .signWith(SignatureAlgorithm.HS512, settings.getTokenSigningKey())
                .compact();

        return token;
    }

    /**
     * Factory method for issuing new Refresh JWT Tokens.
     *
     * @param loginInfo
     *
     * @return String
     */
    public String createRefreshJwtToken(LoginInfo loginInfo) {
        checkArgument(notEmpty(loginInfo.getId()), "cannot.create.token.without.username");
        Claims claims = Jwts.claims().setSubject(loginInfo.getId());
        claims.put("deviceId",loginInfo.getDeviceId());
        claims.put("type", loginInfo.getType());
        claims.put("deviceType",loginInfo.getDeviceType());
        DateTime currentTime = new DateTime();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(settings.getTokenIssuer())
                .setIssuedAt(currentTime.toDate())
                .setExpiration(currentTime.plusDays(settings.getRefreshTokenExpTime()).toDate())
                .signWith(SignatureAlgorithm.HS512, settings.getTokenSigningKey())
                .compact();

        return token;
    }

    /**
     * Factory method for issuing new JWT Tokens.
     *
     * @param loginInfo
     * @return JwtToken
     */
    public JwtToken createResponseToken(LoginInfo loginInfo) {
        String token=createJwtToken(loginInfo);
        String refreshToken=createRefreshJwtToken(loginInfo);
        return new JwtToken(token,refreshToken);
    }


    public String createPaymentJwtToken(String username,String prepayId ,String payType,String pass) {
        checkArgument(notEmpty(username), "cannot.create.token.without.username");
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("prepayId",prepayId);
        claims.put("pass",pass);
        claims.put("payType",payType);
        DateTime currentTime = new DateTime();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(settings.getTokenIssuer())
                .setIssuedAt(currentTime.toDate())
                .setExpiration(currentTime.plusSeconds(settings.getPaymentTokenExpirationTime()).toDate())
                .signWith(SignatureAlgorithm.HS512, settings.getTokenSigningKey())
                .compact();

        return token;
    }


    public JwtToken createPaymentToken(String username,String prepayId , String payType,String pass) {
        String token=createPaymentJwtToken(username,prepayId,payType,pass);
        return new JwtToken(token);
    }
}
