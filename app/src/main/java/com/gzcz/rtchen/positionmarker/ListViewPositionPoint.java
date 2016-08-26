package com.gzcz.rtchen.positionmarker;

/**
 * Created by Rt Chen on 2016/8/26.
 */
public class ListViewPositionPoint {
    int num;
    String dotname;
    int dotnum;
    double latitude;
    double longitude;
    boolean checked;

    public ListViewPositionPoint(int n, int dn, boolean c, PositionPoint p) {
        this.num = n;
        this.dotname = p.getDotName();
        this.dotnum = dn;
        this.latitude = p.getLatitude();
        this.longitude = p.getLongitude();
        this.checked = c;
    }
}