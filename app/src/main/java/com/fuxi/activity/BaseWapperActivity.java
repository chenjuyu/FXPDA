package com.fuxi.activity;

import java.util.List;
import java.util.Vector;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.application.ECApplication;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.CommonUtil;
import com.fuxi.util.Constant;
import com.fuxi.util.Logger;
import com.fuxi.util.NetUtil;
import com.fuxi.util.StatusBarUtil;
import com.fuxi.util.ThreadPoolManager;
import com.fuxi.vo.RequestVo;

/**
 * Title: BaseWapperActivity Description: 活动界面抽象类(父类)
 * 
 * @author LJ
 * 
 */
public abstract class BaseWapperActivity extends Activity implements OnClickListener, OnKeyListener {

    private static final String TAG = "BaseWapperActivity";
    protected ProgressDialog progressDialog;
    private List<BaseTask> record = new Vector<BaseTask>();
    private ThreadPoolManager threadPoolManager;
    public static final int NOT_LOGIN = 403;
    private ButtomClick buttomClick;
    private KeyListener keyListener;
    private TextView head_title;
    private ECApplication application;
    private LinearLayout layout_content;
    private RelativeLayout head_layout; // TitleLayout
    private TextView headLeftBtn;
    protected TextView headRightBtn;
    protected  TextView headsearch;

    private AppManager manager = new AppManager();

    /** ContentView */
    private View inflate;

    protected abstract void loadViewLayout();

    protected abstract void setListener();

    protected abstract void processLogic();

    protected abstract void findViewById();

    public BaseWapperActivity() {
        threadPoolManager = ThreadPoolManager.getInstance();
    }

