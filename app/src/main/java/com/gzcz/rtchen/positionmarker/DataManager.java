package com.gzcz.rtchen.positionmarker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Rt Chen on 2016/8/23.
 */
public class DataManager {
    private static final String PROJECTLISTFILENAME = "_ProjectList";
    private Context mContext = null;

    private ArrayList<String> mProjectsList = null;
    private ArrayList<PositionPoint> mPointsList = null;
    private ArrayList<PositionPointView> mPointViewsList = null;
    private String mCurrentProject = new String();

    private int mLastNum;
    private int mLastDotNum;
    private String mLastDotName;

    public DataManager(Context c) {
        mContext = c;
        initProjectsList();
        readProjectsListFromFile();
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
        sb.append(mLastDotName);
        sb.append(mLastDotNum);
        return sb.toString();
    }

    public void setCurrentProject(String name) {
        this.mCurrentProject = name;
        readPointsListFromeFile();
    }

    public int setCurrentProject(int index) {
        // 输入错误
        if (index <= 0) {
            mCurrentProject = null;
            return -1;
        }
        // 索引越界
        if (index > mProjectsList.size()) {
            mCurrentProject = null;
            return -1;
        }
        // 正常
        this.mCurrentProject = mProjectsList.get(index - 1).toString();
        readPointsListFromeFile();
        return index;
    }

    public String getCurrentProjectName() {
        return mCurrentProject;
    }

    public int addProject(String name) {
        // 工程名列表未被初始化
        if (null == mProjectsList) return -1;
        // 工程名列表为空
//        if (mProjectsList.isEmpty()) {
//            mProjectsList.add(name);
//            saveProjectsListToFile();
//        }
        // 判断工程是否已存在列表当中
        if (mProjectsList.contains(name)) {
            ;
        } else {
            mProjectsList.add(name);
            saveProjectsListToFile();

            SharedPreferences sp = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("Total", 0);
            editor.apply();
        }
        // 返回工程名在列表中的索引
        return mProjectsList.indexOf(name) + 1;
    }

    public int removeProject(String name) {
        // 工程名列表未被初始化
        if (null == mProjectsList) return -1;
        // 判断工程是否已存在列表当中
        if (mProjectsList.contains(name)) {
            if (mCurrentProject.equals(name)) mCurrentProject = null;
            int index = mProjectsList.indexOf(name);
            mProjectsList.remove(name);
            saveProjectsListToFile();
            return index;
        } else {
            return -1;
        }
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
        if (null == mPointsList) return;

        mPointViewsList = new ArrayList<PositionPointView>();
        if (mPointsList.isEmpty()) return;

        int currentNum = 0;
        int currentDotNum = 0;
        String currentDotName = mPointsList.get(0).getDotName();

        for (PositionPoint p : mPointsList) {
            if (!p.getDotName().equals(currentDotName)) {
                currentDotNum = 0;
                currentDotName = p.getDotName();
            }
            mPointViewsList.add(new PositionPointView(++currentNum, ++currentDotNum, false, p));
        }

        mLastNum = currentNum;
        mLastDotName = currentDotName;
        mLastDotNum = currentDotNum;
    }

    private void saveProjectsListToFile() {
        SharedPreferences sp = mContext.getSharedPreferences(PROJECTLISTFILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        int i = 0;
        try {
            for (String s : mProjectsList) {
                i += 1;
                editor.putString(Integer.toString(i), s);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            i = -1;
        }

        if (i == 0) ;
        else {
            editor.putInt("Total", i);
            editor.apply();
        }
    }

    private void savePointsListToFile() {
        SharedPreferences sp = mContext.getSharedPreferences(mCurrentProject, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        int i = 0;
        try {
            for (PositionPoint p : mPointsList) {
                i += 1;
                editor.putString(Integer.toString(i), getJsonObjectFromPoint(p).toString());
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            i = -1;
        }

        if (i == 0) ;
        else {
            editor.putInt("Total", i);
            editor.apply();
        }
    }

    private void readProjectsListFromFile() {
        ArrayList<String> list = new ArrayList<>();
        SharedPreferences sp = mContext.getSharedPreferences(PROJECTLISTFILENAME, Context.MODE_PRIVATE);

        int total = 0;
        int i = 1;
        total = sp.getInt("Total", -1);
        if (-1 == total) {
            mProjectsList = null;
            return;
        } else if (0 == total) {
            mProjectsList = new ArrayList<>();
            return;
        }

        String s = null;
        try {
            for (i = 1; i <= total; ++i) {
                s = sp.getString(Integer.toString(i), "");
                list.add(s);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            i = -1;
        }

        mProjectsList = list;
    }

    private void readPointsListFromeFile() {
        PositionPoint p = new PositionPoint();
        ArrayList<PositionPoint> list = new ArrayList<>();

        SharedPreferences sp = mContext.getSharedPreferences(mCurrentProject, Context.MODE_PRIVATE);

        int i = 1;
        int total = sp.getInt("Total", -1);
        if (-1 == total) {
            mPointsList = null;
            return;
        } else if (0 == total) {
            mPointsList = new ArrayList<>();
            return;
        }

        String s = null;
        try {
            for (i = 1; i <= total; ++i) {
                s = sp.getString(Integer.toString(i), "");
                list.add(getPointFromJsonObject(s));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            i = -1;
        }

        if ((-1 == i) && (0 != total)) mPointsList = null;

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
