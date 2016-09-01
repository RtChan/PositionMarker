package com.gzcz.rtchen.positionmarker;

import android.content.Context;

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

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
