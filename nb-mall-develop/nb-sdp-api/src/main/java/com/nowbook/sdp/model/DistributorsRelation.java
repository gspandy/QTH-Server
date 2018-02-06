package com.nowbook.sdp.model;

//分销商父子关系表
public class DistributorsRelation {
    private Long id;

    private Long distributorsId;//分销商id

    private String parentId;//父分销商id

    private String distributionLevel;//分销商id针对父分销商id的级别

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDistributorsId() {
        return distributorsId;
    }

    public void setDistributorsId(Long distributorsId) {
        this.distributorsId = distributorsId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getDistributionLevel() {
        return distributionLevel;
    }

    public void setDistributionLevel(String distributionLevel) {
        this.distributionLevel = distributionLevel;
    }
}