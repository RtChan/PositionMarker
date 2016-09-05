package com.gzcz.rtchen.positionmarker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.gzcz.rtchen.positionmarker.Fragments.MapFragment;
import com.gzcz.rtchen.positionmarker.Fragments.PointListFragment;
import com.gzcz.rtchen.positionmarker.Fragments.ProjectFragment;
import com.gzcz.rtchen.positionmarker.Fragments.ZXingQRFragment;

import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.FlightController.DJIFlightControllerDelegate;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.RemoteController.DJIRemoteController;
import dji.sdk.base.DJIBaseProduct;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MapFragment.OnFragmentInteractionListener,
        ProjectFragment.OnFragmentInteractionListener,
        PointListFragment.OnFragmentInteractionListener,
        ZXingQRFragment.OnFragmentInteractionListener {


    /* 声明大疆SDK控件 */
    static public double mDroneLocationLat = 181, mDroneLocationLng = 181;
    DJIFlightController mFlightController;
    DJIRemoteController mRemoteController;

    static public double getDroneLocationLat() {
        return mDroneLocationLat;
    }

    static public double getDroneLocationLng() {
        return mDroneLocationLng;
    }

    /* 实例化 静态 DataManager */
    public static DataManager dm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* 实例化 DataManager */
        dm = new DataManager(this);

        /* 判断软件是否已激活 */
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        if (!dm.isActivated(telephonyManager.getDeviceId())) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        //当目标编译工具版本高于22时，请申请以下权限以保证SDK可以正常工作
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        //绑定界面文件
        setContentView(R.layout.activity_main);

        /* ---- Navigation Drawer 初始化 ----  */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Fragment fragment = null;
            Class fragmentClass = ProjectFragment.class;  // 设置初始界面
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        }

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

         /* 注册BroadcastReceiver以检测产品连接状态  */
        IntentFilter filter = new IntentFilter();
        filter.addAction(DjiSdkApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    /* ---- DJI SDK 方法 ---- */
    /*
     * 实例化BroadcastReceiver对象以接收产品连接变化广播
     */
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* onReceive方法会在DJI产品连接状态改变时被调用 */
        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    /*
     * 响应USB连接改变
     */
    private void onProductConnectionChange() {
        Toast toast = Toast.makeText(this, "In Connetcion Change [Rt]", Toast.LENGTH_SHORT);
        toast.show();
        initMissionManager();
//        initFlightController();
    }

    private void initMissionManager() {
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
            mFlightController.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {
                @Override
                public void onResult(DJIFlightControllerDataType.DJIFlightControllerCurrentState state) {
                    mDroneLocationLat = state.getAircraftLocation().getLatitude();
                    mDroneLocationLng = state.getAircraftLocation().getLongitude();
                }
            });
        } else {
            mDroneLocationLat = 180.0;
            mDroneLocationLng = 180.0;
        }
        if (mRemoteController != null) {
            mRemoteController.setHardwareStateUpdateCallback(null);
        }
    }

    // 检查获取到的GPS值是否有效
    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    /* ---- Navigation Drawer 方法 ---- */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("关于");
//            builder.setPositiveButton("知道了", null);
//            builder.show();

            Toast.makeText(MainActivity.this, "成至智能科技有限公司", Toast.LENGTH_SHORT).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Log.d("WHERE", "onNavigationItemSelected");
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass = null;
        if (id == R.id.nav_project) {
            fragmentClass = ProjectFragment.class;
        } else if (id == R.id.nav_map) {
            fragmentClass = MapFragment.class;
        } else if (id == R.id.nav_share) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("成至智能科技").setPositiveButton("知道了",null);
            builder.show();
            return false;
        } else {
            fragmentClass = ProjectFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).addToBackStack(null).commit();
//        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销在MainActivity中注册的mReceiver
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
