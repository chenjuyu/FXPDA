package com.fuxi.activity;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.fuxi.main.R;
import com.fuxi.switchbutton.widget.SwitchButton;
import com.fuxi.switchbutton.widget.SwitchButton.OnChangeListener;
public class PosReportActivity extends BaseWapperActivity {


    private  SwitchButton mSwitchButton;
    static Toast mToast;

    @Override
    protected void loadViewLayout() {
       setContentView(R.layout.activity_posreport);
       setTitle("零售报表");
    }

    @Override
    protected void setListener() {
        mSwitchButton.setOnChangeListener(new OnChangeListener() {

            @Override
            public void onChange(int position) {
                toast(position);
            }
        });

    }

    @Override
    protected void processLogic() {
        setSwitchButton();
    }

    @Override
    protected void findViewById() {
        mSwitchButton =(SwitchButton)findViewById(R.id.switchButton);
    }

    @Override
    public void onClick(View v) {

    }



    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public void setSwitchButton() {
        String[] mTexts = new String[mSwitchButton.getSwitchCount()];
        mTexts[0] = "今天";
        mTexts[1] = "昨天";
        mTexts[2] = "近7天";
        mTexts[3] = "近30天";
        mTexts[4] = "自定义";
        mSwitchButton.setTextArray(mTexts);
        mSwitchButton.notifyDataSetChange();
    }

    private void toast(int position) {
        String msg = "点击了第" + position + "项目";
        if (mToast == null)
            mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        else {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

}
