package com.study.qrscanner.QRGenerating;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.study.qrscanner.R;

public class MailEnterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_enter);

        // 根据元素id查找activity页面元素
        final EditText qrResult = (EditText) findViewById(R.id.editMail);
        Button generate = (Button) findViewById(R.id.generate);

        int maxLength = 50;
        qrResult.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

        // 绑定activity按钮
        generate.setOnClickListener(new View.OnClickListener() {
            String result;

            @Override
            public void onClick(View v) {
                result = qrResult.getText().toString();
                Log.d("Mail Generate Click", "-> text=" + result);
                Intent i = new Intent(MailEnterActivity.this, MailGnrActivity.class);
                i.putExtra("gn", result);
                startActivity(i);
            }
        });
    }
}
