package com.gzcz.rtchen.positionmarker.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.gzcz.rtchen.positionmarker.DjiSdkApplication;
import com.gzcz.rtchen.positionmarker.MainActivity;
import com.gzcz.rtchen.positionmarker.PositionPoint;
import com.gzcz.rtchen.positionmarker.R;

import java.text.DecimalFormat;

import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.FlightController.DJIFlightControllerDelegate;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.base.DJIBaseProduct;

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
    View mView = null;
    private OnFragmentInteractionListener mListener;

    /* ZXingQR用 */
//    Button mButtonQRcode = null;
//    String QRcodebuf = "";
//    ImageView qrImageView = null;
//    double mDroneLocationLat;
//    double mDroneLocationLng;

    /* UI控件用 */
    Spinner mSpinner = null;
    ArrayAdapter<String> mSpinnerAdapter = null;
    TextView mDotName = null;
    Button mAddPoint = null;

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
    }

    /* ---- 更新UI 方法 ---- */
    /*
     * UI点击事件响应
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.locate:{
                if (setDJICallback()) {
                    mAddPoint.setEnabled(true);
                } else {
                    mAddPoint.setEnabled(false);
                }
                break;
            }

            case R.id.btn_addPoint:{
                String s = mDotName.getText().toString();

                if (Double.isNaN(MainActivity.getDroneLocationLat()) || Double.isNaN(MainActivity.getDroneLocationLat())) {
                    Toast.makeText(getContext(), "无人机无GPS信号！", Toast.LENGTH_SHORT).show();
                    break;
                }

                if (s.isEmpty()) s = "null";
                MainActivity.dm.addPoint(new PositionPoint(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng(), s));
                break;
            }

            default:
                break;
        }
    }

    private void updateUI() {
        final TextView mTextView = (TextView) getView().findViewById(R.id.ConnectStatusTextView);
        DecimalFormat df = new DecimalFormat("#.0000");

        final StringBuilder sb = new StringBuilder();

        sb.append(df.format(MainActivity.getDroneLocationLat()));
        sb.append(",");
        sb.append(df.format(MainActivity.getDroneLocationLng()));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(sb.toString());
            }
        });

//
//        if(QRcodebuf != "")
//        {
//            QRcodebuf = (QRcodebuf + "\r\n");
//        }

//        String QRcodelatbuf = df.format(mDroneLocationLat);
//        String QRcodelngbuf = df.format(mDroneLocationLng);
//        QRcodebuf = (QRcodebuf + QRcodelatbuf + " " + QRcodelngbuf);
    }

    @Override
    public void onMapClick(LatLng point) {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.map_fragment, container, false);

        mButtonLocate = (Button) mView.findViewById(R.id.locate);
        mButtonLocate.setOnClickListener(this);
        mMapView = (MapView) mView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

//        mButtonQRcode = (Button)mView.findViewById(R.id.btn_add_qrcode);
//        mButtonQRcode.setOnClickListener(this);
//        qrImageView = (ImageView)mView.findViewById(R.id.iv_qr_image);

        mSpinner = (Spinner) mView.findViewById(R.id.sp_projectslist);
        mSpinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, MainActivity.dm.getProjectsList());
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerAdapter);
//        mSpinner.setOnItemSelectedListener();

        mDotName = (TextView) mView.findViewById(R.id.btn_addPoint);
        mAddPoint = (Button) mView.findViewById(R.id.btn_addPoint);
        mAddPoint.setOnClickListener(this);

        initMapView();

        if (MainActivity.dm.getCurrentProjectName() != null) {
            mSpinner.setSelection(MainActivity.dm.getProjectsList().indexOf(MainActivity.dm.getCurrentProjectName()), true);
        }

        return mView;
    }

    public boolean setDJICallback(){
        DJIBaseProduct mProduct = DjiSdkApplication.getProductInstance();
        DJIFlightController mFlightController = null;

        //已连接产品
        if (mProduct != null && mProduct.isConnected()) {
            if (mProduct instanceof DJIAircraft) {
                mFlightController = ((DJIAircraft) mProduct).getFlightController();
            }
        } else {
            return false;
        }
        //当连接的产品为DJIAircraft时执行
        if (mFlightController != null) {
            mFlightController.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {
                @Override
                public void onResult(DJIFlightControllerDataType.DJIFlightControllerCurrentState state) {
                    MainActivity.mDroneLocationLat = state.getAircraftLocation().getLatitude();
                    MainActivity.mDroneLocationLng = state.getAircraftLocation().getLongitude();
                    updateDroneLocation();
                    cameraUpdate();
                    updateUI();
                }
            });
        } else {
            return false;
        }

        return true;
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng()), 18));
            }
        });
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
     * to the activity and potentially other fragments contained
     * in that
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
