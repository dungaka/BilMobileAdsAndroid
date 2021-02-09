package com.bil.bilmobileads.entity;

import java.util.ArrayList;

public class AdUnitObj {
    public String placement;
    public ADType type;
    public ADFormat defaultFormat; // Kieu hien thi mac dinh
    public boolean isActive; // -> cho hien thi
    public ArrayList<AdInfor> adInfor;
    public BannerSize bannerSize;
    public int refreshTime;

    public AdUnitObj(String placement, ADType type, ADFormat defaultFormat, boolean isActive, ArrayList<AdInfor> adInfor, BannerSize bannerSize, int refreshTime) {
        this.placement = placement;
        this.type = type;
        this.defaultFormat = defaultFormat;
        this.isActive = isActive;
        this.adInfor = adInfor;
        this.bannerSize = bannerSize;
        this.refreshTime = refreshTime;
    }

    public AdUnitObj(String placement, ADType type, ADFormat defaultFormat, boolean isActive, ArrayList<AdInfor> adInfor, int refreshTime) {
        this.placement = placement;
        this.type = type;
        this.defaultFormat = defaultFormat;
        this.isActive = isActive;
        this.adInfor = adInfor;
        this.refreshTime = refreshTime;
    }
}
