package com.gzcz.rtchen.positionmarker.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.gzcz.rtchen.positionmarker.MainActivity;
import com.gzcz.rtchen.positionmarker.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProjectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProjectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectFragment extends Fragment
        implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    View mView = null;
    private OnFragmentInteractionListener mListener;

    private EditText edit_view_prj;
    //    ArrayList<Map<String, Object>> Array_List_prj = new ArrayList<Map<String, Object>>();
    ArrayList<String> Array_List_prj = null;
    ListView list_view_prj = null;
    //SimpleAdapter Simple_Adapter_prj = null;
    ArrayAdapter ArrayAdapter_prj = null;
    Button btn_add_prj;

    public ProjectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProjectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProjectFragment newInstance(String param1, String param2) {
        ProjectFragment fragment = new ProjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.project_fragment, container, false);
        edit_view_prj = (EditText) mView.findViewById(R.id.edit_view_prj);
        list_view_prj = (ListView) mView.findViewById(R.id.list_view_prj);
        btn_add_prj = (Button) mView.findViewById(R.id.btn_add_prj);
        btn_add_prj.setOnClickListener(this);

        Array_List_prj = MainActivity.dm.getProjectsList();

        MainActivity c = (MainActivity) getContext();
//        Simple_Adapter_prj = new SimpleAdapter(c, Array_List_prj, android.R.layout.simple_expandable_list_item_2, new String[]{"prj", "date"}, new int[]{android.R.id.text1, android.R.id.text2});
        ArrayAdapter_prj = new ArrayAdapter(c, android.R.layout.simple_list_item_1, Array_List_prj);

//        list_view_prj.setAdapter(Simple_Adapter_prj);
        list_view_prj.setAdapter(ArrayAdapter_prj);

        list_view_prj.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity activity = (MainActivity) getActivity();
                Fragment fragment = null;
                Class fragmentClass = null;

                fragmentClass = PointListFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            }
        });

        list_view_prj.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                // TODO: 将字符串放入string.xml中
                builder.setTitle("删除该工程？");
                builder.setNegativeButton("取消", null);
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO：删除工程
                    }
                });
                builder.setNeutralButton("重命名", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO：重命名工程
                    }
                });
                builder.setIcon(R.mipmap.ic_launcher);
                builder.show();
                return true; // 此处必须返回true，默认返回false时会重复执行单击事件
            }
        });

        /* 读取 DataManager */
//        MainActivity.dm.getProjectsList();

        // Inflate the layout for this fragment
        return mView;
    }

    // TODO: Rename method, update argument and hook method into UI event
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

    @Override
    public void onClick(View v) {
        String List_view_add_buf = edit_view_prj.getText().toString();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);

//        Map<String,Object> item = new HashMap<String, Object>();
//        item.put("prj", edit_view_prj.getText().toString());
//        item.put("date", str);
//        Array_List_prj.add(item);
        if (List_view_add_buf.equals("")) {
            Toast.makeText(getActivity(), R.string.toast_add_prj_no_title, Toast.LENGTH_LONG).show();
        } else {
            MainActivity.dm.addProject(List_view_add_buf);
        }

//        Simple_Adapter_prj.notifyDataSetChanged();
        ArrayAdapter_prj.notifyDataSetChanged();
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
