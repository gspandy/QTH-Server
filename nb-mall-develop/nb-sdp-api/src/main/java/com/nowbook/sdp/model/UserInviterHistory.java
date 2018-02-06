package com.nowbook.sdp.model;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UserInviterHistory extends PageModel{

    private static final long serialVersionUID = 6404288790255635091L;

    public static enum TYPE {
        ONE(1, "准天使更换推荐人"),
        TWO(2, "升级到合伙人后，原推荐人并没有及时达到黑卡级别");

        private final int value;

        private final String display;

        private TYPE(int number, String display) {
            this.value = number;
            this.display = display;
        }

        public static TYPE fromNumber(int number) {
            for (TYPE type : TYPE.values()) {
                if (Objects.equal(type.value, number)) {
                    return type;
                }
            }
            return null;
        }

        public int toNumber() {
            return value;
        }


        @Override
        public String toString() {
            return display;
        }
    }

    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private Long userId;//团队ID

    @Setter
    @Getter
    private Long inviter;//历史推荐人ID

    @Setter
    @Getter
    private Integer type;//更换推荐人原因
}