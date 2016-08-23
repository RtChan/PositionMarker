package com.gzcz.rtchen.positionmarker;

/**
 * 定义 Point 为坐标点的数据结构
 * Created by Rt Chen on 2016/8/23.
 */
public class Point {
    double Latitude;
    double Longitude;
    String DotName;

    public Point() {
        Latitude = 180.0;
        Longitude = 180.0;
        DotName = "";
    }

    public Point(double lat, double lng, String s) {
        Latitude = lat;
        Longitude = lng;
        DotName = s;
    }
}
