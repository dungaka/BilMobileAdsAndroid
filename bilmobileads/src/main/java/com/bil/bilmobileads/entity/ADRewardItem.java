package com.bil.bilmobileads.entity;

public class ADRewardItem {

    String type;
    double amount;

    public ADRewardItem(String type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public String getType() {
        return this.type;
    }

    public double getAmount() {
        return this.amount;
    }
}
