package com.fuxi.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.TextView;

/**
 * Title: CheckBoxTextView Description: 自定义单选按钮
 * 
 * @author LJ
 * 
 */
public class CheckBoxTextView extends TextView implements Checkable {

    private boolean mChecked;
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    public CheckBoxTextView(Context context) {
        super(context);
    }

    public CheckBoxTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;
        refreshDrawableState();

    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        mChecked = !mChecked;
    }

}
