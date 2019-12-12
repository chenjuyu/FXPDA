package com.fuxi.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.fuxi.util.DBUtil;
import com.fuxi.util.Logger;

/**
 * Title: BaseDao Description: BaseDao配置
 * 
 * @author LJ
 * 
 */
public abstract class BaseDao {
    /** 数据连接对象 */
    protected SQLConnection sqlConnection;
    protected Cursor cursor;
    protected static final int TYPE_READ = 0; // 数据库只读类型
    protected static final int TYPE_WRITE = 1;// 数据库写类型
    private static final String TAG = "BaseDao";
    protected int limit = 15;

    public BaseDao(Context context) {
        sqlConnection = new SQLConnection(context);
    }

    /**
     * 回调业务逻辑
     * 
     * @param back
     * @return
     */
    protected <T> T callBack(int type, DaoCallBack<T> back) {
        T result = null;
        SQLiteDatabase conn = null;
        try {

            switch (type) {
                case TYPE_READ:
                    conn = sqlConnection.getReadableDatabase();
                    break;
                case TYPE_WRITE:
                    conn = sqlConnection.getWritableDatabase();
                    break;
            }
            if (conn == null)
                throw new NullPointerException("SQLiteDatabase conn  is null");
            result = back.invoke(conn);
            // conn.beginTransaction();
            // conn.setTransactionSuccessful();
        } catch (Exception e) {
            // conn.endTransaction();
            Logger.e(TAG, e);
        } finally {
            DBUtil.Release(conn, cursor);
        }
        return result;
    }

    /**
     * 回调接口
     * 
     * @author Administrator
     * 
     * @param <T>
     */
    interface DaoCallBack<T> {
        T invoke(SQLiteDatabase conn);
    }
}
