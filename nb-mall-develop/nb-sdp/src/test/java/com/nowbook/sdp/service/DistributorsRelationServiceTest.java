package com.nowbook.sdp.service;

import com.nowbook.common.model.Response;
import com.nowbook.sdp.base.BaseServiceTest;
import com.nowbook.sdp.model.DistributorsRelation;
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
public class DistributorsRelationServiceTest extends BaseServiceTest {
    @Autowired
    private DistributorsRelationService distributorsRelationService;
    private DistributorsRelation distributorsRelation;

    @Before
    public void init() {
        distributorsRelation = new DistributorsRelation();
        distributorsRelation.setId((long) 111);
        distributorsRelation.setDistributorsId((long) 111);
        distributorsRelation.setDistributionLevel("1");
//        distributorsRelation.setParentIds("1,2");
        distributorsRelationService.insert(distributorsRelation);
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(distributorsRelation.getId(), notNullValue());
    }
    @Test
    public void testUpdate() throws Exception {
        distributorsRelation.setDistributionLevel("1");
        distributorsRelationService.updateByPrimaryKey(distributorsRelation);
        Response<DistributorsRelation> result= distributorsRelationService.selectByPrimaryKey(distributorsRelation.getId());
        assertThat(result.getResult().getDistributionLevel(), is("1"));
    }
    @Test
    public void testDelete() throws Exception {
        distributorsRelationService.deleteByPrimaryKey(distributorsRelation.getId());
        assertThat(distributorsRelationService.selectByPrimaryKey(distributorsRelation.getId()).getResult(), nullValue());
    }



}
