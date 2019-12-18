package com.fuxi.util;

import java.util.Map;
import com.fuxi.vo.Customer;

/**
 * Title: LoginParameterUtil Description: 获取登录参数公共类
 * 
 * @author LYJ
 * 
 */
public class LoginParameterUtil {

    // 登录后的Customer用于存储登录参数
    public static Customer customer;
    // 用户ID
    public static String userId;
    // 登录IP
    public static String onLineId;
    // 部门名称
    public static String deptId;
    // 部门编号
    public static String deptName;
    // RelationMovein
    public static String relationMovein;
    // sysParam
    public static String corpName;
    // sysName
    public static String regId;
    // 服务端图片存储路径
    public static String imagePath;
    // 服务端默认打印模板
    public static String printTemplate;
    // 部门是否含有仓位
    public static boolean hasStorage;
    // 销售订单是否显示库存总数
    public static boolean queryStockTotal;
    // 判断用户是否可以修改单价
    public static boolean unitPricePermitFlag;
    // 判断用户是否可以修改折扣(后台)
    public static boolean discountRatePermitFlag2;
    // 判断用户是否使用品牌权限
    public static boolean useBrandPower;
    // 是否使用离线版本
    public static boolean online;
    // 登录部门是否仓库
    public static boolean isWarehouse;
    // 是否启用订货功能
    public static boolean usingOrderGoodsModule;
    // 判断用户是否可以修改折扣(前台)
    public static boolean discountRatePermitFlag;
    // 最小货品折扣(前台)
    public static double discountRate;
    
    //2019-03-14 装箱单明细，不允许修改数量
    public static boolean EditQty;
    
    
    

    // 货品资料操作权限
    public static Map<String, Boolean> goodsRightMap;
    // 客户资料操作权限
    public static Map<String, Boolean> customerRightMap;
    // 销售订单操作权限
    public static Map<String, Boolean> salesOrderRightMap;
    // 销售发货单操作权限
    public static Map<String, Boolean> salesRightMap;
    // 销售退货单操作权限
    public static Map<String, Boolean> salesReturnRightMap;

    //采购订单操作权限
    public static Map<String,Boolean> purchaseOrderRightMap;

    // 采购收货单操作权限
    public static Map<String, Boolean> purchaseRightMap;
    // 采购退货单操作权限
    public static Map<String, Boolean> purchaseReturnRightMap;
    // 转仓单操作权限
    public static Map<String, Boolean> stockMoveRightMap;
    // 盘点单操作权限
    public static Map<String, Boolean> stocktakingRightMap;
    // 进仓单操作权限
    public static Map<String, Boolean> stockInRightMap;
    // 出仓单操作权限
    public static Map<String, Boolean> stockOutRightMap;
    // 库存查询操作权限
    public static Map<String, Boolean> stocktakingQueryRightMap;
    // 销售小票操作权限
    public static Map<String, Boolean> posSalesRightMap;
    // 装箱单操作权限
    public static Map<String, Boolean> packingBoxRightMap;
    // 赠品单操作权限
    public static Map<String, Boolean> giftMenuRight;
    // 日结操作权限
    public static Map<String, Boolean> dailyKnotsMenuRight;

    // 货品资料字段权限(参考进价)
    public static boolean purchasePriceRight;
    // 货品资料字段权限(货品子类别)
    public static boolean subTypeRight;
    // 货品资料字段权限(品牌)
    public static boolean brandRight;
    // 货品资料字段权限(系列)
    public static boolean brandSerialRight;
    // 货品资料字段权限(性质)
    public static boolean kindRight;
    // 货品资料字段权限(年份)
    public static boolean ageRight;
    // 货品资料字段权限(季节)
    public static boolean seasonRight;
    // 货品资料字段权限(厂商)
    public static boolean supplierRight;
    // 货品资料字段权限(厂商货品编码)
    public static boolean supplierCodeRight;
    // 货品资料字段权限(零售价)
    public static boolean retailSalesRight;
    // 货品资料字段权限(零售价2)
    public static boolean retailSales1Right;
    // 货品资料字段权限(零售价3)
    public static boolean retailSales2Right;
    // 货品资料字段权限(零售价4)
    public static boolean retailSales3Right;
    // 货品资料字段权限(批发价)
    public static boolean tradePriceRight;
    // 货品资料字段权限(批发价2)
    public static boolean salesPrice1Right;
    // 货品资料字段权限(批发价3)
    public static boolean salesPrice2Right;
    // 货品资料字段权限(批发价4)
    public static boolean salesPrice3Right;
    // 货品资料字段权限(首采日期)
    public static boolean purchasedDateRight;
    // 货品资料字段权限(末采日期)
    public static boolean lastPurchasedDateRight;

    // 厂商资料字段权限(厂商电话)
    public static boolean supplierPhoneRight;

    // 采购收货单金额合计
    public static boolean purchaseAmountSumRight;
    // 采购收货单子表单价
    public static boolean purchaseUnitPriceRight;
    // 采购退货单金额合计
    public static boolean purchaseReturnAmountSumRight;
    // 采购收货单子表单价
    public static boolean purchaseReturnUnitPriceRight;
    // 销售发货单金额合计
    public static boolean salesAmountSumRight;
    // 销售发货单子表单价
    public static boolean salesUnitPriceRight;
    // 销售退货单金额合计
    public static boolean salesReturnAmountSumRight;
    // 销售收货单子表单价
    public static boolean salesReturnUnitPriceRight;
    // 销售订单金额合计
    public static boolean salesOrderAmountSumRight;
    // 销售订单子表单价
    public static boolean salesOrderUnitPriceRight;
    // 转仓单金额合计
    public static boolean stockMoveAmountSumRight;
    // 转仓单子表单价
    public static boolean stockMoveUnitPriceRight;
    // 盘点单金额合计
    public static boolean stocktakingAmountSumRight;
    // 进仓单金额合计
    public static boolean stockInAmountSumRight;
    // 进仓单子表单价
    public static boolean stockInUnitPriceRight;
    // 出仓单金额合计
    public static boolean stockOutAmountSumRight;
    // 出仓单子表单价
    public static boolean stockOutUnitPriceRight;

