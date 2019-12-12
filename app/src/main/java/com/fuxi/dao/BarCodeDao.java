package com.fuxi.dao;

import java.math.BigDecimal;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fuxi.vo.BarCode;

/**
 * Title: BarCodeDao Description: 货品条码,货品箱条码DAO
 * 
 * @author LJ,LYJ
 * 
 */
public class BarCodeDao extends BaseDao {
    private static final String TABLE = "BarCode";

    public BarCodeDao(Context context) {
        super(context);
    }

    public BarCode find(final String barcode, final String customerId, final String type) {
        return callBack(TYPE_READ, new DaoCallBack<BarCode>() {
            @Override
            public BarCode invoke(SQLiteDatabase conn) {
           /*  //因为缓存造成条码，扫出的结果与改的对不上，所以所有的条码解析都从服务器拿  邓飞客户问题修改 2019.11.28 改
            	cursor = conn.query(TABLE, null, " BarCode = ? and CustomerId = ? and Type = ?", new String[] {barcode.toLowerCase(), customerId, type}, null, null, null);
                while (cursor.moveToNext()) {
                    BarCode barcode = new BarCode();
                    barcode.setBarcode(cursor.getString(cursor.getColumnIndex("BarCode")));
                    barcode.setGoodsid(cursor.getString(cursor.getColumnIndex("GoodsID")));
                    barcode.setGoodscode(cursor.getString(cursor.getColumnIndex("GoodsCode")));
                    barcode.setGoodsname(cursor.getString(cursor.getColumnIndex("GoodsName")));
                    barcode.setColorcode(cursor.getString(cursor.getColumnIndex("ColorCode")));
                    barcode.setColorname(cursor.getString(cursor.getColumnIndex("ColorName")));
                    barcode.setColorid(cursor.getString(cursor.getColumnIndex("ColorID")));
                    barcode.setSizeid(cursor.getString(cursor.getColumnIndex("SizeID")));
                    barcode.setSizename(cursor.getString(cursor.getColumnIndex("SizeName")));
                    barcode.setSizecode(cursor.getString(cursor.getColumnIndex("SizeCode")));
                    barcode.setIndexno(cursor.getInt(cursor.getColumnIndex("IndexNo")));
                    barcode.setType(cursor.getString(cursor.getColumnIndex("Type")));
                    barcode.setCustomerId(cursor.getString(cursor.getColumnIndex("CustomerId")));
                    barcode.setRetailSales(new BigDecimal(cursor.getDouble(cursor.getColumnIndex("RetailSales"))));
                    barcode.setUnitPrice(new BigDecimal(cursor.getDouble(cursor.getColumnIndex("UnitPrice"))));
                    barcode.setDiscountPrice(new BigDecimal(cursor.getDouble(cursor.getColumnIndex("DiscountPrice"))));
                    barcode.setSizeGroupId(cursor.getString(cursor.getColumnIndex("SizeGroupID")));
                    return barcode;
                }
                  */                
                return null;
            }
        });
    }

    public String findBarCode(final String barcode, final String customerId, final String type) {
        return callBack(TYPE_READ, new DaoCallBack<String>() {
            @Override
            public String invoke(SQLiteDatabase conn) {
                cursor = conn.query(TABLE, null, " BarCode = ? and CustomerId = ? and Type = ?", new String[] {barcode.toLowerCase(), customerId, type}, null, null, null);
                while (cursor.moveToNext()) {
                    return cursor.getString(cursor.getColumnIndex("BarCode"));
                }
                return null;
            }
        });
    }

    public int insert(final BarCode barcode) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
               /* conn.execSQL("insert into BarCode(BarCode,GoodsID,GoodsCode,GoodsName,ColorCode,ColorName,ColorID,SizeID,SizeName,SizeCode,SizeGroupID,IndexNo,RetailSales,CustomerId,Type,UnitPrice,DiscountPrice) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[] {
                        barcode.getBarcode().toLowerCase(), barcode.getGoodsid(), barcode.getGoodscode(), barcode.getGoodsname(), barcode.getColorcode(), barcode.getColorname(), barcode.getColorid(), barcode.getSizeid(), barcode.getSizename(), barcode.getSizecode(), barcode.getSizeGroupId(),
                        barcode.getIndexno(), barcode.getRetailSales(), barcode.getCustomerId(), barcode.getType(), barcode.getUnitPrice(), barcode.getDiscountPrice()});
                return 1; */
            	return 0;  //暂不写数据库了
            }
        });
    }

    public int update(final BigDecimal discountPrice, final String barcode, final String customerId, final String type) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                conn.execSQL("update BarCode set DiscountPrice = ? where GoodsID = ? and CustomerId = ? and Type = ?", new Object[] {discountPrice, barcode.toLowerCase(), customerId, type});
                return 1;
            }
        });
    }

}
