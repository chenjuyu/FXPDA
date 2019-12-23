package com.fuxi.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.fuxi.dao.ParamerDao;
import com.fuxi.main.R;
import com.fuxi.util.AppManager;
import com.fuxi.util.InstallSlientManager;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.NetUtil;
import com.fuxi.util.ParamterFileUtil;
import com.fuxi.vo.Paramer;
import com.fuxi.vo.RequestVo;
import com.fuxi.vo.UpdataInfo;
import com.fuxi.widget.CheckBoxTextView;
import com.fuxi.widget.FontTextView;

/**
 * Title: SettingActivity Description: 系统设置活动界面
 * 
 * @author LYJ
 * 
 */
public class SettingActivity extends BaseWapperActivity implements OnCheckedChangeListener {

    private static final String TAG = "SettingActivity";
    private static final String updatePath = "/common.do?checkVersion";
    private static final String admin_verification = "/login.do?verificationAdmin";
    private static final int UPDATA_CLIENT = 1;
    private static final int GET_UNDATAINFO_ERROR = 0;
    private static final int DOWN_ERROR = -1;
    private static boolean forceUpdate = false;
    private static boolean innerVersion = false;
    private static String strIp;

    private JsToJava jsToJava = new JsToJava();
    private ParamerDao paramerDao = new ParamerDao(this);
    private UpdataInfo info = new UpdataInfo();
    private ProgressDialog pd = null;
    private AlertDialog alertDialog;

    private WebView mWebView;
    private CheckBoxTextView tv_BaseSetting;// 基本设置
    private CheckBoxTextView tv_InputSetting;// 录入设置
    private CheckBoxTextView tv_BarcodePrintSetting;// 条码打印
    private CheckBoxTextView tv_DocuSetting;// 单据设置
    private LinearLayout ll_goods_info;
    private LinearLayout ll_input_setting;
    private LinearLayout ll_docu_setting;
    private LinearLayout ll_barcode_print_setting;
    private LinearLayout ll_update; // 监测更新
    private LinearLayout ll_above; // 关于我们
    private CheckBox cb_useGoodsboxBarcodeInStocktaking;
    private CheckBox cb_useSupplierCodeToAreaProtection;
    private CheckBox cb_coverDoc;
    private CheckBox cb_notAllow;
    private CheckBox cb_useDefSupplier;
    private CheckBox cb_barcodeWarningTone;
    private CheckBox cb_useAreaProtection;
    private CheckBox cb_useSingleDiscount;
    private CheckBox cb_displayInventory;
    private CheckBox cb_preciseToQueryStock;
    private CheckBox cb_firstInputOfGoodsCode;
    private CheckBox cb_notUseNegativeInventoryCheck;

    // 销售订单
    private CheckBox cb_salesOrderEmployee;
    private CheckBox cb_salesOrderCustomer;
    private CheckBox cb_salesOrderBrand;
    private CheckBox cb_salesOrderMemo;
    private CheckBox cb_salesOrderDepartment; // 订货部门
    private CheckBox cb_salesOrderWarehouse; // 发货部门
    private CheckBox cb_salesOrderAmountSum;
    // 销售发货单
    private CheckBox cb_salesEmployee;
    private CheckBox cb_salesCustomer;
    private CheckBox cb_salesBrand;
    private CheckBox cb_salesMemo;
    private CheckBox cb_salesOrderNo; // 订单单号
    private CheckBox cb_salesWarehouse; // 发货部门
    private CheckBox cb_salesAmountSum;
    // 销售退货单
    private CheckBox cb_salesReturnEmployee;
    private CheckBox cb_salesReturnCustomer;
    private CheckBox cb_salesReturnBrand;
    private CheckBox cb_salesReturnMemo;
    private CheckBox cb_salesReturnWarehouse; // 退货部门
    private CheckBox cb_salesReturnDepartment; // 收货部门
    private CheckBox cb_salesReturnAmountSum;


    // 采购订货单
    private CheckBox cb_purchaseOrderEmployee;
    private CheckBox cb_purchaseOrderSupplier;
    private CheckBox cb_purchaseOrderBrand;
    private CheckBox cb_purchaseOrderMemo;
    private CheckBox cb_purchaseOrderDepartment; // 收货部门
    private CheckBox cb_purchaseOrderAmountSum;

    // 采购收货单
    private CheckBox cb_purchaseEmployee;
    private CheckBox cb_purchaseSupplier;
    private CheckBox cb_purchaseBrand;
    private CheckBox cb_purchaseMemo;
    private CheckBox cb_purchaseDepartment; // 收货部门
    private CheckBox cb_purchaseAmountSum;
    // 采购退货单
    private CheckBox cb_purchaseReturnEmployee;
    private CheckBox cb_purchaseReturnSupplier;
    private CheckBox cb_purchaseReturnBrand;
    private CheckBox cb_purchaseReturnMemo;
    private CheckBox cb_purchaseReturnDepartment; // 退货部门
    private CheckBox cb_purchaseReturnAmountSum;
    // 转仓单
    private CheckBox cb_stockMoveEmployee;
    private CheckBox cb_stockMoveMemo;
    private CheckBox cb_stockMoveWarehouseIn; // 转进仓库
    private CheckBox cb_stockMoveWarehouseOut; // 转出仓库
    private CheckBox cb_stockMoveBrand;
    private CheckBox cb_stockMoveAmountSum;
    // 进仓单
    private CheckBox cb_stockInEmployee;
    private CheckBox cb_stockInMemo;
    private CheckBox cb_stockInWarehouse; // 仓库名称
    private CheckBox cb_stockInWarehouseOut; // 转出仓库
    private CheckBox cb_stockInBrand;
    private CheckBox cb_stockInRelationNo; // 对应单号
    private CheckBox cb_stockInAmountSum;
    // 出仓单
    private CheckBox cb_stockOutEmployee;
    private CheckBox cb_stockOutMemo;
    private CheckBox cb_stockOutWarehouse; // 仓库名称
    private CheckBox cb_stockOutWarehouseIn; // 转进仓库
    private CheckBox cb_stockOutBrand;
    private CheckBox cb_stockOutRelationNo; // 对应单号
    private CheckBox cb_stockOutAmountSum;
    // 盘点单
    private CheckBox cb_stocktakingEmployee;
    private CheckBox cb_stocktakingMemo;
    private CheckBox cb_stocktakingWarehouse;
    private CheckBox cb_stocktakingBrand;
    private CheckBox cb_stocktakingAmountSum;

    // 条码打印
    private CheckBox cb_showGoodsCode;
    private CheckBox cb_showGoodsName;
    private CheckBox cb_showColor;
    private CheckBox cb_showSize;
    private CheckBox cb_showRetailSales;


    private TextView tv_useGoodsboxBarcodeInStocktaking;
    private TextView tv_useSupplierCodeToAreaProtection;
    private TextView tv_coverDoc;
    private TextView tv_notAllow;
    private TextView tv_useDefSupplier;
    private TextView tv_barcodeWarningTone;
    private TextView tv_useAreaProtection;
    private TextView tv_useSingleDiscount;
    private TextView tv_displayInventory;
    private TextView tv_preciseToQueryStock;
    private TextView tv_firstInputOfGoodsCode;
    private TextView tv_notUseNegativeInventoryCheck;

    // 销售订单
    private TextView tv_salesOrderEmployee;
    private TextView tv_salesOrderCustomer;
    private TextView tv_salesOrderBrand;
    private TextView tv_salesOrderMemo;
    private TextView tv_salesOrderDepartment; // 订货部门
    private TextView tv_salesOrderWarehouse; // 发货部门
    private TextView tv_salesOrderAmountSum;

    // 销售发货单
    private TextView tv_salesEmployee;
    private TextView tv_salesCustomer;
    private TextView tv_salesBrand;
    private TextView tv_salesMemo;
    private TextView tv_salesOrderNo; // 订单单号
    private TextView tv_salesWarehouse; // 发货部门
    private TextView tv_salesAmountSum;
    // 销售退货单
    private TextView tv_salesReturnEmployee;
    private TextView tv_salesReturnCustomer;
    private TextView tv_salesReturnBrand;
    private TextView tv_salesReturnMemo;
    private TextView tv_salesReturnWarehouse; // 退货部门
    private TextView tv_salesReturnDepartment; // 收货部门
    private TextView tv_salesReturnAmountSum;

    // 采购订货单
    private TextView tv_purchaseOrderEmployee;
    private TextView tv_purchaseOrderSupplier;
    private TextView tv_purchaseOrderBrand;
    private TextView tv_purchaseOrderMemo;
    private TextView tv_purchaseOrderDepartment; // 收货部门
    private TextView tv_purchaseOrderAmountSum;


    // 采购收货单
    private TextView tv_purchaseEmployee;
    private TextView tv_purchaseSupplier;
    private TextView tv_purchaseBrand;
    private TextView tv_purchaseMemo;
    private TextView tv_purchaseDepartment; // 收货部门
    private TextView tv_purchaseAmountSum;
    // 采购退货单
    private TextView tv_purchaseReturnEmployee;
    private TextView tv_purchaseReturnSupplier;
    private TextView tv_purchaseReturnBrand;
    private TextView tv_purchaseReturnMemo;
    private TextView tv_purchaseReturnDepartment; // 退货部门
    private TextView tv_purchaseReturnAmountSum;
    // 转仓单
    private TextView tv_stockMoveEmployee;
    private TextView tv_stockMoveMemo;
    private TextView tv_stockMoveWarehouseIn; // 转进仓库
    private TextView tv_stockMoveWarehouseOut; // 转出仓库
    private TextView tv_stockMoveBrand;
    private TextView tv_stockMoveAmountSum;
    // 进仓单
    private TextView tv_stockInEmployee;
    private TextView tv_stockInMemo;
    private TextView tv_stockInWarehouse; // 仓库名称
    private TextView tv_stockInWarehouseOut; // 转出仓库
    private TextView tv_stockInBrand;
    private TextView tv_stockInRelationNo; // 对应单号
    private TextView tv_stockInAmountSum;
    // 出仓单
    private TextView tv_stockOutEmployee;
    private TextView tv_stockOutMemo;
    private TextView tv_stockOutWarehouse; // 仓库名称
    private TextView tv_stockOutWarehouseIn; // 转进仓库
    private TextView tv_stockOutBrand;
    private TextView tv_stockOutRelationNo; // 对应单号
    private TextView tv_stockOutAmountSum;
    // 盘点单
    private TextView tv_stocktakingEmployee;
    private TextView tv_stocktakingMemo;
    private TextView tv_stocktakingWarehouse;
    private TextView tv_stocktakingBrand;
    private TextView tv_stocktakingAmountSum;

    // 条码打印
    private TextView tv_showGoodsCode;
    private TextView tv_showGoodsName;
    private TextView tv_showColor;
    private TextView tv_showSize;
    private TextView tv_showRetailSales;

    // 装箱单
    private RadioGroup rg_packingBoxGroup;
    private RadioButton rb_relationSales; // 关系销售发货单
    private RadioButton rb_relationStockOut; // 关系出仓单
    private TextView tv_relationSales;
    private TextView tv_relationStockOut;

    // 多货号颜色尺码录入
    private RadioGroup rg_goodsMultiSelectGroup;
    private RadioGroup rg_multiSelectTypeGroup;
    private RadioButton rb_commonSelect;
    private RadioButton rb_multiSelect;
    private RadioButton rb_singleColorMultiSizeSelect;
    private RadioButton rb_multiColorMultiSizeSelect;
    private TextView tv_commonSelect;
    private TextView tv_multiSelect;
    private TextView tv_singleColorMultiSizeSelect;
    private TextView tv_multiColorMultiSizeSelect;

    private EditText etPrintIp; // 打印机服务器地址
    private EditText etPrintPort; // 打印机服务器端口
    private EditText etPrinter; // 默认打印机
    private EditText etPrintBarcodeWidth; // 条码打印宽度
    private EditText etPrintBarcodeHeight; // 条码打印高度
    private EditText etPrintBarcodeType; // 条码打印类型
    private TextView tv_save;
    private TextView tv_version;
    private FontTextView ftv_print_ip;
    private FontTextView ftv_print_port;
    private FontTextView ftv_printer;
    private EditText etNo;
    private EditText etPassword;
    private View tempView;

    private String printIp;
    private String printPort;
    private String printer;
    private String strPrintIp;
    private String strPrintPort;
    private String strPrinter;
    private String printBarcodeHeight;
    private String printBarcodeWidth;
    private String printBarcodeType;
    private String strPrintBarcodeHeight;
    private String strPrintBarcodeWidth;
    private String strPrintBarcodeType;

    private boolean useGoodsboxBarcodeInStocktaking;
    private boolean useSupplierCodeToAreaProtection;
    private boolean coverDoc;
    private boolean notAllow;
    private boolean useDefSupplier;
    private boolean barcodeWarningTone;
    private boolean useAreaProtection;
    private boolean adminValidate;
    private boolean useSingleDiscount;
    private boolean displayInventory;
    private boolean preciseToQueryStock;
    private boolean firstInputOfGoodsCode;
    private boolean notUseNegativeInventoryCheck;

    // 销售订单
    private boolean salesOrderEmployee;
    private boolean salesOrderCustomer;
    private boolean salesOrderBrand;
    private boolean salesOrderMemo;
    private boolean salesOrderDepartment; // 订货部门
    private boolean salesOrderWarehouse; // 发货部门
    private boolean salesOrderAmountSum;
    // 销售发货单
    private boolean salesEmployee;
    private boolean salesCustomer;
    private boolean salesBrand;
    private boolean salesMemo;
    private boolean salesOrderNo; // 订单单号
    private boolean salesWarehouse; // 发货部门
    private boolean salesAmountSum;
    // 销售退货单
    private boolean salesReturnEmployee;
    private boolean salesReturnCustomer;
    private boolean salesReturnBrand;
    private boolean salesReturnMemo;
    private boolean salesReturnWarehouse; // 退货部门
    private boolean salesReturnDepartment; // 收货部门
    private boolean salesReturnAmountSum;

    // 采购收货单
    private boolean purchaseOrderEmployee;
    private boolean purchaseOrderSupplier;
    private boolean purchaseOrderBrand;
    private boolean purchaseOrderMemo;
    private boolean purchaseOrderDepartment; // 收货部门
    private boolean purchaseOrderAmountSum;

    // 采购收货单
    private boolean purchaseEmployee;
    private boolean purchaseSupplier;
    private boolean purchaseBrand;
    private boolean purchaseMemo;
    private boolean purchaseDepartment; // 收货部门
    private boolean purchaseAmountSum;
    // 采购退货单
    private boolean purchaseReturnEmployee;
    private boolean purchaseReturnSupplier;
    private boolean purchaseReturnBrand;
    private boolean purchaseReturnMemo;
    private boolean purchaseReturnDepartment; // 退货部门
    private boolean purchaseReturnAmountSum;
    // 转仓单
    private boolean stockMoveEmployee;
    private boolean stockMoveMemo;
    private boolean stockMoveWarehouseIn; // 转进仓库
    private boolean stockMoveWarehouseOut; // 转出仓库
    private boolean stockMoveBrand;
    private boolean stockMoveAmountSum;
    // 进仓单
    private boolean stockInEmployee;
    private boolean stockInMemo;
    private boolean stockInWarehouse; // 仓库名称
    private boolean stockInWarehouseOut; // 转出仓库
    private boolean stockInBrand;
    private boolean stockInRelationNo; // 对应单号
    private boolean stockInAmountSum;
    // 出仓单
    private boolean stockOutEmployee;
    private boolean stockOutMemo;
    private boolean stockOutWarehouse; // 仓库名称
    private boolean stockOutWarehouseIn; // 转进仓库
    private boolean stockOutBrand;
    private boolean stockOutRelationNo; // 对应单号
    private boolean stockOutAmountSum;
    // 盘点单
    private boolean stocktakingEmployee;
    private boolean stocktakingMemo;
    private boolean stocktakingWarehouse;
    private boolean stocktakingBrand;
    private boolean stocktakingAmountSum;

    // 条码打印
    private boolean showGoodsCode;
    private boolean showGoodsName;
    private boolean showColor;
    private boolean showSize;
    private boolean showRetailSales;

    private String printers = null;
    private String[] listPrinters; // 打印机列表

    // 装箱单
    private int packingBoxRelationType;

    // 多货品颜色尺码录入
    private int goodsMultiSelectType;
    // 多货品颜色尺码录入方式
    private int multiSelectType;

    private Paramer printPIp;
    private Paramer printPPort;
    private Paramer pPrinter;
    private Paramer pPrintBarcodeHeight;
    private Paramer pPrintBarcodeWidth;
    private Paramer pPrintBarcodeType;
    private Paramer pUseGoodsboxBarcodeInStocktaking;
    private Paramer pUseSupplierCodeToAreaProtection;
    private Paramer pCoverDoc;
    private Paramer pNotAllow;
    private Paramer pUseDefSupplier;
    private Paramer pBarcodeWarningTone;
    private Paramer pUseAreaProtection;
    private Paramer pUseSingleDiscount;
    private Paramer pDisplayInventory;
    private Paramer pPreciseToQueryStock;
    private Paramer pFirstInputOfGoodsCode;
    private Paramer pNotUseNegativeInventoryCheck;

    // 销售订单
    private Paramer pSalesOrderEmployee;
    private Paramer pSalesOrderCustomer;
    private Paramer pSalesOrderBrand;
    private Paramer pSalesOrderMemo;
    private Paramer pSalesOrderDepartment; // 订货部门
    private Paramer pSalesOrderWarehouse; // 发货部门
    private Paramer pSalesOrderAmountSum;
    // 销售发货单
    private Paramer pSalesEmployee;
    private Paramer pSalesCustomer;
    private Paramer pSalesBrand;
    private Paramer pSalesMemo;
    private Paramer pSalesOrderNo; // 订单单号
    private Paramer pSalesWarehouse; // 发货部门
    private Paramer pSalesAmountSum;
    // 销售退货单
    private Paramer pSalesReturnEmployee;
    private Paramer pSalesReturnCustomer;
    private Paramer pSalesReturnBrand;
    private Paramer pSalesReturnMemo;
    private Paramer pSalesReturnWarehouse; // 退货部门
    private Paramer pSalesReturnDepartment; // 收货部门
    private Paramer pSalesReturnAmountSum;

    // 采购订单
    private Paramer pPurchaseOrderEmployee;
    private Paramer pPurchaseOrderSupplier;
    private Paramer pPurchaseOrderBrand;
    private Paramer pPurchaseOrderMemo;
    private Paramer pPurchaseOrderDepartment; // 收货部门
    private Paramer pPurchaseOrderAmountSum;

    // 采购收货单
    private Paramer pPurchaseEmployee;
    private Paramer pPurchaseSupplier;
    private Paramer pPurchaseBrand;
    private Paramer pPurchaseMemo;
    private Paramer pPurchaseDepartment; // 收货部门
    private Paramer pPurchaseAmountSum;
    // 采购退货单
    private Paramer pPurchaseReturnEmployee;
    private Paramer pPurchaseReturnSupplier;
    private Paramer pPurchaseReturnBrand;
    private Paramer pPurchaseReturnMemo;
    private Paramer pPurchaseReturnDepartment; // 退货部门
    private Paramer pPurchaseReturnAmountSum;
    // 转仓单
    private Paramer pStockMoveEmployee;
    private Paramer pStockMoveMemo;
    private Paramer pStockMoveWarehouseIn; // 转进仓库
    private Paramer pStockMoveWarehouseOut; // 转出仓库
    private Paramer pStockMoveBrand;
    private Paramer pStockMoveAmountSum;
    // 进仓单
    private Paramer pStockInEmployee;
    private Paramer pStockInMemo;
    private Paramer pStockInWarehouse; // 仓库名称
    private Paramer pStockInWarehouseOut; // 转出仓库
    private Paramer pStockInBrand;
    private Paramer pStockInRelationNo; // 对应单号
    private Paramer pStockInAmountSum;
    // 出仓单
    private Paramer pStockOutEmployee;
    private Paramer pStockOutMemo;
    private Paramer pStockOutWarehouse; // 仓库名称
    private Paramer pStockOutWarehouseIn; // 转进仓库
    private Paramer pStockOutBrand;
    private Paramer pStockOutRelationNo; // 对应单号
    private Paramer pStockOutAmountSum;
    // 盘点单
    private Paramer pStocktakingEmployee;
    private Paramer pStocktakingMemo;
    private Paramer pStocktakingWarehouse;
    private Paramer pStocktakingBrand;
    private Paramer pStocktakingAmountSum;

    private Paramer pShowGoodsCode;
    private Paramer pShowGoodsName;
    private Paramer pShowColor;
    private Paramer pShowSize;
    private Paramer pShowRetailSales;

    // 装箱单
    private Paramer pPackingBoxRelationType;

    // 多货品颜色尺码录入
    private Paramer pGoodsMultiSelectType;
    // 多货品颜色尺码录入方式
    private Paramer pMultiSelectType;


