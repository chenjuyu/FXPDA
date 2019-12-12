package com.fuxi.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fuxi.vo.Paramer;

/**
 * Title: ParamerDao Description: 系统参数DAO
 * 
 * @author LYJ
 * 
 */
public class ParamerDao extends BaseDao {

    private static final String TABLE = "paramer";

    public ParamerDao(Context context) {
        super(context);
    }

    public Paramer find(final String name) {
        return callBack(TYPE_READ, new DaoCallBack<Paramer>() {
            @Override
            public Paramer invoke(SQLiteDatabase conn) {
                cursor = conn.query(TABLE, null, " name = ?", new String[] {name}, null, null, null);
                if (cursor.moveToFirst()) {
                    Paramer paramer = new Paramer();
                    paramer.setValue(cursor.getString(cursor.getColumnIndex("value")));
                    paramer.setName(cursor.getString(cursor.getColumnIndex("name")));
                    paramer.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    return paramer;
                }
                return null;
            }
        });
    }

    public int update(final Paramer paramer) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                ContentValues values = new ContentValues();
                values.put("value", paramer.getValue());
                return conn.update(TABLE, values, "name = ?", new String[] {paramer.getName()});
            }
        });
    }

    public int insert(final Paramer paramer) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                conn.execSQL("insert into paramer(name,value) values(?,?)", new Object[] {paramer.getName(), paramer.getValue()});
                return 1;
            }
        });
    }

}
