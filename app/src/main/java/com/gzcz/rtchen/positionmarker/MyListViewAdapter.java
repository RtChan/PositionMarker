package com.gzcz.rtchen.positionmarker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Rt Chen on 2016/8/25.
 */
public class MyListViewAdapter extends BaseAdapter {

    private class ListViewPositionPoint extends PositionPoint{
        int num;
        int dotnum;
        boolean checked;

        public ListViewPositionPoint(int n, int dn, boolean c, PositionPoint p) {
            super(p.Latitude, p.Longitude, p.DotName);
            this.num = n;
            this.dotnum = dn;
            this.checked = c;
        }
    }

    private int mCurrentNum = 0;
    private int mCurrentDotnum = 0;
    private String mCurrentDotName = "";

    private ArrayList<ListViewPositionPoint> mData = null;
    private LayoutInflater layoutInflater;
    private Context context;

    /* 构造函数 */
    public MyListViewAdapter(Context context,ArrayList<PositionPoint> data){
        this.context=context;
        mData = convertList(data);
        // TODO：转换失败
        if (mData == null) ;

        this.layoutInflater=LayoutInflater.from(context);
    }
    public ArrayList<ListViewPositionPoint> convertList(ArrayList<PositionPoint> data){
        mData = new ArrayList<ListViewPositionPoint>();
        mCurrentDotName = data.get(0).DotName;
        for (PositionPoint p : data) {
            if (p.DotName.equals(mCurrentDotName) == false) {
                mCurrentDotnum = 0;
                mCurrentDotName = p.DotName;
            }
            mData.add(new ListViewPositionPoint(++mCurrentNum, ++mCurrentDotnum, false, p));
        }

        // TODO：转换失败
        //return null;
        return mData;
    }

    /* 组件集合，对应 my_listview.xml 中的控件 */
    public final class ListElem{
        public TextView num;
        public TextView dotname;
        public TextView dotnum;
        public TextView latitude;
        public TextView longitude;
        public CheckBox cb;
     }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListElem elem = null;
        if (convertView == null) {
            elem = new ListElem();
            convertView=layoutInflater.inflate(R.layout.my_listview, null);
            // 获得组件，实例化组件
            elem.num = (TextView) convertView.findViewById(R.id.mtv_num);
            elem.dotname = (TextView) convertView.findViewById(R.id.mtv_dotname);
            elem.dotnum = (TextView) convertView.findViewById(R.id.mtv_dotnum);
            elem.latitude = (TextView) convertView.findViewById(R.id.mtv_latitude);
            elem.longitude = (TextView) convertView.findViewById(R.id.mtv_longitude);
            elem.cb = (CheckBox) convertView.findViewById(R.id.mcb);
            convertView.setTag(elem);
        } else {
            elem = (ListElem) convertView.getTag();
        }

        //绑定数据
        // TODO:修复命名
        elem.num.setText(String.valueOf(mData.get(position).num));
        elem.dotname.setText(mData.get(position).DotName);
        elem.dotnum.setText(String.valueOf(mData.get(position).dotnum));
        elem.latitude.setText(String.valueOf(mData.get(position).Latitude));
        elem.longitude.setText(String.valueOf(mData.get(position).Longitude));
        elem.cb.setChecked(mData.get(position).checked);

        return convertView;
    }
}