    /**
     * 显示设置的参数值
     */
    private void show() {
        // 获取显示值
        printPIp = paramerDao.find("printIp");
        printPPort = paramerDao.find("printPort");
        pPrinter = paramerDao.find("printer");
        pPrintBarcodeHeight = paramerDao.find("printBarcodeHeight");
        pPrintBarcodeWidth = paramerDao.find("printBarcodeWidth");
        pPrintBarcodeType = paramerDao.find("printBarcodeType");
        pUseGoodsboxBarcodeInStocktaking = paramerDao.find("useGoodsboxBarcodeInStocktaking");
        pUseSupplierCodeToAreaProtection = paramerDao.find("useSupplierCodeToAreaProtection");
        pCoverDoc = paramerDao.find("coverDoc");
        pNotAllow = paramerDao.find("notAllow");
        pUseDefSupplier = paramerDao.find("useDefSupplier");
        pBarcodeWarningTone = paramerDao.find("barcodeWarningTone");
        pUseAreaProtection = paramerDao.find("useAreaProtection");
        pUseSingleDiscount = paramerDao.find("useSingleDiscount");
        pDisplayInventory = paramerDao.find("displayInventory");
        pPreciseToQueryStock = paramerDao.find("preciseToQueryStock");
        pFirstInputOfGoodsCode = paramerDao.find("firstInputOfGoodsCode");
        pNotUseNegativeInventoryCheck = paramerDao.find("notUseNegativeInventoryCheck");
        // 其它设置
        // 销售订单
        pSalesOrderEmployee = paramerDao.find("salesOrderEmployee");
        pSalesOrderCustomer = paramerDao.find("salesOrderCustomer");
        pSalesOrderBrand = paramerDao.find("salesOrderBrand");
        pSalesOrderMemo = paramerDao.find("salesOrderMemo");
        pSalesOrderDepartment = paramerDao.find("salesOrderDepartment");
        pSalesOrderWarehouse = paramerDao.find("salesOrderWarehouse");
        pSalesOrderAmountSum = paramerDao.find("salesOrderAmountSum");
        // 销售发货单
        pSalesEmployee = paramerDao.find("salesEmployee");
        pSalesCustomer = paramerDao.find("salesCustomer");
        pSalesBrand = paramerDao.find("salesBrand");
        pSalesMemo = paramerDao.find("salesMemo");
        pSalesOrderNo = paramerDao.find("salesOrderNo");
        pSalesWarehouse = paramerDao.find("salesWarehouse");
        pSalesAmountSum = paramerDao.find("salesAmountSum");
        // 销售退货单
        pSalesReturnEmployee = paramerDao.find("salesReturnEmployee");
        pSalesReturnCustomer = paramerDao.find("salesReturnCustomer");
        pSalesReturnBrand = paramerDao.find("salesReturnBrand");
        pSalesReturnMemo = paramerDao.find("salesReturnMemo");
        pSalesReturnWarehouse = paramerDao.find("salesReturnWarehouse");
        pSalesReturnDepartment = paramerDao.find("salesReturnDepartment");
        pSalesReturnAmountSum = paramerDao.find("salesReturnAmountSum");


        // 采购订货单
        pPurchaseOrderEmployee = paramerDao.find("purchaseOrderEmployee");
        pPurchaseOrderSupplier = paramerDao.find("purchaseOrderSupplier");
        pPurchaseOrderBrand = paramerDao.find("purchaseOrderBrand");
        pPurchaseOrderMemo = paramerDao.find("purchaseOrderMemo");
        pPurchaseOrderDepartment = paramerDao.find("purchaseOrderDepartment");
        pPurchaseOrderAmountSum = paramerDao.find("purchaseOrderAmountSum");

        // 采购收货单
        pPurchaseEmployee = paramerDao.find("purchaseEmployee");
        pPurchaseSupplier = paramerDao.find("purchaseSupplier");
        pPurchaseBrand = paramerDao.find("purchaseBrand");
        pPurchaseMemo = paramerDao.find("purchaseMemo");
        pPurchaseDepartment = paramerDao.find("purchaseDepartment");
        pPurchaseAmountSum = paramerDao.find("purchaseAmountSum");
        // 采购退货单
        pPurchaseReturnEmployee = paramerDao.find("purchaseReturnEmployee");
        pPurchaseReturnSupplier = paramerDao.find("purchaseReturnSupplier");
        pPurchaseReturnBrand = paramerDao.find("purchaseReturnBrand");
        pPurchaseReturnMemo = paramerDao.find("purchaseReturnMemo");
        pPurchaseReturnDepartment = paramerDao.find("purchaseReturnDepartment");
        pPurchaseReturnAmountSum = paramerDao.find("purchaseReturnAmountSum");
        // 转仓单
        pStockMoveEmployee = paramerDao.find("stockMoveEmployee");
        pStockMoveMemo = paramerDao.find("stockMoveMemo");
        pStockMoveWarehouseIn = paramerDao.find("stockMoveWarehouseIn"); // 转进仓库
        pStockMoveWarehouseOut = paramerDao.find("stockMoveWarehouseOut"); // 转出仓库
        pStockMoveBrand = paramerDao.find("stockMoveBrand");
        pStockMoveAmountSum = paramerDao.find("stockMoveAmountSum");
        // 进仓单
        pStockInEmployee = paramerDao.find("stockInEmployee");
        pStockInMemo = paramerDao.find("stockInMemo");
        pStockInWarehouse = paramerDao.find("stockInWarehouse"); // 仓库名称
        pStockInWarehouseOut = paramerDao.find("stockInWarehouseOut"); // 转出仓库
        pStockInBrand = paramerDao.find("stockInBrand");
        pStockInRelationNo = paramerDao.find("stockInRelationNo"); // 对应单号
        pStockInAmountSum = paramerDao.find("stockInAmountSum");
        // 出仓单
        pStockOutEmployee = paramerDao.find("stockOutEmployee");
        pStockOutMemo = paramerDao.find("stockOutMemo");
        pStockOutWarehouse = paramerDao.find("stockOutWarehouse"); // 仓库名称
        pStockOutWarehouseIn = paramerDao.find("stockOutWarehouseIn"); // 转进仓库
        pStockOutBrand = paramerDao.find("stockOutBrand");
        pStockOutRelationNo = paramerDao.find("stockOutRelationNo"); // 对应单号
        pStockOutAmountSum = paramerDao.find("stockOutAmountSum");
        // 盘点单
        pStocktakingEmployee = paramerDao.find("stocktakingEmployee");
        pStocktakingMemo = paramerDao.find("stocktakingMemo");
        pStocktakingWarehouse = paramerDao.find("stocktakingWarehouse");
        pStocktakingBrand = paramerDao.find("stocktakingBrand");
        pStocktakingAmountSum = paramerDao.find("stocktakingAmountSum");
        // 条码打印
        pShowGoodsCode = paramerDao.find("showGoodsCode");
        pShowGoodsName = paramerDao.find("showGoodsName");
        pShowColor = paramerDao.find("showColor");
        pShowSize = paramerDao.find("showSize");
        pShowRetailSales = paramerDao.find("showRetailSales");
        // 装箱单
        pPackingBoxRelationType = paramerDao.find("packingBoxRelationType");
        // 多货品颜色尺码录入
        pGoodsMultiSelectType = paramerDao.find("goodsMultiSelectType");
        // 多货品颜色尺码录入方式
        pMultiSelectType = paramerDao.find("multiSelectType");
        // 非空判断并绑定显示值
        if (null != printPIp) {
            etPrintIp.setText(printPIp.getValue());
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(this);
            if (null != userInfo) {
                etPrintIp.setText(userInfo.getProperty("printIp"));
            }
        }
        if (null != printPPort) {
            etPrintPort.setText(printPPort.getValue());
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(this);
            if (null != userInfo) {
                etPrintPort.setText(userInfo.getProperty("printPort"));
            }
        }
        if (null != pPrinter) {
            etPrinter.setText(pPrinter.getValue());
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(this);
            if (null != userInfo) {
                etPrinter.setText(userInfo.getProperty("printer"));
            }
        }
        if (null != pPrintBarcodeHeight) {
            etPrintBarcodeHeight.setText(pPrintBarcodeHeight.getValue());
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(this);
            if (null != userInfo) {
                etPrintBarcodeHeight.setText(userInfo.getProperty("printBarcodeHeight"));
            }
        }
        if (null != pPrintBarcodeWidth) {
            etPrintBarcodeWidth.setText(pPrintBarcodeWidth.getValue());
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(this);
            if (null != userInfo) {
                etPrintBarcodeWidth.setText(userInfo.getProperty("printBarcodeWidth"));
            }
        }
        if (null != pPrintBarcodeType) {
            etPrintBarcodeType.setText(pPrintBarcodeType.getValue());
        } else {
            Properties userInfo = ParamterFileUtil.getIpInfo(this);
            if (null != userInfo) {
                etPrintBarcodeType.setText(userInfo.getProperty("printBarcodeType"));
            }
        }
        // 系统其它设置
        if (null != pUseGoodsboxBarcodeInStocktaking) {
            useGoodsboxBarcodeInStocktaking = Boolean.parseBoolean(pUseGoodsboxBarcodeInStocktaking.getValue());
            cb_useGoodsboxBarcodeInStocktaking.setChecked(useGoodsboxBarcodeInStocktaking);
        }
        if (null != pUseSupplierCodeToAreaProtection) {
            useSupplierCodeToAreaProtection = Boolean.parseBoolean(pUseSupplierCodeToAreaProtection.getValue());
            cb_useSupplierCodeToAreaProtection.setChecked(useSupplierCodeToAreaProtection);
        }
        if (null != pCoverDoc) {
            coverDoc = Boolean.parseBoolean(pCoverDoc.getValue());
            cb_coverDoc.setChecked(coverDoc);
        }
        if (null != pNotAllow) {
            notAllow = Boolean.parseBoolean(pNotAllow.getValue());
            cb_notAllow.setChecked(notAllow);
        }
        if (null != pUseDefSupplier) {
            useDefSupplier = Boolean.parseBoolean(pUseDefSupplier.getValue());
            cb_useDefSupplier.setChecked(useDefSupplier);
        }
        if (null != pBarcodeWarningTone) {
            barcodeWarningTone = Boolean.parseBoolean(pBarcodeWarningTone.getValue());
            cb_barcodeWarningTone.setChecked(barcodeWarningTone);
        }
        if (null != pUseAreaProtection) {
            useAreaProtection = Boolean.parseBoolean(pUseAreaProtection.getValue());
            cb_useAreaProtection.setChecked(useAreaProtection);
        }
        if (null != pUseSingleDiscount) {
            useSingleDiscount = Boolean.parseBoolean(pUseSingleDiscount.getValue());
            cb_useSingleDiscount.setChecked(useSingleDiscount);
        }
        if (null != pDisplayInventory) {
            displayInventory = Boolean.parseBoolean(pDisplayInventory.getValue());
            cb_displayInventory.setChecked(displayInventory);
        }
        if (null != pPreciseToQueryStock) {
            preciseToQueryStock = Boolean.parseBoolean(pPreciseToQueryStock.getValue());
            cb_preciseToQueryStock.setChecked(preciseToQueryStock);
        }
        if (null != pFirstInputOfGoodsCode) {
            firstInputOfGoodsCode = Boolean.parseBoolean(pFirstInputOfGoodsCode.getValue());
            cb_firstInputOfGoodsCode.setChecked(firstInputOfGoodsCode);
        }
        if (null != pNotUseNegativeInventoryCheck) {
            notUseNegativeInventoryCheck = Boolean.parseBoolean(pNotUseNegativeInventoryCheck.getValue());
            cb_notUseNegativeInventoryCheck.setChecked(notUseNegativeInventoryCheck);
        }
        // 销售订单
        if (null != pSalesOrderEmployee) {
            salesOrderEmployee = Boolean.parseBoolean(pSalesOrderEmployee.getValue());
            cb_salesOrderEmployee.setChecked(salesOrderEmployee);
        }
        if (null != pSalesOrderCustomer) {
            salesOrderCustomer = Boolean.parseBoolean(pSalesOrderCustomer.getValue());
            cb_salesOrderCustomer.setChecked(salesOrderCustomer);
        }
        if (null != pSalesOrderBrand) {
            salesOrderBrand = Boolean.parseBoolean(pSalesOrderBrand.getValue());
            cb_salesOrderBrand.setChecked(salesOrderBrand);
        }
        if (null != pSalesOrderMemo) {
            salesOrderMemo = Boolean.parseBoolean(pSalesOrderMemo.getValue());
            cb_salesOrderMemo.setChecked(salesOrderMemo);
        }
        if (null != pSalesOrderDepartment) {
            salesOrderDepartment = Boolean.parseBoolean(pSalesOrderDepartment.getValue());
            cb_salesOrderDepartment.setChecked(salesOrderDepartment);
        }
        if (null != pSalesOrderWarehouse) {
            salesOrderWarehouse = Boolean.parseBoolean(pSalesOrderWarehouse.getValue());
            cb_salesOrderWarehouse.setChecked(salesOrderWarehouse);
        }
        if (null != pSalesOrderAmountSum) {
            salesOrderAmountSum = Boolean.parseBoolean(pSalesOrderAmountSum.getValue());
            cb_salesOrderAmountSum.setChecked(salesOrderAmountSum);
        }
        // 销售发货单
        if (null != pSalesEmployee) {
            salesEmployee = Boolean.parseBoolean(pSalesEmployee.getValue());
            cb_salesEmployee.setChecked(salesEmployee);
        }
        if (null != pSalesCustomer) {
            salesCustomer = Boolean.parseBoolean(pSalesCustomer.getValue());
            cb_salesCustomer.setChecked(salesCustomer);
        }
        if (null != pSalesBrand) {
            salesBrand = Boolean.parseBoolean(pSalesBrand.getValue());
            cb_salesBrand.setChecked(salesBrand);
        }
        if (null != pSalesMemo) {
            salesMemo = Boolean.parseBoolean(pSalesMemo.getValue());
            cb_salesMemo.setChecked(salesMemo);
        }
        if (null != pSalesOrderNo) {
            salesOrderNo = Boolean.parseBoolean(pSalesOrderNo.getValue());
            cb_salesOrderNo.setChecked(salesOrderNo);
        }
        if (null != pSalesWarehouse) {
            salesWarehouse = Boolean.parseBoolean(pSalesWarehouse.getValue());
            cb_salesWarehouse.setChecked(salesWarehouse);
        }
        if (null != pSalesAmountSum) {
            salesAmountSum = Boolean.parseBoolean(pSalesAmountSum.getValue());
            cb_salesAmountSum.setChecked(salesAmountSum);
        }
        // 销售退货单
        if (null != pSalesReturnEmployee) {
            salesReturnEmployee = Boolean.parseBoolean(pSalesReturnEmployee.getValue());
            cb_salesReturnEmployee.setChecked(salesReturnEmployee);
        }
        if (null != pSalesReturnCustomer) {
            salesReturnCustomer = Boolean.parseBoolean(pSalesReturnCustomer.getValue());
            cb_salesReturnCustomer.setChecked(salesReturnCustomer);
        }
        if (null != pSalesReturnBrand) {
            salesReturnBrand = Boolean.parseBoolean(pSalesReturnBrand.getValue());
            cb_salesReturnBrand.setChecked(salesReturnBrand);
        }
        if (null != pSalesReturnMemo) {
            salesReturnMemo = Boolean.parseBoolean(pSalesReturnMemo.getValue());
            cb_salesReturnMemo.setChecked(salesReturnMemo);
        }
        if (null != pSalesReturnWarehouse) {
            salesReturnWarehouse = Boolean.parseBoolean(pSalesReturnWarehouse.getValue());
            cb_salesReturnWarehouse.setChecked(salesReturnWarehouse);
        }
        if (null != pSalesReturnDepartment) {
            salesReturnDepartment = Boolean.parseBoolean(pSalesReturnDepartment.getValue());
            cb_salesReturnDepartment.setChecked(salesReturnDepartment);
        }
        if (null != pSalesReturnAmountSum) {
            salesReturnAmountSum = Boolean.parseBoolean(pSalesReturnAmountSum.getValue());
            cb_salesReturnAmountSum.setChecked(salesReturnAmountSum);
        }

        // 采购订货单
        if (null != pPurchaseOrderEmployee) {
            purchaseOrderEmployee = Boolean.parseBoolean(pPurchaseOrderEmployee.getValue());
            cb_purchaseOrderEmployee.setChecked(purchaseOrderEmployee);
        }
        if (null != pPurchaseOrderSupplier) {
            purchaseOrderSupplier = Boolean.parseBoolean(pPurchaseOrderSupplier.getValue());
            cb_purchaseOrderSupplier.setChecked(purchaseOrderSupplier);
        }
        if (null != pPurchaseOrderBrand) {
            purchaseOrderBrand = Boolean.parseBoolean(pPurchaseOrderBrand.getValue());
            cb_purchaseOrderBrand.setChecked(purchaseOrderBrand);
        }
        if (null != pPurchaseOrderMemo) {
            purchaseOrderMemo = Boolean.parseBoolean(pPurchaseOrderMemo.getValue());
            cb_purchaseOrderMemo.setChecked(purchaseOrderMemo);
        }
        if (null != pPurchaseOrderDepartment) {
            purchaseOrderDepartment = Boolean.parseBoolean(pPurchaseOrderDepartment.getValue());
            cb_purchaseOrderDepartment.setChecked(purchaseOrderDepartment);
        }
        if (null != pPurchaseOrderAmountSum) {
            purchaseOrderAmountSum = Boolean.parseBoolean(pPurchaseOrderAmountSum.getValue());
            cb_purchaseOrderAmountSum.setChecked(purchaseOrderAmountSum);
        }


        // 采购收货单
        if (null != pPurchaseEmployee) {
            purchaseEmployee = Boolean.parseBoolean(pPurchaseEmployee.getValue());
            cb_purchaseEmployee.setChecked(purchaseEmployee);
        }
        if (null != pPurchaseSupplier) {
            purchaseSupplier = Boolean.parseBoolean(pPurchaseSupplier.getValue());
            cb_purchaseSupplier.setChecked(purchaseSupplier);
        }
        if (null != pPurchaseBrand) {
            purchaseBrand = Boolean.parseBoolean(pPurchaseBrand.getValue());
            cb_purchaseBrand.setChecked(purchaseBrand);
        }
        if (null != pPurchaseMemo) {
            purchaseMemo = Boolean.parseBoolean(pPurchaseMemo.getValue());
            cb_purchaseMemo.setChecked(purchaseMemo);
        }
        if (null != pPurchaseDepartment) {
            purchaseDepartment = Boolean.parseBoolean(pPurchaseDepartment.getValue());
            cb_purchaseDepartment.setChecked(purchaseDepartment);
        }
        if (null != pPurchaseAmountSum) {
            purchaseAmountSum = Boolean.parseBoolean(pPurchaseAmountSum.getValue());
            cb_purchaseAmountSum.setChecked(purchaseAmountSum);
        }
        // 采购退货单
        if (null != pPurchaseReturnEmployee) {
            purchaseReturnEmployee = Boolean.parseBoolean(pPurchaseReturnEmployee.getValue());
            cb_purchaseReturnEmployee.setChecked(purchaseReturnEmployee);
        }
        if (null != pPurchaseReturnSupplier) {
            purchaseReturnSupplier = Boolean.parseBoolean(pPurchaseReturnSupplier.getValue());
            cb_purchaseReturnSupplier.setChecked(purchaseReturnSupplier);
        }
        if (null != pPurchaseReturnBrand) {
            purchaseReturnBrand = Boolean.parseBoolean(pPurchaseReturnBrand.getValue());
            cb_purchaseReturnBrand.setChecked(purchaseReturnBrand);
        }
        if (null != pPurchaseReturnMemo) {
            purchaseReturnMemo = Boolean.parseBoolean(pPurchaseReturnMemo.getValue());
            cb_purchaseReturnMemo.setChecked(purchaseReturnMemo);
        }
        if (null != pPurchaseReturnDepartment) {
            purchaseReturnDepartment = Boolean.parseBoolean(pPurchaseReturnDepartment.getValue());
            cb_purchaseReturnDepartment.setChecked(purchaseReturnDepartment);
        }
        if (null != pPurchaseReturnAmountSum) {
            purchaseReturnAmountSum = Boolean.parseBoolean(pPurchaseReturnAmountSum.getValue());
            cb_purchaseReturnAmountSum.setChecked(purchaseReturnAmountSum);
        }
        // 转仓单
        if (null != pStockMoveEmployee) {
            stockMoveEmployee = Boolean.parseBoolean(pStockMoveEmployee.getValue());
            cb_stockMoveEmployee.setChecked(stockMoveEmployee);
        }
        if (null != pStockMoveMemo) {
            stockMoveMemo = Boolean.parseBoolean(pStockMoveMemo.getValue());
            cb_stockMoveMemo.setChecked(stockMoveMemo);
        }
        if (null != pStockMoveWarehouseIn) {
            stockMoveWarehouseIn = Boolean.parseBoolean(pStockMoveWarehouseIn.getValue());
            cb_stockMoveWarehouseIn.setChecked(stockMoveWarehouseIn);
        }
        if (null != pStockMoveWarehouseOut) {
            stockMoveWarehouseOut = Boolean.parseBoolean(pStockMoveWarehouseOut.getValue());
            cb_stockMoveWarehouseOut.setChecked(stockMoveWarehouseOut);
        }
        if (null != pStockMoveBrand) {
            stockMoveBrand = Boolean.parseBoolean(pStockMoveBrand.getValue());
            cb_stockMoveBrand.setChecked(stockMoveBrand);
        }
        if (null != pStockMoveAmountSum) {
            stockMoveAmountSum = Boolean.parseBoolean(pStockMoveAmountSum.getValue());
            cb_stockMoveAmountSum.setChecked(stockMoveAmountSum);
        }
        // 进仓单
        if (null != pStockInEmployee) {
            stockInEmployee = Boolean.parseBoolean(pStockInEmployee.getValue());
            cb_stockInEmployee.setChecked(stockInEmployee);
        }
        if (null != pStockInMemo) {
            stockInMemo = Boolean.parseBoolean(pStockInMemo.getValue());
            cb_stockInMemo.setChecked(stockInMemo);
        }
        if (null != pStockInWarehouse) {
            stockInWarehouse = Boolean.parseBoolean(pStockInWarehouse.getValue());
            cb_stockInWarehouse.setChecked(stockInWarehouse);
        }
        if (null != pStockInWarehouseOut) {
            stockInWarehouseOut = Boolean.parseBoolean(pStockInWarehouseOut.getValue());
            cb_stockInWarehouseOut.setChecked(stockInWarehouseOut);
        }
        if (null != pStockInBrand) {
            stockInBrand = Boolean.parseBoolean(pStockInBrand.getValue());
            cb_stockInBrand.setChecked(stockInBrand);
        }
        if (null != pStockInRelationNo) {
            stockInRelationNo = Boolean.parseBoolean(pStockInRelationNo.getValue());
            cb_stockInRelationNo.setChecked(stockInRelationNo);
        }
        if (null != pStockInAmountSum) {
            stockInAmountSum = Boolean.parseBoolean(pStockInAmountSum.getValue());
            cb_stockInAmountSum.setChecked(stockInAmountSum);
        }
        // 出仓单
        if (null != pStockOutEmployee) {
            stockOutEmployee = Boolean.parseBoolean(pStockOutEmployee.getValue());
            cb_stockOutEmployee.setChecked(stockOutEmployee);
        }
        if (null != pStockOutMemo) {
            stockOutMemo = Boolean.parseBoolean(pStockOutMemo.getValue());
            cb_stockOutMemo.setChecked(stockOutMemo);
        }
        if (null != pStockOutWarehouse) {
            stockOutWarehouse = Boolean.parseBoolean(pStockOutWarehouse.getValue());
            cb_stockOutWarehouse.setChecked(stockOutWarehouse);
        }
        if (null != pStockOutWarehouseIn) {
            stockOutWarehouseIn = Boolean.parseBoolean(pStockOutWarehouseIn.getValue());
            cb_stockOutWarehouseIn.setChecked(stockOutWarehouseIn);
        }
        if (null != pStockOutBrand) {
            stockOutBrand = Boolean.parseBoolean(pStockOutBrand.getValue());
            cb_stockOutBrand.setChecked(stockOutBrand);
        }
        if (null != pStockOutRelationNo) {
            stockOutRelationNo = Boolean.parseBoolean(pStockOutRelationNo.getValue());
            cb_stockOutRelationNo.setChecked(stockOutRelationNo);
        }
        if (null != pStockOutAmountSum) {
            stockOutAmountSum = Boolean.parseBoolean(pStockOutAmountSum.getValue());
            cb_stockOutAmountSum.setChecked(stockOutAmountSum);
        }
        // 盘点单
        if (null != pStocktakingEmployee) {
            stocktakingEmployee = Boolean.parseBoolean(pStocktakingEmployee.getValue());
            cb_stocktakingEmployee.setChecked(stocktakingEmployee);
        }
        if (null != pStocktakingMemo) {
            stocktakingMemo = Boolean.parseBoolean(pStocktakingMemo.getValue());
            cb_stocktakingMemo.setChecked(stocktakingMemo);
        }
        if (null != pStocktakingWarehouse) {
            stocktakingWarehouse = Boolean.parseBoolean(pStocktakingWarehouse.getValue());
            cb_stocktakingWarehouse.setChecked(stocktakingWarehouse);
        }
        if (null != pStocktakingBrand) {
            stocktakingBrand = Boolean.parseBoolean(pStocktakingBrand.getValue());
            cb_stocktakingBrand.setChecked(stocktakingBrand);
        }
        if (null != pStocktakingAmountSum) {
            stocktakingAmountSum = Boolean.parseBoolean(pStocktakingAmountSum.getValue());
            cb_stocktakingAmountSum.setChecked(stocktakingAmountSum);
        }
        // 条码打印
        if (null != pShowGoodsCode) {
            showGoodsCode = Boolean.parseBoolean(pShowGoodsCode.getValue());
            cb_showGoodsCode.setChecked(showGoodsCode);
        }
        if (null != pShowGoodsName) {
            showGoodsName = Boolean.parseBoolean(pShowGoodsName.getValue());
            cb_showGoodsName.setChecked(showGoodsName);
        }
        if (null != pShowColor) {
            showColor = Boolean.parseBoolean(pShowColor.getValue());
            cb_showColor.setChecked(showColor);
        }
        if (null != pShowSize) {
            showSize = Boolean.parseBoolean(pShowSize.getValue());
            cb_showSize.setChecked(showSize);
        }
        if (null != pShowRetailSales) {
            showRetailSales = Boolean.parseBoolean(pShowRetailSales.getValue());
            cb_showRetailSales.setChecked(showRetailSales);
        }
        // 装箱单设置
        if (null != pPackingBoxRelationType) {
            packingBoxRelationType = Integer.parseInt(pPackingBoxRelationType.getValue());
            selectCheck(packingBoxRelationType);
        }
        // 多货号颜色尺码录入设置
        if (null != pGoodsMultiSelectType) {
            goodsMultiSelectType = Integer.parseInt(pGoodsMultiSelectType.getValue());
            goodsMultiSelectCheck(goodsMultiSelectType);
        }
        // 多货号颜色尺码录入设置
        if (null != pMultiSelectType) {
            multiSelectType = Integer.parseInt(pMultiSelectType.getValue());
            multiSelectCheck(multiSelectType);
        }

    }

