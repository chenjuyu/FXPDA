package com.fuxi.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.dao.ParamerDao;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.vo.Paramer;

/**
 * 仓位设置中心活动界面
 * 
 * @author LYJ
 * 
 */
public class PositionSettingActivity extends BaseWapperActivity implements OnCheckedChangeListener {

    private static final String TAG = "PositionSettingActivity";

    private ParamerDao paramerDao = new ParamerDao(this);

    private TextView tvSave;
    private CheckBox cb_autoSave;
    private CheckBox cb_useLastTimePosition;
    private TextView tv_autoSave;
    private TextView tv_useLastTimePosition;
    private TextView tv_useNearestLeastPosition;
    private TextView tv_useNearestMostPosition;
    private RadioButton rb_useNearestMostPosition;
    private RadioButton rb_useNearestLeastPosition;
    private RadioGroup rg_storageOutGroup;

    private boolean autoSave;
    private boolean useLastTimePosition;
    private int storageOutType;

    private Paramer pAutoSave;
    private Paramer pUseLastTimePosition;
    private Paramer pStorageOutType;


    private void show() {
        // 获取显示值
        pAutoSave = paramerDao.find("autoSave");
        if (null != pAutoSave) {
            autoSave = Boolean.parseBoolean(pAutoSave.getValue());
            cb_autoSave.setChecked(autoSave);
        }
        pUseLastTimePosition = paramerDao.find("useLastTimePosition");
        if (null != pUseLastTimePosition) {
            useLastTimePosition = Boolean.parseBoolean(pUseLastTimePosition.getValue());
            cb_useLastTimePosition.setChecked(useLastTimePosition);
        }
        pStorageOutType = paramerDao.find("storageOutType");
        if (null != pStorageOutType) {
            storageOutType = Integer.parseInt(pStorageOutType.getValue());
            selectCheck(storageOutType);
        }
    }

