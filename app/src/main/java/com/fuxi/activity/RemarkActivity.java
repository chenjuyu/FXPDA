package com.fuxi.activity;

import java.util.HashMap;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.fuxi.main.R;
import com.fuxi.util.Logger;
import com.fuxi.vo.RequestVo;

/**
 * Title: RemarkActivity Description: 添加修改备注活动界面
 * 
 * @author LYJ
 * 
 */
public class RemarkActivity extends BaseWapperActivity {

    private static final String TAG = "RemarkActivity";

    private EditText etRemark;

    private String remark; // 备注信息
    private String id; // ID
    private String idName; // ID名称
    private String updatePath;
    private boolean auditFlag;

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.remark);
        setTitle("备注");
    }

    @Override
    protected void onHeadRightButton(View v) {
        // 点击完成,保存备注信息
        remark = etRemark.getText().toString();
        // 非空判断
        if (null == remark || "".equals(remark.trim())) {
            Toast.makeText(RemarkActivity.this, "备注信息不能为空", Toast.LENGTH_LONG).show();
            return;
        }
        if ((null == id || "".equals(id)) && (null == idName || "".equals(idName))) {
            saveRemarks();
        } else {
            updateRemark();
        }
    }

    @Override
    protected void onHeadLeftButton(View v) {
        // 点击完成,保存备注信息
        remark = etRemark.getText().toString();
        // 当有备注信息存在点击返回时提示
        if (null == remark || "".equals(remark.trim())) {
            super.onHeadLeftButton(v);
        } else {
            if (remark.equals(etRemark.getText().toString())) {
                super.onHeadLeftButton(v);
            } else {
                saveOrNot();
            }
        }
    }

    /**
     * 保存备注信息
     */
    private void saveRemarks() {
        Intent intent = new Intent();
        intent.putExtra("remark", remark);
        setResult(1, intent);
        Toast.makeText(RemarkActivity.this, "添加备注成功", Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * 点击返回时询问是否保存备注信息
     */
    private void saveOrNot() {
        // 询问是否删除
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("保存提示");
        dialog.setMessage("备注信息尚未保存,确定要返回吗？");
        // 相当于点击确认按钮
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        // 相当于点击取消按钮
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        dialog.create();
        dialog.show();
    }

    /**
     * 更新备注信息
     */
    private void updateRemark() {
        RequestVo vo = new RequestVo();
        vo.context = this;
        HashMap map = new HashMap();
        map.put(idName, id);
        vo.requestUrl = updatePath;
        map.put("memo", remark);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {

            @Override
            public void processData(JSONObject paramObject, boolean paramBoolean) {
                try {
                    if (paramObject.getBoolean("success")) {
                        saveRemarks();
                    } else {
                        Toast.makeText(RemarkActivity.this, "添加备注失败", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(RemarkActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }

        });
    }

    @Override
    protected void setListener() {}

    @Override
    protected void processLogic() {
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            id = bundle.getString("id");
            idName = bundle.getString("idName");
            updatePath = bundle.getString("updatePath");
            remark = bundle.getString("memo");
            auditFlag = bundle.getBoolean("auditFlag");
        }
        // 显示备注
        etRemark.setText(remark);
        if (!auditFlag) {
            // 设置右上角图标
            setHeadRightVisibility(View.VISIBLE);
            setHeadRightText(R.string.complete);
        } else {
            etRemark.setEnabled(false);
        }
    }

    @Override
    protected void findViewById() {
        etRemark = (EditText) findViewById(R.id.et_remark);
    }

}
