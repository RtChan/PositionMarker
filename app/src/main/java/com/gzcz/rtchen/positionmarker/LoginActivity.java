package com.gzcz.rtchen.positionmarker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    String IMEIcode = "";
    EditText tv_IMEI = null;
    Button btn_IMEI = null;
    EditText et_IMEI = null;

    public void onClick(View view){
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        IMEIcode = telephonyManager.getDeviceId();

        MainActivity.dm.setActivationKey(et_IMEI.getText().toString());
        if (MainActivity.dm.isActivated(IMEIcode)) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // TODO:存入@String
            Toast.makeText(LoginActivity.this, "激活失败！请检查激活码！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tv_IMEI = (EditText)findViewById(R.id.IMEI);
        et_IMEI = (EditText) findViewById(R.id.password);
        btn_IMEI = (Button) findViewById(R.id.imei_sign_in_button);
        btn_IMEI.setOnClickListener(this);

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        IMEIcode = telephonyManager.getDeviceId();
        tv_IMEI.setText(IMEIcode);
    }
}
