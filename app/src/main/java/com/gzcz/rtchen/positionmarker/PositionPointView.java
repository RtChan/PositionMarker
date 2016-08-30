package com.gzcz.rtchen.positionmarker;

import com.amap.api.maps2d.CoordinateConverter;
import com.amap.api.maps2d.model.LatLng;

/**
 * Created by Rt Chen on 2016/8/26.
 */
public class PositionPointView {
    int num;
    String dotname;
    int dotnum;
    double latitude;
    double longitude;
    boolean checked;
    LatLng amaplatlng;

    public PositionPointView(int n, int dn, boolean c, PositionPoint p) {
        this.num = n;
        this.dotname = p.getDotName();
        this.dotnum = dn;
        this.latitude = p.getLatitude();
        this.longitude = p.getLongitude();
        this.checked = c;
        this.amaplatlng = convertLatLng(p.getLatitude(), p.getLongitude());
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getDotNameAndNum() {
        StringBuilder sb = new StringBuilder();
        sb.append(dotname);
        sb.append(dotnum);
        return sb.toString();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    private LatLng convertLatLng(double lat, double lng) {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS); // CoordType.GPS 待转换坐标类型
        converter.coord(new LatLng(lat, lng));
        LatLng desLatLng = converter.convert();// 执行转换操作
        return desLatLng;
    }
}