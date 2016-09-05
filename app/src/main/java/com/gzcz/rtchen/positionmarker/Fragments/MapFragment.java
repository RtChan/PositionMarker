package com.gzcz.rtchen.positionmarker.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.maps2d.model.TextOptions;
import com.gzcz.rtchen.positionmarker.BaseFpvView;
import com.gzcz.rtchen.positionmarker.DjiSdkApplication;
import com.gzcz.rtchen.positionmarker.MainActivity;
import com.gzcz.rtchen.positionmarker.PositionPoint;
import com.gzcz.rtchen.positionmarker.PositionPointView;
import com.gzcz.rtchen.positionmarker.R;
import com.gzcz.rtchen.positionmarker.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.FlightController.DJIFlightControllerDelegate;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.RemoteController.DJIRemoteController;
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
    Boolean mFpvSize = false;

    DJIFlightController mFlightController = null;
    DJIRemoteController mRemoteController = null;

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

        mLatLngs.clear();
        mAMap.clear();

        if (null == MainActivity.dm.getPointViewsList() || MainActivity.dm.getPointViewsList().isEmpty()) {
            Log.d("TAG", "initMapView: " + "default camera");
            mPos = new LatLng(23.1414, 113.319);
            mPos = Utils.GPStoAMAP(mPos);
            mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPos, 17));
            return;
        }

        Log.d("TAG", "initMapView: " + MainActivity.dm.getCurrentProjectName());
        for (PositionPointView p : MainActivity.dm.getPointViewsList()) {
            mPos = new LatLng(p.getLatitude(), p.getLongitude());
            LatLng mAMapPos = Utils.GPStoAMAP(mPos);
            DecimalFormat df = new DecimalFormat("#.0000");
            mAMap.addMarker(new MarkerOptions().position(mAMapPos).title(p.getDotNameAndNum()).snippet(df.format(p.getLatitude()) + "," + df.format(p.getLongitude())));
            mAMap.addText(new TextOptions().position(mAMapPos).text(p.getDotNameAndNum()).align(1, 0).fontSize(40).fontColor(Color.BLACK).visible(true));
            mLatLngs.add(mAMapPos);
        }
        mAMap.addPolyline(new PolylineOptions().addAll(mLatLngs).width(5).color(Color.argb(255, 1, 1, 1)).visible(true));
        mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLngs.get(mLatLngs.size() - 1), 17));
    }

    /*
     * 在高德地图上更新无人机位置
     */
    public void updateDroneLocation() {
//        CoordinateConverter converter = new CoordinateConverter();
//        converter.from(CoordinateConverter.CoordType.GPS);
//        converter.coord(new LatLng(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng()));
//        final LatLng desLatLng = converter.convert();
        LatLng desLatLng = Utils.GPStoAMAP(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng());

//        飞机图标旋转
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aircraft);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
//        飞机图标旋转

        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(desLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
//        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

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
//        CoordinateConverter converter = new CoordinateConverter();
//        converter.from(CoordinateConverter.CoordType.GPS);
//        converter.coord(l);
//        final LatLng desLatLng = converter.convert();

        final LatLng desLatLng = Utils.GPStoAMAP(l);

        DecimalFormat df = new DecimalFormat("#.0000");

        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(desLatLng);
        markerOptions.title(MainActivity.dm.getLastDotNameAndNum());
        markerOptions.snippet(df.format(desLatLng.latitude) + "," + df.format(desLatLng.longitude));

        final TextOptions textOptions = new TextOptions().position(desLatLng).text(MainActivity.dm.getLastDotNameAndNum()).align(1, 0).fontSize(40).fontColor(Color.BLACK).visible(true);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.checkGpsCoordination(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng())) {
                    markers.add(mAMap.addMarker(markerOptions));
                    mAMap.addText(textOptions);
                }
            }
        });
    }

    public void polyNewLine(LatLng l) {
//        CoordinateConverter converter = new CoordinateConverter();
//        converter.from(CoordinateConverter.CoordType.GPS);
//        converter.coord(l);
//        final LatLng desLatLng = converter.convert();

        LatLng desLatLng = Utils.GPStoAMAP(l);

        mLatLngs.add(desLatLng);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAMap.addPolyline(new PolylineOptions().addAll(mLatLngs).width(5).color(Color.BLACK).visible(true));
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("WHEN", "onCreate: MapF");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("WHEN", "onDestroyView: MapF");
    }

    /* ---- 更新UI 方法 ---- */
    /*
     * UI点击事件响应
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate: {
                if (setDJICallback(true)) {
                    mAddPoint.setEnabled(true);
                } else {
                    mAddPoint.setEnabled(false);
                }
                break;
            }
            case R.id.btn_addPoint: {
                addThePoint();
                break;
            }
            default:
                break;
        }
    }

    private void addThePoint() {
        String s = mDotName.getText().toString();

        if (Double.isNaN(MainActivity.getDroneLocationLat()) || Double.isNaN(MainActivity.getDroneLocationLat())) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "无人机无GPS信号！", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        if (s.isEmpty()) s = "null";

        LatLng l = new LatLng(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng());
        MainActivity.dm.addPoint(new PositionPoint(l.latitude, l.longitude, s));
        markNewLocation(l);
        polyNewLine(l);
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
    }

    @Override
    public void onMapClick(LatLng point) {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("WHEN", "onCreateView: MapF");
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.map_fragment, container, false);

        mButtonLocate = (Button) mView.findViewById(R.id.locate);
        if (null == MainActivity.dm.getCurrentProjectName() || MainActivity.dm.getCurrentProjectName().isEmpty())
            mButtonLocate.setEnabled(false);
        else
            mButtonLocate.setEnabled(true);
        mButtonLocate.setOnClickListener(this);
        mMapView = (MapView) mView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        BaseFpvView bfv = (BaseFpvView) mView.findViewById(R.id.view);
        bfv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustFpvSize();
            }
        });
//        mButtonQRcode = (Button)mView.findViewById(R.id.btn_add_qrcode);
//        mButtonQRcode.setOnClickListener(this);
//        qrImageView = (ImageView)mView.findViewById(R.id.iv_qr_image);

        mSpinner = (Spinner) mView.findViewById(R.id.sp_projectslist);
        mSpinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, MainActivity.dm.getProjectsList());
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("TAG", "onItemSelected: " + position);
                MainActivity.dm.setCurrentProject(position);
                MainActivity.dm.getPointViewsList();
                initMapView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ;
            }
        });

        mDotName = (EditText) mView.findViewById(R.id.et_dotname);
        mAddPoint = (Button) mView.findViewById(R.id.btn_addPoint);
        mAddPoint.setOnClickListener(this);

        if (MainActivity.dm.getCurrentProjectName() != null) {
            mSpinner.setSelection(MainActivity.dm.getProjectsList().indexOf(MainActivity.dm.getCurrentProjectName()), true);
        }

        MainActivity.dm.getPointViewsList();
        initMapView();

        return mView;
    }

    public void adjustFpvSize() {
        DisplayMetrics metric = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     // 屏幕宽度（像素）
        int height = metric.heightPixels;     // 屏幕高度（像素）

        final BaseFpvView bfv = (BaseFpvView) mView.findViewById(R.id.view);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bfv.getLayoutParams();
        if (false == mFpvSize) { // 从小变大
            if (height > width) {   // 竖屏
                layoutParams.width = width;
                layoutParams.height = width / Utils.dip2px(getContext(), 160) * Utils.dip2px(getContext(), 120);
            } else { // 横屏
                mFpvSize = !mFpvSize;
            }
        } else {    // 从大变小
            layoutParams.width = Utils.dip2px(getContext(), 160);
            layoutParams.height = Utils.dip2px(getContext(), 120);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bfv.setLayoutParams(layoutParams);
        }
        });

        mFpvSize = !mFpvSize;
    }

    public boolean setDJICallback(boolean b) {
        DJIBaseProduct mProduct = DjiSdkApplication.getProductInstance();

        //已连接产品
        if (mProduct != null && mProduct.isConnected()) {
            if (mProduct instanceof DJIAircraft) {
                mFlightController = ((DJIAircraft) mProduct).getFlightController();
                mRemoteController = ((DJIAircraft) mProduct).getRemoteController();
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
        } else {
            ;
        }
        //当连接的产品有遥控器时
        if (mRemoteController != null) {
            if (b) {
                mRemoteController.setHardwareStateUpdateCallback(new DJIRemoteController.RCHardwareStateUpdateCallback() {
                    @Override
                    public void onHardwareStateUpdate(DJIRemoteController djiRemoteController, DJIRemoteController.DJIRCHardwareState djircHardwareState) {
                        if (djircHardwareState.customButton1.buttonDown) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "左键按下：切换图传", Toast.LENGTH_SHORT).show();
                                }
                            });
                            adjustFpvSize();
                        }
                        if (djircHardwareState.customButton2.buttonDown) {
                            if (mAddPoint.isEnabled()) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "右键按下：添加点", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                addThePoint();
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "GPS未准备好！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                });
            } else {
                mRemoteController.setHardwareStateUpdateCallback(null);
            }
            return true;
        } else {
            ;
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
        Log.d("WHEN", "onDetach: MapF");
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        setDJICallback(false);
    }

    /* ---- AMap 方法 ---- */
    /*
     * 更新高德地图显示
     */
    private void cameraUpdate() {
//        CoordinateConverter converter = new CoordinateConverter();
//        // CoordType.GPS 待转换坐标类型
//        converter.from(CoordinateConverter.CoordType.GPS);
//        // sourceLatLng待转换坐标点 DPoint类型
//        converter.coord(new LatLng(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng()));
//        // 执行转换操作
//        final LatLng desLatLng = converter.convert();

        final LatLng desLatLng = Utils.GPStoAMAP(MainActivity.getDroneLocationLat(), MainActivity.getDroneLocationLng());

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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
