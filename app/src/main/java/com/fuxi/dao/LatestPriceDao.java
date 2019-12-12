package com.fuxi.dao;

import java.math.BigDecimal;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fuxi.vo.LatestPrice;

/**
 * Title: LatestPriceDao Description: 最近价格DAO
 * 
 * @author LYJ
 * 
 */
public class LatestPriceDao extends BaseDao {

    private static final String TABLE = "latestPrice";

    public LatestPriceDao(Context context) {
        super(context);
    }

    public BigDecimal find(final LatestPrice latestPrice) {
        return callBack(TYPE_READ, new DaoCallBack<BigDecimal>() {
            @Override
            public BigDecimal invoke(SQLiteDatabase conn) {
                cursor = conn.query(TABLE, null, " GoodsID = ? and CustomerID = ? and Type = ? and BarcodeType = ? ", new String[] {latestPrice.getGoodsId(), latestPrice.getCustomerId(), latestPrice.getType(), latestPrice.getBarcodeType()}, null, null, null);
                while (cursor.moveToNext()) {
                    return new BigDecimal(cursor.getDouble(cursor.getColumnIndex("LastPrice")));
                }
                return null;
            }
        });
    }

    public Integer findId(final LatestPrice latestPrice) {
        return callBack(TYPE_READ, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                Integer count = null;
                cursor = conn.query(TABLE, null, " GoodsID = ? and CustomerID = ? and Type = ? and BarcodeType = ? ", new String[] {latestPrice.getGoodsId(), latestPrice.getCustomerId(), latestPrice.getType(), latestPrice.getBarcodeType()}, null, null, null);
                while (cursor.moveToNext()) {
                    count = cursor.getInt(cursor.getColumnIndex("id"));
                }
                return count;
            }
        });
    }

    public int insert(final LatestPrice latestPrice) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                conn.execSQL("insert into latestPrice(GoodsID,CustomerID,Type,BarcodeType,LastPrice) values(?,?,?,?,?)", new Object[] {latestPrice.getGoodsId(), latestPrice.getCustomerId(), latestPrice.getType(), latestPrice.getBarcodeType(), latestPrice.getLastPrice()});
                return 1;
            }
        });
    }

    public int update(final LatestPrice latestPrice) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                conn.execSQL("update latestPrice set LastPrice = ? where GoodsID = ? and CustomerID = ? and Type = ? and BarcodeType = ?", new Object[] {latestPrice.getLastPrice(), latestPrice.getGoodsId(), latestPrice.getCustomerId(), latestPrice.getType(), latestPrice.getBarcodeType()});
                return 1;
            }
        });
    }

}
