package com.fuxi.dao;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fuxi.util.DBUtil;
import com.fuxi.vo.OutLineStocktaking;

/**
 * Title: OutLineStocktakingDao Description: 离线盘点DAO
 * 
 * @author LYJ
 * 
 */
public class OutLineStocktakingDao extends BaseDao {

    private static final String TABLE = "outLineStocktaking";

    public OutLineStocktakingDao(Context context) {
        super(context);
    }

    public List<OutLineStocktaking> getList() {
        return callBack(TYPE_READ, new DaoCallBack<List<OutLineStocktaking>>() {
            @Override
            public List<OutLineStocktaking> invoke(SQLiteDatabase conn) {
                String sql = " select * from outLineStocktaking t order by t.Id desc; ";
                cursor = conn.rawQuery(sql, null);
                if (cursor == null) {
                    return null;
                }
                List<OutLineStocktaking> list = DBUtil.cursor2VOList(cursor, OutLineStocktaking.class);
                return list;
            }
        });
    }

    public List<String> getNoList() {
        return callBack(TYPE_READ, new DaoCallBack<List<String>>() {
            @Override
            public List<String> invoke(SQLiteDatabase conn) {
                String sql = " select distinct No from outLineStocktaking; ";
                cursor = conn.rawQuery(sql, null);
                if (cursor == null) {
                    return null;
                }
                List<String> list = new ArrayList<String>();
                while (cursor.moveToNext()) {
                    String no = cursor.getString(cursor.getColumnIndex("No"));
                    list.add(no);
                }
                return list;
            }
        });
    }

    public List<OutLineStocktaking> find(final String name) {
        return callBack(TYPE_READ, new DaoCallBack<List<OutLineStocktaking>>() {
            @Override
            public List<OutLineStocktaking> invoke(SQLiteDatabase conn) {
                cursor = conn.query(TABLE, null, " No = ?", new String[] {name}, null, null, null);
                List<OutLineStocktaking> list = new ArrayList<OutLineStocktaking>();
                while (cursor.moveToNext()) {
                    OutLineStocktaking OutLineStocktaking = new OutLineStocktaking();
                    OutLineStocktaking.setNo(cursor.getString(cursor.getColumnIndex("No")));
                    OutLineStocktaking.setDepartmentID(cursor.getString(cursor.getColumnIndex("DepartmentID")));
                    OutLineStocktaking.setDepartmentCode(cursor.getString(cursor.getColumnIndex("DepartmentCode")));
                    OutLineStocktaking.setDepartmentName(cursor.getString(cursor.getColumnIndex("DepartmentName")));
                    OutLineStocktaking.setShelvesNo(cursor.getString(cursor.getColumnIndex("ShelvesNo")));
                    OutLineStocktaking.setBarcode(cursor.getString(cursor.getColumnIndex("Barcode")));
                    OutLineStocktaking.setMadeDate(cursor.getString(cursor.getColumnIndex("MadeDate")));
                    OutLineStocktaking.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    OutLineStocktaking.setQuantity(cursor.getInt(cursor.getColumnIndex("Quantity")));
                    list.add(OutLineStocktaking);
                }
                return list;
            }
        });
    }

    public int update(final OutLineStocktaking OutLineStocktaking) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                ContentValues values = new ContentValues();
                values.put("Quantity", OutLineStocktaking.getQuantity());
                return conn.update(TABLE, values, "id = ?", new String[] {String.valueOf(OutLineStocktaking.getId())});
            }
        });
    }

    public int delete(final String id) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                return conn.delete(TABLE, " id = ? ", new String[] {id});
            }
        });
    }

    public int insert(final OutLineStocktaking OutLineStocktaking) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                conn.execSQL("insert into OutLineStocktaking(No,DepartmentID,DepartmentCode,DepartmentName,ShelvesNo,Barcode,Quantity,MadeDate) values(?,?,?,?,?,?,?,?)", new Object[] {OutLineStocktaking.getNo(), OutLineStocktaking.getDepartmentID(), OutLineStocktaking.getDepartmentCode(),
                        OutLineStocktaking.getDepartmentName(), OutLineStocktaking.getShelvesNo(), OutLineStocktaking.getBarcode(), OutLineStocktaking.getQuantity(), OutLineStocktaking.getMadeDate()});
                return 1;
            }
        });
    }

}
