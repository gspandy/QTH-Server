package com.nowbook.restful.security.jwt.extractor;

import com.nowbook.restful.model.token.JwtSettings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * An implementation of {@link TokenExtractor} extracts token from
 * Authorization: Bearer scheme.
 * 
 * @author vladimir.stankovic
 *
 * Aug 5, 2016
 */
@Component
public class JwtHeaderTokenExtractor implements TokenExtractor {

    @Autowired
    private JwtSettings jwtSettings;

    @Override
    public String extract(String header){
        if (StringUtils.isBlank(header)) {
            throw new IllegalArgumentException("authorization.header.cannot.be.blank");
        }

        if (header.length() < jwtSettings.getHeaderPrefix().length()) {
            throw new IllegalArgumentException("invalid.authorization.header.size");
        }

        return header.substring(jwtSettings.getHeaderPrefix().length(), header.length());
    }
}
