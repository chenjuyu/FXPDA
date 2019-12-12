package com.fuxi.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库释放资源
 * 
 */
public class DBUtil {
    public static void Release(Cursor cursor) {
        Release(null, cursor);
    }

    public static void Release(SQLiteDatabase conn) {
        Release(conn, null);
    }

    public static void Release(SQLiteDatabase conn, Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            cursor = null;
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            conn = null;
        }
    }

    /**
     * 通过Cursor转换成对应的VO。注意：Cursor里的字段名（可用别名）必须要和VO的属性名一致
     * 
     */
    @SuppressWarnings({"rawtypes", "unused"})
    private static Object cursor2VO(Cursor c, Class clazz) {
        if (c == null) {
            return null;
        }
        Object obj;
        int i = 1;
        try {
            c.moveToNext();
            obj = setValues2Fields(c, clazz);

            return obj;
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("ERROR @：cursor2VO");
            return null;
        } finally {
            c.close();
        }
    }

    /**
     * 通过Cursor转换成对应的VO集合。注意：Cursor里的字段名（可用别名）必须要和VO的属性名一致
     * 
     * @param c
     * @param clazz
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List cursor2VOList(Cursor c, Class clazz) {
        if (c == null) {
            return null;
        }
        List list = new LinkedList();
        Object obj;
        try {
            while (c.moveToNext()) {
                obj = setValues2Fields(c, clazz);
                list.add(obj);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR @：cursor2VOList");
            return null;
        } finally {
            c.close();
        }
    }

    /**
     * 把值设置进类属性里
     * 
     * @param columnNames
     * @param fields
     * @param c
     * @param obj
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private static Object setValues2Fields(Cursor c, Class clazz) throws Exception {
        String[] columnNames = c.getColumnNames();// 字段数组
        Object obj = clazz.newInstance();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field _field : fields) {
            Class<? extends Object> typeClass = _field.getType();// 属性类型
            for (int j = 0; j < columnNames.length; j++) {
                String columnName = columnNames[j];
                typeClass = getBasicClass(typeClass);
                boolean isBasicType = isBasicType(typeClass);

                if (isBasicType) {
                    if (columnName.equalsIgnoreCase(_field.getName())) {// 是基本类型
                        String _str = c.getString(c.getColumnIndex(columnName));
                        if (_str == null) {
                            break;
                        }
                        String name = _field.getName();// 获取属性的名字
                        name = name.substring(0, 1).toUpperCase() + name.substring(1); // 将属性的首字符大写，方便构造get，set方法
                        _str = _str == null ? "" : _str;
                        Constructor<? extends Object> cons = typeClass.getConstructor(String.class);
                        Object attribute = cons.newInstance(_str);
                        _field.setAccessible(true);
                        _field.set(obj, attribute);
                        break;
                    }
                } else {
                    Object obj2 = setValues2Fields(c, typeClass);// 递归
                    _field.set(obj, obj2);
                    break;
                }

            }
        }
        return obj;
    }

    /**
     * 判断是不是基本类型
     * 
     * @param typeClass
     * @return
     */
    @SuppressWarnings("rawtypes")
    private static boolean isBasicType(Class typeClass) {
        if (typeClass.equals(Integer.class) || typeClass.equals(Long.class) || typeClass.equals(Float.class) || typeClass.equals(Double.class) || typeClass.equals(Boolean.class) || typeClass.equals(Byte.class) || typeClass.equals(Short.class) || typeClass.equals(String.class)
                || typeClass.equals(BigDecimal.class)) {

            return true;

        } else {
            return false;
        }
    }

    /**
     * 获得包装类
     * 
     * @param typeClass
     * @return
     */
    @SuppressWarnings("all")
    public static Class<? extends Object> getBasicClass(Class typeClass) {
        Class _class = basicMap.get(typeClass);
        if (_class == null)
            _class = typeClass;
        return _class;
    }

    @SuppressWarnings("rawtypes")
    private static Map<Class, Class> basicMap = new HashMap<Class, Class>();
    static {
        basicMap.put(int.class, Integer.class);
        basicMap.put(long.class, Long.class);
        basicMap.put(float.class, Float.class);
        basicMap.put(double.class, Double.class);
        basicMap.put(boolean.class, Boolean.class);
        basicMap.put(byte.class, Byte.class);
        basicMap.put(short.class, Short.class);
        basicMap.put(BigDecimal.class, BigDecimal.class);
    }

    public static ContentValues vo2CV(Object model) {
        Field[] field = model.getClass().getDeclaredFields();
        ContentValues cv = new ContentValues();
        for (int j = 0; j < field.length; j++) {// 遍历所有属性
            try {
                String name = field[j].getName();// 获取属性的名字
                name = name.substring(0, 1).toUpperCase() + name.substring(1); // 将属性的首字符大写，方便构造get，set方法
                String type = field[j].getGenericType().toString(); // 获取属性的类型
                Method m = model.getClass().getMethod("get" + name);
                String value = m.invoke(model) == null ? null : m.invoke(model).toString(); // 调用getter方法获取属性值
                if (value != null) {
                    cv.put(name, value);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }

        return cv;
    }

}