    /**
     * 保存系统设置参数
     */
    private void save() {
        try {
            int count = 0;
            printIp = etPrintIp.getText().toString().trim();
            printPort = etPrintPort.getText().toString().trim();
            printer = etPrinter.getText().toString().trim();
            // 条码打印参数
            printBarcodeHeight = etPrintBarcodeHeight.getText().toString().trim();
            printBarcodeWidth = etPrintBarcodeWidth.getText().toString().trim();
            printBarcodeType = etPrintBarcodeType.getText().toString().trim();

            // 打印机IP
            // if (printIp == null || "".equals(printIp)) {
            // Builder dialog = new
            // AlertDialog.Builder(SettingActivity.this,AlertDialog.THEME_HOLO_LIGHT);
            // dialog.setTitle("提示");
            // dialog.setMessage("使用云打印功能,需配置打印机IP和端口");
            // // 相当于点击确认按钮
            // dialog.setPositiveButton("确认", new
            // DialogInterface.OnClickListener() {
            // @Override
            // public void onClick(DialogInterface dialog, int which) {}
            // });
            // // 相当于点击取消按钮
            // dialog.setCancelable(false);
            // dialog.create();
            // dialog.show();
            // }
            // 打印机端口
            // if ((printPort == null || "".equals(printPort)) && (printIp !=
            // null && !"".equals(printIp))) {
            // Toast.makeText(SettingActivity.this, "打印机端口不能为空",
            // Toast.LENGTH_SHORT).show();
            // return;
            // }
            printPIp = new Paramer("printIp", printIp);
            printPPort = new Paramer("printPort", printPort);
            pPrinter = new Paramer("printer", printer);
            // 条码打印
            pPrintBarcodeHeight = new Paramer("printBarcodeHeight", printBarcodeHeight);
            pPrintBarcodeWidth = new Paramer("printBarcodeWidth", printBarcodeWidth);
            pPrintBarcodeType = new Paramer("printBarcodeType", printBarcodeType);
            if (printIp != null && !"".equals(printIp) && printPPort != null && !"".equals(printPort)) {
                count = paramerDao.update(printPIp);
                ParamterFileUtil.saveIpInfo("printIp", printIp, this);
                if (count == 0) {
                    count = paramerDao.insert(printPIp);
                }
                count = paramerDao.update(printPPort);
                ParamterFileUtil.saveIpInfo("printPort", printPort, this);
                if (count == 0) {
                    count = paramerDao.insert(printPPort);
                }
                count = paramerDao.update(pPrinter);
                ParamterFileUtil.saveIpInfo("printer", printer, this);
                if (count == 0) {
                    count = paramerDao.insert(pPrinter);
                }
            }
            if (count > 0) {
                // 打印机IP
                Paramer tprintIp = paramerDao.find("printIp");
                if (null == tprintIp) {
                    Properties userInfo = ParamterFileUtil.getIpInfo(this);
                    if (null != userInfo) {
                        strPrintIp = userInfo.getProperty("printIp");
                        paramerDao.insert(new Paramer("printIp", strPrintIp));
                    }
                } else {
                    strPrintIp = tprintIp.getValue();
                }
                // 打印机端口
                Paramer tprintPort = paramerDao.find("printPort");
                if (null == tprintPort) {
                    Properties userInfo = ParamterFileUtil.getIpInfo(this);
                    if (null != userInfo) {
                        strPrintPort = userInfo.getProperty("printPort");
                        paramerDao.insert(new Paramer("printPort", strPrintPort));
                    }
                } else {
                    strPrintPort = tprintPort.getValue();
                }
                // 打印机
                Paramer tprinter = paramerDao.find("printer");
                if (null == tprinter) {
                    Properties userInfo = ParamterFileUtil.getIpInfo(this);
                    if (null != userInfo) {
                        strPrinter = userInfo.getProperty("printer");
                        paramerDao.insert(new Paramer("printer", strPrinter));
                    }
                } else {
                    strPrinter = tprinter.getValue();
                }
            }
            // 条码打印
            count = paramerDao.update(pPrintBarcodeHeight);
            ParamterFileUtil.saveIpInfo("printBarcodeHeight", printBarcodeHeight, this);
            if (count == 0) {
                count = paramerDao.insert(pPrintBarcodeHeight);
            }
            count = paramerDao.update(pPrintBarcodeWidth);
            ParamterFileUtil.saveIpInfo("printBarcodeWidth", printBarcodeWidth, this);
            if (count == 0) {
                count = paramerDao.insert(pPrintBarcodeWidth);
            }
            count = paramerDao.update(pPrintBarcodeType);
            ParamterFileUtil.saveIpInfo("printBarcodeType", printBarcodeType, this);
            if (count == 0) {
                count = paramerDao.insert(pPrintBarcodeType);
            }
            if (count > 0) {
                // 条码打印高度
                Paramer tprintBarcodeHeight = paramerDao.find("printBarcodeHeight");
                if (null == tprintBarcodeHeight) {
                    Properties userInfo = ParamterFileUtil.getIpInfo(this);
                    if (null != userInfo) {
                        strPrintBarcodeHeight = userInfo.getProperty("printBarcodeHeight");
                        paramerDao.insert(new Paramer("printBarcodeHeight", strPrintBarcodeHeight));
                    }
                } else {
                    strPrintBarcodeHeight = tprintBarcodeHeight.getValue();
                }
                // 条码打印宽度
                Paramer tprintBarcodeWidth = paramerDao.find("printBarcodeWidth");
                if (null == tprintBarcodeWidth) {
                    Properties userInfo = ParamterFileUtil.getIpInfo(this);
                    if (null != userInfo) {
                        strPrintBarcodeWidth = userInfo.getProperty("printBarcodeWidth");
                        paramerDao.insert(new Paramer("printBarcodeWidth", strPrintBarcodeWidth));
                    }
                } else {
                    strPrintBarcodeWidth = tprintBarcodeWidth.getValue();
                }
                // 条码打印类别
                Paramer tprintBarcodeType = paramerDao.find("printBarcodeType");
                if (null == tprintBarcodeType) {
                    Properties userInfo = ParamterFileUtil.getIpInfo(this);
                    if (null != userInfo) {
                        strPrintBarcodeType = userInfo.getProperty("printBarcodeType");
                        paramerDao.insert(new Paramer("printBarcodeType", strPrintBarcodeType));
                    }
                } else {
                    strPrintBarcodeType = tprintBarcodeType.getValue();
                }
            }
            // 保存其它信息
            if (null != pUseGoodsboxBarcodeInStocktaking) {
                Paramer tpuseGoodsboxBarcodeInStocktaking = paramerDao.find("useGoodsboxBarcodeInStocktaking");
                if (tpuseGoodsboxBarcodeInStocktaking == null) {
                    count = paramerDao.insert(pUseGoodsboxBarcodeInStocktaking);
                } else {
                    count = paramerDao.update(pUseGoodsboxBarcodeInStocktaking);
                }
                LoginParameterUtil.useGoodsboxBarcodeInStocktaking = useGoodsboxBarcodeInStocktaking;
            }
        /*    if (null != pUseSupplierCodeToAreaProtection) {
                Paramer tpuseSupplierCodeToAreaProtection = paramerDao.find("useSupplierCodeToAreaProtection");
                if (tpuseSupplierCodeToAreaProtection == null) {
                    count = paramerDao.insert(pUseSupplierCodeToAreaProtection);
                } else {
                    count = paramerDao.update(pUseSupplierCodeToAreaProtection);
                }
                LoginParameterUtil.useSupplierCodeToAreaProtection = useSupplierCodeToAreaProtection;
            } */
            if (null != pCoverDoc) {
                Paramer tpCoverDoc = paramerDao.find("coverDoc");
                if (tpCoverDoc == null) {
                    count = paramerDao.insert(pCoverDoc);
                } else {
                    count = paramerDao.update(pCoverDoc);
                }
                LoginParameterUtil.updateToDoc = coverDoc;
            }
            if (null != pNotAllow) {
                Paramer tpNotAllow = paramerDao.find("notAllow");
                if (tpNotAllow == null) {
                    count = paramerDao.insert(pNotAllow);
                } else {
                    count = paramerDao.update(pNotAllow);
                }
                LoginParameterUtil.notAllow = notAllow;
            }
            if (null != pUseDefSupplier) {
                Paramer tpUseDefSupplier = paramerDao.find("useDefSupplier");
                if (tpUseDefSupplier == null) {
                    count = paramerDao.insert(pUseDefSupplier);
                } else {
                    count = paramerDao.update(pUseDefSupplier);
                }
                LoginParameterUtil.useDefSupplier = useDefSupplier;
            }
            if (null != pBarcodeWarningTone) {
                Paramer tpBarcodeWarningTone = paramerDao.find("barcodeWarningTone");
                if (tpBarcodeWarningTone == null) {
                    count = paramerDao.insert(pBarcodeWarningTone);
                } else {
                    count = paramerDao.update(pBarcodeWarningTone);
                }
                LoginParameterUtil.barcodeWarningTone = barcodeWarningTone;
            }
           /* if (null != pUseAreaProtection) {
                Paramer tpuseAreaProtection = paramerDao.find("useAreaProtection");
                if (tpuseAreaProtection == null) {
                    count = paramerDao.insert(pUseAreaProtection);
                } else {
                    count = paramerDao.update(pUseAreaProtection);
                }
                LoginParameterUtil.useAreaProtection = useAreaProtection;
            } */
            if (null != pUseSingleDiscount) {
                Paramer tpuseSingleDiscount = paramerDao.find("useSingleDiscount");
                if (tpuseSingleDiscount == null) {
                    count = paramerDao.insert(pUseSingleDiscount);
                } else {
                    count = paramerDao.update(pUseSingleDiscount);
                }
                LoginParameterUtil.useSingleDiscount = useSingleDiscount;
            }
            if (null != pDisplayInventory) {
                Paramer tpdisplayInventory = paramerDao.find("displayInventory");
                if (tpdisplayInventory == null) {
                    count = paramerDao.insert(pDisplayInventory);
                } else {
                    count = paramerDao.update(pDisplayInventory);
                }
                LoginParameterUtil.displayInventory = displayInventory;
            }
            if (null != pFirstInputOfGoodsCode) {
                Paramer tpfirstInputOfGoodsCode = paramerDao.find("firstInputOfGoodsCode");
                if (tpfirstInputOfGoodsCode == null) {
                    count = paramerDao.insert(pFirstInputOfGoodsCode);
                } else {
                    count = paramerDao.update(pFirstInputOfGoodsCode);
                }
                LoginParameterUtil.firstInputOfGoodsCode = firstInputOfGoodsCode;
            }
          /*  if (null != pNotUseNegativeInventoryCheck) {
                Paramer tpnotUseNegativeInventoryCheck = paramerDao.find("notUseNegativeInventoryCheck");
                if (tpnotUseNegativeInventoryCheck == null) {
                    count = paramerDao.insert(pNotUseNegativeInventoryCheck);
                } else {
                    count = paramerDao.update(pNotUseNegativeInventoryCheck);
                }
                LoginParameterUtil.notUseNegativeInventoryCheck = notUseNegativeInventoryCheck;
            }
            */
            // 其它设置
            if (null != pSalesOrderEmployee) {
                Paramer tsalesOrderEmployee = paramerDao.find("salesOrderEmployee");
                if (tsalesOrderEmployee == null) {
                    count = paramerDao.insert(pSalesOrderEmployee);
                } else {
                    count = paramerDao.update(pSalesOrderEmployee);
                }
                LoginParameterUtil.salesOrderEmployee = salesOrderEmployee;
            }
            if (null != pSalesOrderCustomer) {
                Paramer tsalesOrderCustomer = paramerDao.find("salesOrderCustomer");
                if (tsalesOrderCustomer == null) {
                    count = paramerDao.insert(pSalesOrderCustomer);
                } else {
                    count = paramerDao.update(pSalesOrderCustomer);
                }
                LoginParameterUtil.salesOrderCustomer = salesOrderCustomer;
            }
            if (null != pSalesOrderBrand) {
                Paramer tsalesOrderBrand = paramerDao.find("salesOrderBrand");
                if (tsalesOrderBrand == null) {
                    count = paramerDao.insert(pSalesOrderBrand);
                } else {
                    count = paramerDao.update(pSalesOrderBrand);
                }
                LoginParameterUtil.salesOrderBrand = salesOrderBrand;
            }
            if (null != pSalesOrderMemo) {
                Paramer tsalesOrderMemo = paramerDao.find("salesOrderMemo");
                if (tsalesOrderMemo == null) {
                    count = paramerDao.insert(pSalesOrderMemo);
                } else {
                    count = paramerDao.update(pSalesOrderMemo);
                }
                LoginParameterUtil.salesOrderMemo = salesOrderMemo;
            }
            if (null != pSalesOrderDepartment) {
                Paramer tsalesOrderDepartment = paramerDao.find("salesOrderDepartment");
                if (tsalesOrderDepartment == null) {
                    count = paramerDao.insert(pSalesOrderDepartment);
                } else {
                    count = paramerDao.update(pSalesOrderDepartment);
                }
                LoginParameterUtil.salesOrderDepartment = salesOrderDepartment;
            }
            if (null != pSalesOrderWarehouse) {
                Paramer tsalesOrderWarehouse = paramerDao.find("salesOrderWarehouse");
                if (tsalesOrderWarehouse == null) {
                    count = paramerDao.insert(pSalesOrderWarehouse);
                } else {
                    count = paramerDao.update(pSalesOrderWarehouse);
                }
                LoginParameterUtil.salesOrderWarehouse = salesOrderWarehouse;
            }
            if (null != pSalesOrderAmountSum) {
                Paramer tsalesOrderAmountSum = paramerDao.find("salesOrderAmountSum");
                if (tsalesOrderAmountSum == null) {
                    count = paramerDao.insert(pSalesOrderAmountSum);
                } else {
                    count = paramerDao.update(pSalesOrderAmountSum);
                }
                LoginParameterUtil.salesOrderAmountSum = salesOrderAmountSum;
            }
            if (null != pPreciseToQueryStock) {
                Paramer tpreciseToQueryStock = paramerDao.find("preciseToQueryStock");
                if (tpreciseToQueryStock == null) {
                    count = paramerDao.insert(pPreciseToQueryStock);
                } else {
                    count = paramerDao.update(pPreciseToQueryStock);
                }
                LoginParameterUtil.preciseToQueryStock = preciseToQueryStock;
            }
            // 销售发货单
            if (null != pSalesEmployee) {
                Paramer tsalesEmployee = paramerDao.find("salesEmployee");
                if (tsalesEmployee == null) {
                    count = paramerDao.insert(pSalesEmployee);
                } else {
                    count = paramerDao.update(pSalesEmployee);
                }
                LoginParameterUtil.salesEmployee = salesEmployee;
            }
            if (null != pSalesCustomer) {
                Paramer tsalesCustomer = paramerDao.find("salesCustomer");
                if (tsalesCustomer == null) {
                    count = paramerDao.insert(pSalesCustomer);
                } else {
                    count = paramerDao.update(pSalesCustomer);
                }
                LoginParameterUtil.salesCustomer = salesCustomer;
            }
            if (null != pSalesBrand) {
                Paramer tsalesBrand = paramerDao.find("salesBrand");
                if (tsalesBrand == null) {
                    count = paramerDao.insert(pSalesBrand);
                } else {
                    count = paramerDao.update(pSalesBrand);
                }
                LoginParameterUtil.salesBrand = salesBrand;
            }
            if (null != pSalesMemo) {
                Paramer tsalesMemo = paramerDao.find("salesMemo");
                if (tsalesMemo == null) {
                    count = paramerDao.insert(pSalesMemo);
                } else {
                    count = paramerDao.update(pSalesMemo);
                }
                LoginParameterUtil.salesMemo = salesMemo;
            }
            if (null != pSalesOrderNo) {
                Paramer tsalesOrderNo = paramerDao.find("salesOrderNo");
                if (tsalesOrderNo == null) {
                    count = paramerDao.insert(pSalesOrderNo);
                } else {
                    count = paramerDao.update(pSalesOrderNo);
                }
                LoginParameterUtil.salesOrderNo = salesOrderNo;
            }
            if (null != pSalesWarehouse) {
                Paramer tsalesWarehouse = paramerDao.find("salesWarehouse");
                if (tsalesWarehouse == null) {
                    count = paramerDao.insert(pSalesWarehouse);
                } else {
                    count = paramerDao.update(pSalesWarehouse);
                }
                LoginParameterUtil.salesWarehouse = salesWarehouse;
            }
            if (null != pSalesAmountSum) {
                Paramer tsalesAmountSum = paramerDao.find("salesAmountSum");
                if (tsalesAmountSum == null) {
                    count = paramerDao.insert(pSalesAmountSum);
                } else {
                    count = paramerDao.update(pSalesAmountSum);
                }
                LoginParameterUtil.salesAmountSum = salesAmountSum;
            }
            if (null != pSalesReturnEmployee) {
                Paramer tsalesReturnEmployee = paramerDao.find("salesReturnEmployee");
                if (tsalesReturnEmployee == null) {
                    count = paramerDao.insert(pSalesReturnEmployee);
                } else {
                    count = paramerDao.update(pSalesReturnEmployee);
                }
                LoginParameterUtil.salesReturnEmployee = salesReturnEmployee;
            }
            if (null != pSalesReturnCustomer) {
                Paramer tsalesReturnCustomer = paramerDao.find("salesReturnCustomer");
                if (tsalesReturnCustomer == null) {
                    count = paramerDao.insert(pSalesReturnCustomer);
                } else {
                    count = paramerDao.update(pSalesReturnCustomer);
                }
                LoginParameterUtil.salesReturnCustomer = salesReturnCustomer;
            }
            if (null != pSalesReturnBrand) {
                Paramer tsalesReturnBrand = paramerDao.find("salesReturnBrand");
                if (tsalesReturnBrand == null) {
                    count = paramerDao.insert(pSalesReturnBrand);
                } else {
                    count = paramerDao.update(pSalesReturnBrand);
                }
                LoginParameterUtil.salesReturnBrand = salesReturnBrand;
            }
            if (null != pSalesReturnMemo) {
                Paramer tsalesReturnMemo = paramerDao.find("salesReturnMemo");
                if (tsalesReturnMemo == null) {
                    count = paramerDao.insert(pSalesReturnMemo);
                } else {
                    count = paramerDao.update(pSalesReturnMemo);
                }
                LoginParameterUtil.salesReturnMemo = salesReturnMemo;
            }
            if (null != pSalesReturnWarehouse) {
                Paramer tsalesReturnWarehouse = paramerDao.find("salesReturnWarehouse");
                if (tsalesReturnWarehouse == null) {
                    count = paramerDao.insert(pSalesReturnWarehouse);
                } else {
                    count = paramerDao.update(pSalesReturnWarehouse);
                }
                LoginParameterUtil.salesReturnWarehouse = salesReturnWarehouse;
            }
            if (null != pSalesReturnDepartment) {
                Paramer tsalesReturnDepartment = paramerDao.find("salesReturnDepartment");
                if (tsalesReturnDepartment == null) {
                    count = paramerDao.insert(pSalesReturnDepartment);
                } else {
                    count = paramerDao.update(pSalesReturnDepartment);
                }
                LoginParameterUtil.salesReturnDepartment = salesReturnDepartment;
            }
            if (null != pSalesReturnAmountSum) {
                Paramer tsalesReturnAmountSum = paramerDao.find("salesReturnAmountSum");
                if (tsalesReturnAmountSum == null) {
                    count = paramerDao.insert(pSalesReturnAmountSum);
                } else {
                    count = paramerDao.update(pSalesReturnAmountSum);
                }
                LoginParameterUtil.salesReturnAmountSum = salesReturnAmountSum;
            }


            // 采购订单
            if (null != pPurchaseOrderEmployee) {
                Paramer tpurchaseOrderEmployee = paramerDao.find("purchaseOrderEmployee");
                if (tpurchaseOrderEmployee == null) {
                    count = paramerDao.insert(pPurchaseOrderEmployee);
                } else {
                    count = paramerDao.update(pPurchaseOrderEmployee);
                }
                LoginParameterUtil.purchaseOrderEmployee = purchaseOrderEmployee;
            }
            if (null != pPurchaseOrderSupplier) {
                Paramer tpurchaseOrderSupplier = paramerDao.find("purchaseOrderSupplier");
                if (tpurchaseOrderSupplier == null) {
                    count = paramerDao.insert(pPurchaseOrderSupplier);
                } else {
                    count = paramerDao.update(pPurchaseOrderSupplier);
                }
                LoginParameterUtil.purchaseOrderSupplier = purchaseOrderSupplier;
            }
            if (null != pPurchaseOrderBrand) {
                Paramer tpurchaseOrderBrand = paramerDao.find("purchaseOrderBrand");
                if (tpurchaseOrderBrand == null) {
                    count = paramerDao.insert(pPurchaseOrderBrand);
                } else {
                    count = paramerDao.update(pPurchaseOrderBrand);
                }
                LoginParameterUtil.purchaseOrderBrand = purchaseOrderBrand;
            }
            if (null != pPurchaseOrderMemo) {
                Paramer tpurchaseOrderMemo = paramerDao.find("purchaseOrderMemo");
                if (tpurchaseOrderMemo == null) {
                    count = paramerDao.insert(pPurchaseOrderMemo);
                } else {
                    count = paramerDao.update(pPurchaseOrderMemo);
                }
                LoginParameterUtil.purchaseOrderMemo = purchaseOrderMemo;
            }
            if (null != pPurchaseOrderDepartment) {
                Paramer tpurchaseOrderDepartment = paramerDao.find("purchaseOrderDepartment");
                if (tpurchaseOrderDepartment == null) {
                    count = paramerDao.insert(pPurchaseOrderDepartment);
                } else {
                    count = paramerDao.update(pPurchaseOrderDepartment);
                }
                LoginParameterUtil.purchaseOrderDepartment = purchaseOrderDepartment;
            }
            if (null != pPurchaseOrderAmountSum) {
                Paramer tpurchaseOrderAmountSum = paramerDao.find("purchaseOrderAmountSum");
                if (tpurchaseOrderAmountSum == null) {
                    count = paramerDao.insert(pPurchaseOrderAmountSum);
                } else {
                    count = paramerDao.update(pPurchaseOrderAmountSum);
                }
                LoginParameterUtil.purchaseOrderAmountSum = purchaseOrderAmountSum;
            }




            // 采购收货单
            if (null != pPurchaseEmployee) {
                Paramer tpurchaseEmployee = paramerDao.find("purchaseEmployee");
                if (tpurchaseEmployee == null) {
                    count = paramerDao.insert(pPurchaseEmployee);
                } else {
                    count = paramerDao.update(pPurchaseEmployee);
                }
                LoginParameterUtil.purchaseEmployee = purchaseEmployee;
            }
            if (null != pPurchaseSupplier) {
                Paramer tpurchaseSupplier = paramerDao.find("purchaseSupplier");
                if (tpurchaseSupplier == null) {
                    count = paramerDao.insert(pPurchaseSupplier);
                } else {
                    count = paramerDao.update(pPurchaseSupplier);
                }
                LoginParameterUtil.purchaseSupplier = purchaseSupplier;
            }
            if (null != pPurchaseBrand) {
                Paramer tpurchaseBrand = paramerDao.find("purchaseBrand");
                if (tpurchaseBrand == null) {
                    count = paramerDao.insert(pPurchaseBrand);
                } else {
                    count = paramerDao.update(pPurchaseBrand);
                }
                LoginParameterUtil.purchaseBrand = purchaseBrand;
            }
            if (null != pPurchaseMemo) {
                Paramer tpurchaseMemo = paramerDao.find("purchaseMemo");
                if (tpurchaseMemo == null) {
                    count = paramerDao.insert(pPurchaseMemo);
                } else {
                    count = paramerDao.update(pPurchaseMemo);
                }
                LoginParameterUtil.purchaseMemo = purchaseMemo;
            }
            if (null != pPurchaseDepartment) {
                Paramer tpurchaseDepartment = paramerDao.find("purchaseDepartment");
                if (tpurchaseDepartment == null) {
                    count = paramerDao.insert(pPurchaseDepartment);
                } else {
                    count = paramerDao.update(pPurchaseDepartment);
                }
                LoginParameterUtil.purchaseDepartment = purchaseDepartment;
            }
            if (null != pPurchaseAmountSum) {
                Paramer tpurchaseAmountSum = paramerDao.find("purchaseAmountSum");
                if (tpurchaseAmountSum == null) {
                    count = paramerDao.insert(pPurchaseAmountSum);
                } else {
                    count = paramerDao.update(pPurchaseAmountSum);
                }
                LoginParameterUtil.purchaseAmountSum = purchaseAmountSum;
            }
            // 采购退货单
            if (null != pPurchaseReturnEmployee) {
                Paramer tpurchaseReturnEmployee = paramerDao.find("purchaseReturnEmployee");
                if (tpurchaseReturnEmployee == null) {
                    count = paramerDao.insert(pPurchaseReturnEmployee);
                } else {
                    count = paramerDao.update(pPurchaseReturnEmployee);
                }
                LoginParameterUtil.purchaseReturnEmployee = purchaseReturnEmployee;
            }
            if (null != pPurchaseReturnSupplier) {
                Paramer tpurchaseReturnSupplier = paramerDao.find("purchaseReturnSupplier");
                if (tpurchaseReturnSupplier == null) {
                    count = paramerDao.insert(pPurchaseReturnSupplier);
                } else {
                    count = paramerDao.update(pPurchaseReturnSupplier);
                }
                LoginParameterUtil.purchaseReturnSupplier = purchaseReturnSupplier;
            }
            if (null != pPurchaseReturnBrand) {
                Paramer tpurchaseReturnBrand = paramerDao.find("purchaseReturnBrand");
                if (tpurchaseReturnBrand == null) {
                    count = paramerDao.insert(pPurchaseReturnBrand);
                } else {
                    count = paramerDao.update(pPurchaseReturnBrand);
                }
                LoginParameterUtil.purchaseReturnBrand = purchaseReturnBrand;
            }
            if (null != pPurchaseReturnMemo) {
                Paramer tpurchaseReturnMemo = paramerDao.find("purchaseReturnMemo");
                if (tpurchaseReturnMemo == null) {
                    count = paramerDao.insert(pPurchaseReturnMemo);
                } else {
                    count = paramerDao.update(pPurchaseReturnMemo);
                }
                LoginParameterUtil.purchaseReturnMemo = purchaseReturnMemo;
            }
            if (null != pPurchaseReturnDepartment) {
                Paramer tpurchaseReturnDepartment = paramerDao.find("purchaseReturnDepartment");
                if (tpurchaseReturnDepartment == null) {
                    count = paramerDao.insert(pPurchaseReturnDepartment);
                } else {
                    count = paramerDao.update(pPurchaseReturnDepartment);
                }
                LoginParameterUtil.purchaseReturnDepartment = purchaseReturnDepartment;
            }
            if (null != pPurchaseReturnAmountSum) {
                Paramer tpurchaseReturnAmountSum = paramerDao.find("purchaseReturnAmountSum");
                if (tpurchaseReturnAmountSum == null) {
                    count = paramerDao.insert(pPurchaseReturnAmountSum);
                } else {
                    count = paramerDao.update(pPurchaseReturnAmountSum);
                }
                LoginParameterUtil.purchaseReturnAmountSum = purchaseReturnAmountSum;
            }
            // 转仓单
            if (null != pStockMoveEmployee) {
                Paramer tstockMoveEmployee = paramerDao.find("stockMoveEmployee");
                if (tstockMoveEmployee == null) {
                    count = paramerDao.insert(pStockMoveEmployee);
                } else {
                    count = paramerDao.update(pStockMoveEmployee);
                }
                LoginParameterUtil.stockMoveEmployee = stockMoveEmployee;
            }
            if (null != pStockMoveMemo) {
                Paramer tstockMoveMemo = paramerDao.find("stockMoveMemo");
                if (tstockMoveMemo == null) {
                    count = paramerDao.insert(pStockMoveMemo);
                } else {
                    count = paramerDao.update(pStockMoveMemo);
                }
                LoginParameterUtil.stockMoveMemo = stockMoveMemo;
            }
            if (null != pStockMoveWarehouseIn) {
                Paramer tstockMoveWarehouseIn = paramerDao.find("stockMoveWarehouseIn");
                if (tstockMoveWarehouseIn == null) {
                    count = paramerDao.insert(pStockMoveWarehouseIn);
                } else {
                    count = paramerDao.update(pStockMoveWarehouseIn);
                }
                LoginParameterUtil.stockMoveWarehouseIn = stockMoveWarehouseIn;
            }
            if (null != pStockMoveWarehouseOut) {
                Paramer tstockMoveWarehouseOut = paramerDao.find("stockMoveWarehouseOut");
                if (tstockMoveWarehouseOut == null) {
                    count = paramerDao.insert(pStockMoveWarehouseOut);
                } else {
                    count = paramerDao.update(pStockMoveWarehouseOut);
                }
                LoginParameterUtil.stockMoveWarehouseOut = stockMoveWarehouseOut;
            }
            if (null != pStockMoveBrand) {
                Paramer tstockMoveBrand = paramerDao.find("stockMoveBrand");
                if (tstockMoveBrand == null) {
                    count = paramerDao.insert(pStockMoveBrand);
                } else {
                    count = paramerDao.update(pStockMoveBrand);
                }
                LoginParameterUtil.stockMoveBrand = stockMoveBrand;
            }
            if (null != pStockMoveAmountSum) {
                Paramer tstockMoveAmountSum = paramerDao.find("stockMoveAmountSum");
                if (tstockMoveAmountSum == null) {
                    count = paramerDao.insert(pStockMoveAmountSum);
                } else {
                    count = paramerDao.update(pStockMoveAmountSum);
                }
                LoginParameterUtil.stockMoveAmountSum = stockMoveAmountSum;
            }
            // 进仓单
            if (null != pStockInEmployee) {
                Paramer tstockInEmployee = paramerDao.find("stockInEmployee");
                if (tstockInEmployee == null) {
                    count = paramerDao.insert(pStockInEmployee);
                } else {
                    count = paramerDao.update(pStockInEmployee);
                }
                LoginParameterUtil.stockInEmployee = stockInEmployee;
            }
            if (null != pStockInMemo) {
                Paramer tstockInMemo = paramerDao.find("stockInMemo");
                if (tstockInMemo == null) {
                    count = paramerDao.insert(pStockInMemo);
                } else {
                    count = paramerDao.update(pStockInMemo);
                }
                LoginParameterUtil.stockInMemo = stockInMemo;
            }
            if (null != pStockInWarehouse) {
                Paramer tstockInWarehouse = paramerDao.find("stockInWarehouse");
                if (tstockInWarehouse == null) {
                    count = paramerDao.insert(pStockInWarehouse);
                } else {
                    count = paramerDao.update(pStockInWarehouse);
                }
                LoginParameterUtil.stockInWarehouse = stockInWarehouse;
            }
            if (null != pStockInWarehouseOut) {
                Paramer tstockInWarehouseOut = paramerDao.find("stockInWarehouseOut");
                if (tstockInWarehouseOut == null) {
                    count = paramerDao.insert(pStockInWarehouseOut);
                } else {
                    count = paramerDao.update(pStockInWarehouseOut);
                }
                LoginParameterUtil.stockInWarehouseOut = stockInWarehouseOut;
            }
            if (null != pStockInBrand) {
                Paramer tstockInBrand = paramerDao.find("stockInBrand");
                if (tstockInBrand == null) {
                    count = paramerDao.insert(pStockInBrand);
                } else {
                    count = paramerDao.update(pStockInBrand);
                }
                LoginParameterUtil.stockInBrand = stockInBrand;
            }
            if (null != pStockInRelationNo) {
                Paramer tstockInRelationNo = paramerDao.find("stockInRelationNo");
                if (tstockInRelationNo == null) {
                    count = paramerDao.insert(pStockInRelationNo);
                } else {
                    count = paramerDao.update(pStockInRelationNo);
                }
                LoginParameterUtil.stockInRelationNo = stockInRelationNo;
            }
            if (null != pStockInAmountSum) {
                Paramer tstockInAmountSum = paramerDao.find("stockInAmountSum");
                if (tstockInAmountSum == null) {
                    count = paramerDao.insert(pStockInAmountSum);
                } else {
                    count = paramerDao.update(pStockInAmountSum);
                }
                LoginParameterUtil.stockInAmountSum = stockInAmountSum;
            }
            // 出仓单
            if (null != pStockOutEmployee) {
                Paramer tstockOutEmployee = paramerDao.find("stockOutEmployee");
                if (tstockOutEmployee == null) {
                    count = paramerDao.insert(pStockOutEmployee);
                } else {
                    count = paramerDao.update(pStockOutEmployee);
                }
                LoginParameterUtil.stockOutEmployee = stockOutEmployee;
            }
            if (null != pStockOutMemo) {
                Paramer tstockOutMemo = paramerDao.find("stockOutMemo");
                if (tstockOutMemo == null) {
                    count = paramerDao.insert(pStockOutMemo);
                } else {
                    count = paramerDao.update(pStockOutMemo);
                }
                LoginParameterUtil.stockOutMemo = stockOutMemo;
            }
            if (null != pStockOutWarehouse) {
                Paramer tstockOutWarehouse = paramerDao.find("stockOutWarehouse");
                if (tstockOutWarehouse == null) {
                    count = paramerDao.insert(pStockOutWarehouse);
                } else {
                    count = paramerDao.update(pStockOutWarehouse);
                }
                LoginParameterUtil.stockOutWarehouse = stockOutWarehouse;
            }
            if (null != pStockOutWarehouseIn) {
                Paramer tstockOutWarehouseIn = paramerDao.find("stockOutWarehouseIn");
                if (tstockOutWarehouseIn == null) {
                    count = paramerDao.insert(pStockOutWarehouseIn);
                } else {
                    count = paramerDao.update(pStockOutWarehouseIn);
                }
                LoginParameterUtil.stockOutWarehouseIn = stockOutWarehouseIn;
            }
            if (null != pStockOutBrand) {
                Paramer tstockOutBrand = paramerDao.find("stockOutBrand");
                if (tstockOutBrand == null) {
                    count = paramerDao.insert(pStockOutBrand);
                } else {
                    count = paramerDao.update(pStockOutBrand);
                }
                LoginParameterUtil.stockOutBrand = stockOutBrand;
            }
            if (null != pStockOutRelationNo) {
                Paramer tstockOutRelationNo = paramerDao.find("stockOutRelationNo");
                if (tstockOutRelationNo == null) {
                    count = paramerDao.insert(pStockOutRelationNo);
                } else {
                    count = paramerDao.update(pStockOutRelationNo);
                }
                LoginParameterUtil.stockOutRelationNo = stockOutRelationNo;
            }
            if (null != pStockOutAmountSum) {
                Paramer tstockOutAmountSum = paramerDao.find("stockOutAmountSum");
                if (tstockOutAmountSum == null) {
                    count = paramerDao.insert(pStockOutAmountSum);
                } else {
                    count = paramerDao.update(pStockOutAmountSum);
                }
                LoginParameterUtil.stockOutAmountSum = stockOutAmountSum;
            }
            // 盘点单
            if (null != pStocktakingEmployee) {
                Paramer tstocktakingEmployee = paramerDao.find("stocktakingEmployee");
                if (tstocktakingEmployee == null) {
                    count = paramerDao.insert(pStocktakingEmployee);
                } else {
                    count = paramerDao.update(pStocktakingEmployee);
                }
                LoginParameterUtil.stocktakingEmployee = stocktakingEmployee;
            }
            if (null != pStocktakingMemo) {
                Paramer tstocktakingMemo = paramerDao.find("stocktakingMemo");
                if (tstocktakingMemo == null) {
                    count = paramerDao.insert(pStocktakingMemo);
                } else {
                    count = paramerDao.update(pStocktakingMemo);
                }
                LoginParameterUtil.stocktakingMemo = stocktakingMemo;
            }
            if (null != pStocktakingWarehouse) {
                Paramer tstocktakingWarehouse = paramerDao.find("stocktakingWarehouse");
                if (tstocktakingWarehouse == null) {
                    count = paramerDao.insert(pStocktakingWarehouse);
                } else {
                    count = paramerDao.update(pStocktakingWarehouse);
                }
                LoginParameterUtil.stocktakingWarehouse = stocktakingWarehouse;
            }
            if (null != pStocktakingBrand) {
                Paramer tstocktakingBrand = paramerDao.find("stocktakingBrand");
                if (tstocktakingBrand == null) {
                    count = paramerDao.insert(pStocktakingBrand);
                } else {
                    count = paramerDao.update(pStocktakingBrand);
                }
                LoginParameterUtil.stocktakingBrand = stocktakingBrand;
            }
            if (null != pStocktakingAmountSum) {
                Paramer tstocktakingAmountSum = paramerDao.find("stocktakingAmountSum");
                if (tstocktakingAmountSum == null) {
                    count = paramerDao.insert(pStocktakingAmountSum);
                } else {
                    count = paramerDao.update(pStocktakingAmountSum);
                }
                LoginParameterUtil.stocktakingAmountSum = stocktakingAmountSum;
            }
            // 条码打印
            if (null != pShowGoodsCode) {
                Paramer tshowGoodsCode = paramerDao.find("showGoodsCode");
                if (tshowGoodsCode == null) {
                    count = paramerDao.insert(pShowGoodsCode);
                } else {
                    count = paramerDao.update(pShowGoodsCode);
                }
                LoginParameterUtil.showGoodsCode = showGoodsCode;
            }
            if (null != pShowGoodsName) {
                Paramer tshowGoodsName = paramerDao.find("showGoodsName");
                if (tshowGoodsName == null) {
                    count = paramerDao.insert(pShowGoodsName);
                } else {
                    count = paramerDao.update(pShowGoodsName);
                }
                LoginParameterUtil.showGoodsName = showGoodsName;
            }
            if (null != pShowColor) {
                Paramer tshowColor = paramerDao.find("showColor");
                if (tshowColor == null) {
                    count = paramerDao.insert(pShowColor);
                } else {
                    count = paramerDao.update(pShowColor);
                }
                LoginParameterUtil.showColor = showColor;
            }
            if (null != pShowSize) {
                Paramer tshowSize = paramerDao.find("showSize");
                if (tshowSize == null) {
                    count = paramerDao.insert(pShowSize);
                } else {
                    count = paramerDao.update(pShowSize);
                }
                LoginParameterUtil.showSize = showSize;
            }
            if (null != pShowRetailSales) {
                Paramer tshowRetailSales = paramerDao.find("showRetailSales");
                if (tshowRetailSales == null) {
                    count = paramerDao.insert(pShowRetailSales);
                } else {
                    count = paramerDao.update(pShowRetailSales);
                }
                LoginParameterUtil.showRetailSales = showRetailSales;
            }
            // 装箱单
            if (null != pPackingBoxRelationType) {
                Paramer tpStorageOutType = paramerDao.find("packingBoxRelationType");
                if (tpStorageOutType == null) {
                    count = paramerDao.insert(pPackingBoxRelationType);
                } else {
                    count = paramerDao.update(pPackingBoxRelationType);
                }
                LoginParameterUtil.packingBoxRelationType = packingBoxRelationType;
            }
            // 多货品颜色尺码录入
            if (null != pGoodsMultiSelectType) {
                Paramer tpGoodsMultiSelectType = paramerDao.find("goodsMultiSelectType");
                if (tpGoodsMultiSelectType == null) {
                    count = paramerDao.insert(pGoodsMultiSelectType);
                } else {
                    count = paramerDao.update(pGoodsMultiSelectType);
                }
                LoginParameterUtil.goodsMultiSelectType = goodsMultiSelectType;
            }
            // 多货品颜色尺码录入
            if (null != pMultiSelectType) {
                Paramer tpMultiSelectType = paramerDao.find("multiSelectType");
                if (tpMultiSelectType == null) {
                    count = paramerDao.insert(pMultiSelectType);
                } else {
                    count = paramerDao.update(pMultiSelectType);
                }
                LoginParameterUtil.multiSelectType = multiSelectType;
            }

            // 保存成功
            // Toast.makeText(SettingActivity.this, R.string.save_success,
            // Toast.LENGTH_SHORT).show();
            LoginParameterUtil.customer.setPrintIp(strPrintIp);
            LoginParameterUtil.customer.setPrintPort(strPrintPort);
            LoginParameterUtil.customer.setPrinter(strPrinter);
            LoginParameterUtil.customer.setPrintBarcodeHeight(strPrintBarcodeHeight);
            LoginParameterUtil.customer.setPrintBarcodeWidth(strPrintBarcodeWidth);
            LoginParameterUtil.customer.setPrintBarcodeType(strPrintBarcodeType);
            Builder dialog = new AlertDialog.Builder(SettingActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("系统提示");
            dialog.setMessage("设置保存成功！");
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            // 相当于点击取消按钮
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        } catch (Exception e) {
            // 保存失败
            Toast.makeText(SettingActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
            Logger.e(TAG, "设置信息保存失败");
        }
    }

    /**
     * 获取当前程序的版本号
     * 
     * @return
     * @throws Exception
     */
    private String getVersionName() throws Exception {
        // 获取PackageManager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        return packInfo.versionName;
    }

    /**
     * 检查是否有最新版本
     */
    private void checkVersion() {
        try {
            if (info.getVersion().equals(getVersionName())) {
                AlertDialog.Builder builer = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
                builer.setTitle("版本升级");
                builer.setMessage("当前已是最新版本V" + getVersionName());
                // 当点确定按钮时从服务器上下载 新的apk 然后安装
                builer.setPositiveButton("确定", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "版本号相同无需升级");
                    }
                });
                builer.setCancelable(false);
                AlertDialog dialog = builer.create();
                dialog.show();
            } else {
                Log.i(TAG, "版本号不同 ,提示用户升级 ");
                Message msg = new Message();
                msg.what = UPDATA_CLIENT;
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            Toast.makeText(SettingActivity.this, "系统错误", Toast.LENGTH_LONG).show();
            Logger.e(TAG, e.getMessage());
        }
    }

