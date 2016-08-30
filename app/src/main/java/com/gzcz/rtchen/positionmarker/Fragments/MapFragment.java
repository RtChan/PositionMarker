package com.gzcz.rtchen.positionmarker.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.CoordinateConverter;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.maps2d.model.TextOptions;
import com.gzcz.rtchen.positionmarker.DjiSdkApplication;
import com.gzcz.rtchen.positionmarker.MainActivity;
import com.gzcz.rtchen.positionmarker.PositionPoint;
import com.gzcz.rtchen.positionmarker.PositionPointView;
import com.gzcz.rtchen.positionmarker.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.FlightController.DJIFlightControllerDelegate;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.base.DJIBaseProduct;

/**
 * Created by RtChen on 2016/7/18.
 */
public class MapFragment extends Fragment implements View.OnClickListener, AMap.OnMapClickListener {
    /* 声明高德SDK控件 */
    MapView mMapView = null;
    AMap mAMap = null;
    Marker mDroneMarker = null;
    Marker marker[] = new Marker[1024];
    int markerNumber = 1;
    List<LatLng> mLatLngs = new ArrayList<LatLng>();
    List<Marker> markers = new ArrayList<Marker>();
    Button mButtonLocate = null;

    // TODO:删除此测试代码
    static double testnum = 0;

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
    EditText mDotName = null;
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
        LatLng mPos = null;

        if (mAMap == null) {
            mAMap = mMapView.getMap();
            mAMap.setOnMapClickListener(this);// add the listener for click for amap object
        }

        if (null == MainActivity.dm.getPointViewsList() || MainActivity.dm.getPointViewsList().isEmpty()) {
            mPos = new LatLng(23.1414, 113.319);
            mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPos, 17));
        }

        for (PositionPointView p : MainActivity.dm.getPointViewsList()) {
            mPos = new LatLng(p.getLatitude(), p.getLongitude());
            //TODO:使用工具类转换坐标系
            DecimalFormat df = new DecimalFormat("#.0000");
            mAMap.addMarker(new MarkerOptions().position(mPos).title(p.getDotNameAndNum()).snippet(df.format(p.getLatitude()) + "," + df.format(p.getLongitude())));
            mAMap.addText(new TextOptions().position(mPos).text(p.getDotNameAndNum()).fontColor(Color.BLACK).visible(true));
            mLatLngs.add(new LatLng(p.getLatitude(),p.getLongitude()));
        }
        mAMap.addPolyline(new PolylineOptions().addAll(mLatLngs).width(5).color(Color.argb(255, 1, 1, 1)).visible(true));
        mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPos, 17));
    }

    /*
     * 在高德地图上更新无人机位置
     */
    public void updateDroneLocation() {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(new LatLng(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng()));
        final LatLng desLatLng = converter.convert();

        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(desLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDroneMarker != null) {
                    mDroneMarker.remove();
                }

                if (MainActivity.checkGpsCoordination(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng())) {
                    mDroneMarker = mAMap.addMarker(markerOptions);
                }
            }
        });
    }

    public void markNewLocation(LatLng l) {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(l);
        final LatLng desLatLng = converter.convert();

        DecimalFormat df = new DecimalFormat("#.0000");

        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(desLatLng);
        markerOptions.title(MainActivity.dm.getLastDotNameAndNum());
        markerOptions.snippet(df.format(desLatLng.latitude) + "," + df.format(desLatLng.longitude));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.checkGpsCoordination(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng())) {
                    markers.add(mAMap.addMarker(markerOptions));
                }
            }
        });
    }

    public void polyNewLine(LatLng l) {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(l);
        final LatLng desLatLng = converter.convert();

        /*
         * 注意：使用PolylineOptions().add()方法添加连线点无效！
         */
        mLatLngs.add(desLatLng);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAMap.addPolyline(new PolylineOptions().addAll(mLatLngs).width(10).color(Color.BLACK).visible(true));
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
            case R.id.locate: {
                if (setDJIUpdateStateCallback(true)) {
                    mAddPoint.setEnabled(true);
                } else {
                    mAddPoint.setEnabled(false);
                }
                break;
            }
            case R.id.btn_addPoint: {
                String s = mDotName.getText().toString();

                if (Double.isNaN(MainActivity.getDroneLocationLat()) || Double.isNaN(MainActivity.getDroneLocationLat())) {
                    Toast.makeText(getContext(), "无人机无GPS信号！", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (s.isEmpty()) s = "null";

                LatLng l = new LatLng(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng());
                MainActivity.dm.addPoint(new PositionPoint(l.latitude, l.longitude, s));
                markNewLocation(l);
                polyNewLine(l);
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

        mDotName = (EditText) mView.findViewById(R.id.et_dotname);
        mAddPoint = (Button) mView.findViewById(R.id.btn_addPoint);
        mAddPoint.setOnClickListener(this);

        if (MainActivity.dm.getCurrentProjectName() != null) {
            mSpinner.setSelection(MainActivity.dm.getProjectsList().indexOf(MainActivity.dm.getCurrentProjectName()), true);
        }

        initMapView();

        return mView;
    }

    public boolean setDJIUpdateStateCallback(boolean b) {
        DJIBaseProduct mProduct = DjiSdkApplication.getProductInstance();
        DJIFlightController mFlightController = null;

        //已连接产品
        if (mProduct != null && mProduct.isConnected()) {
            if (mProduct instanceof DJIAircraft) {
                mFlightController = ((DJIAircraft) mProduct).getFlightController();
            }
        }
        //当连接的产品为DJIAircraft时执行
        if (mFlightController != null) {
            if (b) {
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
                mFlightController.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {
                    @Override
                    public void onResult(DJIFlightControllerDataType.DJIFlightControllerCurrentState state) {
                        MainActivity.mDroneLocationLat = state.getAircraftLocation().getLatitude();
                        MainActivity.mDroneLocationLng = state.getAircraftLocation().getLongitude();
                    }
                });
            }
            return true;
        }
        return false;
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

        setDJIUpdateStateCallback(false);
    }

    /* ---- AMap 方法 ---- */
    /*
     * 更新高德地图显示
     */
    private void cameraUpdate() {
        CoordinateConverter converter = new CoordinateConverter();
        // CoordType.GPS 待转换坐标类型
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标点 DPoint类型
        converter.coord(new LatLng(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng()));
        // 执行转换操作
        final LatLng desLatLng = converter.convert();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(desLatLng, 18));
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
