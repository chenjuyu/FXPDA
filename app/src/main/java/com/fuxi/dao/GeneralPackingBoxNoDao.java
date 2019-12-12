package com.fuxi.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fuxi.vo.GeneralPackingBoxNo;

/**
 * Title: GeneralPackingBoxNoDao Description: 装箱单生成箱号DAO
 * 
 * @author LYJ
 * 
 */
public class GeneralPackingBoxNoDao extends BaseDao {

    private static final String TABLE = "GeneralPackingBoxNo";

    public GeneralPackingBoxNoDao(Context context) {
        super(context);
    }

    public GeneralPackingBoxNo find(final String customerId, final String madeDate) {
        return callBack(TYPE_READ, new DaoCallBack<GeneralPackingBoxNo>() {
            @Override
            public GeneralPackingBoxNo invoke(SQLiteDatabase conn) {
                cursor = conn.query(TABLE, null, " CustomerID = ? and MadeDate = ?", new String[] {customerId, madeDate}, null, null, null);
                if (cursor.moveToFirst()) {
                    GeneralPackingBoxNo generalPackingBoxNo = new GeneralPackingBoxNo();
                    generalPackingBoxNo.setBoxNo(cursor.getString(cursor.getColumnIndex("BoxNo")));
                    generalPackingBoxNo.setCustomerId(cursor.getString(cursor.getColumnIndex("CustomerID")));
                    generalPackingBoxNo.setMadeDate(cursor.getString(cursor.getColumnIndex("MadeDate")));
                    generalPackingBoxNo.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    generalPackingBoxNo.setType(cursor.getInt(cursor.getColumnIndex("Type")));
                    return generalPackingBoxNo;
                }
                return null;
            }
        });
    }

    public int update(final GeneralPackingBoxNo generalPackingBoxNo) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                ContentValues values = new ContentValues();
                values.put("BoxNo", generalPackingBoxNo.getBoxNo());
                values.put("Type", generalPackingBoxNo.getType());
                return conn.update(TABLE, values, "CustomerID = ? and MadeDate = ?", new String[] {generalPackingBoxNo.getCustomerId(), generalPackingBoxNo.getMadeDate()});
            }
        });
    }

    public int insert(final GeneralPackingBoxNo generalPackingBoxNo) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                conn.execSQL("insert into GeneralPackingBoxNo(BoxNo,CustomerID,MadeDate,Type) values(?,?,?,?)", new Object[] {generalPackingBoxNo.getBoxNo(), generalPackingBoxNo.getCustomerId(), generalPackingBoxNo.getMadeDate(), generalPackingBoxNo.getType()});
                return 1;
            }
        });
    }

    public int delete(final String madeDate) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                return conn.delete(TABLE, " MadeDate <> ? ", new String[] {madeDate});
            }
        });
    }

}
