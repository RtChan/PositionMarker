package com.gzcz.rtchen.positionmarker;

import com.amap.api.maps2d.CoordinateConverter;
import com.amap.api.maps2d.model.LatLng;

/**
 * Created by Rt Chen on 2016/8/31.
 */
public class Utils {
    public static LatLng GPStoAMAP(LatLng gps){
        LatLng amap = null;

        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);// CoordType.GPS 待转换坐标类型
        converter.coord(gps);// gps待转换坐标点

        amap = converter.convert();// 执行转换操作
        return amap;
    }

    public static LatLng GPStoAMAP(PositionPoint p) {
        LatLng amap = null;

        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(new LatLng(p.getLatitude(), p.getLongitude()));

        amap = converter.convert();
        return amap;
    }

    public static LatLng GPStoAMAP(double lat, double lng) {
        LatLng amap = null;

        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(new LatLng(lat, lng));

        amap = converter.convert();
        return amap;
    }
}
