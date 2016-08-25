package com.gzcz.rtchen.positionmarker;

/**
 * 定义 PositionPoint 为坐标点的数据结构
 * Created by Rt Chen on 2016/8/23.
 */
public class PositionPoint {
    protected double Latitude;
    protected double Longitude;
    protected String DotName;

    public PositionPoint() {
        Latitude = 180.0;
        Longitude = 180.0;
        DotName = "";
    }

    public PositionPoint(double lat, double lng, String s) {
        Latitude = lat;
        Longitude = lng;
        DotName = s;
    }
}
