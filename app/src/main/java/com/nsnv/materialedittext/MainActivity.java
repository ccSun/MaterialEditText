package com.nsnv.materialedittext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.nsnv.libedittext.MEditText;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MEditText medt_usr = (MEditText) findViewById(R.id.medt_usr);
        final MEditText medt_pwd = (MEditText) findViewById(R.id.medt_pwd);

        // 最大输入8个字
        medt_usr.setIntMaxCount(8);
        // 失去焦点后显示loading 动画
        medt_usr.setBottomLoadingEnabled(true);
        medt_usr.setTextMyDef("HELLO");

        Button btn_error = (Button) findViewById(R.id.btn_error);
        btn_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                medt_usr.stopBottomLoading();
                medt_usr.setError("用户名错误");
            }
        });
        Button btn_hint = (Button) findViewById(R.id.btn_hint);
        btn_hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                medt_usr.stopBottomLoading();
                medt_usr.setBottomHint("恭喜用户名可用");
            }
        });
    }
}