    // 系统设置参数
    // ==== 基本参数 ====//
    public static boolean useGoodsboxBarcodeInStocktaking; // 盘点单增加箱条码
    public static boolean useSupplierCodeToAreaProtection; // 根据厂商货品编码校验地区重版
    public static boolean updateToDoc; // 设置条码校验的时候是否以校验结果为准覆盖原来的单据
    public static boolean notAllow; // 设置条码校验的时候是否大于或非单据货品不允许录入
    public static boolean useDefSupplier; // 使用货品中的默认厂商
    public static boolean barcodeWarningTone; // 条码扫描错误时使用提示音
    public static boolean useAreaProtection; // 销售订单和发货单使用区域保护
    public static boolean useSingleDiscount; // 销售订单和发货单使用整单折扣
    public static boolean displayInventory; // 销售订单和发货单长按显示库存
    public static boolean preciseToQueryStock; // 库存查询不忽略颜色和尺码
    public static boolean firstInputOfGoodsCode; // 销售订单优先使用货号录入
    public static boolean notUseNegativeInventoryCheck; // 单据是否取消负库存检查
    public static boolean showGiftMenuFlag; // 是否显示赠品单功能

    // 条码打印
    public static boolean showGoodsCode; // 是否显示货品编码
    public static boolean showGoodsName; // 是否显示货品名称
    public static boolean showColor; // 是否显示颜色
    public static boolean showSize; // 是否显示尺码
    public static boolean showRetailSales;; // 是否显示零售价

    // 小票打印参数
    public static String possalesTile; // 小票抬头品牌
    public static String possalesParam1; // 小票抬头品牌标语
    public static String possalesParam2; // 小票抬头欢迎语
    public static String possalesParam3; // 小票结尾
    public static String possalesParam4; // 小票结尾
    public static String possalesParam5; // 小票结尾


    // 装箱单设置参数
    public static int packingBoxRelationType;

    // 多货品颜色尺码录入设置
    public static int goodsMultiSelectType;
    // 多货品颜色尺码录入方式设置
    public static int multiSelectType;

    // 仓位管理的参数
    public static boolean autoSave;
    public static boolean useLastTimePosition;
    public static int storageOutType;

    // ==== 单据列表参数 ====//
    // 销售订单
    public static boolean salesOrderEmployee;
    public static boolean salesOrderCustomer;
    public static boolean salesOrderBrand;
    public static boolean salesOrderMemo;
    public static boolean salesOrderDepartment; // 订货部门
    public static boolean salesOrderWarehouse; // 发货部门
    public static boolean salesOrderAmountSum;

    // 销售发货单
    public static boolean salesEmployee;
    public static boolean salesCustomer;
    public static boolean salesBrand;
    public static boolean salesMemo;
    public static boolean salesOrderNo; // 订单单号
    public static boolean salesWarehouse; // 发货部门
    public static boolean salesAmountSum;

    // 销售退货单
    public static boolean salesReturnEmployee;
    public static boolean salesReturnCustomer;
    public static boolean salesReturnBrand;
    public static boolean salesReturnMemo;
    public static boolean salesReturnWarehouse; // 退货部门
    public static boolean salesReturnDepartment; // 收货部门
    public static boolean salesReturnAmountSum;

    // 采购收货单
    public static boolean purchaseEmployee;
    public static boolean purchaseSupplier;
    public static boolean purchaseBrand;
    public static boolean purchaseMemo;
    public static boolean purchaseDepartment; // 收货部门
    public static boolean purchaseAmountSum;

    // 采购退货单
    public static boolean purchaseReturnEmployee;
    public static boolean purchaseReturnSupplier;
    public static boolean purchaseReturnBrand;
    public static boolean purchaseReturnMemo;
    public static boolean purchaseReturnDepartment; // 退货部门
    public static boolean purchaseReturnAmountSum;

    // 转仓单
    public static boolean stockMoveEmployee;
    public static boolean stockMoveMemo;
    public static boolean stockMoveWarehouseIn; // 转进仓库
    public static boolean stockMoveWarehouseOut; // 转出仓库
    public static boolean stockMoveBrand;
    public static boolean stockMoveAmountSum;

    // 进仓单
    public static boolean stockInEmployee;
    public static boolean stockInMemo;
    public static boolean stockInWarehouse; // 仓库名称
    public static boolean stockInWarehouseOut; // 转出仓库
    public static boolean stockInBrand;
    public static boolean stockInRelationNo; // 对应单号
    public static boolean stockInAmountSum;

    // 出仓单
    public static boolean stockOutEmployee;
    public static boolean stockOutMemo;
    public static boolean stockOutWarehouse; // 仓库名称
    public static boolean stockOutWarehouseIn; // 转进仓库
    public static boolean stockOutBrand;
    public static boolean stockOutRelationNo; // 对应单号
    public static boolean stockOutAmountSum;

    // 盘点单
    public static boolean stocktakingEmployee;
    public static boolean stocktakingMemo;
    public static boolean stocktakingWarehouse;
    public static boolean stocktakingBrand;
    public static boolean stocktakingAmountSum;

}
