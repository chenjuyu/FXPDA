package com.fuxi.dao;

import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fuxi.util.DBUtil;
import com.fuxi.vo.SalesOrder;
import com.fuxi.vo.SalesOrderDetail;

/**
 * Title: SalesOrderDao Description: 销售订单DAO
 * 
 * @author LYJ
 * 
 */
public class SalesOrderDao extends BaseDao {

    private static final String TABLE = "SalesOrder";
    private static final String Detail = "SalesOrderDetail";

    public SalesOrderDao(Context context) {
        super(context);
    }

    public List<SalesOrder> list(final int page, final String madeby) {
        return callBack(TYPE_READ, new DaoCallBack<List<SalesOrder>>() {
            @Override
            public List<SalesOrder> invoke(SQLiteDatabase conn) {
                int index = (page - 1) * limit;
                String sql = "Select * from person limit ?,?";
                cursor = conn.rawQuery(sql, new String[] {(index) + "", limit + ""});
                if (cursor == null) {
                    return null;
                }
                List<SalesOrder> salesOrderList = DBUtil.cursor2VOList(cursor, SalesOrder.class);
                return salesOrderList;
            }
        });
    }

    public int insertOrder(final SalesOrder salesOrder) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                ContentValues cv = DBUtil.vo2CV(salesOrder);
                conn.insert(TABLE, null, cv);
                return 1;
            }
        });
    }

    public int updateDetail(final SalesOrderDetail salesOrderDetail) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                // 检查是否存在相同商品
                ContentValues cv = DBUtil.vo2CV(salesOrderDetail);
                conn.update(Detail, cv, "id=?", new String[salesOrderDetail.getID()]);
                return 1;
            }
        });
    }

    public int insertDetail(final SalesOrderDetail salesOrderDetail) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                ContentValues cv = DBUtil.vo2CV(salesOrderDetail);
                conn.insert(Detail, null, cv);
                return 1;
            }
        });
    }

    public List<SalesOrderDetail> listDetail(final String relationID) {
        return callBack(TYPE_READ, new DaoCallBack<List<SalesOrderDetail>>() {
            @Override
            public List<SalesOrderDetail> invoke(SQLiteDatabase conn) {
                StringBuffer sb = new StringBuffer();
                sb.append(" select * from  ").append(Detail).append(" where RelationID = '").append(relationID).append("'");
                cursor = conn.rawQuery(sb.toString(), null);
                List<SalesOrderDetail> rsList = DBUtil.cursor2VOList(cursor, SalesOrderDetail.class);
                return rsList;
            }
        });
    }

}
