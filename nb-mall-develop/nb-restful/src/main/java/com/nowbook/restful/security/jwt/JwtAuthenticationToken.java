package com.nowbook.restful.security.jwt;

/**
 * 移动端安全认证Token
 *
 * @version 2014-7-5
 */

import com.nowbook.user.model.LoginInfo;
import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class JwtAuthenticationToken implements Serializable   {

    private static final long serialVersionUID = 1463027118399382008L;
    private static Logger logger = LoggerFactory.getLogger(JwtAuthenticationToken.class);

    @Getter
    LoginInfo loginInfo;
    @Getter
    @Setter
    private String token;
    @Getter
    @Setter
    private String refreshToken;
    public JwtAuthenticationToken(LoginInfo loginInfo,String token, String refreshToken) {
        this.loginInfo = loginInfo;
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public JwtAuthenticationToken(String token) {
        this.token = token;
    }


    /**
     * Parses and validates JWT Token signature.
     *
     * @throws IllegalArgumentException
     */
    public Jws<Claims> parseClaims(String signingKey){
        try {
            return Jwts.parser().setSigningKey(signingKey).parseClaimsJws(this.token);
        } catch (UnsupportedJwtException ue) {
            logger.error("fail to get token  with token:{}, error:{}", this.token, ue.getMessage());
            throw new IllegalArgumentException("token.invalid", ue);
        } catch (MalformedJwtException me) {
            logger.error("fail to get token  with token:{}, error:{}", this.token, me.getMessage());
            throw new IllegalArgumentException("token.invalid", me);
        } catch (IllegalArgumentException ie) {
            logger.error("fail to get token  with token:{}, error:{}", this.token, ie.getMessage());
            throw new IllegalArgumentException("token.invalid", ie);
        } catch (SignatureException se) {
            logger.error("fail to get token  with token:{}, error:{}", this.token, se.getMessage());
            throw new IllegalArgumentException("token.invalid", se);
        } catch (ExpiredJwtException expiredEx) {
            logger.error("jwt token is expired token:{}, error:{}", this.token, expiredEx.getMessage());
            throw new IllegalArgumentException("token.expired", expiredEx);
        }
    }
}
