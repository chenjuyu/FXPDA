package com.fuxi.dao;

import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fuxi.util.DBUtil;
import com.fuxi.vo.Images;

/**
 * Title: ImagesDao Description: 货品图片DAO
 * 
 * @author LYJ
 * 
 */
public class ImagesDao extends BaseDao {

    private static final String TABLE = "images";

    public ImagesDao(Context context) {
        super(context);
    }

    public List<Images> getList() {
        return callBack(TYPE_READ, new DaoCallBack<List<Images>>() {
            @Override
            public List<Images> invoke(SQLiteDatabase conn) {
                String sql = " select * from images ";
                cursor = conn.rawQuery(sql, null);
                if (cursor == null) {
                    return null;
                }
                List<Images> images = DBUtil.cursor2VOList(cursor, Images.class);
                return images;
            }
        });
    }

    public Images find(final String goodsCode) {
        return callBack(TYPE_READ, new DaoCallBack<Images>() {
            @Override
            public Images invoke(SQLiteDatabase conn) {
                cursor = conn.query(TABLE, null, " GoodsCode = ? ", new String[] {goodsCode}, null, null, null);
                if (cursor.moveToFirst() == false) {
                    return null;
                }
                Images images = new Images();
                images.setGoodsCode(cursor.getString(cursor.getColumnIndex("GoodsCode")));
                images.setMadeDate(cursor.getString(cursor.getColumnIndex("MadeDate")));
                return images;
            }
        });
    }

    public int update(final Images images) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                ContentValues values = new ContentValues();
                values.put("MadeDate", images.getMadeDate());
                conn.execSQL(" update images set MadeDate = ? where GoodsCode = ? ", new Object[] {images.getMadeDate(), images.getGoodsCode()});
                return 1;
            }
        });
    }

    public int insert(final Images images) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                conn.execSQL(" insert into images(GoodsCode,MadeDate) values(?,?) ", new Object[] {images.getGoodsCode(), images.getMadeDate()});
                return 1;
            }
        });
    }

    public int delete(final String goodsCode) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                return conn.delete(TABLE, " GoodsCode = ? ", new String[] {goodsCode});
            }
        });
    }

}
