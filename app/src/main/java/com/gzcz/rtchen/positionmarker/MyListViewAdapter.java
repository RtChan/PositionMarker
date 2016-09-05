package com.gzcz.rtchen.positionmarker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

/*
 * CheckBox处理参考：http://blog.csdn.net/qq435757399/article/details/8256453/
 */

/**
 * Created by Rt Chen on 2016/8/25.
 */
public class MyListViewAdapter extends BaseAdapter {

    private ArrayList<PositionPointView> mData = null;
    private LayoutInflater layoutInflater;
    private Context context;

    /* 构造函数 */
    public MyListViewAdapter(Context context,ArrayList<PositionPointView> data){
        this.context=context;
        mData = data;

        this.layoutInflater=LayoutInflater.from(context);
    }

    /* 组件集合，对应 my_listview.xml 中的控件 */
    public final class ViewHolder{
        public TextView num;
        public TextView dotname;
        public TextView dotnum;
        public TextView latitude;
        public TextView longitude;
        public android.widget.CheckBox cb;
     }

    private class CheckBoxOnClickListener implements View.OnClickListener{
        int pos;
        public CheckBoxOnClickListener(int inPosition) {
            pos = inPosition;
        }
        @Override
        public void onClick(View v) {
            mData.get(pos).setChecked(!mData.get(pos).isChecked());
            Log.d("Adapter", "onClick: " + String.valueOf(pos));
        }
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder = null;
        CheckBoxOnClickListener myListener = null;

        /*
         * 此处不能按照常规getTag，否则列表滚动或者变动后，无法接收新的响应
         */
//        if (convertView == null) {
            mHolder = new ViewHolder();
            myListener = new CheckBoxOnClickListener(position);
            convertView=layoutInflater.inflate(R.layout.my_listview, null);
            // 获得组件，实例化组件
            mHolder.num = (TextView) convertView.findViewById(R.id.mtv_num);
            mHolder.dotname = (TextView) convertView.findViewById(R.id.mtv_dotname);
            mHolder.dotnum = (TextView) convertView.findViewById(R.id.mtv_dotnum);
            mHolder.latitude = (TextView) convertView.findViewById(R.id.mtv_latitude);
            mHolder.longitude = (TextView) convertView.findViewById(R.id.mtv_longitude);
            mHolder.cb = (CheckBox) convertView.findViewById(R.id.mcb);
            convertView.setTag(mHolder);
//        } else {
//            mHolder = (ViewHolder) convertView.getTag();
//        }

        DecimalFormat df = new DecimalFormat("#.0000");


        //绑定数据
        final PositionPointView lvpp = mData.get(position); //为了给CheckBox用
        mHolder.num.setText(String.valueOf(mData.get(position).num));
        mHolder.dotname.setText(mData.get(position).dotname);
        mHolder.dotnum.setText(String.valueOf(mData.get(position).dotnum));
        mHolder.latitude.setText(df.format(mData.get(position).latitude));
        mHolder.longitude.setText(df.format(mData.get(position).longitude));
        mHolder.cb.setChecked(mData.get(position).checked);

        mHolder.cb.setOnClickListener(myListener);

        return convertView;
    }

    public void refresh(ArrayList<PositionPointView> data) {
        mData = data;
        notifyDataSetChanged();
    }
}
