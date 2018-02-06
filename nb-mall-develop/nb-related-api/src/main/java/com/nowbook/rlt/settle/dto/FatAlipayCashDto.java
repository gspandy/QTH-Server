package com.nowbook.rlt.settle.dto;

import com.nowbook.common.utils.BeanMapper;
import com.nowbook.rlt.settle.model.AlipayCash;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-08-04 9:34 AM  <br>
 * Author:cheng
 */
@ToString
public class FatAlipayCashDto extends AlipayCash {

    private static final long serialVersionUID = 2498904359904165277L;

    @Getter
    @Setter
    private Long hasCashed;     // 已提现金额


    public static FatAlipayCashDto transform(AlipayCash alipayCash, Long hasCashed) {
        FatAlipayCashDto dto = new FatAlipayCashDto();
        BeanMapper.copy(alipayCash, dto);
        dto.setHasCashed(hasCashed);
        return dto;
    }
}
