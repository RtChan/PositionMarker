package com.gzcz.rtchen.positionmarker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rt Chen on 2016/8/23.
 */
public class DataManager {
    private static final String PROJECTLISTFILENAME = "_ProjectList";
    private static final String ACTIVATIONFILENAME = "_Activation";
    private static final String PACKAGENAME = "com.gzcz.rtchen.positionmarker";
    private Context mContext = null;

    private ArrayList<String> mProjectsList = null;
    private ArrayList<PositionPoint> mPointsList = null;
    private ArrayList<PositionPointView> mPointViewsList = null;
    private HashMap<String ,Integer> mDotNameNumberList = null;
    private String mCurrentProject = new String();

    private int mLastNum;
    private int mLastDotNum;
    private String mLastDotName;

    public DataManager(Context c) {
        mContext = c;
        initActivation();
        initProjectsList();
        initLastStatement();
        readProjectsListFromFile();
    }

    private void initLastStatement(){
        mLastNum = 0;
        mLastDotNum = 0;
        mLastDotName = "";
    }

    private void initProjectsList() {
        SharedPreferences sp = mContext.getSharedPreferences(PROJECTLISTFILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        int total = sp.getInt("Total", -1);
        if (-1 == total) {
            editor.putInt("Total", 0);
            editor.apply();
        }
    }

    private void initActivation(){
        SharedPreferences sp = mContext.getSharedPreferences(PROJECTLISTFILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        String key = sp.getString("Key", null);
        if (null == key) {
            editor.putString("Key","null");
            editor.apply();
        }
    }

    public void setActivationKey(String newKey) {
        SharedPreferences sp = mContext.getSharedPreferences(PROJECTLISTFILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        String key = sp.getString("Key", null);
        if (null == key) {
            return;
        } else {
            editor.putString("Key", newKey);
            editor.apply();
        }
    }

    private void setOemActivationKey(String imei) {
        HashMap<String, String> oem = new HashMap<String, String>();
        oem.put("868180022671821","854ae07ec3b521a8c5fffcc437b14cbf");  // 双卡手机1：IMEI1
        oem.put("868180023292080","d4f17cfbdec0fd389ba93574c20c78aa");  // 双卡手机1：IMEI2
        oem.put("359700200053276","610199b92f4b6d500c7d3458bdd99a84");  // 设备1

        oem.put("357747051493810","06bf517bdac9e06022ccb06bbf93a2fa");

        if (oem.containsKey(imei)) {
            setActivationKey(oem.get(imei));
        }
    }

    public boolean isActivated(String imei) {
        setOemActivationKey(imei);

        String StoredKey = getActivationKey();
        if (null == StoredKey) {
            return false;
        }

        long Imei = Long.valueOf(imei);

        // TEL + 2016 + 433
        Imei |= 386083382016433L;
        StringBuilder src = new StringBuilder();
        src.append(String.valueOf(Imei));

        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(src.toString().getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
               if ((b & 0xFF) < 0x10)
                      hex.append("0");
               hex.append(Integer.toHexString(b & 0xFF));
        }

//        Log.d("LOGIN", "357747051493810 " + "isActivated: " + hex.toString());

        if (StoredKey.equalsIgnoreCase(hex.toString())) {
            return true;
        }

        return false;
    }

    private String getActivationKey() {
        SharedPreferences sp = mContext.getSharedPreferences(PROJECTLISTFILENAME, Context.MODE_PRIVATE);

        String key = sp.getString("Key", null);
        if (null == key) {
            return null;
        } else {
            return key;
        }
    }

    public ArrayList<String> getProjectsList() {
        readProjectsListFromFile();
        return mProjectsList;
    }

    public ArrayList<PositionPoint> getPointsList() {
        readPointsListFromeFile();
        return mPointsList;
    }

    public ArrayList<PositionPointView> getPointViewsList() {
        getPointsList();
        convertPointsListToPointViewsList();
        return mPointViewsList;
    }

    public String getLastDotNameAndNum() {
        StringBuilder sb = new StringBuilder();

        getPointViewsList();

        sb.append(mLastDotName);
        sb.append(mLastDotNum);
        return sb.toString();
    }

    public void setCurrentProject(String name) {
        this.mCurrentProject = name;
        readPointsListFromeFile();
    }

    public int setCurrentProject(int index) {
        // 工程名列表未被初始化
        if (null == mProjectsList) return -1;
        // 输入错误
        if (index < 0) {
            mCurrentProject = null;
            return -1;
        }
        // 索引越界
        if (index > mProjectsList.size()-1) {
            mCurrentProject = null;
            return -1;
        }
        // 正常
        this.mCurrentProject = mProjectsList.get(index);
        readPointsListFromeFile();
        return index;
    }

    public String getCurrentProjectName() {
        return mCurrentProject;
    }

    public int addProject(String name) {
        // 工程名列表未被初始化
        if (null == mProjectsList) return -1;
        // 判断工程是否已存在列表当中
        if (!mProjectsList.contains(name)) {
            mProjectsList.add(name);
            saveProjectsListToFile();
            // 初始化新工程的数据文件
            SharedPreferences sp = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("Total", 0);
            editor.apply();
        }
        // 返回工程名在列表中的索引
        return mProjectsList.indexOf(name);
    }

    public int removeProject(String name) {
        // 工程名列表未被初始化
        if (null == mProjectsList) return -1;
        // 判断工程是否已存在列表当中
        if (mProjectsList.contains(name)) {
            if (mCurrentProject.equals(name)) mCurrentProject = null;
            int index = mProjectsList.indexOf(name);
            mProjectsList.remove(name);
            // 保存修改到工程列表文件
            saveProjectsListToFile();
            // 删除数据文件
            File file= new File("/data/data/"+ PACKAGENAME+"/shared_prefs",name +".xml");
            if(file.exists()){
                file.delete();
            }
            // 返回被删条目原索引
            return index;
        } else {
            return -1;
        }
    }

    public int renameProject(int index, String newName) {
        // 工程名列表未被初始化
        if (null == mProjectsList) return -1;
        // 判断索引是否越界
        if (index >= mProjectsList.size()) return -1;
        // 判断索引是否非法
        if (index < 0) return -1;

        // 更改工程数据文件名称
        String oldName = mProjectsList.get(index);
        File oldFile = new File("/data/data/"+ PACKAGENAME+"/shared_prefs",oldName +".xml");
        File newFile = new File("/data/data/"+ PACKAGENAME+"/shared_prefs",newName +".xml");
        if(oldFile.exists()){
            if (!oldFile.renameTo(newFile)) {
                return -1;
            }
        }
        // 更改工程名数组中的数据
        mProjectsList.remove(index);
        mProjectsList.add(newName);
        index = mProjectsList.size() - 1;
        // 保存数组数据至文件中
        saveProjectsListToFile();
        // 更新其他变量数据
        setCurrentProject(index);
        getPointViewsList();
        return index;
    }

    public int renameProject(String oldName, String newName) {
        // 工程名列表未被初始化
        if (null == mProjectsList) return -1;
        // 判断是否存在该工程
        if (!mProjectsList.contains(oldName)) return -1;
        // 更改工程数据文件名称
        File oldFile = new File("/data/data/"+ PACKAGENAME+"/shared_prefs",oldName +".xml");
        File newFile = new File("/data/data/"+ PACKAGENAME+"/shared_prefs",newName +".xml");
        if(oldFile.exists()){
            if (!oldFile.renameTo(newFile)) {
                return -1;
            }
        }
        // 更改工程名数组中的数据
        mProjectsList.remove(oldName);
        mProjectsList.add(newName);
        int index = mProjectsList.size() - 1;
        // 保存数组数据至文件中
        saveProjectsListToFile();
        // 更新其他变量数据
        setCurrentProject(newName);
        getPointViewsList();
        return index;
    }

    public int addPoint(PositionPoint p) {
        // 点列表未被初始化
        if (null == mPointsList) return -1;
        // 未设定当前操作的工程
        if (null == mCurrentProject) return -1;
        // 添加点至列表
        // TODO: 判断点名是否存在，若存在则在点名末尾插入，不存在则在列表末尾插入
        mPointsList.add(p);
        // 将改动写入文件
        savePointsListToFile();

        return mPointsList.size();
    }

    public int removePoint(int index) {
        // 点列表未被初始化
        if (null == mPointsList) return -1;
        // 未设定当前操作的工程
        if (null == mCurrentProject) return -1;
        // 索引大于列表长度
        if (index > mPointsList.size() + 1) return -1;
        // 从列表中移除点
        mPointsList.remove(index);
        // 将改动写入文件
        savePointsListToFile();

        return index;
    }

    private void convertPointsListToPointViewsList() {
        getPointsList();
        if (null == mPointsList) return;

        mPointViewsList = new ArrayList<PositionPointView>();

        if (mPointsList.isEmpty()) {// 列表为空
            initLastStatement();
            return;
        }

        int currentNum = 0;
        int currentDotNum = 0;
        Log.d("TAG", "convertPointsListToPointViewsList: " + mPointsList.get(0).toString());
        String currentDotName = mPointsList.get(0).getDotName();

        for (PositionPoint p : mPointsList) {
            if (!p.getDotName().equals(currentDotName)) {   // 判断点名是否改变
                currentDotNum = 0;
                currentDotName = p.getDotName();
            }

            /*
             * 续编序号
             */
//            currentDotName = p.getDotName();
//            if (!mDotNameNumberList.containsKey(currentDotName)){
//                mDotNameNumberList.put(currentDotName,1);
//            } else {
//                currentDotNum = mDotNameNumberList.get(currentDotName);
//                mDotNameNumberList.put(currentDotName, ++currentDotNum);
//            }

            /*
             * 普通序号
             */
            currentDotNum += 1;
            mPointViewsList.add(new PositionPointView(++currentNum, currentDotNum, false, p));
        }

        mLastNum = currentNum;
        mLastDotName = currentDotName;
        mLastDotNum = currentDotNum;
    }

    private void saveProjectsListToFile() {
        SharedPreferences sp = mContext.getSharedPreferences(PROJECTLISTFILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (null == mProjectsList) return; // 列表未被初始化

        int i = 0;
        try {
            for (String s : mProjectsList) {
                editor.putString(Integer.toString(i), s);
                i += 1;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            i = -1;
        }

        // 写入失败，清除缓存文件
        if (-1 == i) {
            editor.clear().apply();
            return;
        }

        // 写入成功，更新“Total”标签
        editor.putInt("Total", i);
        editor.apply();
    }

    private void savePointsListToFile() {
        SharedPreferences sp = mContext.getSharedPreferences(mCurrentProject, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (null == mPointsList) return; // 列表未被初始化

        int i = 0;
        try {
            for (PositionPoint p : mPointsList) {
                editor.putString(Integer.toString(i), getJsonObjectFromPoint(p).toString());
                i += 1;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            i = -1;
        }

        // 写入失败，清除缓存文件
        if (-1 == i) {
            editor.clear().apply();
            return;
        }

        // 写入成功，更新“Total”标签
        editor.putInt("Total", i);
        editor.apply();
    }

    private void readProjectsListFromFile() {
        ArrayList<String> list = new ArrayList<>();
        SharedPreferences sp = mContext.getSharedPreferences(PROJECTLISTFILENAME, Context.MODE_PRIVATE);

        int total = 0;
        int i = 1;
        total = sp.getInt("Total", -1);
        if (-1 == total) {  // 列表非法初始化
            mProjectsList = null;
            return;
        } else if (0 == total) {    // 列表为空
            mProjectsList = new ArrayList<>();
            return;
        }

        String s = null;
        try {
            for (i = 0; i < total; ++i) {
                s = sp.getString(Integer.toString(i), "");
                list.add(s);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            i = -1;
        }

        // 读取发生错误
        if (-1 == i) {
            mPointsList = null;
            return;
        }

        // 读取成功
        mProjectsList = list;
    }

    private void readPointsListFromeFile() {
        PositionPoint p = new PositionPoint();
        ArrayList<PositionPoint> list = new ArrayList<>();

        SharedPreferences sp = mContext.getSharedPreferences(mCurrentProject, Context.MODE_PRIVATE);

        int i = 0;
        int total = sp.getInt("Total", -1);
        if (-1 == total) {  //列表非法初始化
            mPointsList = null;
            return;
        } else if (0 == total) {    //列表为空
            mPointsList = new ArrayList<>();
            return;
        }

        String s = null;
        try {
            for (i = 0; i < total; ++i) {
                s = sp.getString(Integer.toString(i), "");
                list.add(getPointFromJsonObject(s));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            i = -1;
        }

        // 读取发生错误
        if (-1 == i) {
            mPointsList = null;
            return;
        }

        // 读取成功
        mPointsList = list;
    }

    @Nullable
    private JSONObject getJsonObjectFromPoint(PositionPoint p) {
        JSONObject mJsonObj = new JSONObject();
        try {
            mJsonObj.put("Latitude", p.Latitude);
            mJsonObj.put("Longitude", p.Longitude);
            mJsonObj.put("DotName", p.DotName);
        } catch (JSONException e) {
            e.printStackTrace();
            mJsonObj = null;
        }

        if (null == mJsonObj) return null;
        else return mJsonObj;
    }

    @Nullable
    private PositionPoint getPointFromJsonObject(String js) {
        JSONObject mJsonObj = null;
        PositionPoint mPoint = null;
        double mLat;
        double mLng;
        String mStr;

        try {
            mJsonObj = new JSONObject(js);
            mLat = mJsonObj.getDouble("Latitude");
            mLng = mJsonObj.getDouble("Longitude");
            mStr = mJsonObj.getString("DotName");
            mPoint = new PositionPoint(mLat, mLng, mStr);
        } catch (JSONException e) {
            e.printStackTrace();
            mPoint = null;
        }

        if (null == mPoint) return null;
        else return mPoint;
    }
}