    private void initView() {
        loadViewLayout();
        findViewById();
        setListener();
        processLogic();
    }

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        AppManager.getAppManager().addActivity(this);
        application = (ECApplication) getApplication();
        application.addActvity(this);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.setContentView(R.layout.frame);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.view_title);
        layout_content = (LinearLayout) super.findViewById(R.id.frame_content);
        head_layout = (RelativeLayout) super.findViewById(R.id.head_layout);
        head_title = (TextView) super.findViewById(R.id.head_title);
        headLeftBtn = (TextView) super.findViewById(R.id.head_left);
        headRightBtn = (TextView) super.findViewById(R.id.head_right);

        headsearch =(TextView) super.findViewById(R.id.head_search);

        buttomClick = new ButtomClick();
        keyListener = new KeyListener();
        initView();
        int statusColor = Color.parseColor("#0000AA");
        StatusBarUtil.setColorNoTranslucent(this, statusColor);
        headLeftBtn.setOnTouchListener(buttomClick);
        headRightBtn.setOnTouchListener(buttomClick);
        headsearch.setOnTouchListener(buttomClick);

        if(this instanceof VipActivity || this instanceof StockQueryActivity) {//特殊处理
            head_title.setOnTouchListener(buttomClick);
        }
        // 设置位于最上层
        // headRightBtn.bringToFront();

    }

    public abstract interface DataCallback<T> {
        public abstract void processData(T paramObject, boolean paramBoolean);
    }

    protected void showProgressDialog() {
        if ((!isFinishing()) && (this.progressDialog == null)) {
            this.progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        }
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.setMessage(getString(R.string.LoadContent));
        this.progressDialog.show();
    }

    protected void showProgressDialog(String title, String content) {
        if ((!isFinishing()) && (this.progressDialog == null)) {
            this.progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        }
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.setTitle(title);
        this.progressDialog.setMessage(content);
        this.progressDialog.show();
    }

    protected void closeProgressDialog() {
        if (this.progressDialog != null)
            this.progressDialog.dismiss();
    }

    protected void getDataFromServer(RequestVo reqVo, DataCallback callBack) {
        if (record == null) {
            return;
        }
        showProgressDialog();
        BaseHandler handler = new BaseHandler(this, callBack, reqVo);
        BaseTask taskThread = new BaseTask(this, reqVo, handler);
        record.add(taskThread);
        this.threadPoolManager.addTask(taskThread);
    }

    protected void getDataFromServerNoProcess(RequestVo reqVo, DataCallback callBack) {
        if (record == null) {
            return;
        }
        BaseHandler handler = new BaseHandler(this, callBack, reqVo);
        BaseTask taskThread = new BaseTask(this, reqVo, handler);
        record.add(taskThread);
        this.threadPoolManager.addTask(taskThread);
    }

    @SuppressWarnings("unchecked")
    class BaseHandler extends Handler {
        private Context context;
        private DataCallback callBack;
        private RequestVo reqVo;

        public BaseHandler(Context context, DataCallback callBack, RequestVo reqVo) {
            this.context = context;
            this.callBack = callBack;
            this.reqVo = reqVo;
        }

        public void handleMessage(Message msg) {
            if (record == null) {
                return;
            }
            closeProgressDialog();
            if (msg.what == Constant.SUCCESS) {
                if (msg.obj == null) {
                    Toast.makeText(BaseWapperActivity.this, getString(R.string.net_error), Toast.LENGTH_LONG).show();
                }
            } else if (msg.what == Constant.NET_FAILED) {
                if (msg.obj == null) {
                    CommonUtil.showInfoDialog(context, getString(R.string.net_no));
                }
            } else if (msg.what == Constant.IP_ERROE) {
                if (msg.obj == null) {
                    CommonUtil.showInfoDialog(context, "服务器地址异常,请在[系统设置]中检查服务器连接地址是否正确!请确保连接地址中的特殊符号为英文状态");
                }
            } else if (msg.what == Constant.UNKNOW_ERROE) {
                if (msg.obj == null) {
                    Toast.makeText(BaseWapperActivity.this, "出现未知的错误", Toast.LENGTH_LONG).show();
                }
            }
            callBack.processData(msg.obj, true);
            Logger.d(TAG, "recordSize:" + record.size());
        }
    }

    class BaseTask implements Runnable {
        private Context context;
        private RequestVo reqVo;
        private Handler handler;

        public BaseTask(Context context, RequestVo reqVo, Handler handler) {
            this.context = context;
            this.reqVo = reqVo;
            this.handler = handler;
        }

        @SuppressWarnings("unused")
        @Override
        public void run() {
            Object obj = null;
            Message msg = Message.obtain();
            try {
                if (NetUtil.hasNetwork(context)) {
                    obj = NetUtil.post(reqVo);
                    if ("toLogin".equals(String.valueOf(obj))) {
                        Looper.prepare();
                        Builder dialog = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("系统提示");
                        dialog.setMessage("登录超时,请重新登录");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Activity activity = manager.findSecondToLastActivity();
                                Intent intent = new Intent(activity, LoginActivity.class);
                                startActivity(intent);
                                manager.finishActivity(activity);
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.create();
                        dialog.show();
                        Looper.loop();
                    } else {
                        msg.what = Constant.SUCCESS;
                        msg.obj = obj;
                        handler.sendMessage(msg);
                        if (record != null) {
                            record.remove(this);
                        }
                    }
                } else {
                    msg.what = Constant.NET_FAILED;
                    msg.obj = obj;
                    handler.sendMessage(msg);
                    if (record != null) {
                        record.remove(this);
                    }
                }
            } catch (RuntimeException e) {
                msg.what = Constant.IP_ERROE;
                msg.obj = obj;
                handler.sendMessage(msg);
                if (record != null) {
                    record.remove(this);
                }
            } catch (Exception e) {
                msg.what = Constant.UNKNOW_ERROE;
                msg.obj = obj;
                handler.sendMessage(msg);
                if (record != null) {
                    record.remove(this);
                }
            }
        }

    }

    private class ButtomClick implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.head_left:
                    onHeadLeftButton(v);
                    break;
                case R.id.head_right:
                    onHeadRightButton(v);
                    break;
                case R.id.head_search:
                    onHeadSearchButton(v);
                    break;
                case R.id.head_title:
                    onHeadTileButton(v);
                    break;
                default:
                    break;
            }
            return false;
        }

    }

    private class KeyListener implements OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            return false;
        }
    }

    public void setTitle(CharSequence title) {
        head_title.setText(title);
    }

    public void setTitle(int titleId) {
        head_title.setText(titleId);
    }

    public void setContentView(int layoutResID) {
        inflate = getLayoutInflater().inflate(layoutResID, null);
        setContentView(inflate);
    }

    public void setContentView(View view) {
        layout_content.removeAllViews();
        layout_content.addView(inflate);
    }

    public View findViewById(int id) {
        return inflate.findViewById(id);
    }

    protected void setHeadLeftVisibility(int visibility) {
        headLeftBtn.setVisibility(visibility);
    }

    protected void setHeadRightVisibility(int visibility) {
        headRightBtn.setVisibility(visibility);
    }
    protected void setHeadSearchVisibility(int visibility) {
        headsearch.setVisibility(visibility);
    }


    protected void setHeadRightText(int textid) {
        headRightBtn.setText(textid);
    }

    protected void onHeadLeftButton(View v) {
        finish();
    }

    protected void onHeadRightButton(View v) {

    }

    protected void onHeadSearchButton(View v) {

    }

    protected void onHeadTileButton(View v) {

    }

    //新增设置标题栏背景色2020-01-11
    protected void sethead_layout(){
        head_layout.setBackground(getResources().getDrawable(R.color.icon_blue));
        StatusBarUtil.setColorNoTranslucent(this, R.color.icon_blue);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        application.removeActvity(this);
        record.clear();
        record = null;
        threadPoolManager = null;
        layout_content = null;
        inflate = null;
        head_layout = null;
        headLeftBtn = null;
        headRightBtn = null;
        head_title = null;
        buttomClick = null;
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        application = null;
    }

    // headRightBtn的get方法
    public TextView getHeadRightBtn() {
        return headRightBtn;
    }

    public void addTask(AsyncTaskInterface task) {
        BaseAsyncTask at = new BaseAsyncTask(task);
        at.execute("");
    }

    public abstract interface AsyncTaskInterface<T> {
        public abstract void preExecute();

        public abstract void postExecute();

        public abstract void inBackground();

        public abstract void progressUpdate();
    }

    // 设置声音
    public void settingVoice(String type, Context context) {
        NotificationManager manger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        // 自定义声音 声音文件放在ram目录下，没有此目录自己创建一个
        if ("noFound".equals(type)) {
            notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.no_find);
            // 设置震动
            Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(800);
            // vibrator.cancel();
        } else if ("error".equals(type)) {
            notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.error);
            // 设置震动
            Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(1200);
            // vibrator.cancel();
        }
        // 使用系统默认声音用下面这条
        // notification.defaults=Notification.DEFAULT_SOUND;
        manger.notify(1, notification);
    }

    // 设置声音
    public void settingScanVoice(Context context) {
        NotificationManager manger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        // 自定义声音 声音文件放在ram目录下，没有此目录自己创建一个
        notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.beep);
        // 设置震动
        Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
        // vibrator.cancel();
        // 使用系统默认声音用下面这条
        // notification.defaults=Notification.DEFAULT_SOUND;
        manger.notify(1, notification);
    }

    class BaseAsyncTask extends AsyncTask<String, Integer, String> {
        private AsyncTaskInterface taskI;

        public BaseAsyncTask(AsyncTaskInterface task) {
            taskI = task;
        }

        // TestAsyncTask被后台线程执行后，被UI线程被调用，一般用于初始化界面控件，如进度条
        @Override
        protected void onPreExecute() {
            taskI.preExecute();
            super.onPreExecute();
        }

        // doInBackground执行完后由UI线程调用，用于更新界面操作
        @Override
        protected void onPostExecute(String result) {
            taskI.postExecute();
            super.onPostExecute(result);
        }

        // 在PreExcute执行后被启动AysncTask的后台线程调用，将结果返回给UI线程
        @Override
        protected String doInBackground(String... params) {
            taskI.inBackground();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            taskI.progressUpdate();
            super.onProgressUpdate(values);
        }

    }

}
