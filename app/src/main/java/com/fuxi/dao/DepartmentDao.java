package com.fuxi.dao;

import java.util.List;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.fuxi.util.DBUtil;
import com.fuxi.vo.Department;

/**
 * Title: DepartmentDao Description: 部门DAO
 * 
 * @author LYJ
 * 
 */
public class DepartmentDao extends BaseDao {

    private static final String TABLE = "department";

    public DepartmentDao(Context context) {
        super(context);
    }

    public Department findFirst() {
        return callBack(TYPE_READ, new DaoCallBack<Department>() {
            @Override
            public Department invoke(SQLiteDatabase conn) {
                cursor = conn.query(TABLE, null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    Department department = new Department();
                    department.setDepartmentId(cursor.getString(cursor.getColumnIndex("DepartmentID")));
                    department.setDepartment(cursor.getString(cursor.getColumnIndex("Department")));
                    department.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    department.setMustExistsGoodsFlag(cursor.getInt(cursor.getColumnIndex("MustExistsGoodsFlag")));
                    department.setDepartmentCode(cursor.getString(cursor.getColumnIndex("DepartmentCode")));
                    department.setMadeDate(cursor.getString(cursor.getColumnIndex("MadeDate")));
                    return department;
                }
                return null;
            }
        });
    }

    public List<Department> getList(final String parma) {
        return callBack(TYPE_READ, new DaoCallBack<List<Department>>() {
            @Override
            public List<Department> invoke(SQLiteDatabase conn) {
                String sql = " select * from department where Department like ? or DepartmentCode like ? ";
                cursor = conn.rawQuery(sql, new String[] {"%" + parma + "%", "%" + parma + "%"});
                if (cursor == null) {
                    return null;
                }
                List<Department> departments = DBUtil.cursor2VOList(cursor, Department.class);
                return departments;
            }
        });
    }

    public int insert(final Department department) {
        return callBack(TYPE_WRITE, new DaoCallBack<Integer>() {
            @Override
            public Integer invoke(SQLiteDatabase conn) {
                conn.execSQL("insert into department(departmentID, department, mustExistsGoodsFlag, departmentCode, madeDate) values(?,?,?,?,?)",
                        new Object[] {department.getDepartmentId(), department.getDepartment(), department.getMustExistsGoodsFlag(), department.getDepartmentCode(), department.getMadeDate()});
                return 1;
            }
        });
    }

    /**
     * 删除表中的所有数据
     */
    public void deleteAll() {
        SQLiteDatabase db = sqlConnection.getWritableDatabase();
        db.execSQL("delete from " + TABLE);
    }


}