    /**
     * 下载最新版本的APK程序
     */
    protected void showUpdataDialog() {
        AlertDialog.Builder builer = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        builer.setTitle("版本升级");
        builer.setMessage(info.getDescription());
        // 当点确定按钮时从服务器上下载 新的apk 然后安装
        builer.setPositiveButton("确定", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "下载apk,更新");
                downLoadApk();
            }
        });
        // 当点取消按钮时进行登录
        if (!forceUpdate) {
            builer.setNegativeButton("取消", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });
        }
        builer.setCancelable(false);
        AlertDialog dialog = builer.create();
        dialog.show();
    }

    /**
     * 连接服务器中下载APK
     */
    protected void downLoadApk() {
        pd = new ProgressDialog(this); // 进度条对话框
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("正在下载更新");
        pd.show();
        new Thread() {
            @Override
            public void run() {
                try {
                    File file = getFileFromServer(strIp + info.getUrl(), pd);
                    sleep(3000);
                    installApk(file);
                    pd.dismiss(); // 结束掉进度条对话框
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = DOWN_ERROR;
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 安装APK程序
     * 
     * @param file
     */
    protected void installApk(File file) {
        // boolean flag =
        // InstallSlientManager.deleteDatabase(getApplicationContext());
        // if(flag){
        InstallSlientManager.install(file.getAbsolutePath(), getApplicationContext());
        // }
        finish();
    }

    /**
     * 处理线程返回的信息
     */
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATA_CLIENT:
                    // 对话框通知用户升级程序
                    showUpdataDialog();
                    break;
                case GET_UNDATAINFO_ERROR:
                    // 服务器超时
                    boolean flag = NetUtil.hasNetwork(getApplicationContext());
                    if (!flag) {
                        Toast.makeText(SettingActivity.this, "当前无网络连接,请检查网络设置后重试", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DOWN_ERROR:
                    // 下载APK失败
                    Toast.makeText(SettingActivity.this, "下载新版本失败", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                    break;
            }
        }
    };

    /**
     * 获取当前服务器路径
     */
    private void getRequestPath() {
        String flagStr = this.getString(R.string.inner_version);
        innerVersion = Boolean.parseBoolean(flagStr);
        if (innerVersion) {
            // 内部版本
            strIp = this.getString(R.string.inner_ip);
        } else {
            // 对外版本
            Paramer ip = paramerDao.find("ip");
            if (null != ip) {
                strIp = ip.getValue();
            } else {
                Properties userInfo = ParamterFileUtil.getIpInfo(this);
                if (null != userInfo) {
                    strIp = userInfo.getProperty("path");
                }
            }
        }
    }

    /**
     * 单选打印机
     * 
     * @param view
     */
    private void showPrinterListAlertDialog(View view) {
        if (printers == null || "".equals(printers)) {
            Toast.makeText(SettingActivity.this, "获取打印机列表失败，请检查打印机IP和端口是否正确，或稍后重试", Toast.LENGTH_LONG).show();
        } else {
            listPrinters = printers.split(",");
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            alertBuilder.setTitle("选择打印机");
            alertBuilder.setItems(listPrinters, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int index) {
                    String tprinter = listPrinters[index];
                    etPrinter.setText(tprinter);
                    // 关闭提示框
                    alertDialog.dismiss();
                }
            });
            alertDialog = alertBuilder.create();
            alertDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.base_setting:
                tv_BaseSetting.setChecked(true);
                tv_InputSetting.setChecked(false);
                tv_BarcodePrintSetting.setChecked(false);
                tv_DocuSetting.setChecked(false);
                ll_goods_info.setVisibility(View.VISIBLE);
                ll_docu_setting.setVisibility(View.GONE);
                ll_input_setting.setVisibility(View.GONE);
                ll_barcode_print_setting.setVisibility(View.GONE);
                break;
            case R.id.barcode_print_setting:
                tv_BaseSetting.setChecked(false);
                tv_InputSetting.setChecked(false);
                tv_DocuSetting.setChecked(false);
                tv_BarcodePrintSetting.setChecked(true);
                ll_goods_info.setVisibility(View.GONE);
                ll_input_setting.setVisibility(View.GONE);
                ll_docu_setting.setVisibility(View.GONE);
                ll_barcode_print_setting.setVisibility(View.VISIBLE);
                break;
            case R.id.docu_setting:
                tv_BaseSetting.setChecked(false);
                tv_InputSetting.setChecked(false);
                tv_BarcodePrintSetting.setChecked(false);
                tv_DocuSetting.setChecked(true);
                ll_goods_info.setVisibility(View.GONE);
                ll_input_setting.setVisibility(View.GONE);
                ll_barcode_print_setting.setVisibility(View.GONE);
                ll_docu_setting.setVisibility(View.VISIBLE);
                break;
            case R.id.input_setting:
                tv_BaseSetting.setChecked(false);
                tv_InputSetting.setChecked(true);
                tv_BarcodePrintSetting.setChecked(false);
                tv_DocuSetting.setChecked(false);
                ll_goods_info.setVisibility(View.GONE);
                ll_input_setting.setVisibility(View.VISIBLE);
                ll_barcode_print_setting.setVisibility(View.GONE);
                ll_docu_setting.setVisibility(View.GONE);
                break;
            case R.id.save:
                save();
                break;
            case R.id.ftv_printer:
                // 选择打印机
                showPrinterListAlertDialog(v);
                break;
            case R.id.ll_update:
                // 监测新版本
                getRequestPath();
                getUpdataInfo();
                break;
            case R.id.ll_above:
                // 关于我们
                Intent intent = new Intent(SettingActivity.this, AboveUsActivity.class);
                startActivity(intent);
                break;
            case R.id.useGoodsboxBarcodeInStocktaking:
            case R.id.tvUseGoodsboxBarcodeInStocktaking:
                // 单据显示经手人和备注
                if (useGoodsboxBarcodeInStocktaking) {
                    useGoodsboxBarcodeInStocktaking = false;
                } else {
                    useGoodsboxBarcodeInStocktaking = true;
                }
                if (null == pUseGoodsboxBarcodeInStocktaking) {
                    pUseGoodsboxBarcodeInStocktaking = new Paramer("useGoodsboxBarcodeInStocktaking", String.valueOf(useGoodsboxBarcodeInStocktaking));
                } else {
                    pUseGoodsboxBarcodeInStocktaking.setValue(String.valueOf(useGoodsboxBarcodeInStocktaking));
                }
                cb_useGoodsboxBarcodeInStocktaking.setChecked(useGoodsboxBarcodeInStocktaking);
                break;
            case R.id.useSupplierCodeToAreaProtection:
            case R.id.tvUseSupplierCodeToAreaProtection:
                // 单据显示经手人和备注
                if (useSupplierCodeToAreaProtection) {
                    useSupplierCodeToAreaProtection = false;
                } else {
                    useSupplierCodeToAreaProtection = true;
                }
                if (null == pUseSupplierCodeToAreaProtection) {
                    pUseSupplierCodeToAreaProtection = new Paramer("useSupplierCodeToAreaProtection", String.valueOf(useSupplierCodeToAreaProtection));
                } else {
                    pUseSupplierCodeToAreaProtection.setValue(String.valueOf(useSupplierCodeToAreaProtection));
                }
                cb_useSupplierCodeToAreaProtection.setChecked(useSupplierCodeToAreaProtection);
                break;
            case R.id.coverDoc:
            case R.id.tvCoverDoc:
                if (!adminValidate) {
                    // 弹出管理员验证界面
                    showAdminLogin();
                    tempView = v;
                } else {
                    // 设置权限
                    updateCoverSaveState();
                }
                break;
            case R.id.notAllow:
            case R.id.tvNotAllow:
                if (!adminValidate) {
                    // 弹出管理员验证界面
                    showAdminLogin();
                    tempView = v;
                } else {
                    // 设置权限
                    updateNotAllowState();
                }
                break;
            case R.id.useDefSupplier:
            case R.id.tvUseDefSupplier:
                // 采购单据中使用货品默认的厂商
                if (useDefSupplier) {
                    useDefSupplier = false;
                } else {
                    useDefSupplier = true;
                }
                if (null == pUseDefSupplier) {
                    pUseDefSupplier = new Paramer("useDefSupplier", String.valueOf(useDefSupplier));
                } else {
                    pUseDefSupplier.setValue(String.valueOf(useDefSupplier));
                }
                cb_useDefSupplier.setChecked(useDefSupplier);
                break;
            case R.id.firstInputOfGoodsCode:
            case R.id.tvFirstInputOfGoodsCode:
                // 采购单据中使用货品默认的厂商
                if (firstInputOfGoodsCode) {
                    firstInputOfGoodsCode = false;
                } else {
                    firstInputOfGoodsCode = true;
                }
                if (null == pFirstInputOfGoodsCode) {
                    pFirstInputOfGoodsCode = new Paramer("firstInputOfGoodsCode", String.valueOf(firstInputOfGoodsCode));
                } else {
                    pFirstInputOfGoodsCode.setValue(String.valueOf(firstInputOfGoodsCode));
                }
                cb_firstInputOfGoodsCode.setChecked(firstInputOfGoodsCode);
                break;
            case R.id.notUseNegativeInventoryCheck:
            case R.id.tvNotUseNegativeInventoryCheck:
                // 采购单据中使用货品默认的厂商
                if (notUseNegativeInventoryCheck) {
                    notUseNegativeInventoryCheck = false;
                } else {
                    notUseNegativeInventoryCheck = true;
                }
                if (null == pNotUseNegativeInventoryCheck) {
                    pNotUseNegativeInventoryCheck = new Paramer("notUseNegativeInventoryCheck", String.valueOf(notUseNegativeInventoryCheck));
                } else {
                    pNotUseNegativeInventoryCheck.setValue(String.valueOf(notUseNegativeInventoryCheck));
                }
                cb_notUseNegativeInventoryCheck.setChecked(notUseNegativeInventoryCheck);
                break;
            case R.id.preciseToQueryStock:
            case R.id.tvPreciseToQueryStock:
                // 采购单据中使用货品默认的厂商
                if (preciseToQueryStock) {
                    preciseToQueryStock = false;
                } else {
                    preciseToQueryStock = true;
                }
                if (null == pPreciseToQueryStock) {
                    pPreciseToQueryStock = new Paramer("preciseToQueryStock", String.valueOf(preciseToQueryStock));
                } else {
                    pPreciseToQueryStock.setValue(String.valueOf(preciseToQueryStock));
                }
                cb_preciseToQueryStock.setChecked(preciseToQueryStock);
                break;
            case R.id.barcodeWarningTone:
            case R.id.tvBarcodeWarningTone:
                // 条码扫描错误时使用声音提示
                if (barcodeWarningTone) {
                    barcodeWarningTone = false;
                } else {
                    barcodeWarningTone = true;
                }
                if (null == pBarcodeWarningTone) {
                    pBarcodeWarningTone = new Paramer("barcodeWarningTone", String.valueOf(barcodeWarningTone));
                } else {
                    pBarcodeWarningTone.setValue(String.valueOf(barcodeWarningTone));
                }
                cb_barcodeWarningTone.setChecked(barcodeWarningTone);
                break;
            case R.id.displayInventory:
            case R.id.tvDisplayInventory:
                // 条码扫描错误时使用声音提示
                if (displayInventory) {
                    displayInventory = false;
                } else {
                    displayInventory = true;
                }
                if (null == pDisplayInventory) {
                    pDisplayInventory = new Paramer("displayInventory", String.valueOf(displayInventory));
                } else {
                    pDisplayInventory.setValue(String.valueOf(displayInventory));
                }
                cb_displayInventory.setChecked(displayInventory);
                break;
            case R.id.useSingleDiscount:
            case R.id.tvUseSingleDiscount:
                // 条码扫描错误时使用声音提示
                if (useSingleDiscount) {
                    useSingleDiscount = false;
                } else {
                    useSingleDiscount = true;
                }
                if (null == pUseSingleDiscount) {
                    pUseSingleDiscount = new Paramer("useSingleDiscount", String.valueOf(useSingleDiscount));
                } else {
                    pUseSingleDiscount.setValue(String.valueOf(useSingleDiscount));
                }
                cb_useSingleDiscount.setChecked(useSingleDiscount);
                break;
            case R.id.salesOrderEmployee:
            case R.id.tvSalesOrderEmployee:
                // 条码扫描错误时使用声音提示
                if (salesOrderEmployee) {
                    salesOrderEmployee = false;
                } else {
                    salesOrderEmployee = true;
                }
                if (null == pSalesOrderEmployee) {
                    pSalesOrderEmployee = new Paramer("salesOrderEmployee", String.valueOf(salesOrderEmployee));
                } else {
                    pSalesOrderEmployee.setValue(String.valueOf(salesOrderEmployee));
                }
                cb_salesOrderEmployee.setChecked(salesOrderEmployee);
                break;
            case R.id.salesOrderCustomer:
            case R.id.tvSalesOrderCustomer:
                // 条码扫描错误时使用声音提示
                if (salesOrderCustomer) {
                    salesOrderCustomer = false;
                } else {
                    salesOrderCustomer = true;
                }
                if (null == pSalesOrderCustomer) {
                    pSalesOrderCustomer = new Paramer("salesOrderCustomer", String.valueOf(salesOrderCustomer));
                } else {
                    pSalesOrderCustomer.setValue(String.valueOf(salesOrderCustomer));
                }
                cb_salesOrderCustomer.setChecked(salesOrderCustomer);
                break;
            case R.id.salesOrderBrand:
            case R.id.tvSalesOrderBrand:
                // 条码扫描错误时使用声音提示
                if (salesOrderBrand) {
                    salesOrderBrand = false;
                } else {
                    salesOrderBrand = true;
                }
                if (null == pSalesOrderBrand) {
                    pSalesOrderBrand = new Paramer("salesOrderBrand", String.valueOf(salesOrderBrand));
                } else {
                    pSalesOrderBrand.setValue(String.valueOf(salesOrderBrand));
                }
                cb_salesOrderBrand.setChecked(salesOrderBrand);
                break;
            case R.id.salesOrderMemo:
            case R.id.tvSalesOrderMemo:
                // 条码扫描错误时使用声音提示
                if (salesOrderMemo) {
                    salesOrderMemo = false;
                } else {
                    salesOrderMemo = true;
                }
                if (null == pSalesOrderMemo) {
                    pSalesOrderMemo = new Paramer("salesOrderMemo", String.valueOf(salesOrderMemo));
                } else {
                    pSalesOrderMemo.setValue(String.valueOf(salesOrderMemo));
                }
                cb_salesOrderMemo.setChecked(salesOrderMemo);
                break;
            case R.id.salesOrderDepartment:
            case R.id.tvSalesOrderDepartment:
                // 条码扫描错误时使用声音提示
                if (salesOrderDepartment) {
                    salesOrderDepartment = false;
                } else {
                    salesOrderDepartment = true;
                }
                if (null == pSalesOrderDepartment) {
                    pSalesOrderDepartment = new Paramer("salesOrderDepartment", String.valueOf(salesOrderDepartment));
                } else {
                    pSalesOrderDepartment.setValue(String.valueOf(salesOrderDepartment));
                }
                cb_salesOrderDepartment.setChecked(salesOrderDepartment);
                break;
            case R.id.salesOrderWarehouse:
            case R.id.tvSalesOrderWarehouse:
                // 条码扫描错误时使用声音提示
                if (salesOrderWarehouse) {
                    salesOrderWarehouse = false;
                } else {
                    salesOrderWarehouse = true;
                }
                if (null == pSalesOrderWarehouse) {
                    pSalesOrderWarehouse = new Paramer("salesOrderWarehouse", String.valueOf(salesOrderWarehouse));
                } else {
                    pSalesOrderWarehouse.setValue(String.valueOf(salesOrderWarehouse));
                }
                cb_salesOrderWarehouse.setChecked(salesOrderWarehouse);
                break;
            case R.id.salesOrderAmountSum:
            case R.id.tvSalesOrderAmountSum:
                // 条码扫描错误时使用声音提示
                if (salesOrderAmountSum) {
                    salesOrderAmountSum = false;
                } else {
                    salesOrderAmountSum = true;
                }
                if (null == pSalesOrderAmountSum) {
                    pSalesOrderAmountSum = new Paramer("salesOrderAmountSum", String.valueOf(salesOrderAmountSum));
                } else {
                    pSalesOrderAmountSum.setValue(String.valueOf(salesOrderAmountSum));
                }
                cb_salesOrderAmountSum.setChecked(salesOrderAmountSum);
                break;
            case R.id.salesEmployee:
            case R.id.tvSalesEmployee:
                // 条码扫描错误时使用声音提示
                if (salesEmployee) {
                    salesEmployee = false;
                } else {
                    salesEmployee = true;
                }
                if (null == pSalesEmployee) {
                    pSalesEmployee = new Paramer("salesEmployee", String.valueOf(salesEmployee));
                } else {
                    pSalesEmployee.setValue(String.valueOf(salesEmployee));
                }
                cb_salesEmployee.setChecked(salesEmployee);
                break;
            case R.id.salesCustomer:
            case R.id.tvSalesCustomer:
                // 条码扫描错误时使用声音提示
                if (salesCustomer) {
                    salesCustomer = false;
                } else {
                    salesCustomer = true;
                }
                if (null == pSalesCustomer) {
                    pSalesCustomer = new Paramer("salesCustomer", String.valueOf(salesCustomer));
                } else {
                    pSalesCustomer.setValue(String.valueOf(salesCustomer));
                }
                cb_salesCustomer.setChecked(salesCustomer);
                break;
            case R.id.salesBrand:
            case R.id.tvSalesBrand:
                // 条码扫描错误时使用声音提示
                if (salesBrand) {
                    salesBrand = false;
                } else {
                    salesBrand = true;
                }
                if (null == pSalesBrand) {
                    pSalesBrand = new Paramer("salesBrand", String.valueOf(salesBrand));
                } else {
                    pSalesBrand.setValue(String.valueOf(salesBrand));
                }
                cb_salesBrand.setChecked(salesBrand);
                break;
            case R.id.salesMemo:
            case R.id.tvSalesMemo:
                // 条码扫描错误时使用声音提示
                if (salesMemo) {
                    salesMemo = false;
                } else {
                    salesMemo = true;
                }
                if (null == pSalesMemo) {
                    pSalesMemo = new Paramer("salesMemo", String.valueOf(salesMemo));
                } else {
                    pSalesMemo.setValue(String.valueOf(salesMemo));
                }
                cb_salesMemo.setChecked(salesMemo);
                break;
            case R.id.salesOrderNo:
            case R.id.tvSalesOrderNo:
                // 条码扫描错误时使用声音提示
                if (salesOrderNo) {
                    salesOrderNo = false;
                } else {
                    salesOrderNo = true;
                }
                if (null == pSalesOrderNo) {
                    pSalesOrderNo = new Paramer("salesOrderNo", String.valueOf(salesOrderNo));
                } else {
                    pSalesOrderNo.setValue(String.valueOf(salesOrderNo));
                }
                cb_salesOrderNo.setChecked(salesOrderNo);
                break;
            case R.id.salesWarehouse:
            case R.id.tvSalesWarehouse:
                // 条码扫描错误时使用声音提示
                if (salesWarehouse) {
                    salesWarehouse = false;
                } else {
                    salesWarehouse = true;
                }
                if (null == pSalesWarehouse) {
                    pSalesWarehouse = new Paramer("salesWarehouse", String.valueOf(salesWarehouse));
                } else {
                    pSalesWarehouse.setValue(String.valueOf(salesWarehouse));
                }
                cb_salesWarehouse.setChecked(salesWarehouse);
                break;
            case R.id.salesAmountSum:
            case R.id.tvSalesAmountSum:
                // 条码扫描错误时使用声音提示
                if (salesAmountSum) {
                    salesAmountSum = false;
                } else {
                    salesAmountSum = true;
                }
                if (null == pSalesAmountSum) {
                    pSalesAmountSum = new Paramer("salesAmountSum", String.valueOf(salesAmountSum));
                } else {
                    pSalesAmountSum.setValue(String.valueOf(salesAmountSum));
                }
                cb_salesAmountSum.setChecked(salesAmountSum);
                break;
            case R.id.salesReturnEmployee:
            case R.id.tvSalesReturnEmployee:
                // 条码扫描错误时使用声音提示
                if (salesReturnEmployee) {
                    salesReturnEmployee = false;
                } else {
                    salesReturnEmployee = true;
                }
                if (null == pSalesReturnEmployee) {
                    pSalesReturnEmployee = new Paramer("salesReturnEmployee", String.valueOf(salesReturnEmployee));
                } else {
                    pSalesReturnEmployee.setValue(String.valueOf(salesReturnEmployee));
                }
                cb_salesReturnEmployee.setChecked(salesReturnEmployee);
                break;
            case R.id.salesReturnCustomer:
            case R.id.tvSalesReturnCustomer:
                // 条码扫描错误时使用声音提示
                if (salesReturnCustomer) {
                    salesReturnCustomer = false;
                } else {
                    salesReturnCustomer = true;
                }
                if (null == pSalesReturnCustomer) {
                    pSalesReturnCustomer = new Paramer("salesReturnCustomer", String.valueOf(salesReturnCustomer));
                } else {
                    pSalesReturnCustomer.setValue(String.valueOf(salesReturnCustomer));
                }
                cb_salesReturnCustomer.setChecked(salesReturnCustomer);
                break;
            case R.id.salesReturnBrand:
            case R.id.tvSalesReturnBrand:
                // 条码扫描错误时使用声音提示
                if (salesReturnBrand) {
                    salesReturnBrand = false;
                } else {
                    salesReturnBrand = true;
                }
                if (null == pSalesReturnBrand) {
                    pSalesReturnBrand = new Paramer("salesReturnBrand", String.valueOf(salesReturnBrand));
                } else {
                    pSalesReturnBrand.setValue(String.valueOf(salesReturnBrand));
                }
                cb_salesReturnBrand.setChecked(salesReturnBrand);
                break;
            case R.id.salesReturnMemo:
            case R.id.tvSalesReturnMemo:
                // 条码扫描错误时使用声音提示
                if (salesReturnMemo) {
                    salesReturnMemo = false;
                } else {
                    salesReturnMemo = true;
                }
                if (null == pSalesReturnMemo) {
                    pSalesReturnMemo = new Paramer("salesReturnMemo", String.valueOf(salesReturnMemo));
                } else {
                    pSalesReturnMemo.setValue(String.valueOf(salesReturnMemo));
                }
                cb_salesReturnMemo.setChecked(salesReturnMemo);
                break;
            case R.id.salesReturnWarehouse:
            case R.id.tvSalesReturnWarehouse:
                // 条码扫描错误时使用声音提示
                if (salesReturnWarehouse) {
                    salesReturnWarehouse = false;
                } else {
                    salesReturnWarehouse = true;
                }
                if (null == pSalesReturnWarehouse) {
                    pSalesReturnWarehouse = new Paramer("salesReturnWarehouse", String.valueOf(salesReturnWarehouse));
                } else {
                    pSalesReturnWarehouse.setValue(String.valueOf(salesReturnWarehouse));
                }
                cb_salesReturnWarehouse.setChecked(salesReturnWarehouse);
                break;
            case R.id.salesReturnDepartment:
            case R.id.tvSalesReturnDepartment:
                // 条码扫描错误时使用声音提示
                if (salesReturnDepartment) {
                    salesReturnDepartment = false;
                } else {
                    salesReturnDepartment = true;
                }
                if (null == pSalesReturnDepartment) {
                    pSalesReturnDepartment = new Paramer("salesReturnDepartment", String.valueOf(salesReturnDepartment));
                } else {
                    pSalesReturnDepartment.setValue(String.valueOf(salesReturnDepartment));
                }
                cb_salesReturnDepartment.setChecked(salesReturnDepartment);
                break;
            case R.id.salesReturnAmountSum:
            case R.id.tvSalesReturnAmountSum:
                // 条码扫描错误时使用声音提示
                if (salesReturnAmountSum) {
                    salesReturnAmountSum = false;
                } else {
                    salesReturnAmountSum = true;
                }
                if (null == pSalesReturnAmountSum) {
                    pSalesReturnAmountSum = new Paramer("salesReturnAmountSum", String.valueOf(salesReturnAmountSum));
                } else {
                    pSalesReturnAmountSum.setValue(String.valueOf(salesReturnAmountSum));
                }
                cb_salesReturnAmountSum.setChecked(salesReturnAmountSum);
                break;

            case R.id.purchaseOrderEmployee:
            case R.id.tvPurchaseOrderEmployee:
                // 条码扫描错误时使用声音提示
                if (purchaseOrderEmployee) {
                    purchaseOrderEmployee = false;
                } else {
                    purchaseOrderEmployee = true;
                }
                if (null == pPurchaseOrderEmployee) {
                    pPurchaseOrderEmployee = new Paramer("purchaseOrderEmployee", String.valueOf(purchaseOrderEmployee));
                } else {
                    pPurchaseOrderEmployee.setValue(String.valueOf(purchaseOrderEmployee));
                }
                cb_purchaseOrderEmployee.setChecked(purchaseOrderEmployee);
                  break;

            case R.id.purchaseOrderSupplier:
            case R.id.tvPurchaseOrderSupplier:
                // 条码扫描错误时使用声音提示
                if (purchaseOrderSupplier) {
                    purchaseOrderSupplier = false;
                } else {
                    purchaseOrderSupplier = true;
                }
                if (null == pPurchaseOrderSupplier) {
                    pPurchaseOrderSupplier = new Paramer("purchaseOrderSupplier", String.valueOf(purchaseOrderSupplier));
                } else {
                    pPurchaseOrderSupplier.setValue(String.valueOf(purchaseOrderSupplier));
                }
                cb_purchaseOrderSupplier.setChecked(purchaseOrderSupplier);
                break;

            case R.id.purchaseOrderBrand:
            case R.id.tvPurchaseOrderBrand:
                // 条码扫描错误时使用声音提示
                if (purchaseOrderBrand) {
                    purchaseOrderBrand = false;
                } else {
                    purchaseOrderBrand = true;
                }
                if (null == pPurchaseOrderBrand) {
                    pPurchaseOrderBrand = new Paramer("purchaseOrderBrand", String.valueOf(purchaseOrderBrand));
                } else {
                    pPurchaseOrderBrand.setValue(String.valueOf(purchaseOrderBrand));
                }
                cb_purchaseOrderBrand.setChecked(purchaseOrderBrand);
                break;

            case R.id.purchaseOrderMemo:
            case R.id.tvPurchaseOrderMemo:
                // 条码扫描错误时使用声音提示
                if (purchaseOrderMemo) {
                    purchaseOrderMemo = false;
                } else {
                    purchaseOrderMemo = true;
                }
                if (null == pPurchaseOrderMemo) {
                    pPurchaseOrderMemo = new Paramer("purchaseOrderMemo", String.valueOf(purchaseOrderMemo));
                } else {
                    pPurchaseOrderMemo.setValue(String.valueOf(purchaseOrderMemo));
                }
                cb_purchaseOrderMemo.setChecked(purchaseOrderMemo);
                break;


            case R.id.purchaseOrderDepartment:
            case R.id.tvPurchaseOrderDepartment:
                // 条码扫描错误时使用声音提示
                if (purchaseOrderDepartment) {
                    purchaseOrderDepartment = false;
                } else {
                    purchaseOrderDepartment = true;
                }
                if (null == pPurchaseOrderDepartment) {
                    pPurchaseOrderDepartment = new Paramer("purchaseOrderDepartment", String.valueOf(purchaseOrderDepartment));
                } else {
                    pPurchaseOrderDepartment.setValue(String.valueOf(purchaseOrderDepartment));
                }
                cb_purchaseOrderDepartment.setChecked(purchaseOrderDepartment);
                break;
            case R.id.purchaseOrderAmountSum:
            case R.id.tvPurchaseOrderAmountSum:
                // 条码扫描错误时使用声音提示
                if (purchaseOrderAmountSum) {
                    purchaseOrderAmountSum = false;
                } else {
                    purchaseOrderAmountSum = true;
                }
                if (null == pPurchaseOrderAmountSum) {
                    pPurchaseOrderAmountSum = new Paramer("purchaseOrderAmountSum", String.valueOf(purchaseOrderAmountSum));
                } else {
                    pPurchaseOrderAmountSum.setValue(String.valueOf(purchaseOrderAmountSum));
                }
                cb_purchaseOrderAmountSum.setChecked(purchaseOrderAmountSum);
                break;

            case R.id.purchaseEmployee:
            case R.id.tvPurchaseEmployee:
                // 条码扫描错误时使用声音提示
                if (purchaseEmployee) {
                    purchaseEmployee = false;
                } else {
                    purchaseEmployee = true;
                }
                if (null == pPurchaseEmployee) {
                    pPurchaseEmployee = new Paramer("purchaseEmployee", String.valueOf(purchaseEmployee));
                } else {
                    pPurchaseEmployee.setValue(String.valueOf(purchaseEmployee));
                }
                cb_purchaseEmployee.setChecked(purchaseEmployee);
                break;
            case R.id.purchaseSupplier:
            case R.id.tvPurchaseSupplier:
                // 条码扫描错误时使用声音提示
                if (purchaseSupplier) {
                    purchaseSupplier = false;
                } else {
                    purchaseSupplier = true;
                }
                if (null == pPurchaseSupplier) {
                    pPurchaseSupplier = new Paramer("purchaseSupplier", String.valueOf(purchaseSupplier));
                } else {
                    pPurchaseSupplier.setValue(String.valueOf(purchaseSupplier));
                }
                cb_purchaseSupplier.setChecked(purchaseSupplier);
                break;
            case R.id.purchaseBrand:
            case R.id.tvPurchaseBrand:
                // 条码扫描错误时使用声音提示
                if (purchaseBrand) {
                    purchaseBrand = false;
                } else {
                    purchaseBrand = true;
                }
                if (null == pPurchaseBrand) {
                    pPurchaseBrand = new Paramer("purchaseBrand", String.valueOf(purchaseBrand));
                } else {
                    pPurchaseBrand.setValue(String.valueOf(purchaseBrand));
                }
                cb_purchaseBrand.setChecked(purchaseBrand);
                break;
            case R.id.purchaseMemo:
            case R.id.tvPurchaseMemo:
                // 条码扫描错误时使用声音提示
                if (purchaseMemo) {
                    purchaseMemo = false;
                } else {
                    purchaseMemo = true;
                }
                if (null == pPurchaseMemo) {
                    pPurchaseMemo = new Paramer("purchaseMemo", String.valueOf(purchaseMemo));
                } else {
                    pPurchaseMemo.setValue(String.valueOf(purchaseMemo));
                }
                cb_purchaseMemo.setChecked(purchaseMemo);
                break;
            case R.id.purchaseDepartment:
            case R.id.tvPurchaseDepartment:
                // 条码扫描错误时使用声音提示
                if (purchaseDepartment) {
                    purchaseDepartment = false;
                } else {
                    purchaseDepartment = true;
                }
                if (null == pPurchaseDepartment) {
                    pPurchaseDepartment = new Paramer("purchaseDepartment", String.valueOf(purchaseDepartment));
                } else {
                    pPurchaseDepartment.setValue(String.valueOf(purchaseDepartment));
                }
                cb_purchaseDepartment.setChecked(purchaseDepartment);
                break;
            case R.id.purchaseAmountSum:
            case R.id.tvPurchaseAmountSum:
                // 条码扫描错误时使用声音提示
                if (purchaseAmountSum) {
                    purchaseAmountSum = false;
                } else {
                    purchaseAmountSum = true;
                }
                if (null == pPurchaseAmountSum) {
                    pPurchaseAmountSum = new Paramer("purchaseAmountSum", String.valueOf(purchaseAmountSum));
                } else {
                    pPurchaseAmountSum.setValue(String.valueOf(purchaseAmountSum));
                }
                cb_purchaseAmountSum.setChecked(purchaseAmountSum);
                break;
            case R.id.purchaseReturnEmployee:
            case R.id.tvPurchaseReturnEmployee:
                // 条码扫描错误时使用声音提示
                if (purchaseReturnEmployee) {
                    purchaseReturnEmployee = false;
                } else {
                    purchaseReturnEmployee = true;
                }
                if (null == pPurchaseReturnEmployee) {
                    pPurchaseReturnEmployee = new Paramer("purchaseReturnEmployee", String.valueOf(purchaseReturnEmployee));
                } else {
                    pPurchaseReturnEmployee.setValue(String.valueOf(purchaseReturnEmployee));
                }
                cb_purchaseReturnEmployee.setChecked(purchaseReturnEmployee);
                break;
            case R.id.purchaseReturnSupplier:
            case R.id.tvPurchaseReturnSupplier:
                // 条码扫描错误时使用声音提示
                if (purchaseReturnSupplier) {
                    purchaseReturnSupplier = false;
                } else {
                    purchaseReturnSupplier = true;
                }
                if (null == pPurchaseReturnSupplier) {
                    pPurchaseReturnSupplier = new Paramer("purchaseReturnSupplier", String.valueOf(purchaseReturnSupplier));
                } else {
                    pPurchaseReturnSupplier.setValue(String.valueOf(purchaseReturnSupplier));
                }
                cb_purchaseReturnSupplier.setChecked(purchaseReturnSupplier);
                break;
            case R.id.purchaseReturnBrand:
            case R.id.tvPurchaseReturnBrand:
                // 条码扫描错误时使用声音提示
                if (purchaseReturnBrand) {
                    purchaseReturnBrand = false;
                } else {
                    purchaseReturnBrand = true;
                }
                if (null == pPurchaseReturnBrand) {
                    pPurchaseReturnBrand = new Paramer("purchaseReturnBrand", String.valueOf(purchaseReturnBrand));
                } else {
                    pPurchaseReturnBrand.setValue(String.valueOf(purchaseReturnBrand));
                }
                cb_purchaseReturnBrand.setChecked(purchaseReturnBrand);
                break;
            case R.id.purchaseReturnMemo:
            case R.id.tvPurchaseReturnMemo:
                // 条码扫描错误时使用声音提示
                if (purchaseReturnMemo) {
                    purchaseReturnMemo = false;
                } else {
                    purchaseReturnMemo = true;
                }
                if (null == pPurchaseReturnMemo) {
                    pPurchaseReturnMemo = new Paramer("purchaseReturnMemo", String.valueOf(purchaseReturnMemo));
                } else {
                    pPurchaseReturnMemo.setValue(String.valueOf(purchaseReturnMemo));
                }
                cb_purchaseReturnMemo.setChecked(purchaseReturnMemo);
                break;
            case R.id.purchaseReturnDepartment:
            case R.id.tvPurchaseReturnDepartment:
                // 条码扫描错误时使用声音提示
                if (purchaseReturnDepartment) {
                    purchaseReturnDepartment = false;
                } else {
                    purchaseReturnDepartment = true;
                }
                if (null == pPurchaseReturnDepartment) {
                    pPurchaseReturnDepartment = new Paramer("purchaseReturnDepartment", String.valueOf(purchaseReturnDepartment));
                } else {
                    pPurchaseReturnDepartment.setValue(String.valueOf(purchaseReturnDepartment));
                }
                cb_purchaseReturnDepartment.setChecked(purchaseReturnDepartment);
                break;
            case R.id.purchaseReturnAmountSum:
            case R.id.tvPurchaseReturnAmountSum:
                // 条码扫描错误时使用声音提示
                if (purchaseReturnAmountSum) {
                    purchaseReturnAmountSum = false;
                } else {
                    purchaseReturnAmountSum = true;
                }
                if (null == pPurchaseReturnAmountSum) {
                    pPurchaseReturnAmountSum = new Paramer("purchaseReturnAmountSum", String.valueOf(purchaseReturnAmountSum));
                } else {
                    pPurchaseReturnAmountSum.setValue(String.valueOf(purchaseReturnAmountSum));
                }
                cb_purchaseReturnAmountSum.setChecked(purchaseReturnAmountSum);
                break;
            case R.id.stockMoveEmployee:
            case R.id.tvStockMoveEmployee:
                // 条码扫描错误时使用声音提示
                if (stockMoveEmployee) {
                    stockMoveEmployee = false;
                } else {
                    stockMoveEmployee = true;
                }
                if (null == pStockMoveEmployee) {
                    pStockMoveEmployee = new Paramer("stockMoveEmployee", String.valueOf(stockMoveEmployee));
                } else {
                    pStockMoveEmployee.setValue(String.valueOf(stockMoveEmployee));
                }
                cb_stockMoveEmployee.setChecked(stockMoveEmployee);
                break;
            case R.id.stockMoveMemo:
            case R.id.tvStockMoveMemo:
                // 条码扫描错误时使用声音提示
                if (stockMoveMemo) {
                    stockMoveMemo = false;
                } else {
                    stockMoveMemo = true;
                }
                if (null == pStockMoveMemo) {
                    pStockMoveMemo = new Paramer("stockMoveMemo", String.valueOf(stockMoveMemo));
                } else {
                    pStockMoveMemo.setValue(String.valueOf(stockMoveMemo));
                }
                cb_stockMoveMemo.setChecked(stockMoveMemo);
                break;
            case R.id.stockMoveWarehouseIn:
            case R.id.tvStockMoveWarehouseIn:
                // 条码扫描错误时使用声音提示
                if (stockMoveWarehouseIn) {
                    stockMoveWarehouseIn = false;
                } else {
                    stockMoveWarehouseIn = true;
                }
                if (null == pStockMoveWarehouseIn) {
                    pStockMoveWarehouseIn = new Paramer("stockMoveWarehouseIn", String.valueOf(stockMoveWarehouseIn));
                } else {
                    pStockMoveWarehouseIn.setValue(String.valueOf(stockMoveWarehouseIn));
                }
                cb_stockMoveWarehouseIn.setChecked(stockMoveWarehouseIn);
                break;
            case R.id.stockMoveWarehouseOut:
            case R.id.tvStockMoveWarehouseOut:
                // 条码扫描错误时使用声音提示
                if (stockMoveWarehouseOut) {
                    stockMoveWarehouseOut = false;
                } else {
                    stockMoveWarehouseOut = true;
                }
                if (null == pStockMoveWarehouseOut) {
                    pStockMoveWarehouseOut = new Paramer("stockMoveWarehouseOut", String.valueOf(stockMoveWarehouseOut));
                } else {
                    pStockMoveWarehouseOut.setValue(String.valueOf(stockMoveWarehouseOut));
                }
                cb_stockMoveWarehouseOut.setChecked(stockMoveWarehouseOut);
                break;
            case R.id.stockMoveBrand:
            case R.id.tvStockMoveBrand:
                // 条码扫描错误时使用声音提示
                if (stockMoveBrand) {
                    stockMoveBrand = false;
                } else {
                    stockMoveBrand = true;
                }
                if (null == pStockMoveBrand) {
                    pStockMoveBrand = new Paramer("stockMoveBrand", String.valueOf(stockMoveBrand));
                } else {
                    pStockMoveBrand.setValue(String.valueOf(stockMoveBrand));
                }
                cb_stockMoveBrand.setChecked(stockMoveBrand);
                break;
            case R.id.stockMoveAmountSum:
            case R.id.tvStockMoveAmountSum:
                // 条码扫描错误时使用声音提示
                if (stockMoveAmountSum) {
                    stockMoveAmountSum = false;
                } else {
                    stockMoveAmountSum = true;
                }
                if (null == pStockMoveAmountSum) {
                    pStockMoveAmountSum = new Paramer("stockMoveAmountSum", String.valueOf(stockMoveAmountSum));
                } else {
                    pStockMoveAmountSum.setValue(String.valueOf(stockMoveAmountSum));
                }
                cb_stockMoveAmountSum.setChecked(stockMoveAmountSum);
                break;
            case R.id.stockInEmployee:
            case R.id.tvStockInEmployee:
                // 条码扫描错误时使用声音提示
                if (stockInEmployee) {
                    stockInEmployee = false;
                } else {
                    stockInEmployee = true;
                }
                if (null == pStockInEmployee) {
                    pStockInEmployee = new Paramer("stockInEmployee", String.valueOf(stockInEmployee));
                } else {
                    pStockInEmployee.setValue(String.valueOf(stockInEmployee));
                }
                cb_stockInEmployee.setChecked(stockInEmployee);
                break;
            case R.id.stockInMemo:
            case R.id.tvStockInMemo:
                // 条码扫描错误时使用声音提示
                if (stockInMemo) {
                    stockInMemo = false;
                } else {
                    stockInMemo = true;
                }
                if (null == pStockInMemo) {
                    pStockInMemo = new Paramer("stockInMemo", String.valueOf(stockInMemo));
                } else {
                    pStockInMemo.setValue(String.valueOf(stockInMemo));
                }
                cb_stockInMemo.setChecked(stockInMemo);
                break;
            case R.id.stockInWarehouse:
            case R.id.tvStockInWarehouse:
                // 条码扫描错误时使用声音提示
                if (stockInWarehouse) {
                    stockInWarehouse = false;
                } else {
                    stockInWarehouse = true;
                }
                if (null == pStockInWarehouse) {
                    pStockInWarehouse = new Paramer("stockInWarehouse", String.valueOf(stockInWarehouse));
                } else {
                    pStockInWarehouse.setValue(String.valueOf(stockInWarehouse));
                }
                cb_stockInWarehouse.setChecked(stockInWarehouse);
                break;
            case R.id.stockInWarehouseOut:
            case R.id.tvStockInWarehouseOut:
                // 条码扫描错误时使用声音提示
                if (stockInWarehouseOut) {
                    stockInWarehouseOut = false;
                } else {
                    stockInWarehouseOut = true;
                }
                if (null == pStockInWarehouseOut) {
                    pStockInWarehouseOut = new Paramer("stockInWarehouseOut", String.valueOf(stockInWarehouseOut));
                } else {
                    pStockInWarehouseOut.setValue(String.valueOf(stockInWarehouseOut));
                }
                cb_stockInWarehouseOut.setChecked(stockInWarehouseOut);
                break;
            case R.id.stockInBrand:
            case R.id.tvStockInBrand:
                // 条码扫描错误时使用声音提示
                if (stockInBrand) {
                    stockInBrand = false;
                } else {
                    stockInBrand = true;
                }
                if (null == pStockInBrand) {
                    pStockInBrand = new Paramer("stockInBrand", String.valueOf(stockInBrand));
                } else {
                    pStockInBrand.setValue(String.valueOf(stockInBrand));
                }
                cb_stockInBrand.setChecked(stockInBrand);
                break;
            case R.id.stockInRelationNo:
            case R.id.tvStockInRelationNo:
                // 条码扫描错误时使用声音提示
                if (stockInRelationNo) {
                    stockInRelationNo = false;
                } else {
                    stockInRelationNo = true;
                }
                if (null == pStockInRelationNo) {
                    pStockInRelationNo = new Paramer("stockInRelationNo", String.valueOf(stockInRelationNo));
                } else {
                    pStockInRelationNo.setValue(String.valueOf(stockInRelationNo));
                }
                cb_stockInRelationNo.setChecked(stockInRelationNo);
                break;
            case R.id.stockInAmountSum:
            case R.id.tvStockInAmountSum:
                // 条码扫描错误时使用声音提示
                if (stockInAmountSum) {
                    stockInAmountSum = false;
                } else {
                    stockInAmountSum = true;
                }
                if (null == pStockInAmountSum) {
                    pStockInAmountSum = new Paramer("stockInAmountSum", String.valueOf(stockInAmountSum));
                } else {
                    pStockInAmountSum.setValue(String.valueOf(stockInAmountSum));
                }
                cb_barcodeWarningTone.setChecked(stockInAmountSum);
                break;
            case R.id.stockOutEmployee:
            case R.id.tvStockOutEmployee:
                // 条码扫描错误时使用声音提示
                if (stockOutEmployee) {
                    stockOutEmployee = false;
                } else {
                    stockOutEmployee = true;
                }
                if (null == pStockOutEmployee) {
                    pStockOutEmployee = new Paramer("stockOutEmployee", String.valueOf(stockOutEmployee));
                } else {
                    pStockOutEmployee.setValue(String.valueOf(stockOutEmployee));
                }
                cb_stockOutEmployee.setChecked(stockOutEmployee);
                break;
            case R.id.stockOutMemo:
            case R.id.tvStockOutMemo:
                // 条码扫描错误时使用声音提示
                if (stockOutMemo) {
                    stockOutMemo = false;
                } else {
                    stockOutMemo = true;
                }
                if (null == pStockOutMemo) {
                    pStockOutMemo = new Paramer("stockOutMemo", String.valueOf(stockOutMemo));
                } else {
                    pStockOutMemo.setValue(String.valueOf(stockOutMemo));
                }
                cb_stockOutMemo.setChecked(stockOutMemo);
                break;
            case R.id.stockOutWarehouse:
            case R.id.tvStockOutWarehouse:
                // 条码扫描错误时使用声音提示
                if (stockOutWarehouse) {
                    stockOutWarehouse = false;
                } else {
                    stockOutWarehouse = true;
                }
                if (null == pStockOutWarehouse) {
                    pStockOutWarehouse = new Paramer("stockOutWarehouse", String.valueOf(stockOutWarehouse));
                } else {
                    pStockOutWarehouse.setValue(String.valueOf(stockOutWarehouse));
                }
                cb_stockOutWarehouse.setChecked(stockOutWarehouse);
                break;
            case R.id.stockOutWarehouseIn:
            case R.id.tvStockOutWarehouseIn:
                // 条码扫描错误时使用声音提示
                if (stockOutWarehouseIn) {
                    stockOutWarehouseIn = false;
                } else {
                    stockOutWarehouseIn = true;
                }
                if (null == pStockOutWarehouseIn) {
                    pStockOutWarehouseIn = new Paramer("stockOutWarehouseIn", String.valueOf(stockOutWarehouseIn));
                } else {
                    pStockOutWarehouseIn.setValue(String.valueOf(stockOutWarehouseIn));
                }
                cb_stockOutWarehouseIn.setChecked(stockOutWarehouseIn);
                break;
            case R.id.stockOutBrand:
            case R.id.tvStockOutBrand:
                // 条码扫描错误时使用声音提示
                if (stockOutBrand) {
                    stockOutBrand = false;
                } else {
                    stockOutBrand = true;
                }
                if (null == pStockOutBrand) {
                    pStockOutBrand = new Paramer("stockOutBrand", String.valueOf(stockOutBrand));
                } else {
                    pStockOutBrand.setValue(String.valueOf(stockOutBrand));
                }
                cb_stockOutBrand.setChecked(stockOutBrand);
                break;
            case R.id.stockOutRelationNo:
            case R.id.tvStockOutRelationNo:
                // 条码扫描错误时使用声音提示
                if (stockOutRelationNo) {
                    stockOutRelationNo = false;
                } else {
                    stockOutRelationNo = true;
                }
                if (null == pStockOutRelationNo) {
                    pStockOutRelationNo = new Paramer("stockOutRelationNo", String.valueOf(stockOutRelationNo));
                } else {
                    pStockOutRelationNo.setValue(String.valueOf(stockOutRelationNo));
                }
                cb_stockOutRelationNo.setChecked(stockOutRelationNo);
                break;
            case R.id.stockOutAmountSum:
            case R.id.tvStockOutAmountSum:
                // 条码扫描错误时使用声音提示
                if (stockOutAmountSum) {
                    stockOutAmountSum = false;
                } else {
                    stockOutAmountSum = true;
                }
                if (null == pStockOutAmountSum) {
                    pStockOutAmountSum = new Paramer("stockOutAmountSum", String.valueOf(stockOutAmountSum));
                } else {
                    pStockOutAmountSum.setValue(String.valueOf(stockOutAmountSum));
                }
                cb_stockOutAmountSum.setChecked(stockOutAmountSum);
                break;
            case R.id.stocktakingEmployee:
            case R.id.tvStocktakingEmployee:
                // 条码扫描错误时使用声音提示
                if (stocktakingEmployee) {
                    stocktakingEmployee = false;
                } else {
                    stocktakingEmployee = true;
                }
                if (null == pStocktakingEmployee) {
                    pStocktakingEmployee = new Paramer("stocktakingEmployee", String.valueOf(stocktakingEmployee));
                } else {
                    pStocktakingEmployee.setValue(String.valueOf(stocktakingEmployee));
                }
                cb_stocktakingEmployee.setChecked(stocktakingEmployee);
                break;
            case R.id.stocktakingMemo:
            case R.id.tvStocktakingMemo:
                // 条码扫描错误时使用声音提示
                if (stocktakingMemo) {
                    stocktakingMemo = false;
                } else {
                    stocktakingMemo = true;
                }
                if (null == pStocktakingMemo) {
                    pStocktakingMemo = new Paramer("stocktakingMemo", String.valueOf(stocktakingMemo));
                } else {
                    pStocktakingMemo.setValue(String.valueOf(stocktakingMemo));
                }
                cb_stocktakingMemo.setChecked(stocktakingMemo);
                break;
            case R.id.stocktakingWarehouse:
            case R.id.tvStocktakingWarehouse:
                // 条码扫描错误时使用声音提示
                if (stocktakingWarehouse) {
                    stocktakingWarehouse = false;
                } else {
                    stocktakingWarehouse = true;
                }
                if (null == pStocktakingWarehouse) {
                    pStocktakingWarehouse = new Paramer("stocktakingWarehouse", String.valueOf(stocktakingWarehouse));
                } else {
                    pStocktakingWarehouse.setValue(String.valueOf(stocktakingWarehouse));
                }
                cb_stocktakingWarehouse.setChecked(stocktakingWarehouse);
                break;
            case R.id.stocktakingBrand:
            case R.id.tvStocktakingBrand:
                // 条码扫描错误时使用声音提示
                if (stocktakingBrand) {
                    stocktakingBrand = false;
                } else {
                    stocktakingBrand = true;
                }
                if (null == pStocktakingBrand) {
                    pStocktakingBrand = new Paramer("stocktakingBrand", String.valueOf(stocktakingBrand));
                } else {
                    pStocktakingBrand.setValue(String.valueOf(stocktakingBrand));
                }
                cb_stocktakingBrand.setChecked(stocktakingBrand);
                break;
            case R.id.stocktakingAmountSum:
            case R.id.tvStocktakingAmountSum:
                // 条码扫描错误时使用声音提示
                if (stocktakingAmountSum) {
                    stocktakingAmountSum = false;
                } else {
                    stocktakingAmountSum = true;
                }
                if (null == pStocktakingAmountSum) {
                    pStocktakingAmountSum = new Paramer("stocktakingAmountSum", String.valueOf(stocktakingAmountSum));
                } else {
                    pStocktakingAmountSum.setValue(String.valueOf(stocktakingAmountSum));
                }
                cb_stocktakingAmountSum.setChecked(stocktakingAmountSum);
                break;
            case R.id.showGoodsCode:
            case R.id.tvShowGoodsCode:
                if (showGoodsCode) {
                    showGoodsCode = false;
                } else {
                    showGoodsCode = true;
                }
                if (null == pShowGoodsCode) {
                    pShowGoodsCode = new Paramer("showGoodsCode", String.valueOf(showGoodsCode));
                } else {
                    pShowGoodsCode.setValue(String.valueOf(showGoodsCode));
                }
                cb_showGoodsCode.setChecked(showGoodsCode);
                break;
            case R.id.showGoodsName:
            case R.id.tvShowGoodsName:
                if (showGoodsName) {
                    showGoodsName = false;
                } else {
                    showGoodsName = true;
                }
                if (null == pShowGoodsName) {
                    pShowGoodsName = new Paramer("showGoodsName", String.valueOf(showGoodsName));
                } else {
                    pShowGoodsName.setValue(String.valueOf(showGoodsName));
                }
                cb_showGoodsName.setChecked(showGoodsName);
                break;
            case R.id.showColor:
            case R.id.tvShowColor:
                if (showColor) {
                    showColor = false;
                } else {
                    showColor = true;
                }
                if (null == pShowColor) {
                    pShowColor = new Paramer("showColor", String.valueOf(showColor));
                } else {
                    pShowColor.setValue(String.valueOf(showColor));
                }
                cb_showColor.setChecked(showColor);
                break;
            case R.id.showSize:
            case R.id.tvShowSize:
                if (showSize) {
                    showSize = false;
                } else {
                    showSize = true;
                }
                if (null == pShowSize) {
                    pShowSize = new Paramer("showSize", String.valueOf(showSize));
                } else {
                    pShowSize.setValue(String.valueOf(showSize));
                }
                cb_showSize.setChecked(showSize);
                break;
            case R.id.showRetailSales:
            case R.id.tvShowRetailSales:
                if (showRetailSales) {
                    showRetailSales = false;
                } else {
                    showRetailSales = true;
                }
                if (null == pShowRetailSales) {
                    pShowRetailSales = new Paramer("showRetailSales", String.valueOf(showRetailSales));
                } else {
                    pShowRetailSales.setValue(String.valueOf(showRetailSales));
                }
                cb_showRetailSales.setChecked(showRetailSales);
                break;
            case R.id.useAreaProtection:
            case R.id.tvUseAreaProtection:
                if (!adminValidate) {
                    // 弹出管理员验证界面
                    showAdminLogin();
                    tempView = v;
                } else {
                    // 设置权限
                    updateuseAreaProtectionState();
                }
                break;
            case R.id.ftv_print_ip:
                getRequestPath();
                Builder dialog = new AlertDialog.Builder(SettingActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                dialog.setTitle("PDA云打印IP设置");
                dialog.setMessage("使用步骤:\n①首次使用PDA云打印时,请在连接打印机的电脑端使用浏览器前往 " + strIp + "/print/CLodop_Setup_for_Win32NT.exe下载 C-Lodop云打印服务程序。\n" + "②在打印机地址中输入安装C-Lodop云打印服务程序电脑的IP。");
                // 相当于点击确认按钮
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                // 相当于点击取消按钮
                dialog.setCancelable(false);
                dialog.create();
                dialog.show();
                break;
            case R.id.ftv_print_port:
                Builder dialogs = new AlertDialog.Builder(SettingActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                dialogs.setTitle("PDA云打印端口设置");
                dialogs.setMessage("请输入您安装的C-Lodop云打印服务程序对应的服务端口。");
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setBackground(this.getResources().getDrawable(R.drawable.print_prot));
                dialogs.setView(imageView);
                // 相当于点击确认按钮
                dialogs.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                // 相当于点击取消按钮
                dialogs.setCancelable(false);
                dialogs.create();
                dialogs.show();
                break;
            case R.id.verification:
                String userNo = etNo.getText().toString();
                String userPassword = etPassword.getText().toString();
                systemAdminVerification(tempView, userNo, userPassword);
                break;
            case R.id.cancel:
                alertDialog.dismiss();
                break;
            case R.id.relationSales:
            case R.id.tvRelationSales:
                onCheckedChanged(null, R.id.relationSales);
                break;
            case R.id.relationStockOut:
            case R.id.tvRelationStockOut:
                onCheckedChanged(null, R.id.relationStockOut);
                break;
            case R.id.commonSelect:
            case R.id.tvCommonSelect:
                onCheckedChanged(null, R.id.commonSelect);
                break;
            case R.id.multiSelect:
            case R.id.tvMultiSelect:
                onCheckedChanged(null, R.id.multiSelect);
                break;
            case R.id.singleColorMultiSizeSelect:
            case R.id.tvSingleColorMultiSizeSelect:
                onCheckedChanged(null, R.id.singleColorMultiSizeSelect);
                break;
            case R.id.multiColorMultiSizeSelect:
            case R.id.tvMultiColorMultiSizeSelect:
                onCheckedChanged(null, R.id.multiColorMultiSizeSelect);
                break;
            default:
                break;
        }
    }

    /**
     * 弹出系统管理员登录界面
     */
    private void showAdminLogin() {
        View view = (LinearLayout) getLayoutInflater().inflate(R.layout.list_admin_login, null);
        // 初始化
        etNo = (EditText) view.findViewById(R.id.validate_no);
        etPassword = (EditText) view.findViewById(R.id.validate_password);
        TextView tvCancel = (TextView) view.findViewById(R.id.cancel);
        TextView tvVerification = (TextView) view.findViewById(R.id.verification);
        // 绑定事件
        tvCancel.setOnClickListener(this);
        tvVerification.setOnClickListener(this);
        // 显示界面
        Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("验证管理员身份");
        dialog.setView(view);
        dialog.setCancelable(false);
        dialog.create();
        alertDialog = dialog.show();
    }

    /**
     * 管理员身份验证
     */
    private void systemAdminVerification(final View v, String userNo, String userPassword) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = admin_verification;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("userNo", userNo);
        map.put("userPassword", userPassword);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("obj")) {
                        alertDialog.dismiss();
                        adminValidate = true;
                        onClick(v);
                    } else {
                        Toast.makeText(SettingActivity.this, "用户名或密码错误，管理员身份验证失败", Toast.LENGTH_SHORT).show();
                        etPassword.setText(null);
                        etPassword.setFocusable(true);
                    }
                } catch (Exception e) {
                    Toast.makeText(SettingActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 条码校验时允许修改单据权限修改(管理员设置)
     */
    private void updateCoverSaveState() {
        // 条码校验时允许修改单据
        if (coverDoc) {
            coverDoc = false;
        } else {
            coverDoc = true;
        }
        if (null == pCoverDoc) {
            pCoverDoc = new Paramer("coverDoc", String.valueOf(coverDoc));
        } else {
            pCoverDoc.setValue(String.valueOf(coverDoc));
        }
        cb_coverDoc.setChecked(coverDoc);
    }

    /**
     * 条码校验时非单据货品或数量大于单据数量不允许录入权限修改(管理员设置)
     */
    private void updateNotAllowState() {
        // 条码校验时允许修改单据
        if (notAllow) {
            notAllow = false;
        } else {
            notAllow = true;
        }
        if (null == pNotAllow) {
            pNotAllow = new Paramer("notAllow", String.valueOf(notAllow));
        } else {
            pNotAllow.setValue(String.valueOf(notAllow));
        }
        cb_notAllow.setChecked(notAllow);
    }

    /**
     * 条码校验时允许修改单据权限修改(管理员设置)
     */
    private void updateuseAreaProtectionState() {
        // 采购单据显示单价及金额合计
        if (useAreaProtection) {
            useAreaProtection = false;
        } else {
            useAreaProtection = true;
        }
        if (null == pUseAreaProtection) {
            pUseAreaProtection = new Paramer("useAreaProtection", String.valueOf(useAreaProtection));
        } else {
            pUseAreaProtection.setValue(String.valueOf(useAreaProtection));
        }
        cb_useAreaProtection.setChecked(useAreaProtection);
    }

    /**
     * 获取服务端APK的版本信息
     */
    private void getUpdataInfo() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = updatePath;
        vo.context = this;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject obj = retObj.getJSONObject("obj");
                        info.setVersion(obj.getString("Version")); // 获取版本号
                        info.setUrl(obj.getString("Url")); // 获取要升级的APK文件
                        info.setDescription(obj.getString("Description")); // 获取该文件的信息
                        forceUpdate = obj.getBoolean("ForceUpdate"); // 是否强制更新
                        // info.setDescription("检测到最新版本,请及时更新!"); //获取该文件的信息
                        checkVersion();
                    } else {
                        Message msg = new Message();
                        msg.what = GET_UNDATAINFO_ERROR;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Logger.e(TAG, e.getMessage());
                    Message msg = new Message();
                    msg.what = GET_UNDATAINFO_ERROR;
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 连接服务器下载最新版本的APK文件
     * 
     * @param path
     * @param pd
     * @return
     * @throws Exception
     */
    public File getFileFromServer(String path, ProgressDialog pd) throws Exception {
        // 如果相等的话表示当前的SDCard挂载在手机上并且是可用的
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            // 获取到文件的大小
            int max = conn.getContentLength();
            pd.setMax(100);
            InputStream is = conn.getInputStream();
            File file = new File(Environment.getExternalStorageDirectory(), "fuxiPDA_" + info.getVersion() + ".apk");
            FileOutputStream fos = new FileOutputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            int len;
            int total = 0;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                total += len;
                // 获取当前下载量
                pd.setProgress((total * 100) / max);
                fos.flush();
            }
            fos.close();
            bis.close();
            is.close();
            return file;
        } else {
            return null;
        }
    }

    /**
     * 获取打印机
     */
    private void getPrinter() {
        // 打印设置
        WebSettings settings = mWebView.getSettings();
        settings.setTextSize(TextSize.SMALLEST);
        // 自适应屏幕
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        settings.setUseWideViewPort(true);// 关键点
        // // 设置可以支持缩放
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true); // 启用内置缩放装置
        // 去掉缩放按钮
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        // //扩大比例的缩放
        // settings.setUseWideViewPort(true);
        // 支持JS
        settings.setJavaScriptEnabled(true);
        // 设置 缓存模式
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启 DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 自适应屏幕
        settings.setLoadWithOverviewMode(true);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        if (mDensity == 240) {
            settings.setDefaultZoom(ZoomDensity.FAR);
        } else if (mDensity == 160) {
            settings.setDefaultZoom(ZoomDensity.MEDIUM);
        } else if (mDensity == 120) {
            settings.setDefaultZoom(ZoomDensity.CLOSE);
        } else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
            settings.setDefaultZoom(ZoomDensity.FAR);
        } else if (mDensity == DisplayMetrics.DENSITY_TV) {
            settings.setDefaultZoom(ZoomDensity.FAR);
        } else {
            settings.setDefaultZoom(ZoomDensity.MEDIUM);
        }
        getRequestPath();
        String loadIp = strIp + "/common.do?getPrinterList&printIp=" + printIp + "&printPort=" + printPort;
        mWebView.addJavascriptInterface(jsToJava, "androidShare");
        mWebView.loadUrl(loadIp);
    }

    /**
     * 获取JS的回调值 Title: JsToJava Description:
     * 
     * @author LYJ
     * 
     */
    private class JsToJava {

        @JavascriptInterface
        public void jsMethod(String paramFromJS) {
            printers = paramFromJS;// 处理返回的结果
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_setting);
        setTitle("系统设置");
    }

    @Override
    protected void processLogic() {
        cb_useAreaProtection.setVisibility(View.GONE);
        tv_useAreaProtection.setVisibility(View.GONE);

        cb_useSupplierCodeToAreaProtection.setVisibility(View.GONE);
        tv_useSupplierCodeToAreaProtection.setVisibility(View.GONE);

        cb_notUseNegativeInventoryCheck.setVisibility(View.GONE);
        tv_notUseNegativeInventoryCheck.setVisibility(View.GONE);


        if (NetUtil.hasNetwork(getApplicationContext())) {
            if (LoginParameterUtil.online) {
                try {
                    tv_version.setText("V" + getVersionName());
                    tv_BaseSetting.callOnClick();
                } catch (Exception e) {
                    Toast.makeText(SettingActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
                show();
                // 获取打印机列表
                printIp = etPrintIp.getText().toString();
                printPort = etPrintPort.getText().toString();
                if (printIp != null && !"".equals(printIp) && !"null".equals(printIp) && printPort != null && !"".equals(printPort) && !"null".equals(printPort)) {
                    // 获取打印机列表
                    getPrinter();
                }
                // 系统管理员登录  20191213 vicky 让去掉
                /*     if ("1".equals(LoginParameterUtil.customer.getUserId())) {
                    adminValidate = true;
                } else {
                    adminValidate = false;
                } */
                adminValidate =true;

            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SettingActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                dialog.setTitle("系统提示");
                dialog.setMessage("当前用户未登录，操作非法！");
                // 相当于点击确认按钮
                dialog.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppManager.getAppManager().AppExit(getApplicationContext());
                    }
                });
                dialog.setCancelable(false);
                dialog.create();
                dialog.show();
            }
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(SettingActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("系统提示");
            dialog.setMessage("当前为离线状态，此功能不可用！");
            // 相当于点击确认按钮
            dialog.setPositiveButton("返回", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        }
    }

    @Override
    protected void setListener() {
        tv_save.setOnClickListener(this);
        ll_update.setOnClickListener(this);
        ll_above.setOnClickListener(this);
        cb_useGoodsboxBarcodeInStocktaking.setOnClickListener(this);
        cb_useSupplierCodeToAreaProtection.setOnClickListener(this);
        cb_coverDoc.setOnClickListener(this);
        cb_notAllow.setOnClickListener(this);
        cb_useDefSupplier.setOnClickListener(this);
        cb_barcodeWarningTone.setOnClickListener(this);
        cb_useAreaProtection.setOnClickListener(this);
        cb_salesOrderEmployee.setOnClickListener(this);
        cb_salesOrderCustomer.setOnClickListener(this);
        cb_salesOrderBrand.setOnClickListener(this);
        cb_salesOrderMemo.setOnClickListener(this);
        cb_salesOrderDepartment.setOnClickListener(this); // 订货部门
        cb_salesOrderWarehouse.setOnClickListener(this); // 发货部门
        cb_salesOrderAmountSum.setOnClickListener(this);
        cb_salesEmployee.setOnClickListener(this);
        cb_salesCustomer.setOnClickListener(this);
        cb_salesBrand.setOnClickListener(this);
        cb_salesMemo.setOnClickListener(this);
        cb_salesOrderNo.setOnClickListener(this); // 订单单号
        cb_salesWarehouse.setOnClickListener(this); // 发货部门
        cb_salesAmountSum.setOnClickListener(this);
        cb_salesReturnEmployee.setOnClickListener(this);
        cb_salesReturnCustomer.setOnClickListener(this);
        cb_salesReturnBrand.setOnClickListener(this);
        cb_salesReturnMemo.setOnClickListener(this);
        cb_salesReturnWarehouse.setOnClickListener(this); // 退货部门
        cb_salesReturnDepartment.setOnClickListener(this); // 收货部门
        cb_salesReturnAmountSum.setOnClickListener(this);


        cb_purchaseOrderEmployee.setOnClickListener(this);
        cb_purchaseOrderSupplier.setOnClickListener(this);
        cb_purchaseOrderBrand.setOnClickListener(this);
        cb_purchaseOrderMemo.setOnClickListener(this);
        cb_purchaseOrderDepartment.setOnClickListener(this); // 收货部门
        cb_purchaseOrderAmountSum.setOnClickListener(this);


        cb_purchaseEmployee.setOnClickListener(this);
        cb_purchaseSupplier.setOnClickListener(this);
        cb_purchaseBrand.setOnClickListener(this);
        cb_purchaseMemo.setOnClickListener(this);
        cb_purchaseDepartment.setOnClickListener(this); // 收货部门
        cb_purchaseAmountSum.setOnClickListener(this);
        cb_purchaseReturnEmployee.setOnClickListener(this);
        cb_purchaseReturnSupplier.setOnClickListener(this);
        cb_purchaseReturnBrand.setOnClickListener(this);
        cb_purchaseReturnMemo.setOnClickListener(this);
        cb_purchaseReturnDepartment.setOnClickListener(this); // 退货部门
        cb_purchaseReturnAmountSum.setOnClickListener(this);
        cb_stockMoveEmployee.setOnClickListener(this);
        cb_stockMoveMemo.setOnClickListener(this);
        cb_stockMoveWarehouseIn.setOnClickListener(this); // 转进仓库
        cb_stockMoveWarehouseOut.setOnClickListener(this); // 转出仓库
        cb_stockMoveBrand.setOnClickListener(this);
        cb_stockMoveAmountSum.setOnClickListener(this);
        cb_stockInEmployee.setOnClickListener(this);
        cb_stockInMemo.setOnClickListener(this);
        cb_stockInWarehouse.setOnClickListener(this); // 仓库名称
        cb_stockInWarehouseOut.setOnClickListener(this); // 转出仓库
        cb_stockInBrand.setOnClickListener(this);
        cb_stockInRelationNo.setOnClickListener(this); // 对应单号
        cb_stockInAmountSum.setOnClickListener(this);
        cb_stockOutEmployee.setOnClickListener(this);
        cb_stockOutMemo.setOnClickListener(this);
        cb_stockOutWarehouse.setOnClickListener(this); // 仓库名称
        cb_stockOutWarehouseIn.setOnClickListener(this); // 转进仓库
        cb_stockOutBrand.setOnClickListener(this);
        cb_stockOutRelationNo.setOnClickListener(this); // 对应单号
        cb_stockOutAmountSum.setOnClickListener(this);
        cb_stocktakingEmployee.setOnClickListener(this);
        cb_stocktakingMemo.setOnClickListener(this);
        cb_stocktakingWarehouse.setOnClickListener(this);
        cb_stocktakingBrand.setOnClickListener(this);
        cb_stocktakingAmountSum.setOnClickListener(this);
        cb_displayInventory.setOnClickListener(this);
        cb_useSingleDiscount.setOnClickListener(this);
        cb_preciseToQueryStock.setOnClickListener(this);
        cb_firstInputOfGoodsCode.setOnClickListener(this);
        cb_notUseNegativeInventoryCheck.setOnClickListener(this);
        tv_useGoodsboxBarcodeInStocktaking.setOnClickListener(this);
        tv_useSupplierCodeToAreaProtection.setOnClickListener(this);
        tv_coverDoc.setOnClickListener(this);
        tv_notAllow.setOnClickListener(this);
        tv_useDefSupplier.setOnClickListener(this);
        tv_barcodeWarningTone.setOnClickListener(this);
        tv_useAreaProtection.setOnClickListener(this);
        tv_salesOrderEmployee.setOnClickListener(this);
        tv_salesOrderCustomer.setOnClickListener(this);
        tv_salesOrderBrand.setOnClickListener(this);
        tv_salesOrderMemo.setOnClickListener(this);
        tv_salesOrderDepartment.setOnClickListener(this); // 订货部门
        tv_salesOrderWarehouse.setOnClickListener(this); // 发货部门
        tv_salesOrderAmountSum.setOnClickListener(this);
        tv_salesEmployee.setOnClickListener(this);
        tv_salesCustomer.setOnClickListener(this);
        tv_salesBrand.setOnClickListener(this);
        tv_salesMemo.setOnClickListener(this);
        tv_salesOrderNo.setOnClickListener(this); // 订单单号
        tv_salesWarehouse.setOnClickListener(this); // 发货部门
        tv_salesAmountSum.setOnClickListener(this);
        tv_salesReturnEmployee.setOnClickListener(this);
        tv_salesReturnCustomer.setOnClickListener(this);
        tv_salesReturnBrand.setOnClickListener(this);
        tv_salesReturnMemo.setOnClickListener(this);
        tv_salesReturnWarehouse.setOnClickListener(this); // 退货部门
        tv_salesReturnDepartment.setOnClickListener(this); // 收货部门
        tv_salesReturnAmountSum.setOnClickListener(this);

        tv_purchaseOrderEmployee.setOnClickListener(this);
        tv_purchaseOrderSupplier.setOnClickListener(this);
        tv_purchaseOrderBrand.setOnClickListener(this);
        tv_purchaseOrderMemo.setOnClickListener(this);
        tv_purchaseOrderDepartment.setOnClickListener(this); // 收货部门
        tv_purchaseOrderAmountSum.setOnClickListener(this);

        tv_purchaseEmployee.setOnClickListener(this);
        tv_purchaseSupplier.setOnClickListener(this);
        tv_purchaseBrand.setOnClickListener(this);
        tv_purchaseMemo.setOnClickListener(this);
        tv_purchaseDepartment.setOnClickListener(this); // 收货部门
        tv_purchaseAmountSum.setOnClickListener(this);
        tv_purchaseReturnEmployee.setOnClickListener(this);
        tv_purchaseReturnSupplier.setOnClickListener(this);
        tv_purchaseReturnBrand.setOnClickListener(this);
        tv_purchaseReturnMemo.setOnClickListener(this);
        tv_purchaseReturnDepartment.setOnClickListener(this); // 退货部门
        tv_purchaseReturnAmountSum.setOnClickListener(this);
        tv_stockMoveEmployee.setOnClickListener(this);
        tv_stockMoveMemo.setOnClickListener(this);
        tv_stockMoveWarehouseIn.setOnClickListener(this); // 转进仓库
        tv_stockMoveWarehouseOut.setOnClickListener(this); // 转出仓库
        tv_stockMoveBrand.setOnClickListener(this);
        tv_stockMoveAmountSum.setOnClickListener(this);
        tv_stockInEmployee.setOnClickListener(this);
        tv_stockInMemo.setOnClickListener(this);
        tv_stockInWarehouse.setOnClickListener(this); // 仓库名称
        tv_stockInWarehouseOut.setOnClickListener(this); // 转出仓库
        tv_stockInBrand.setOnClickListener(this);
        tv_stockInRelationNo.setOnClickListener(this); // 对应单号
        tv_stockInAmountSum.setOnClickListener(this);
        tv_stockOutEmployee.setOnClickListener(this);
        tv_stockOutMemo.setOnClickListener(this);
        tv_stockOutWarehouse.setOnClickListener(this); // 仓库名称
        tv_stockOutWarehouseIn.setOnClickListener(this); // 转进仓库
        tv_stockOutBrand.setOnClickListener(this);
        tv_stockOutRelationNo.setOnClickListener(this); // 对应单号
        tv_stockOutAmountSum.setOnClickListener(this);
        tv_stocktakingEmployee.setOnClickListener(this);
        tv_stocktakingMemo.setOnClickListener(this);
        tv_stocktakingWarehouse.setOnClickListener(this);
        tv_stocktakingBrand.setOnClickListener(this);
        tv_stocktakingAmountSum.setOnClickListener(this);
        tv_displayInventory.setOnClickListener(this);
        tv_useSingleDiscount.setOnClickListener(this);
        tv_preciseToQueryStock.setOnClickListener(this);
        tv_firstInputOfGoodsCode.setOnClickListener(this);
        tv_notUseNegativeInventoryCheck.setOnClickListener(this);
        ftv_print_ip.setOnClickListener(this);
        ftv_print_port.setOnClickListener(this);
        ftv_printer.setOnClickListener(this);
        tv_BaseSetting.setOnClickListener(this);
        tv_InputSetting.setOnClickListener(this);
        tv_BarcodePrintSetting.setOnClickListener(this);
        tv_DocuSetting.setOnClickListener(this);
        // 条码打印
        tv_showGoodsCode.setOnClickListener(this);
        tv_showGoodsName.setOnClickListener(this);
        tv_showColor.setOnClickListener(this);
        tv_showSize.setOnClickListener(this);
        tv_showRetailSales.setOnClickListener(this);
        cb_showGoodsCode.setOnClickListener(this);
        cb_showGoodsName.setOnClickListener(this);
        cb_showColor.setOnClickListener(this);
        cb_showSize.setOnClickListener(this);
        cb_showRetailSales.setOnClickListener(this);
        TouchListener tl = new TouchListener();
        etPrintBarcodeType.setOnTouchListener(tl);
        // 装箱单设置
        rg_packingBoxGroup.setOnCheckedChangeListener(this);
        tv_relationSales.setOnClickListener(this);
        tv_relationStockOut.setOnClickListener(this);
        rb_relationSales.setOnClickListener(this);
        rb_relationStockOut.setOnClickListener(this);
        // 多颜色尺码录入设置
        rg_goodsMultiSelectGroup.setOnCheckedChangeListener(this);
        rg_multiSelectTypeGroup.setOnCheckedChangeListener(this);
        tv_commonSelect.setOnClickListener(this);
        tv_multiSelect.setOnClickListener(this);
        rb_commonSelect.setOnClickListener(this);
        rb_multiSelect.setOnClickListener(this);
        tv_singleColorMultiSizeSelect.setOnClickListener(this);
        tv_multiColorMultiSizeSelect.setOnClickListener(this);
        rb_singleColorMultiSizeSelect.setOnClickListener(this);
        rb_multiColorMultiSizeSelect.setOnClickListener(this);
    }

    @Override
    protected void findViewById() {
        mWebView = (WebView) findViewById(R.id.myWeb);
        tv_BaseSetting = (CheckBoxTextView) findViewById(R.id.base_setting);
        tv_InputSetting = (CheckBoxTextView) findViewById(R.id.input_setting);
        tv_BarcodePrintSetting = (CheckBoxTextView) findViewById(R.id.barcode_print_setting);
        tv_DocuSetting = (CheckBoxTextView) findViewById(R.id.docu_setting);
        ll_goods_info = (LinearLayout) findViewById(R.id.ll_goods_info);
        ll_input_setting = (LinearLayout) findViewById(R.id.ll_input_setting);
        ll_docu_setting = (LinearLayout) findViewById(R.id.ll_docu_setting);
        ll_barcode_print_setting = (LinearLayout) findViewById(R.id.ll_barcode_print_setting);
        ll_update = (LinearLayout) findViewById(R.id.ll_update);
        ll_above = (LinearLayout) findViewById(R.id.ll_above);
        etPrintIp = (EditText) findViewById(R.id.et_print_ip);
        etPrintPort = (EditText) findViewById(R.id.et_print_port);
        etPrinter = (EditText) findViewById(R.id.et_printer);
        etPrintBarcodeWidth = (EditText) findViewById(R.id.et_print_barcode_width);
        etPrintBarcodeHeight = (EditText) findViewById(R.id.et_print_barcode_height);
        etPrintBarcodeType = (EditText) findViewById(R.id.et_print_barcode_type);
        tv_save = (TextView) findViewById(R.id.save);
        tv_version = (TextView) findViewById(R.id.version);
        cb_useGoodsboxBarcodeInStocktaking = (CheckBox) findViewById(R.id.useGoodsboxBarcodeInStocktaking);
        cb_useSupplierCodeToAreaProtection = (CheckBox) findViewById(R.id.useSupplierCodeToAreaProtection);
        cb_notAllow = (CheckBox) findViewById(R.id.notAllow);
        cb_coverDoc = (CheckBox) findViewById(R.id.coverDoc);
        cb_useDefSupplier = (CheckBox) findViewById(R.id.useDefSupplier);
        cb_barcodeWarningTone = (CheckBox) findViewById(R.id.barcodeWarningTone);
        cb_useAreaProtection = (CheckBox) findViewById(R.id.useAreaProtection);
        cb_salesOrderEmployee = (CheckBox) findViewById(R.id.salesOrderEmployee);
        cb_salesOrderCustomer = (CheckBox) findViewById(R.id.salesOrderCustomer);
        cb_salesOrderBrand = (CheckBox) findViewById(R.id.salesOrderBrand);
        cb_salesOrderMemo = (CheckBox) findViewById(R.id.salesOrderMemo);
        cb_salesOrderDepartment = (CheckBox) findViewById(R.id.salesOrderDepartment); // 订货部门
        cb_salesOrderWarehouse = (CheckBox) findViewById(R.id.salesOrderWarehouse); // 发货部门
        cb_salesOrderAmountSum = (CheckBox) findViewById(R.id.salesOrderAmountSum);
        cb_salesEmployee = (CheckBox) findViewById(R.id.salesEmployee);
        cb_salesCustomer = (CheckBox) findViewById(R.id.salesCustomer);
        cb_salesBrand = (CheckBox) findViewById(R.id.salesBrand);
        cb_salesMemo = (CheckBox) findViewById(R.id.salesMemo);
        cb_salesOrderNo = (CheckBox) findViewById(R.id.salesOrderNo); // 订单单号
        cb_salesWarehouse = (CheckBox) findViewById(R.id.salesWarehouse); // 发货部门
        cb_salesAmountSum = (CheckBox) findViewById(R.id.salesAmountSum);
        cb_salesReturnEmployee = (CheckBox) findViewById(R.id.salesReturnEmployee);
        cb_salesReturnCustomer = (CheckBox) findViewById(R.id.salesReturnCustomer);
        cb_salesReturnBrand = (CheckBox) findViewById(R.id.salesReturnBrand);
        cb_salesReturnMemo = (CheckBox) findViewById(R.id.salesReturnMemo);
        cb_salesReturnWarehouse = (CheckBox) findViewById(R.id.salesReturnWarehouse); // 退货部门
        cb_salesReturnDepartment = (CheckBox) findViewById(R.id.salesReturnDepartment); // 收货部门
        cb_salesReturnAmountSum = (CheckBox) findViewById(R.id.salesReturnAmountSum);

        cb_purchaseOrderEmployee = (CheckBox) findViewById(R.id.purchaseOrderEmployee);
        cb_purchaseOrderSupplier = (CheckBox) findViewById(R.id.purchaseOrderSupplier);
        cb_purchaseOrderBrand = (CheckBox) findViewById(R.id.purchaseOrderBrand);
        cb_purchaseOrderMemo = (CheckBox) findViewById(R.id.purchaseOrderMemo);
        cb_purchaseOrderDepartment = (CheckBox) findViewById(R.id.purchaseOrderDepartment); // 收货部门
        cb_purchaseOrderAmountSum = (CheckBox) findViewById(R.id.purchaseOrderAmountSum);

        cb_purchaseEmployee = (CheckBox) findViewById(R.id.purchaseEmployee);
        cb_purchaseSupplier = (CheckBox) findViewById(R.id.purchaseSupplier);
        cb_purchaseBrand = (CheckBox) findViewById(R.id.purchaseBrand);
        cb_purchaseMemo = (CheckBox) findViewById(R.id.purchaseMemo);
        cb_purchaseDepartment = (CheckBox) findViewById(R.id.purchaseDepartment); // 收货部门
        cb_purchaseAmountSum = (CheckBox) findViewById(R.id.purchaseAmountSum);
        cb_purchaseReturnEmployee = (CheckBox) findViewById(R.id.purchaseReturnEmployee);
        cb_purchaseReturnSupplier = (CheckBox) findViewById(R.id.purchaseReturnSupplier);
        cb_purchaseReturnBrand = (CheckBox) findViewById(R.id.purchaseReturnBrand);
        cb_purchaseReturnMemo = (CheckBox) findViewById(R.id.purchaseReturnMemo);
        cb_purchaseReturnDepartment = (CheckBox) findViewById(R.id.purchaseReturnDepartment); // 退货部门
        cb_purchaseReturnAmountSum = (CheckBox) findViewById(R.id.purchaseReturnAmountSum);
        cb_stockMoveEmployee = (CheckBox) findViewById(R.id.stockMoveEmployee);
        cb_stockMoveMemo = (CheckBox) findViewById(R.id.stockMoveMemo);
        cb_stockMoveWarehouseIn = (CheckBox) findViewById(R.id.stockMoveWarehouseIn); // 转进仓库
        cb_stockMoveWarehouseOut = (CheckBox) findViewById(R.id.stockMoveWarehouseOut); // 转出仓库
        cb_stockMoveBrand = (CheckBox) findViewById(R.id.stockMoveBrand);
        cb_stockMoveAmountSum = (CheckBox) findViewById(R.id.stockMoveAmountSum);
        cb_stockInEmployee = (CheckBox) findViewById(R.id.stockInEmployee);
        cb_stockInMemo = (CheckBox) findViewById(R.id.stockInMemo);
        cb_stockInWarehouse = (CheckBox) findViewById(R.id.stockInWarehouse); // 仓库名称
        cb_stockInWarehouseOut = (CheckBox) findViewById(R.id.stockInWarehouseOut); // 转出仓库
        cb_stockInBrand = (CheckBox) findViewById(R.id.stockInBrand);
        cb_stockInRelationNo = (CheckBox) findViewById(R.id.stockInRelationNo); // 对应单号
        cb_stockInAmountSum = (CheckBox) findViewById(R.id.stockInAmountSum);
        cb_stockOutEmployee = (CheckBox) findViewById(R.id.stockOutEmployee);
        cb_stockOutMemo = (CheckBox) findViewById(R.id.stockOutMemo);
        cb_stockOutWarehouse = (CheckBox) findViewById(R.id.stockOutWarehouse); // 仓库名称
        cb_stockOutWarehouseIn = (CheckBox) findViewById(R.id.stockOutWarehouseIn); // 转进仓库
        cb_stockOutBrand = (CheckBox) findViewById(R.id.stockOutBrand);
        cb_stockOutRelationNo = (CheckBox) findViewById(R.id.stockOutRelationNo); // 对应单号
        cb_stockOutAmountSum = (CheckBox) findViewById(R.id.stockOutAmountSum);
        cb_stocktakingEmployee = (CheckBox) findViewById(R.id.stocktakingEmployee);
        cb_stocktakingMemo = (CheckBox) findViewById(R.id.stocktakingMemo);
        cb_stocktakingWarehouse = (CheckBox) findViewById(R.id.stocktakingWarehouse);
        cb_stocktakingBrand = (CheckBox) findViewById(R.id.stocktakingBrand);
        cb_stocktakingAmountSum = (CheckBox) findViewById(R.id.stocktakingAmountSum);
        cb_useSingleDiscount = (CheckBox) findViewById(R.id.useSingleDiscount);
        cb_displayInventory = (CheckBox) findViewById(R.id.displayInventory);
        cb_preciseToQueryStock = (CheckBox) findViewById(R.id.preciseToQueryStock);
        cb_firstInputOfGoodsCode = (CheckBox) findViewById(R.id.firstInputOfGoodsCode);
        cb_notUseNegativeInventoryCheck = (CheckBox) findViewById(R.id.notUseNegativeInventoryCheck);
        cb_showGoodsCode = (CheckBox) findViewById(R.id.showGoodsCode);
        cb_showGoodsName = (CheckBox) findViewById(R.id.showGoodsName);
        cb_showColor = (CheckBox) findViewById(R.id.showColor);
        cb_showSize = (CheckBox) findViewById(R.id.showSize);
        cb_showRetailSales = (CheckBox) findViewById(R.id.showRetailSales);
        tv_useGoodsboxBarcodeInStocktaking = (TextView) findViewById(R.id.tvUseGoodsboxBarcodeInStocktaking);
        tv_useSupplierCodeToAreaProtection = (TextView) findViewById(R.id.tvUseSupplierCodeToAreaProtection);
        tv_coverDoc = (TextView) findViewById(R.id.tvCoverDoc);
        tv_notAllow = (TextView) findViewById(R.id.tvNotAllow);
        tv_useDefSupplier = (TextView) findViewById(R.id.tvUseDefSupplier);
        tv_barcodeWarningTone = (TextView) findViewById(R.id.tvBarcodeWarningTone);
        tv_useAreaProtection = (TextView) findViewById(R.id.tvUseAreaProtection);
        tv_salesOrderEmployee = (TextView) findViewById(R.id.tvSalesOrderEmployee);
        tv_salesOrderCustomer = (TextView) findViewById(R.id.tvSalesOrderCustomer);
        tv_salesOrderBrand = (TextView) findViewById(R.id.tvSalesOrderBrand);
        tv_salesOrderMemo = (TextView) findViewById(R.id.tvSalesOrderMemo);
        tv_salesOrderDepartment = (TextView) findViewById(R.id.tvSalesOrderDepartment); // 订货部门
        tv_salesOrderWarehouse = (TextView) findViewById(R.id.tvSalesOrderWarehouse); // 发货部门
        tv_salesOrderAmountSum = (TextView) findViewById(R.id.tvSalesOrderAmountSum);
        tv_salesEmployee = (TextView) findViewById(R.id.tvSalesEmployee);
        tv_salesCustomer = (TextView) findViewById(R.id.tvSalesCustomer);
        tv_salesBrand = (TextView) findViewById(R.id.tvSalesBrand);
        tv_salesMemo = (TextView) findViewById(R.id.tvSalesMemo);
        tv_salesOrderNo = (TextView) findViewById(R.id.tvSalesOrderNo); // 订单单号
        tv_salesWarehouse = (TextView) findViewById(R.id.tvSalesWarehouse); // 发货部门
        tv_salesAmountSum = (TextView) findViewById(R.id.tvSalesAmountSum);
        tv_salesReturnEmployee = (TextView) findViewById(R.id.tvSalesReturnEmployee);
        tv_salesReturnCustomer = (TextView) findViewById(R.id.tvSalesReturnCustomer);
        tv_salesReturnBrand = (TextView) findViewById(R.id.tvSalesReturnBrand);
        tv_salesReturnMemo = (TextView) findViewById(R.id.tvSalesReturnMemo);
        tv_salesReturnWarehouse = (TextView) findViewById(R.id.tvSalesReturnWarehouse); // 退货部门
        tv_salesReturnDepartment = (TextView) findViewById(R.id.tvSalesReturnDepartment); // 收货部门
        tv_salesReturnAmountSum = (TextView) findViewById(R.id.tvSalesReturnAmountSum);

        tv_purchaseOrderEmployee = (TextView) findViewById(R.id.tvPurchaseOrderEmployee);
        tv_purchaseOrderSupplier = (TextView) findViewById(R.id.tvPurchaseOrderSupplier);
        tv_purchaseOrderBrand = (TextView) findViewById(R.id.tvPurchaseOrderBrand);
        tv_purchaseOrderMemo = (TextView) findViewById(R.id.tvPurchaseOrderMemo);
        tv_purchaseOrderDepartment = (TextView) findViewById(R.id.tvPurchaseOrderDepartment); // 收货部门
        tv_purchaseOrderAmountSum = (TextView) findViewById(R.id.tvPurchaseOrderAmountSum);

        tv_purchaseEmployee = (TextView) findViewById(R.id.tvPurchaseEmployee);
        tv_purchaseSupplier = (TextView) findViewById(R.id.tvPurchaseSupplier);
        tv_purchaseBrand = (TextView) findViewById(R.id.tvPurchaseBrand);
        tv_purchaseMemo = (TextView) findViewById(R.id.tvPurchaseMemo);
        tv_purchaseDepartment = (TextView) findViewById(R.id.tvPurchaseDepartment); // 收货部门
        tv_purchaseAmountSum = (TextView) findViewById(R.id.tvPurchaseAmountSum);
        tv_purchaseReturnEmployee = (TextView) findViewById(R.id.tvPurchaseReturnEmployee);
        tv_purchaseReturnSupplier = (TextView) findViewById(R.id.tvPurchaseReturnSupplier);
        tv_purchaseReturnBrand = (TextView) findViewById(R.id.tvPurchaseReturnBrand);
        tv_purchaseReturnMemo = (TextView) findViewById(R.id.tvPurchaseReturnMemo);
        tv_purchaseReturnDepartment = (TextView) findViewById(R.id.tvPurchaseReturnDepartment); // 退货部门
        tv_purchaseReturnAmountSum = (TextView) findViewById(R.id.tvPurchaseReturnAmountSum);
        tv_stockMoveEmployee = (TextView) findViewById(R.id.tvStockMoveEmployee);
        tv_stockMoveMemo = (TextView) findViewById(R.id.tvStockMoveMemo);
        tv_stockMoveWarehouseIn = (TextView) findViewById(R.id.tvStockMoveWarehouseIn); // 转进仓库
        tv_stockMoveWarehouseOut = (TextView) findViewById(R.id.tvStockMoveWarehouseOut); // 转出仓库
        tv_stockMoveBrand = (TextView) findViewById(R.id.tvStockMoveBrand);
        tv_stockMoveAmountSum = (TextView) findViewById(R.id.tvStockMoveAmountSum);
        tv_stockInEmployee = (TextView) findViewById(R.id.tvStockInEmployee);
        tv_stockInMemo = (TextView) findViewById(R.id.tvStockInMemo);
        tv_stockInWarehouse = (TextView) findViewById(R.id.tvStockInWarehouse); // 仓库名称
        tv_stockInWarehouseOut = (TextView) findViewById(R.id.tvStockInWarehouseOut); // 转出仓库
        tv_stockInBrand = (TextView) findViewById(R.id.tvStockInBrand);
        tv_stockInRelationNo = (TextView) findViewById(R.id.tvStockInRelationNo); // 对应单号
        tv_stockInAmountSum = (TextView) findViewById(R.id.tvStockInAmountSum);
        tv_stockOutEmployee = (TextView) findViewById(R.id.tvStockOutEmployee);
        tv_stockOutMemo = (TextView) findViewById(R.id.tvStockOutMemo);
        tv_stockOutWarehouse = (TextView) findViewById(R.id.tvStockOutWarehouse); // 仓库名称
        tv_stockOutWarehouseIn = (TextView) findViewById(R.id.tvStockOutWarehouseIn); // 转进仓库
        tv_stockOutBrand = (TextView) findViewById(R.id.tvStockOutBrand);
        tv_stockOutRelationNo = (TextView) findViewById(R.id.tvStockOutRelationNo); // 对应单号
        tv_stockOutAmountSum = (TextView) findViewById(R.id.tvStockOutAmountSum);
        tv_stocktakingEmployee = (TextView) findViewById(R.id.tvStocktakingEmployee);
        tv_stocktakingMemo = (TextView) findViewById(R.id.tvStocktakingMemo);
        tv_stocktakingWarehouse = (TextView) findViewById(R.id.tvStocktakingWarehouse);
        tv_stocktakingBrand = (TextView) findViewById(R.id.tvStocktakingBrand);
        tv_stocktakingAmountSum = (TextView) findViewById(R.id.tvStocktakingAmountSum);
        tv_relationSales = (TextView) findViewById(R.id.tvRelationSales);
        tv_relationStockOut = (TextView) findViewById(R.id.tvRelationStockOut);
        tv_useSingleDiscount = (TextView) findViewById(R.id.tvUseSingleDiscount);
        tv_displayInventory = (TextView) findViewById(R.id.tvDisplayInventory);
        tv_commonSelect = (TextView) findViewById(R.id.tvCommonSelect);
        tv_multiSelect = (TextView) findViewById(R.id.tvMultiSelect);
        tv_singleColorMultiSizeSelect = (TextView) findViewById(R.id.tvSingleColorMultiSizeSelect);
        tv_multiColorMultiSizeSelect = (TextView) findViewById(R.id.tvMultiColorMultiSizeSelect);
        tv_preciseToQueryStock = (TextView) findViewById(R.id.tvPreciseToQueryStock);
        tv_firstInputOfGoodsCode = (TextView) findViewById(R.id.tvFirstInputOfGoodsCode);
        tv_notUseNegativeInventoryCheck = (TextView) findViewById(R.id.tvNotUseNegativeInventoryCheck);
        tv_showGoodsCode = (TextView) findViewById(R.id.tvShowGoodsCode);
        tv_showGoodsName = (TextView) findViewById(R.id.tvShowGoodsName);
        tv_showColor = (TextView) findViewById(R.id.tvShowColor);
        tv_showSize = (TextView) findViewById(R.id.tvShowSize);
        tv_showRetailSales = (TextView) findViewById(R.id.tvShowRetailSales);
        ftv_print_ip = (FontTextView) findViewById(R.id.ftv_print_ip);
        ftv_print_port = (FontTextView) findViewById(R.id.ftv_print_port);
        ftv_printer = (FontTextView) findViewById(R.id.ftv_printer);
        rg_packingBoxGroup = (RadioGroup) findViewById(R.id.packingBoxGroup);
        rg_goodsMultiSelectGroup = (RadioGroup) findViewById(R.id.goodsMultiSelectGroup);
        rg_multiSelectTypeGroup = (RadioGroup) findViewById(R.id.multiSelectTypeGroup);
        rb_relationSales = (RadioButton) findViewById(R.id.relationSales);
        rb_relationStockOut = (RadioButton) findViewById(R.id.relationStockOut);
        rb_commonSelect = (RadioButton) findViewById(R.id.commonSelect);
        rb_multiSelect = (RadioButton) findViewById(R.id.multiSelect);
        rb_singleColorMultiSizeSelect = (RadioButton) findViewById(R.id.singleColorMultiSizeSelect);
        rb_multiColorMultiSizeSelect = (RadioButton) findViewById(R.id.multiColorMultiSizeSelect);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.relationSales:
                packingBoxRelationType = 0;
                break;
            case R.id.relationStockOut:
                packingBoxRelationType = 1;
                break;
            case R.id.commonSelect:
                goodsMultiSelectType = 0;
                break;
            case R.id.multiSelect:
                goodsMultiSelectType = 1;
                break;
            case R.id.singleColorMultiSizeSelect:
                multiSelectType = 0;
                break;
            case R.id.multiColorMultiSizeSelect:
                multiSelectType = 1;
                break;
            default:
                packingBoxRelationType = 0;
                goodsMultiSelectType = 0;
                break;
        }
        // 装箱单
        if (null == pPackingBoxRelationType) {
            pPackingBoxRelationType = new Paramer("packingBoxRelationType", String.valueOf(packingBoxRelationType));
        } else {
            pPackingBoxRelationType.setValue(String.valueOf(packingBoxRelationType));
        }
        selectCheck(packingBoxRelationType);
        // 多货品颜色尺码录入
        if (null == pGoodsMultiSelectType) {
            pGoodsMultiSelectType = new Paramer("goodsMultiSelectType", String.valueOf(goodsMultiSelectType));
        } else {
            pGoodsMultiSelectType.setValue(String.valueOf(goodsMultiSelectType));
        }
        goodsMultiSelectCheck(goodsMultiSelectType);
        // 多货品颜色尺码录入方式
        if (null == pMultiSelectType) {
            pMultiSelectType = new Paramer("multiSelectType", String.valueOf(multiSelectType));
        } else {
            pMultiSelectType.setValue(String.valueOf(multiSelectType));
        }
        multiSelectCheck(multiSelectType);
    }

    /**
     * 改变单选按钮的值(装箱单)
     * 
     * @param relationType
     */
    private void selectCheck(int relationType) {
        if (relationType == 0) {
            rb_relationSales.setChecked(true);
            rb_relationStockOut.setChecked(false);
        } else if (relationType == 1) {
            rb_relationStockOut.setChecked(true);
            rb_relationSales.setChecked(false);
        }
    }

    /**
     * 改变单选按钮的值(多货号颜色尺码录入)
     * 
     * @param goodsMultiSelectType
     */
    private void goodsMultiSelectCheck(int goodsMultiSelectType) {
        if (goodsMultiSelectType == 0) {
            rb_commonSelect.setChecked(true);
            rb_multiSelect.setChecked(false);
        } else if (goodsMultiSelectType == 1) {
            rb_commonSelect.setChecked(false);
            rb_multiSelect.setChecked(true);
        }
    }

    /**
     * 改变单选按钮的值(多货号颜色尺码录入方式)
     * 
     * @param multiSelectType
     */
    private void multiSelectCheck(int multiSelectType) {
        if (multiSelectType == 0) {
            rb_singleColorMultiSizeSelect.setChecked(true);
            rb_multiColorMultiSizeSelect.setChecked(false);
        } else if (multiSelectType == 1) {
            rb_singleColorMultiSizeSelect.setChecked(false);
            rb_multiColorMultiSizeSelect.setChecked(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case R.id.et_print_barcode_type:
                if (resultCode == 1) {
                    etPrintBarcodeType.setText(data.getStringExtra("Name"));
                }
                break;
            default:
                break;
        }
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.et_print_barcode_type:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(SettingActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectBarcodePrintType");
                        startActivityForResult(intent, R.id.et_print_barcode_type);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }

}
