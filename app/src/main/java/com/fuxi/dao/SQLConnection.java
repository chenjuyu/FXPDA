package com.fuxi.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.fuxi.util.Logger;

/**
 * Title: SQLConnection Description: SQLite数据库(创建,升级,初始化)
 * 
 * @author LYJ
 * 
 */
public class SQLConnection extends SQLiteOpenHelper {

    private static final String TAG = "SQLConnection";
    public static final String DATABASE_NAME = "fuxipda.db"; // 数据库名称
    public static final int DATABASE_VERSION = 18;// 数据库版本

    public SQLConnection(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /**
         * 参数表
         */
        String param = "create table paramer (id integer primary key autoincrement, name varchar(200), value varchar(200) )";
        db.execSQL(param);
        // 条码表
        String barcode =
                "create table barcode(GoodsID varchar(50),ColorID varchar(50),SizeID varchar(50)," + "BarCode varchar(50),GoodsName varchar(100),GoodsCode varchar(100),ColorName varchar(100),ColorCode varchar(100)"
                        + ",SizeName varchar(100),SizeCode varchar(100),SizeGroupID varchar(100),IndexNo int,RetailSales money,UnitPrice money,PrimaryUnitPrice money,DiscountPrice money,CustomerId varchar(50),Type varchar(50))";
        db.execSQL(barcode);
        // 保存新增或修改的货品图片
        String images = "create table images(id integer primary key autoincrement,GoodsCode varchar(50),MadeDate varchar(20))";
        db.execSQL(images);
        // 保存移位信息
        String stockRemove =
                " create table stockRemove(id integer primary key autoincrement,DeptID varchar(50),DeptName varchar(50), Barcode varchar(50),SupplierCode varchar(50)," + "GoodsCode varchar(50),GoodsName varchar(50),Color varchar(50),Size varchar(50),Storage varchar(50),"
                        + "StorageID varchar(50),GoodsID varchar(50),ColorID varchar(50),SizeID varchar(50),Quantity int) ";
        db.execSQL(stockRemove);
        // 保存客户订货的货品的最近一次价格
        String latestPrice = " create table latestPrice(id integer primary key autoincrement,GoodsID varchar(50),CustomerID varchar(50),Type varchar(50),BarcodeType varchar(20),LastPrice money) ";
        db.execSQL(latestPrice);
        // 保存用户登录时验证的设备信息
        String register = " create table register(id integer primary key autoincrement,Secret varchar(100),Type int,MadeDate varchar(50)) ";
        db.execSQL(register);
        // 保存箱条码的扫描记录
        String goodsBoxBarcodeRecord = " create table goodsBoxBarcodeRecord(id integer primary key autoincrement,BillID varchar(50),GoodsBoxBarcode varchar(50),SizeStr varchar(50),GoodsID varchar(50),ColorID varchar(50),BoxQty int) ";
        db.execSQL(goodsBoxBarcodeRecord);
        // 保存箱条码的扫描记录
        String outLineStocktaking = " create table outLineStocktaking(id integer primary key autoincrement,No varchar(100),ShelvesNo varchar(50),DepartmentCode varchar(50),DepartmentName varchar(100),DepartmentID varchar(50),Barcode varchar(60),Quantity int,MadeDate varchar(50)) ";
        db.execSQL(outLineStocktaking);
        // 生成装箱单箱号
        String generalPackingBoxNo = " create table generalPackingBoxNo(id integer primary key autoincrement,BoxNo varchar(100),CustomerID varchar(60),MadeDate varchar(50),Type int) ";
        db.execSQL(generalPackingBoxNo);
        // 部门信息表(用于离线盘点时使用)
        String department = " create table department(id integer primary key autoincrement,DepartmentID varchar(100),Department varchar(100),MustExistsGoodsFlag int,DepartmentCode varchar(100),MadeDate varchar(50)) ";
        db.execSQL(department);
        intData(db);
        Logger.d(TAG, param);
        // 若不是第一个版本安装，直接执行数据库升级
        // 请不要修改FIRST_DATABASE_VERSION的值，其为第一个数据库版本大小
        final int FIRST_DATABASE_VERSION = 5;
        onUpgrade(db, FIRST_DATABASE_VERSION, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // 删除原来的参数表
            db.execSQL("delete from paramer;");
            // 新增参数
            if (!paramIsExist(db, "url")) {
                db.execSQL("insert into paramer(name,value) values ('url','http://192.168.1.110:8888');");
            }
            if (!paramIsExist(db, "useLastPrice")) {
                db.execSQL("insert into paramer(name,value) values ('useLastPrice','false');");
            }
            if (!paramIsExist(db, "useAreaProtection")) {
                db.execSQL("insert into paramer(name,value) values ('useAreaProtection','false');");
            }
            if (!paramIsExist(db, "coverDoc")) {
                db.execSQL("insert into paramer(name,value) values ('coverDoc','false');");
            }
            if (!paramIsExist(db, "notAllow")) {
                db.execSQL("insert into paramer(name,value) values ('notAllow','false');");
            }
            if (!paramIsExist(db, "useDefSupplier")) {
                db.execSQL("insert into paramer(name,value) values ('useDefSupplier','false');");
            }
            if (!paramIsExist(db, "barcodeWarningTone")) {
                db.execSQL("insert into paramer(name,value) values ('barcodeWarningTone','true');");
            }
            if (!paramIsExist(db, "useGoodsboxBarcodeInStocktaking")) {
                db.execSQL("insert into paramer(name,value) values ('useGoodsboxBarcodeInStocktaking','false');");
            }
            if (!paramIsExist(db, "salesOrderCustomer")) {
                db.execSQL("insert into paramer(name,value) values ('salesOrderCustomer','true');");
            }
            if (!paramIsExist(db, "salesOrderBrand")) {
                db.execSQL("insert into paramer(name,value) values ('salesOrderBrand','false');");
            }
            if (!paramIsExist(db, "salesOrderMemo")) {
                db.execSQL("insert into paramer(name,value) values ('salesOrderMemo','false');");
            }
            if (!paramIsExist(db, "salesOrderDepartment")) {
                db.execSQL("insert into paramer(name,value) values ('salesOrderDepartment','true');");
            }
            if (!paramIsExist(db, "salesOrderWarehouse")) {
                db.execSQL("insert into paramer(name,value) values ('salesOrderWarehouse','false');");
            }
            if (!paramIsExist(db, "salesEmployee")) {
                db.execSQL("insert into paramer(name,value) values ('salesEmployee','false');");
            }
            if (!paramIsExist(db, "salesCustomer")) {
                db.execSQL("insert into paramer(name,value) values ('salesCustomer','true');");
            }
            if (!paramIsExist(db, "salesBrand")) {
                db.execSQL("insert into paramer(name,value) values ('salesBrand','false');");
            }
            if (!paramIsExist(db, "salesMemo")) {
                db.execSQL("insert into paramer(name,value) values ('salesMemo','false');");
            }
            if (!paramIsExist(db, "salesOrderNo")) {
                db.execSQL("insert into paramer(name,value) values ('salesOrderNo','false');");
            }
            if (!paramIsExist(db, "salesWarehouse")) {
                db.execSQL("insert into paramer(name,value) values ('salesWarehouse','true');");
            }
            if (!paramIsExist(db, "salesReturnEmployee")) {
                db.execSQL("insert into paramer(name,value) values ('salesReturnEmployee','false');");
            }
            if (!paramIsExist(db, "salesReturnCustomer")) {
                db.execSQL("insert into paramer(name,value) values ('salesReturnCustomer','true');");
            }
            if (!paramIsExist(db, "salesReturnBrand")) {
                db.execSQL("insert into paramer(name,value) values ('salesReturnBrand','false');");
            }
            if (!paramIsExist(db, "salesReturnMemo")) {
                db.execSQL("insert into paramer(name,value) values ('salesReturnMemo','false');");
            }
            if (!paramIsExist(db, "salesReturnWarehouse")) {
                db.execSQL("insert into paramer(name,value) values ('salesReturnWarehouse','false');");
            }
            if (!paramIsExist(db, "salesReturnDepartment")) {
                db.execSQL("insert into paramer(name,value) values ('salesReturnDepartment','true');");
            }
            if (!paramIsExist(db, "purchaseEmployee")) {
                db.execSQL("insert into paramer(name,value) values ('purchaseEmployee','false');");
            }
            if (!paramIsExist(db, "purchaseSupplier")) {
                db.execSQL("insert into paramer(name,value) values ('purchaseSupplier','true');");
            }
            if (!paramIsExist(db, "purchaseBrand")) {
                db.execSQL("insert into paramer(name,value) values ('purchaseBrand','false');");
            }
            if (!paramIsExist(db, "purchaseMemo")) {
                db.execSQL("insert into paramer(name,value) values ('purchaseMemo','false');");
            }
            if (!paramIsExist(db, "purchaseDepartment")) {
                db.execSQL("insert into paramer(name,value) values ('purchaseDepartment','true');");
            }
            if (!paramIsExist(db, "purchaseReturnEmployee")) {
                db.execSQL("insert into paramer(name,value) values ('purchaseReturnEmployee','false');");
            }
            if (!paramIsExist(db, "purchaseReturnSupplier")) {
                db.execSQL("insert into paramer(name,value) values ('purchaseReturnSupplier','true');");
            }
            if (!paramIsExist(db, "purchaseReturnBrand")) {
                db.execSQL("insert into paramer(name,value) values ('purchaseReturnBrand','false');");
            }
            if (!paramIsExist(db, "purchaseReturnMemo")) {
                db.execSQL("insert into paramer(name,value) values ('purchaseReturnMemo','false');");
            }
            if (!paramIsExist(db, "purchaseReturnDepartment")) {
                db.execSQL("insert into paramer(name,value) values ('purchaseReturnDepartment','true');");
            }
            if (!paramIsExist(db, "stockMoveEmployee")) {
                db.execSQL("insert into paramer(name,value) values ('stockMoveEmployee','false');");
            }
            if (!paramIsExist(db, "stockMoveMemo")) {
                db.execSQL("insert into paramer(name,value) values ('stockMoveMemo','false');");
            }
            if (!paramIsExist(db, "stockMoveWarehouseIn")) {
                db.execSQL("insert into paramer(name,value) values ('stockMoveWarehouseIn','false');");
            }
            if (!paramIsExist(db, "stockMoveWarehouseOut")) {
                db.execSQL("insert into paramer(name,value) values ('stockMoveWarehouseOut','true');");
            }
            if (!paramIsExist(db, "stockMoveBrand")) {
                db.execSQL("insert into paramer(name,value) values ('stockMoveBrand','false');");
            }
            if (!paramIsExist(db, "stockInEmployee")) {
                db.execSQL("insert into paramer(name,value) values ('stockInEmployee','false');");
            }
            if (!paramIsExist(db, "stockInMemo")) {
                db.execSQL("insert into paramer(name,value) values ('stockInMemo','false');");
            }
            if (!paramIsExist(db, "stockInWarehouse")) {
                db.execSQL("insert into paramer(name,value) values ('stockInWarehouse','true');");
            }
            if (!paramIsExist(db, "stockInWarehouseOut")) {
                db.execSQL("insert into paramer(name,value) values ('stockInWarehouseOut','false');");
            }
            if (!paramIsExist(db, "stockInBrand")) {
                db.execSQL("insert into paramer(name,value) values ('stockInBrand','false');");
            }
            if (!paramIsExist(db, "stockInRelationNo")) {
                db.execSQL("insert into paramer(name,value) values ('stockInRelationNo','false');");
            }
            if (!paramIsExist(db, "stockOutEmployee")) {
                db.execSQL("insert into paramer(name,value) values ('stockOutEmployee','false');");
            }
            if (!paramIsExist(db, "stockOutMemo")) {
                db.execSQL("insert into paramer(name,value) values ('stockOutMemo','false');");
            }
            if (!paramIsExist(db, "stockOutWarehouse")) {
                db.execSQL("insert into paramer(name,value) values ('stockOutWarehouse','true');");
            }
            if (!paramIsExist(db, "stockOutWarehouseIn")) {
                db.execSQL("insert into paramer(name,value) values ('stockOutWarehouseIn','false');");
            }
            if (!paramIsExist(db, "stockOutBrand")) {
                db.execSQL("insert into paramer(name,value) values ('stockOutBrand','false');");
            }
            if (!paramIsExist(db, "stockOutRelationNo")) {
                db.execSQL("insert into paramer(name,value) values ('stockOutRelationNo','false');");
            }
            if (!paramIsExist(db, "stocktakingEmployee")) {
                db.execSQL("insert into paramer(name,value) values ('stocktakingEmployee','false');");
            }
            if (!paramIsExist(db, "stocktakingMemo")) {
                db.execSQL("insert into paramer(name,value) values ('stocktakingMemo','false');");
            }
            if (!paramIsExist(db, "stocktakingWarehouse")) {
                db.execSQL("insert into paramer(name,value) values ('stocktakingWarehouse','true');");
            }
            if (!paramIsExist(db, "stocktakingBrand")) {
                db.execSQL("insert into paramer(name,value) values ('stocktakingBrand','false');");
            }
            if (!paramIsExist(db, "stocktakingAmountSum")) {
                db.execSQL("insert into paramer(name,value) values ('stocktakingAmountSum','true');");
            }
            if (!paramIsExist(db, "stockOutAmountSum")) {
                db.execSQL("insert into paramer(name,value) values ('stockOutAmountSum','true');");
            }
            if (!paramIsExist(db, "stockInAmountSum")) {
                db.execSQL("insert into paramer(name,value) values ('stockInAmountSum','true');");
            }
            if (!paramIsExist(db, "stockMoveAmountSum")) {
                db.execSQL("insert into paramer(name,value) values ('stockMoveAmountSum','true');");
            }
            if (!paramIsExist(db, "purchaseReturnAmountSum")) {
                db.execSQL("insert into paramer(name,value) values ('purchaseReturnAmountSum','false');");
            }
            if (!paramIsExist(db, "purchaseAmountSum")) {
                db.execSQL("insert into paramer(name,value) values ('purchaseAmountSum','false');");
            }
            if (!paramIsExist(db, "salesReturnAmountSum")) {
                db.execSQL("insert into paramer(name,value) values ('salesReturnAmountSum','true');");
            }
            if (!paramIsExist(db, "salesAmountSum")) {
                db.execSQL("insert into paramer(name,value) values ('salesAmountSum','true');");
            }
            if (!paramIsExist(db, "salesOrderAmountSum")) {
                db.execSQL("insert into paramer(name,value) values ('salesOrderAmountSum','true');");
            }
            if (!paramIsExist(db, "packingBoxRelationType")) {
                db.execSQL("insert into paramer(name,value) values ('packingBoxRelationType','0');");
            }
            if (!paramIsExist(db, "autoSave")) {
                db.execSQL("insert into paramer(name,value) values ('autoSave','false');");
            }
            if (!paramIsExist(db, "useLastTimePosition")) {
                db.execSQL("insert into paramer(name,value) values ('useLastTimePosition','fasle');");
            }
            if (!paramIsExist(db, "storageOutType")) {
                db.execSQL("insert into paramer(name,value) values ('storageOutType','0');");
            }
            if (!paramIsExist(db, "goodsMultiSelectType")) {
                db.execSQL("insert into paramer(name,value) values ('goodsMultiSelectType','0');");
            }
            if (!paramIsExist(db, "multiSelectType")) {
                db.execSQL("insert into paramer(name,value) values ('multiSelectType','0');");
            }
            if (!paramIsExist(db, "useSingleDiscount")) {
                db.execSQL("insert into paramer(name,value) values ('useSingleDiscount','fasle');");
            }
            if (!paramIsExist(db, "displayInventory")) {
                db.execSQL("insert into paramer(name,value) values ('displayInventory','false');");
            }
            if (!paramIsExist(db, "preciseToQueryStock")) {
                db.execSQL("insert into paramer(name,value) values ('preciseToQueryStock','false');");
            }
            if (!paramIsExist(db, "firstInputOfGoodsCode")) {
                db.execSQL("insert into paramer(name,value) values ('firstInputOfGoodsCode','false');");
            }
            if (!paramIsExist(db, "useSupplierCodeToAreaProtection")) {
                db.execSQL("insert into paramer(name,value) values ('useSupplierCodeToAreaProtection','false');");
            }
            if (!paramIsExist(db, "notUseNegativeInventoryCheck")) {
                db.execSQL("insert into paramer(name,value) values ('notUseNegativeInventoryCheck','false');");
            }
            if (!paramIsExist(db, "rememberPassword")) {
                db.execSQL("insert into paramer(name,value) values ('rememberPassword','false');");
            }
            // 条码打印
            if (!paramIsExist(db, "showGoodsCode")) {
                db.execSQL("insert into paramer(name,value) values ('showGoodsCode','false');");
            }
            if (!paramIsExist(db, "showGoodsName")) {
                db.execSQL("insert into paramer(name,value) values ('showGoodsName','false');");
            }
            if (!paramIsExist(db, "showColor")) {
                db.execSQL("insert into paramer(name,value) values ('showColor','false');");
            }
            if (!paramIsExist(db, "showSize")) {
                db.execSQL("insert into paramer(name,value) values ('showSize','false');");
            }
            if (!paramIsExist(db, "showRetailSales")) {
                db.execSQL("insert into paramer(name,value) values ('showRetailSales','false');");
            }
            if (!paramIsExist(db, "printBarcodeHeight")) {
                db.execSQL("insert into paramer(name,value) values ('printBarcodeHeight','100');");
            }
            if (!paramIsExist(db, "printBarcodeWidth")) {
                db.execSQL("insert into paramer(name,value) values ('printBarcodeWidth','300');");
            }
            if (!paramIsExist(db, "printBarcodeType")) {
                db.execSQL("insert into paramer(name,value) values ('printBarcodeType','CODE_93');");
            }
            // 新增表
            if (!tabbleIsExist(db, "outLineStocktaking")) {
                String outLineStocktaking = " create table outLineStocktaking(id integer primary key autoincrement,No varchar(100),ShelvesNo varchar(50),DepartmentCode varchar(50),DepartmentName varchar(100),DepartmentID varchar(50),Barcode varchar(60),Quantity int,MadeDate varchar(50)) ";
                db.execSQL(outLineStocktaking);
            }
            if (!tabbleIsExist(db, "generalPackingBoxNo")) {
                // 生成装箱单箱号
                String generalPackingBoxNo = " create table generalPackingBoxNo(id integer primary key autoincrement,BoxNo varchar(100),CustomerID varchar(60),MadeDate varchar(50),Type int) ";
                db.execSQL(generalPackingBoxNo);
            }
            if (!tabbleIsExist(db, "department")) {
                String department = " create table department(id integer primary key autoincrement,DepartmentID varchar(100),Department varchar(100),MustExistsGoodsFlag int,DepartmentCode varchar(100),MadeDate varchar(50)) ";
                db.execSQL(department);
            }
            // 添加列
            if (!checkColumnExists(db, "generalPackingBoxNo", "Type")) {
                db.execSQL("alter table generalPackingBoxNo add column Type int;");
            }
            if (!checkColumnExists(db, "register", "MadeDate")) {
                db.execSQL("alter table register add column MadeDate varchar(50);");
            }
        }
    }

