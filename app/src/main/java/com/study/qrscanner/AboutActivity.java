package com.study.qrscanner;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.study.qrscanner.helpers.BaseActivity;

public class AboutActivity extends AppCompatActivity {

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(BaseActivity.MAIN_CONTENT_FADEIN_DURATION);
        }
        overridePendingTransition(0, 0);
        // 设置可跳转的链接
        ((TextView) findViewById(R.id.secusoWebsite)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.githubURL)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.libURL)).setMovementMethod(LinkMovementMethod.getInstance());
        // 设置版本
        ((TextView) findViewById(R.id.textFieldVersionName)).setText(getString(R.string.version_number, BuildConfig.VERSION_NAME));
        // 设置作者
        ((TextView) findViewById(R.id.textFieldAuthorNames)).setText(getString(R.string.about_author_string, getString(R.string.about_author_names)));
    }
}
