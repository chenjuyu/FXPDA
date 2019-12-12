package com.fuxi.dao;

import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fuxi.util.DBUtil;
import com.fuxi.vo.GoodsBoxBarcodeRecord;

/**
 * Title: GoodsBoxBarcodeRecordDao Description: 箱条码明细记录DAO
 * 
 * @author LYJ
 * 
 */
public class GoodsBoxBarcodeRecordDao extends BaseDao {

    private static final String TABLE = "goodsBoxBarcodeRecord";

    public GoodsBoxBarcodeRecordDao(Context context) {
        super(context);
    }

    public List<GoodsBoxBarcodeRecord> getList(final String billId) {
        return callBack(TYPE_READ, new DaoCallBack<List<GoodsBoxBarcodeRecord>>() {
            @Override
            public List<GoodsBoxBarcodeRecord> invoke(SQLiteDatabase conn) {
                String sql = " select id,BillID,GoodsBoxBarcode,SizeStr,BoxQty,GoodsID,ColorID from goodsBoxBarcodeRecord where BillID = '" + billId + "' ";
                cursor = conn.rawQuery(sql, null);
                if (cursor == null) {
                    return null;
                }
                List<GoodsBoxBarcodeRecord> sr = DBUtil.cursor2VOList(cursor, GoodsBoxBarcodeRecord.class);
                return sr;
            }
        });
    }

    public List<GoodsBoxBarcodeRecord> find(final GoodsBoxBarcodeRecord barcodeRecord) {
        return callBack(TYPE_READ, new DaoCallBack<List<GoodsBoxBarcodeRecord>>() {
            @Override
            public List<GoodsBoxBarcodeRecord> invoke(SQLiteDatabase conn) {
                String sql =
                        " select id,BillID,GoodsBoxBarcode,SizeStr,BoxQty,GoodsID,ColorID from goodsBoxBarcodeRecord where " + " BillID = '" + barcodeRecord.getBillId() + "' and SizeStr = '" + barcodeRecord.getSizeStr() + "' " + " and GoodsID = '" + barcodeRecord.getGoodsId() + "' and ColorID = '"
                                + barcodeRecord.getColorId() + "' ";
                cursor = conn.rawQuery(sql, null);
                if (cursor == null) {
                    return null;
                }
                List<GoodsBoxBarcodeRecord> sr = DBUtil.cursor2VOList(cursor, GoodsBoxBarcodeRecord.class);
                return sr;
            }
        });
    }

    public Integer findBarCodeBoxQty(final GoodsBoxBarcodeRecord barcodeRecord) {
        return callBack(TYPE_READ, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                cursor = conn.query(TABLE, null, " BillID = ? and GoodsBoxBarcode = ? ", new String[] {barcodeRecord.getBillId(), barcodeRecord.getGoodsBoxBarcode()}, null, null, null);
                while (cursor.moveToNext()) {
                    return cursor.getInt(cursor.getColumnIndex("BoxQty"));
                }
                return 0;
            }
        });
    }

    public int insert(final GoodsBoxBarcodeRecord barcodeRecord) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                conn.execSQL("insert into goodsBoxBarcodeRecord(BillID,GoodsBoxBarcode,SizeStr,GoodsID,ColorID,BoxQty) values(?,?,?,?,?,?)",
                        new Object[] {barcodeRecord.getBillId(), barcodeRecord.getGoodsBoxBarcode(), barcodeRecord.getSizeStr(), barcodeRecord.getGoodsId(), barcodeRecord.getColorId(), barcodeRecord.getBoxQty()});
                return 1;
            }
        });
    }

    public int update(final GoodsBoxBarcodeRecord barcodeRecord) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                ContentValues values = new ContentValues();
                values.put("BoxQty", barcodeRecord.getBoxQty());
                return conn.update(TABLE, values, " BillID = ? and GoodsBoxBarcode = ? ", new String[] {barcodeRecord.getBillId(), barcodeRecord.getGoodsBoxBarcode()});
            }
        });
    }

    public int delete(final String billId, final String goodsBoxBarcode) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                return conn.delete(TABLE, " billId = ? and goodsBoxBarcode = ?", new String[] {billId, goodsBoxBarcode});
            }
        });
    }

    public int delete(final String billId, final String sizeStr, final String goodsId, final String colorId) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                return conn.delete(TABLE, " billId = ? and sizeStr = ? and goodsId = ? and colorId = ?", new String[] {billId, sizeStr, goodsId, colorId});
            }
        });
    }

    public int deleteAll(final String billId) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                return conn.delete(TABLE, " billId = ? ", new String[] {billId});
            }
        });
    }

}
