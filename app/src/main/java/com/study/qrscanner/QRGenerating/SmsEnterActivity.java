package com.study.qrscanner.QRGenerating;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.study.qrscanner.R;

public class SmsEnterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_enter);

        final EditText qrSms = (EditText) findViewById(R.id.editTel);
        final EditText qrText = (EditText) findViewById(R.id.editText1);
        Button generate = (Button) findViewById(R.id.generate);

        int maxLength = 15;
        qrSms.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

        int maxLength2 = 300;
        qrText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength2)});


        generate.setOnClickListener(new View.OnClickListener() {
            String result;

            @Override
            public void onClick(View v) {
                result = qrSms.getText().toString() + ":" + qrText.getText().toString();
                Intent i = new Intent(SmsEnterActivity.this, SmsGnrActivity.class);
                i.putExtra("gn", result);
                startActivity(i);
            }
        });
    }
}
