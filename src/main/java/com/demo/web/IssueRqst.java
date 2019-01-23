package com.demo.web;

import java.io.Serializable;

public class IssueRqst implements Serializable {

    private Integer value;

    public IssueRqst(Integer value) {
        this.value = value;
    }

    public IssueRqst() {
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}

