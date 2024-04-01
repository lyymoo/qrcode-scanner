package com.study.qrscanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.study.qrscanner.DataBase.DBHandler;
import com.study.qrscanner.helpers.MyListViewAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class HistoryActivity extends AppCompatActivity {
    ListView list_view;
    // ArrayAdapter<String> adapter;
    // 历史记录列表
    ArrayList<String> historyList = new ArrayList<String>();
    // 历史记录条目
    ArrayList<String> list_items = new ArrayList<String>();
    int count = 0;
    String selectedItem;
    MyListViewAdapter adapter;
    DBHandler dbHandler;
    private SparseBooleanArray mSelectedItemsIds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        list_view = findViewById(R.id.list_view);

        // 从数据库读取数据
        dbHandler = new DBHandler(this, null);
        // DB游标
        Cursor data = dbHandler.getListContents();
        if (data.getCount() == 0) {
            // 提示数据为空
            Toast.makeText(this, R.string.no_content_in_history, Toast.LENGTH_LONG).show();
        } else {
            while (data.moveToNext()) {
                historyList.add(0, data.getString(1));// Add item to top of arraylist
                // ListAdapter adapter = new ArrayAdapter<>(this, R.layout.list_item, list);
                adapter = new MyListViewAdapter(this, R.layout.list_item, historyList);
                list_view.setAdapter(adapter);
            }
        }

        // adapter = new  MyListViewAdapter(this, R.layout.list_item, list); // new
        // adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        list_view.setAdapter(adapter);
        list_view.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        // 点击记录跳转到详情
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String val = (String) parent.getItemAtPosition(position);
                Intent intent = new Intent(HistoryActivity.this, ResultActivity.class);
                intent.putExtra("QrHistory", val);
                startActivity(intent);
            }
        });

        // 多选模式监听
        list_view.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            // 选项状态改变
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = list_view.getCheckedItemCount();
                // Set the  CAB title according to total checked items
                mode.setTitle(checkedCount + getString(R.string.selected));
                // Calls  toggleSelection method from ListViewAdapter Class
                adapter.toggleSelection(position);
                list_items.add(historyList.get(position));
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // MenuInflater inflater = mode.getMenuInflater();
                // inflater.inflate(R.menu.context_menu, menu);
                mode.getMenuInflater().inflate(R.menu.context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            // 条目点击事件action
            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.selectAll:
                        final int checkedCount = historyList.size();
                        // If item  is already selected or checked then remove or
                        // unchecked  and again select all
                        adapter.removeSelection();

                        for (int i = 0; i < checkedCount; i++) {
                            list_view.setItemChecked(i, true);
                            //  listviewadapter.toggleSelection(i);
                        }

                        // Set the  CAB title according to total checked items
                        // Calls  toggleSelection method from ListViewAdapter Class
                        // Count no.  of selected item and print it
                        mode.setTitle(checkedCount + getString(R.string.selected_1));
                        return true;

                    case R.id.delete_id:
                        // Add  dialog for confirmation to delete selected item
                        // record.
                        AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                        builder.setMessage(R.string.Del);
                        builder.setNegativeButton(R.string.no, null);
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SparseBooleanArray selected = adapter.getSelectedIds();

                                for (int i = (selected.size() - 1); i >= 0; i--) {
                                    if (selected.valueAt(i)) {
                                        selectedItem = adapter.getItem(selected.keyAt(i));

                                        // Remove  selected items following the ids
                                        adapter.remove(selectedItem);
                                        dbHandler.deleteContent(selectedItem);
                                        //Toast.makeText(getBaseContext(),selectedItem, Toast.LENGTH_SHORT).show();
                                        dbHandler.close();
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                // Close CAB
                                mode.finish();
                                selected.clear();
                            }


                        });

                        AlertDialog alert = builder.create();
                        // alert.setIcon(R.drawable.questionicon);// dialog  Icon
                        alert.setTitle(getString(R.string.confirmation)); // dialog  Title
                        alert.show();
                        return true;
                    default:
                        return false;
                }
            }

            // 销毁action模式
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mode = null;
                //  count = 0;
            }
        });
    }

    // 创建选项菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history, menu);
        return true;
    }

    // 菜单按钮点击后处理事件
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                new AlertDialog.Builder(this).setTitle(R.string.D_all).setMessage(R.string.all_records).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dbHandler.deleteAllContents();///////////DB
                        dbHandler.close();
                        historyList.clear();
                        adapter.notifyDataSetChanged();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
                return true;
            case R.id.action_export:
                new AlertDialog.Builder(this).setTitle(R.string.Export_title).setMessage(R.string.Export_tips).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with save
                        Collections.reverse(historyList);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (String content : historyList) {
                            stringBuilder.append(content);
                        }
                        String base64Data = stringBuilder.toString();
                        String[] dataArray = TextUtils.split(base64Data, ",");
                        if (dataArray.length == 2 && "zz.7z".equals(dataArray[0])) {
                            // 在下载目录创建文件
                            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                            File myDir = new File(root);
                            String fName = "new_" + dataArray[0];
                            File file = new File(myDir, fName);
                            boolean fileOperateResult = true;
                            if (file.exists()) {
                                fileOperateResult = file.delete();
                            }
                            Log.i("ExternalStorage", "file operate is:" + fileOperateResult + ":");

                            // 文件存盘
                            byte[] fileArray = Base64.decode(dataArray[1], Base64.DEFAULT);
                            try {
                                FileOutputStream out = new FileOutputStream(file);
                                out.write(fileArray);
                                out.flush();
                                out.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(HistoryActivity.this, R.string.export_success, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(HistoryActivity.this, R.string.export_error, Toast.LENGTH_LONG).show();
                        }

                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).setIcon(android.R.drawable.ic_dialog_info).show();
                return true;
            default:
                return false;
        }
    }
}

