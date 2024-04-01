package com.study.qrscanner;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListView;

import com.study.qrscanner.helpers.ExpandableListAdapter;
import com.study.qrscanner.helpers.HelpDataDump;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class HelpActivity extends AppCompatActivity {

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        ExpandableListAdapter expandableListAdapter;
        HelpDataDump helpDataDump = new HelpDataDump(this);

        ExpandableListView generalExpandableListView = (ExpandableListView) findViewById(R.id.generalExpandableListView);

        LinkedHashMap<String, List<String>> expandableListDetail = helpDataDump.getDataGeneral();
        List<String> expandableListTitleGeneral = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new ExpandableListAdapter(this, expandableListTitleGeneral, expandableListDetail);
        generalExpandableListView.setAdapter(expandableListAdapter);

        overridePendingTransition(0, 0);

    }

    protected int getNavigationDrawerID() {
        return R.id.nav_help;
    }
}
