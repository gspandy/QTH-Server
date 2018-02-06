package com.nowbook.user.mysql;

import com.nowbook.user.model.Address;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-14
 */
@Repository
public class AddressDao extends SqlSessionDaoSupport {

    public Address findById(Integer id) {
        return getSqlSession().selectOne("Address.findById", id);
    }

    public List<Address> findByParentId(Integer parentId) {
        return getSqlSession().selectList("Address.findByParentId", parentId);
    }

    public List<Address> findByLevel(Integer level) {
        return getSqlSession().selectList("Address.findByLevel", level);
    }

    public Integer create(Address address) {
        getSqlSession().insert("Address.create", address);
        return address.getId();
    }
}
