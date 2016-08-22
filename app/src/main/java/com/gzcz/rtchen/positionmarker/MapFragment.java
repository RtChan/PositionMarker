package com.gzcz.rtchen.positionmarker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;

import java.text.DecimalFormat;

/**
 * Created by RtChen on 2016/7/18.
 */
public class MapFragment extends Fragment implements View.OnClickListener,AMap.OnMapClickListener {
    /* 声明高德SDK控件 */
    MapView mMapView = null;
    AMap mAMap = null;
    Marker mDroneMarker = null;

    Button mButtonLocate = null;

    /* Fragment 用 */
    private OnFragmentInteractionListener mListener;


    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /* 构造函数 */
    public MapFragment() {
        // Required empty public constructor
    }

    //初始化地图控件
    private void initMapView() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();
            mAMap.setOnMapClickListener(this);// add the listener for click for amap object
        }
        LatLng mOriginPos = new LatLng(23.1414, 113.319);
        mAMap.addMarker(new MarkerOptions().position(mOriginPos).title("Marker in Origin"));
        mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mOriginPos, 17));
    }

    /*
     * 在高德地图上更新无人机位置
     */
    public void updateDroneLocation(){
        LatLng pos = new LatLng(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng());
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDroneMarker != null) {
                    mDroneMarker.remove();
                }

                if (MainActivity.checkGpsCoordination(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng())) {
                    mDroneMarker =  mAMap.addMarker(markerOptions);
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("WHERE", "MapFragment onCreate()");
//        mButtonLocate = (Button) getView().findViewById(R.id.locate);
//        mButtonLocate.setOnClickListener(this);

        //获取地图控件引用
//        mMapView = (MapView) getView().findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
//        mMapView.onCreate(savedInstanceState);

        /* 初始化SDK控件 */
//        initMapView();
    }

    /* ---- 更新UI 方法 ---- */
    /*
     * UI点击事件响应
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate:{
                updateDroneLocation();
                cameraUpdate();
                updateUI();
                break;
            }
            default:
                break;
        }
    }

    private void updateUI() {
        TextView mTextView = (TextView) getView().findViewById(R.id.ConnectStatusTextView);
        DecimalFormat df = new DecimalFormat("#.0000");

        mTextView.setText(df.format(MainActivity.getDroneLocationLat()));
        mTextView.append(",");
        mTextView.append(df.format(MainActivity.getDroneLocationLng()));
    }

    @Override
    public void onMapClick(LatLng point) {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.map_fragment, container, false);

        mButtonLocate = (Button) view.findViewById(R.id.locate);
        mButtonLocate.setOnClickListener(this);
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        initMapView();

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /* ---- AMap 方法 ---- */
    /*
     * 更新高德地图显示
     */
    private void cameraUpdate(){
        mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng()), 18));
    }

    /*
     * 高德地图生命周期管理
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
