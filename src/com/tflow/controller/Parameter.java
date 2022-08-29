package com.tflow.controller;

import com.tflow.model.PageParameter;

public class Parameter {

    PageParameter pageParameter;
    String value;

    public Parameter(PageParameter pageParameter, String value) {
        this.pageParameter = pageParameter;
        this.value = value;
    }

    public PageParameter getPageParameter() {
        return pageParameter;
    }

    public String getValue() {
        return value;
    }
}