    /**
     * 保存
     */
    private void save() {
        try {
            int count = 0;
            if (null != pAutoSave) {
                Paramer tpAutoSave = paramerDao.find("autoSave");
                if (tpAutoSave == null) {
                    count = paramerDao.insert(pAutoSave);
                } else {
                    count = paramerDao.update(pAutoSave);
                }
                LoginParameterUtil.autoSave = autoSave;
            }
            if (null != pUseLastTimePosition) {
                Paramer tpUseLastTimePosition = paramerDao.find("useLastTimePosition");
                if (tpUseLastTimePosition == null) {
                    count = paramerDao.insert(pUseLastTimePosition);
                } else {
                    count = paramerDao.update(pUseLastTimePosition);
                }
                LoginParameterUtil.useLastTimePosition = useLastTimePosition;
            }
            if (null != pStorageOutType) {
                Paramer tpStorageOutType = paramerDao.find("storageOutType");
                if (tpStorageOutType == null) {
                    count = paramerDao.insert(pStorageOutType);
                } else {
                    count = paramerDao.update(pStorageOutType);
                }
                LoginParameterUtil.storageOutType = storageOutType;
            }
            // 保存成功
            Builder dialog = new AlertDialog.Builder(PositionSettingActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("系统提示");
            dialog.setMessage("设置保存成功！");
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            // 相当于点击取消按钮
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        } catch (Exception e) {
            // 保存失败
            Toast.makeText(PositionSettingActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
            Logger.e(TAG, "设置信息保存失败");
        }
    }

    /**
     * 改变单选按钮的值
     * 
     * @param storageOutType
     */
    private void selectCheck(int storageOutType) {
        if (storageOutType == 0) {
            rb_useNearestLeastPosition.setChecked(true);
            rb_useNearestMostPosition.setChecked(false);
        } else if (storageOutType == 1) {
            rb_useNearestMostPosition.setChecked(true);
            rb_useNearestLeastPosition.setChecked(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                save();
                break;
            case R.id.autoSave:
            case R.id.tvAutoSave:
                if (autoSave) {
                    autoSave = false;
                } else {
                    autoSave = true;
                }
                if (null == pAutoSave) {
                    pAutoSave = new Paramer("autoSave", String.valueOf(autoSave));
                } else {
                    pAutoSave.setValue(String.valueOf(autoSave));
                }
                cb_autoSave.setChecked(autoSave);
                break;
            case R.id.useLastTimePosition:
            case R.id.tvUseLastTimePosition:
                if (useLastTimePosition) {
                    useLastTimePosition = false;
                } else {
                    useLastTimePosition = true;
                }
                if (null == pUseLastTimePosition) {
                    pUseLastTimePosition = new Paramer("useLastTimePosition", String.valueOf(useLastTimePosition));
                } else {
                    pUseLastTimePosition.setValue(String.valueOf(useLastTimePosition));
                }
                cb_useLastTimePosition.setChecked(useLastTimePosition);
                break;
            case R.id.useNearestLeastPosition:
            case R.id.tvUseNearestLeastPosition:
                onCheckedChanged(null, R.id.useNearestLeastPosition);
                break;
            case R.id.useNearestMostPosition:
            case R.id.tvUseNearestMostPosition:
                onCheckedChanged(null, R.id.useNearestMostPosition);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_position_setting);
        setTitle("仓储设置");
    }


    @Override
    protected void processLogic() {
        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                show();
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(PositionSettingActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                dialog.setTitle("系统提示");
                dialog.setMessage("当前用户未登录，操作非法！");
                // 相当于点击确认按钮
                dialog.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppManager.getAppManager().AppExit(getApplicationContext());
                    }
                });
                dialog.setCancelable(false);
                dialog.create();
                dialog.show();
            }
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(PositionSettingActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("系统提示");
            dialog.setMessage("当前为离线状态，此功能不可用！");
            // 相当于点击确认按钮
            dialog.setPositiveButton("返回", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        }
    }

    @Override
    protected void setListener() {
        tvSave.setOnClickListener(this);
        cb_autoSave.setOnClickListener(this);
        cb_useLastTimePosition.setOnClickListener(this);
        tv_autoSave.setOnClickListener(this);
        tv_useLastTimePosition.setOnClickListener(this);
        tv_useNearestLeastPosition.setOnClickListener(this);
        tv_useNearestMostPosition.setOnClickListener(this);
        rb_useNearestMostPosition.setOnClickListener(this);
        rb_useNearestLeastPosition.setOnClickListener(this);
        rg_storageOutGroup.setOnCheckedChangeListener(this);
    }

    @Override
    protected void findViewById() {
        tvSave = (TextView) findViewById(R.id.save);
        cb_autoSave = (CheckBox) findViewById(R.id.autoSave);
        cb_useLastTimePosition = (CheckBox) findViewById(R.id.useLastTimePosition);
        tv_autoSave = (TextView) findViewById(R.id.tvAutoSave);
        tv_useLastTimePosition = (TextView) findViewById(R.id.tvUseLastTimePosition);
        tv_useNearestLeastPosition = (TextView) findViewById(R.id.tvUseNearestLeastPosition);
        tv_useNearestMostPosition = (TextView) findViewById(R.id.tvUseNearestMostPosition);
        rb_useNearestMostPosition = (RadioButton) findViewById(R.id.useNearestMostPosition);
        rb_useNearestLeastPosition = (RadioButton) findViewById(R.id.useNearestLeastPosition);
        rg_storageOutGroup = (RadioGroup) findViewById(R.id.storageOutGroup);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.useNearestLeastPosition:
                storageOutType = 0;
                break;
            case R.id.useNearestMostPosition:
                storageOutType = 1;
                break;
            default:
                storageOutType = 0;
                break;
        }
        if (null == pStorageOutType) {
            pStorageOutType = new Paramer("storageOutType", String.valueOf(storageOutType));
        } else {
            pStorageOutType.setValue(String.valueOf(storageOutType));
        }
        selectCheck(storageOutType);
    }


}
