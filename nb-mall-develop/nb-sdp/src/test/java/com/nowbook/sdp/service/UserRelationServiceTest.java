package com.nowbook.sdp.service;

import com.nowbook.common.model.Response;
import com.nowbook.sdp.base.BaseServiceTest;
import com.nowbook.sdp.model.UserRelation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by winter on 16/4/27.
 */
public class UserRelationServiceTest extends BaseServiceTest {
    @Autowired
    private DistributorsRelationService distributorsRelationService;
    private UserRelation userRelation;

    @Before
    public void init() {
//        userRelation = new UserRelation();
//        userRelation.setId((long) 111);
//        userRelation.setDistributorsId((long) 111);
//        userRelation.setDistributionLevel("1");
////        userRelation.setParentIds("1,2");
//        distributorsRelationService.insert(userRelation);
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(userRelation.getId(), notNullValue());
    }
    @Test
    public void testUpdate() throws Exception {
//        userRelation.setDistributionLevel("1");
//        distributorsRelationService.updateByPrimaryKey(userRelation);
//        Response<UserRelation> result= distributorsRelationService.selectByPrimaryKey(userRelation.getId());
//        assertThat(result.getResult().getDistributionLevel(), is("1"));
    }
    @Test
    public void testDelete() throws Exception {
        distributorsRelationService.deleteByPrimaryKey(userRelation.getId());
        assertThat(distributorsRelationService.selectByPrimaryKey(userRelation.getId()).getResult(), nullValue());
    }



}
