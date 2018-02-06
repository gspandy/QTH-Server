package com.nowbook.rlt.settle.dto;

import com.nowbook.common.utils.BeanMapper;
import com.nowbook.rlt.settle.model.DepositAccount;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-06-10 10:01 AM  <br>
 * Author:cheng
 */
@ToString
public class DepositAccountDto extends DepositAccount {
    private static final long serialVersionUID = -9045251473280594500L;

    @Getter
    @Setter
    private Boolean locked;             // 是否保证金余额过低被锁定

    @Getter
    @Setter
    private Long depositNeed;        // 应缴纳金额



    public static DepositAccountDto transform(DepositAccount account, Boolean locked) {
        DepositAccountDto dto = new DepositAccountDto();
        BeanMapper.copy(account, dto);
        dto.setLocked(locked);
        return dto;
    }
}
