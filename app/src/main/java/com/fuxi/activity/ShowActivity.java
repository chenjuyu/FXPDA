package com.fuxi.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fuxi.main.R;
import com.ihidea.multilinechooselib.MultiLineChooseLayout;

import java.util.ArrayList;
import java.util.List;

public class ShowActivity extends Activity {
    private List<String> mColorData = new ArrayList<>();

    private MultiLineChooseLayout singleChoose;

    private TextView singleChooseTv;

    private Button button;

    List<String> singleChooseResult = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        singleChoose = (MultiLineChooseLayout) findViewById(R.id.singleChoose);
        singleChooseTv = (TextView) findViewById(R.id.singleChooseTv);


        initData();
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singleChoose.cancelAllSelectedItems();
            }
        });

        //多选
        singleChoose.setOnItemClickListener(new MultiLineChooseLayout.onItemClickListener() {
            @Override
            public void onItemClick(int position, String text) {
                singleChooseResult = singleChoose.getAllItemSelectedTextWithListArray();
                if (singleChooseResult != null && singleChooseResult.size() > 0) {
                    String textSelect = "";
                    for (int i = 0; i < singleChooseResult.size(); i++) {
                        textSelect += singleChooseResult.get(i) + " , ";
                    }
                    singleChooseTv.setText("结果：" + position);
                }
            }
        });

    }

    private void initData() {

        mColorData.add("电脑");
        mColorData.add("手机");
        mColorData.add("钥匙");
        mColorData.add("毛笔");
        mColorData.add("足球");
        mColorData.add("雨伞");
        mColorData.add("电视");
        mColorData.add("天气");

        singleChoose.setList(mColorData);

    }
}