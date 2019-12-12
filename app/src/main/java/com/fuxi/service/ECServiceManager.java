package com.fuxi.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.fuxi.util.Logger;
import com.fuxi.vo.Users;

/**
 * Title: ECServiceManager Description:
 * 
 * @author LJ
 * 
 */
public class ECServiceManager extends Service {

    private MyIECManager myIECManager;
    private static final String TAG = "ECServiceManager";

    @Override
    public void onCreate() {
        super.onCreate();
        myIECManager = new MyIECManager();
        Logger.d(TAG, "ECServiceManager is start");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.d(TAG, "onBind ");
        return myIECManager;
    }

    private class MyIECManager extends Binder implements IECManager {
        @Override
        public void testSaveUser(Users users) {
            // TODO Auto-generated method stub

        }

    }
}
