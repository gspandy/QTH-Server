package com.nowbook.trade.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Description：物流信息
 * Author：Guo Chaopeng
 * Created on 14-4-21-下午2:47
 */
@Getter
@Setter
@ToString
public class LogisticsInfo implements Serializable {

    private static final long serialVersionUID = -3327784150845223752L;

    @Getter
    @Setter
    private Long id;               //自增主键

    @Getter
    @Setter
    private Long orderId;         //订单号

    @Getter
    @Setter
    private Long senderId;       //发物流人的id

    @Getter
    @Setter
    private String senderName;  //发物流人的名称

    @Getter
    @Setter
    private Integer sendFee;     //运费

    @Getter
    @Setter
    private Integer logisticsStatus;     //物流状态

    public static enum Status {
        SEND(1, "已发货"),
        RECEIVE(2, "已收货"),
        REJECT(3, "买家拒绝收货");

        private final int value;

        private final String description;

        private Status(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return this.value;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    @Getter
    @Setter
    private String companyName;   //物流公司名称

    @Getter
    @Setter
    private String freightNote;    //物流运单编号

    @Getter
    @Setter
    private Date createdAt;          //创建时间

    @Getter
    @Setter
    private Date updatedAt;          //修改时间
}
