package com.nowbook.shop.dao;

import com.nowbook.shop.model.ShopInteriorCategory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-07-11
 */
@Repository
public class ShopInteriorCategoryDao extends SqlSessionDaoSupport {

    public Long create(ShopInteriorCategory shopInteriorCategory) {
        getSqlSession().insert("ShopInteriorCategory.create", shopInteriorCategory);
        return shopInteriorCategory.getId();
    }

    public ShopInteriorCategory findById(Long id) {
        return getSqlSession().selectOne("ShopInteriorCategory.findById", id);
    }

    public ShopInteriorCategory findByShopId(Long shopId) {
        return getSqlSession().selectOne("ShopInteriorCategory.findByShopId", shopId);
    }

    public void update(ShopInteriorCategory shopInteriorCategory) {
        getSqlSession().update("ShopInteriorCategory.update", shopInteriorCategory);
    }

    public void delete(Long id) {
        getSqlSession().delete("ShopInteriorCategory.delete", id);
    }

    public void deleteByShopId(Long shopId) {
        getSqlSession().delete("ShopInteriorCategory.deleteByShopId", shopId);
    }
}
