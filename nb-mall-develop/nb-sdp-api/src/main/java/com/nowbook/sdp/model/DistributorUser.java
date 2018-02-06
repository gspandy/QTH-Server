package com.nowbook.sdp.model;

import com.nowbook.user.model.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by pujian on 2016/5/24.
 */
@ToString
@EqualsAndHashCode
public class DistributorUser extends PageModel implements Serializable {
    private static final long serialVersionUID = 8272452385961190314L;
    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private Long distributorId;

    @Setter
    @Getter
    private Long userId;

    @Setter
    @Getter
    private User user;

    @Setter
    @Getter
    private Date operTime;
}
