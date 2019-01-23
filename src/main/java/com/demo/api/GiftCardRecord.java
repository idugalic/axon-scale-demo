package com.demo.api;

public class GiftCardRecord {

    private String id;
    private Integer initialValue;
    private Integer remainingValue;

    public GiftCardRecord(String id, Integer initialValue, Integer remainingValue) {
        this.id = id;
        this.initialValue = initialValue;
        this.remainingValue = remainingValue;
    }

    public GiftCardRecord() {
    }

    public String getId() {
        return id;
    }

    public Integer getInitialValue() {
        return initialValue;
    }

    public Integer getRemainingValue() {
        return remainingValue;
    }
}
