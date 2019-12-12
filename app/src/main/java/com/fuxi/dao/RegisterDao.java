package com.fuxi.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fuxi.vo.Register;

/**
 * Title: RegisterDao Description: PDA注册DAO
 * 
 * @author LYJ
 * 
 */
public class RegisterDao extends BaseDao {
    private static final String TABLE = "register";

    public RegisterDao(Context context) {
        super(context);
    }

    public Register find() {
        return callBack(TYPE_READ, new DaoCallBack<Register>() {
            @Override
            public Register invoke(SQLiteDatabase conn) {
                String sql = " select * from register ";
                cursor = conn.rawQuery(sql, null);
                while (cursor.moveToNext()) {
                    Register register = new Register();
                    register.setSecret(cursor.getString(cursor.getColumnIndex("Secret")));
                    register.setMadeDate(cursor.getString(cursor.getColumnIndex("MadeDate")));
                    register.setType(cursor.getInt(cursor.getColumnIndex("Type")));
                    return register;
                }
                return null;
            }
        });
    }

    public int insert(final Register register) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                conn.execSQL(" delete from register ");
                conn.execSQL("insert into register(Secret,Type,MadeDate) values(?,?,?)", new Object[] {register.getSecret(), register.getType(), register.getMadeDate()});
                return 1;
            }
        });
    }
}
