package com.nowbook.admin.dto;

import com.nowbook.shop.dto.ShopDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by IntelliJ IDEA.
 * WxUser: AnsonChan
 * Date: 14-1-24
 */
@ToString
public class ListShopDto extends ShopDto {
    private static final long serialVersionUID = -8264797353524458255L;
    @Getter
    @Setter
    private String subDomain;
}
