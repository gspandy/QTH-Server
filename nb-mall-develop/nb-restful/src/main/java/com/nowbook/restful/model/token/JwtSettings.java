package com.nowbook.restful.model.token;


import com.nowbook.restful.security.jwt.JwtAuthenticationToken;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtSettings {
    @Getter
    @Setter
    @Value("#{app.headerPrefix}")
    private String headerPrefix;
    @Getter
    @Setter
    @Value("#{app.tokenHeader}")
    private String tokenHeader;
    /**
     * {@link JwtAuthenticationToken} will expire after this time.
     */
    @Getter
    @Setter
    @Value("#{app.tokenExpirationTime}")
    private Integer tokenExpirationTime;

    /**
     * Token issuer.
     */
    @Getter
    @Setter
    @Value("#{app.tokenIssuer}")
    private String tokenIssuer;

    /**
     * Key is used to sign {@link JwtAuthenticationToken}.
     */
    @Getter
    @Setter
    @Value("#{app.tokenSigningKey}")
    private String tokenSigningKey;

    /**
     * {@link JwtAuthenticationToken} can be refreshed during this timeframe.
     */
    @Getter
    @Setter
    @Value("#{app.refreshTokenExpTime}")
    private Integer refreshTokenExpTime;

    @Getter
    @Setter
    @Value("#{app.paymentTokenExpirationTime}")
    private Integer paymentTokenExpirationTime;
    /**
     * Api path.
     */
    @Getter
    @Setter
    @Value("#{app.apiPath}")
    private String apiPath;



}
