package com.qiaoxg.flowlayoutdemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiaoxg.flowlayoutdemo.flowLayout.FlowLayout;
import com.qiaoxg.flowlayoutdemo.flowLayout.TagAdapter;
import com.qiaoxg.flowlayoutdemo.flowLayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TagAdapter mTagAdapter;
    private List<View> mDeleteViewList = new ArrayList<>();
    private List<String> mKeywordList = new ArrayList<>();

    private TagFlowLayout historyFrameLayout;
    private EditText searchEt;
    private View cancelBtn;
    ImageView btnDeleteHistory;
    TextView btnDone;
    TextView btnClearAllHistory;
    private View mNoDataView;
    private View mDataView;

    private ProgressDialog loadingDialog;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String keyWord = (String) msg.obj;
                    loadingDialog.dismiss();
                    mKeywordList.add(keyWord);
                    mTagAdapter.notifyDataChanged();
                    if (mKeywordList.size() > 0) {
                        mDataView.setVisibility(View.VISIBLE);
                        mNoDataView.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("搜索中...");

        initData();
        initView();
        initFrameLayout();
    }

    private void initData() {
//        mKeywordList.add("搜索激励");
//        mKeywordList.add("清除全");
//        mKeywordList.add("搜索激");
//        mKeywordList.add("记录");
//        mKeywordList.add("确认清除全部搜索记录");
//        mKeywordList.add("全部搜索记录");
    }

    private void initView() {
        mDataView = findViewById(R.id.history_layout);
        mNoDataView = findViewById(R.id.noDataLayout);

        btnClearAllHistory = findViewById(R.id.btn_clearAllHistory);
        btnClearAllHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSureDialog("确认清除全部搜索记录？", 0, true);
            }
        });
        btnDone = findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (View deleteView : mDeleteViewList) {
                    deleteView.setVisibility(View.INVISIBLE);
                }

                btnDeleteHistory.setVisibility(View.VISIBLE);
                btnDone.setVisibility(View.GONE);
                btnClearAllHistory.setVisibility(View.GONE);
            }
        });
        btnDeleteHistory = findViewById(R.id.btn_deleteHistory);
        btnDeleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (View deleteView : mDeleteViewList) {
                    deleteView.setVisibility(View.VISIBLE);
                }

                btnDeleteHistory.setVisibility(View.GONE);
                btnDone.setVisibility(View.VISIBLE);
                btnClearAllHistory.setVisibility(View.VISIBLE);
            }
        });


        historyFrameLayout = findViewById(R.id.history_frameLayout);
        searchEt = findViewById(R.id.search_et);

        cancelBtn = findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchEt.setText("");
            }
        });
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(searchEt.getText().toString())) {
                    cancelBtn.setVisibility(View.GONE);
                } else {
                    cancelBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH && !TextUtils.isEmpty(searchEt.getText().toString())) {
                    // 先隐藏键盘
                    ((InputMethodManager) searchEt.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(MainActivity.this
                                            .getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    // 搜索，进行自己要的操作...
                    loadingDialog.show();
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = searchEt.getText().toString();
                    mHandler.sendMessageDelayed(msg, 1500);
                    return true;
                }
                return false;
            }
        });

    }

    private void initFrameLayout() {
        mTagAdapter = new TagAdapter<String>(mKeywordList) {

            @Override
            public View getView(FlowLayout parent, final int position, final String s) {
                View view = getLayoutInflater().inflate(R.layout.item_search_history, historyFrameLayout, false);
                TextView title = view.findViewById(R.id.label_tv);
                title.setText(s);
                title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchEt.setText(s);
                        loadingDialog.show();
                        mHandler.sendEmptyMessageDelayed(1, 2000);
                    }
                });

                View deleteView = view.findViewById(R.id.delete_btn);
                deleteView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSureDialog("确认删除此条搜索记录？", position, false);
                    }
                });

                mDeleteViewList.add(deleteView);
                return view;
            }

            @Override
            public boolean setSelected(int position, String s) {
                return s.equals("Android");
            }
        };

        historyFrameLayout.setAdapter(mTagAdapter);
    }

    private void showSureDialog(String keyWord, final int position, final boolean isAll) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(keyWord);
        dialog.setTitle("提示");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isAll) {
                    mKeywordList.clear();
                    mTagAdapter.notifyDataChanged();
                    mDataView.setVisibility(View.GONE);
                    mNoDataView.setVisibility(View.VISIBLE);
                } else {
                    mKeywordList.remove(position);
                    mDeleteViewList.remove(position);
                    mTagAdapter.notifyDataChanged();

                    if (mKeywordList.size() > 0) {
                        for (View deleteView : mDeleteViewList) {
                            deleteView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        mDataView.setVisibility(View.GONE);
                        mNoDataView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }
}
