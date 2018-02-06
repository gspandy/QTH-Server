package com.nowbook.third.model.token;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * return response token
 */
public class JwtToken implements Serializable {
    private static final long serialVersionUID = -9202310569677007176L;
    @Getter
    @Setter
    private String token;
    @Getter
    @Setter
    private String refreshToken;

    @Getter
    @Setter
    private String mobile;

    @Getter
    @Setter
    private String nick;

    @Getter
    @Setter
    private String realName;

    public JwtToken() {
    }
    public JwtToken(String token) {

        this.token = token;
    }
    public JwtToken(String token,String refreshToken) {

        this.token = token;
        this.refreshToken=refreshToken;
    }

}
