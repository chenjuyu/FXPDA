package com.fuxi.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import com.fuxi.adspter.SelectAdapter;
import com.fuxi.dao.DepartmentDao;
import com.fuxi.main.R;
import com.fuxi.vo.Department;
import com.fuxi.widget.RefreshListView;

/**
 * Title: SelectActivity Description: 离线选择活动界面
 * 
 * @author LYJ
 * 
 */
public class OutLineSelectActivity extends BaseWapperActivity {

    private static final String TAG = "OutLineSelectActivity";

    private EditText et_param;

    // 实例化对象
    private Handler handler = new Handler();
    private ListView lvDatas; // ListView
    private SelectAdapter adapter;
    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private DepartmentDao departmentDao = new DepartmentDao(this);

    private int count;
    private String selectType = null;

    /**
     * 根据不同的参数设置选择界面的显示标题
     */
    private void setTitle() {
        if ("selectWarehouseOutLine".equals(selectType)) {
            setTitle("选择仓库");
        }
    }

    /**
     * 根据参数获取显示数据
     * 
     * @param type
     */
    private void getData() {
        dataList.clear();
        String key = et_param.getText().toString().trim();
        List<Department> list = departmentDao.getList(key);
        departmentToConvert(list);
    }

    private void departmentToConvert(List<Department> list) {
        for (int i = 0; i < list.size(); i++) {
            Department department = list.get(i);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Name", department.getDepartment());
            map.put("DepartmentID", department.getDepartmentId());
            map.put("MustExistsGoodsFlag", department.getMustExistsGoodsFlag());
            map.put("DepartmentCode", department.getDepartmentCode());
            dataList.add(map);
        }
        adapter.refresh();
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.select_activity);
        // set select page method
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            selectType = bundle.getString("selectType");
        }
    }

    /**
     * 显示输入法
     */
    private void hideKeyBorder() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();// isOpen若返回true，则表示输入法打开
        if (isOpen) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void setListener() {
        et_param.addTextChangedListener(textWatcher);
        adapter = new SelectAdapter(this, dataList);
        lvDatas.setAdapter(adapter);
        lvDatas.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                RefreshListView listView = (RefreshListView) arg0;
                HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(arg2);
                adapter.setSelectIndex(arg2);
                Iterator iter = map.keySet().iterator();
                Intent intent = new Intent();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    intent.putExtra(entry.getKey(), String.valueOf(entry.getValue()));
                }
                setResult(1, intent);
                finish();
            }

        });

    }

    @Override
    protected void processLogic() {
        setTitle();
        getData();
    }

    @Override
    protected void findViewById() {
        lvDatas = (ListView) findViewById(R.id.lv_datas);
        et_param = (EditText) findViewById(R.id.param);
    }


    @Override
    public void finish() {
        hideKeyBorder();
        super.finish();
        // 关闭窗体动画显示
        this.overridePendingTransition(R.anim.activity_close, 0);
    }

    /**
     * 延迟线程，看是否还有下一个字符输入
     */
    private Runnable delayRun = new Runnable() {

        @Override
        public void run() {
            // 在这里调用服务器的接口，获取数据
            getData();
        }
    };

    /**
     * 监听TextView的字符输入变化
     */
    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (delayRun != null) {
                // 每次editText有变化的时候，则移除上次发出的延迟线程
                handler.removeCallbacks(delayRun);
            }
            if (s.length() == 0) {
                count = 0;
                if (count > 0) {
                    handler.removeCallbacks(delayRun);
                } else {
                    handler.postDelayed(delayRun, 500);
                    count++;
                }
            } else {
                // 延迟800ms，如果不再输入字符，则执行该线程的run方法
                handler.postDelayed(delayRun, 500);
            }
        }
    };

}