    private void intData(SQLiteDatabase db) {
        db.execSQL("insert into paramer(name,value) values ('url','http://192.168.1.110:8888');");
        // 默认不选中使用最近价格
        db.execSQL("insert into paramer(name,value) values ('useLastPrice','false');");
        db.execSQL("insert into paramer(name,value) values ('useAreaProtection','false');");
        db.execSQL("insert into paramer(name,value) values ('coverDoc','false');");
        db.execSQL("insert into paramer(name,value) values ('notAllow','false');");
        db.execSQL("insert into paramer(name,value) values ('useDefSupplier','false');");
        db.execSQL("insert into paramer(name,value) values ('barcodeWarningTone','true');");
        db.execSQL("insert into paramer(name,value) values ('useGoodsboxBarcodeInStocktaking','false');");
        db.execSQL("insert into paramer(name,value) values ('salesOrderCustomer','true');");
        db.execSQL("insert into paramer(name,value) values ('salesOrderBrand','false');");
        db.execSQL("insert into paramer(name,value) values ('salesOrderMemo','false');");
        db.execSQL("insert into paramer(name,value) values ('salesOrderDepartment','true');");
        db.execSQL("insert into paramer(name,value) values ('salesOrderWarehouse','false');");
        db.execSQL("insert into paramer(name,value) values ('salesEmployee','false');");
        db.execSQL("insert into paramer(name,value) values ('salesCustomer','true');");
        db.execSQL("insert into paramer(name,value) values ('salesBrand','false');");
        db.execSQL("insert into paramer(name,value) values ('salesMemo','false');");
        db.execSQL("insert into paramer(name,value) values ('salesOrderNo','false');");
        db.execSQL("insert into paramer(name,value) values ('salesWarehouse','true');");
        db.execSQL("insert into paramer(name,value) values ('salesReturnEmployee','false');");
        db.execSQL("insert into paramer(name,value) values ('salesReturnCustomer','true');");
        db.execSQL("insert into paramer(name,value) values ('salesReturnBrand','false');");
        db.execSQL("insert into paramer(name,value) values ('salesReturnMemo','false');");
        db.execSQL("insert into paramer(name,value) values ('salesReturnWarehouse','false');");
        db.execSQL("insert into paramer(name,value) values ('salesReturnDepartment','true');");
        db.execSQL("insert into paramer(name,value) values ('purchaseEmployee','false');");
        db.execSQL("insert into paramer(name,value) values ('purchaseSupplier','true');");
        db.execSQL("insert into paramer(name,value) values ('purchaseBrand','false');");
        db.execSQL("insert into paramer(name,value) values ('purchaseMemo','false');");
        db.execSQL("insert into paramer(name,value) values ('purchaseDepartment','true');");
        db.execSQL("insert into paramer(name,value) values ('purchaseReturnEmployee','false');");
        db.execSQL("insert into paramer(name,value) values ('purchaseReturnSupplier','true');");
        db.execSQL("insert into paramer(name,value) values ('purchaseReturnBrand','false');");
        db.execSQL("insert into paramer(name,value) values ('purchaseReturnMemo','false');");
        db.execSQL("insert into paramer(name,value) values ('purchaseReturnDepartment','true');");
        db.execSQL("insert into paramer(name,value) values ('stockMoveEmployee','false');");
        db.execSQL("insert into paramer(name,value) values ('stockMoveMemo','false');");
        db.execSQL("insert into paramer(name,value) values ('stockMoveWarehouseIn','false');");
        db.execSQL("insert into paramer(name,value) values ('stockMoveWarehouseOut','true');");
        db.execSQL("insert into paramer(name,value) values ('stockMoveBrand','false');");
        db.execSQL("insert into paramer(name,value) values ('stockInEmployee','false');");
        db.execSQL("insert into paramer(name,value) values ('stockInMemo','false');");
        db.execSQL("insert into paramer(name,value) values ('stockInWarehouse','true');");
        db.execSQL("insert into paramer(name,value) values ('stockInWarehouseOut','false');");
        db.execSQL("insert into paramer(name,value) values ('stockInBrand','false');");
        db.execSQL("insert into paramer(name,value) values ('stockInRelationNo','false');");
        db.execSQL("insert into paramer(name,value) values ('stockOutEmployee','false');");
        db.execSQL("insert into paramer(name,value) values ('stockOutMemo','false');");
        db.execSQL("insert into paramer(name,value) values ('stockOutWarehouse','true');");
        db.execSQL("insert into paramer(name,value) values ('stockOutWarehouseIn','false');");
        db.execSQL("insert into paramer(name,value) values ('stockOutBrand','false');");
        db.execSQL("insert into paramer(name,value) values ('stockOutRelationNo','false');");
        db.execSQL("insert into paramer(name,value) values ('stocktakingEmployee','false');");
        db.execSQL("insert into paramer(name,value) values ('stocktakingMemo','false');");
        db.execSQL("insert into paramer(name,value) values ('stocktakingWarehouse','true');");
        db.execSQL("insert into paramer(name,value) values ('stocktakingBrand','false');");
        db.execSQL("insert into paramer(name,value) values ('stocktakingAmountSum','true');");
        db.execSQL("insert into paramer(name,value) values ('stockOutAmountSum','true');");
        db.execSQL("insert into paramer(name,value) values ('stockInAmountSum','true');");
        db.execSQL("insert into paramer(name,value) values ('stockMoveAmountSum','true');");
        db.execSQL("insert into paramer(name,value) values ('purchaseReturnAmountSum','false');");
        db.execSQL("insert into paramer(name,value) values ('purchaseAmountSum','false');");
        db.execSQL("insert into paramer(name,value) values ('salesReturnAmountSum','true');");
        db.execSQL("insert into paramer(name,value) values ('salesAmountSum','true');");
        db.execSQL("insert into paramer(name,value) values ('salesOrderAmountSum','true');");
        db.execSQL("insert into paramer(name,value) values ('rememberPassword','false');");
        // 装箱单设置
        db.execSQL("insert into paramer(name,value) values ('packingBoxRelationType','0');");
        // 多货品颜色尺码录入
        db.execSQL("insert into paramer(name,value) values ('goodsMultiSelectType','0');");
        // 多货品颜色尺码录入方式
        db.execSQL("insert into paramer(name,value) values ('multiSelectType','0');");
        // 仓位设置
        db.execSQL("insert into paramer(name,value) values ('autoSave','false');");
        db.execSQL("insert into paramer(name,value) values ('useLastTimePosition','fasle');");
        db.execSQL("insert into paramer(name,value) values ('storageOutType','0');");
        // 新增参数
        db.execSQL("insert into paramer(name,value) values ('useSingleDiscount','false');");
        db.execSQL("insert into paramer(name,value) values ('displayInventory','false');");
        db.execSQL("insert into paramer(name,value) values ('preciseToQueryStock','false');");
        db.execSQL("insert into paramer(name,value) values ('firstInputOfGoodsCode','false');");
        db.execSQL("insert into paramer(name,value) values ('useSupplierCodeToAreaProtection','false');");
        db.execSQL("insert into paramer(name,value) values ('notUseNegativeInventoryCheck','false');");
        // 条码打印
        db.execSQL("insert into paramer(name,value) values ('showGoodsCode','false');");
        db.execSQL("insert into paramer(name,value) values ('showGoodsName','false');");
        db.execSQL("insert into paramer(name,value) values ('showColor','false');");
        db.execSQL("insert into paramer(name,value) values ('showSize','false');");
        db.execSQL("insert into paramer(name,value) values ('showRetailSales','false');");
        db.execSQL("insert into paramer(name,value) values ('printBarcodeHeight','100');");
        db.execSQL("insert into paramer(name,value) values ('printBarcodeWidth','300');");
        db.execSQL("insert into paramer(name,value) values ('printBarcodeType','CODE_93');");

    }

    /**
     * 检查表是否存在
     * 
     * @param tableName
     * @return
     */
    public boolean tabbleIsExist(SQLiteDatabase db, String tableName) {
        boolean result = false;
        if (tableName == null) {
            return result;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tableName.trim() + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
            Logger.d(TAG, e.getMessage());
        }
        return result;
    }

    /**
     * 检查表是否存在
     * 
     * @param tableName
     * @return
     */
    public boolean paramIsExist(SQLiteDatabase db, String paramName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from paramer where name ='" + paramName.trim() + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
            Logger.d(TAG, e.getMessage());
        }
        return result;
    }

    /**
     * 检查表中某列是否存在
     * 
     * @param db
     * @param tableName 表名
     * @param columnName 列名
     * @return
     */
    public boolean checkColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from sqlite_master where name = ? and sql like ?", new String[] {tableName, "%" + columnName + "%"});
            result = null != cursor && cursor.moveToFirst();
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

}
