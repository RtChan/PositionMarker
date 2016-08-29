package com.gzcz.rtchen.positionmarker;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ZXingQRFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ZXingQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ZXingQRFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /* 翻页显示二维码定义变量 */
    int page_number = 1;
    int now_page = 1;
    int enter_number = 0;
    int now_enter_number = 0;
    String QRcodebuf = "";
    String Display_part = "";
    TextView textView = null;
    ImageView qrImageView = null;
    Button btn_previous_page = null;
    Button btn_next_page = null;

    public ZXingQRFragment() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    //ZXing 之间的跳转使用 Bundle 来传输数据
    public static ZXingQRFragment newInstance(String QRcodebuf) {
        ZXingQRFragment fragment = new ZXingQRFragment();
        Bundle args = new Bundle();
        args.putString("QRcodebuf",QRcodebuf);
        fragment.setArguments(args);
        return fragment;
    }

    /* 计算所选数据一共需要使用多少页二维码显示 */
    public void Judge_number(String QRcodebuf){
        for(int i = 0; i < QRcodebuf.length(); i++){
            if (QRcodebuf.charAt(i) == '\n'){
                page_number = page_number + 1;
            }
        }

        page_number = (page_number - 1) / 20 + 1;
    }

    /* 将获取的字符串对应页数分组放入 Display_part 字符串中，进行当前页数对应的二维码数据显示 */
    public void clone_QRcodebuf(){
        Display_part = "";
        int i = 0;
        enter_number = 0;
        now_enter_number = 20 * (now_page - 1);

        if(now_enter_number != 0){
            for(; i < QRcodebuf.length(); i++){
                if (QRcodebuf.charAt(i) == '\n'){
                    enter_number = enter_number + 1;
                }

                if (enter_number == now_enter_number){
                    break;
                }
            }

            i = i + 1;
        }

        for(; (i < QRcodebuf.length()) && (now_enter_number < (20 * now_page)); i++){
            if (QRcodebuf.charAt(i) == '\n'){
                now_enter_number = now_enter_number + 1;
            }

            Display_part = Display_part + QRcodebuf.charAt(i);
        }
    }

    @Override
    public void onClick(View view){

        //按钮触发，上一页 下一页翻页显示二维码
        switch (view.getId()){
            case R.id.btn_previous_page:{
                Log.d("TAG", "onClick: "+Integer.toString(now_page)+Integer.toString(page_number));
                if (now_page > 1){
                    now_page = now_page - 1;
                    clone_QRcodebuf();

                    textView.setText(String.valueOf(now_page) + "/" + String.valueOf(page_number));

                    ZXingQR zxingQR = new ZXingQR();
                    try {
                        Bitmap qrCodeBitmap = zxingQR.createQRCode(Display_part, 1000);
                        qrImageView.setImageBitmap(qrCodeBitmap);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }

                break;
            }

            case R.id.btn_next_page:{
                Log.d("TAG", "onClick: "+Integer.toString(now_page)+Integer.toString(page_number));
                if (now_page < page_number){
                    now_page = now_page + 1;
                    clone_QRcodebuf();

                    textView.setText(String.valueOf(now_page) + "/" + String.valueOf(page_number));

                    ZXingQR zxingQR = new ZXingQR();
                    try {
                        Bitmap qrCodeBitmap = zxingQR.createQRCode(Display_part, 1000);
                        qrImageView.setImageBitmap(qrCodeBitmap);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }

                break;
            }
        }
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
        View mView = inflater.inflate(R.layout.zxingqr_fragment, container,false);

        textView = (TextView)mView.findViewById(R.id.text_page_number);
        qrImageView = (ImageView)mView.findViewById(R.id.iv_qr_image);
        btn_previous_page = (Button)mView.findViewById(R.id.btn_previous_page);
        btn_next_page = (Button)mView.findViewById(R.id.btn_next_page);
        btn_previous_page.setOnClickListener(this);
        btn_next_page.setOnClickListener(this);

        if(getArguments() != null){
            QRcodebuf = getArguments().getString("QRcodebuf");
            Judge_number(QRcodebuf);
        }

        textView.setText(String.valueOf(now_page) + "/" + String.valueOf(page_number));

        clone_QRcodebuf();
        ZXingQR zxingQR = new ZXingQR();
        try {
            Bitmap qrCodeBitmap = zxingQR.createQRCode(Display_part, 1000);
            qrImageView.setImageBitmap(qrCodeBitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return mView;
        // Inflate the layout for this fragment
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
