package com.fuxi.dao;

import java.util.List;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fuxi.util.DBUtil;
import com.fuxi.vo.StockRemove;

/**
 * Title: StockRemoveDao Description: 货架移位DAO(仓位管理)
 * 
 * @author LYJ
 * 
 */
public class StockRemoveDao extends BaseDao {
    private static final String TABLE = "stockRemove";

    public StockRemoveDao(Context context) {
        super(context);
    }

    public List<StockRemove> getList() {
        return callBack(TYPE_READ, new DaoCallBack<List<StockRemove>>() {
            @Override
            public List<StockRemove> invoke(SQLiteDatabase conn) {
                String sql = " select * from stockRemove ";
                cursor = conn.rawQuery(sql, null);
                if (cursor == null) {
                    return null;
                }
                List<StockRemove> sr = DBUtil.cursor2VOList(cursor, StockRemove.class);
                return sr;
            }
        });
    }

    public Integer getStockRemoveTotalCount() {
        return callBack(TYPE_READ, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                int count = 0;
                String sql = " select sum(Quantity) Quantity from stockRemove ";
                cursor = conn.rawQuery(sql, null);
                if (cursor == null) {
                    return null;
                }
                while (cursor.moveToNext()) {
                    count = cursor.getInt(cursor.getColumnIndex("Quantity"));
                }
                return count;
            }
        });
    }

    public StockRemove find(final StockRemove stockRemove) {
        return callBack(TYPE_READ, new DaoCallBack<StockRemove>() {
            @Override
            public StockRemove invoke(SQLiteDatabase conn) {
                cursor = conn.query(TABLE, null, " DeptID = ? and StorageID = ? and GoodsID = ? and ColorID = ? and SizeID = ? ", new String[] {stockRemove.getDeptId(), stockRemove.getStorageId(), stockRemove.getGoodsId(), stockRemove.getColorId(), stockRemove.getSizeId()}, null, null, null);
                if (cursor.moveToFirst() == false) {
                    return null;
                }
                StockRemove stockRemove = new StockRemove();
                stockRemove.setId(cursor.getInt(cursor.getColumnIndex("id")));
                stockRemove.setDeptId(cursor.getString(cursor.getColumnIndex("DeptID")));
                stockRemove.setBarcode(cursor.getString(cursor.getColumnIndex("Barcode")));
                stockRemove.setGoodsCode(cursor.getString(cursor.getColumnIndex("SupplierCode")));
                stockRemove.setGoodsCode(cursor.getString(cursor.getColumnIndex("GoodsCode")));
                stockRemove.setGoodsName(cursor.getString(cursor.getColumnIndex("GoodsName")));
                stockRemove.setColor(cursor.getString(cursor.getColumnIndex("Color")));
                stockRemove.setSize(cursor.getString(cursor.getColumnIndex("Size")));
                stockRemove.setStorage(cursor.getString(cursor.getColumnIndex("Storage")));
                stockRemove.setStorageId(cursor.getString(cursor.getColumnIndex("StorageID")));
                stockRemove.setGoodsId(cursor.getString(cursor.getColumnIndex("GoodsID")));
                stockRemove.setColorId(cursor.getString(cursor.getColumnIndex("ColorID")));
                stockRemove.setSizeId(cursor.getString(cursor.getColumnIndex("SizeID")));
                stockRemove.setQuantity(cursor.getInt(cursor.getColumnIndex("Quantity")));
                return stockRemove;
            }
        });
    }

    public int update(final StockRemove stockRemove) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                conn.execSQL(" update stockRemove set Quantity = ? where id = ? ", new Object[] {stockRemove.getQuantity(), stockRemove.getId()});
                return 1;
            }
        });
    }

    public int insert(final StockRemove stockRemove) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                conn.execSQL(
                        " insert into stockRemove(DeptID,DeptName,Barcode,SupplierCode,GoodsCode,GoodsName,Color,Size,Storage,StorageID,GoodsID,ColorID,SizeID,Quantity) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?) ",
                        new Object[] {stockRemove.getDeptId(), stockRemove.getDeptName(), stockRemove.getBarcode(), stockRemove.getSupplierCode(), stockRemove.getGoodsCode(), stockRemove.getGoodsName(), stockRemove.getColor(), stockRemove.getSize(), stockRemove.getStorage(),
                                stockRemove.getStorageId(), stockRemove.getGoodsId(), stockRemove.getColorId(), stockRemove.getSizeId(), stockRemove.getQuantity()});
                return 1;
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

}
