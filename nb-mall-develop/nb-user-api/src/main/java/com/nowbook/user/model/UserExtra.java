package com.nowbook.user.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yangzefeng on 14-3-3
 */
public class UserExtra implements Serializable{
    private static final long serialVersionUID = 6404288790255635091L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private String wechatOpenId;


    @Getter
    @Setter
    private String nick;//昵称

    @Getter
    @Setter
    @URL
    private String avatar;//头像

    @Getter
    @Setter
    private String qrCodeUrl;//二维码

    @Getter
    @Setter
    private Double longitude;//经纬

    @Getter
    @Setter
    private Double latitude;//经纬

    @Getter
    @Setter
    private String payPassword;//支付密码


    @Getter
    @Setter
    private String thirdId;//第三方ID

    @Getter
    @Setter
    private Integer thirdType;//第三方类型

    @Getter
    @Setter
    private Integer businessId;

    @Getter
    @Setter
    private Integer tradeQuantity; //交易数目

    @Getter
    @Setter
    private Long tradeSum; //交易总额

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private Date updatedAt;

    public void increaseTradeSum(Long fee) {
        tradeSum = tradeSum==null?fee:fee+tradeSum;
        tradeQuantity = tradeQuantity==null?1:tradeQuantity+1;
    }

    public void increaseTradeSum(Long fee, Integer quantity) {
        tradeSum = tradeSum==null?fee:fee+tradeSum;
        tradeQuantity = tradeQuantity==null?1:tradeQuantity+quantity;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof UserExtra)) {
            return false;
        }
        UserExtra that = (UserExtra) o;
        return Objects.equal(this.id, that.id) && Objects.equal(this.userId, that.userId)
                && Objects.equal(this.businessId, that.businessId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id, this.userId);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .add("code",code)
                .add("wechatOpenId",wechatOpenId)
                .add("nick",nick)
                .add("avatar",avatar)
                .add("qrCodeUrl",qrCodeUrl)
                .add("longitude",longitude)
                .add("thirdId",thirdId)
                .add("thirdType",thirdType)
                .add("businessId", businessId)
                .add("tradeQuantity", tradeQuantity)
                .add("tradeSum", tradeSum)
                .toString();
    }
}
