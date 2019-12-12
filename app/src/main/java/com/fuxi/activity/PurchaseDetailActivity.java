package com.fuxi.activity;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.fuxi.adspter.OddAdapter;
import com.fuxi.adspter.SalesOddAdapter;
import com.fuxi.dao.BarCodeDao;
import com.fuxi.dao.GoodsBoxBarcodeRecordDao;
import com.fuxi.dao.LatestPriceDao;
import com.fuxi.dao.ParamerDao;
import com.fuxi.main.R;
import com.fuxi.task.ImageTask;
import com.fuxi.util.Logger;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.Tools;
import com.fuxi.vo.BarCode;
import com.fuxi.vo.GoodsBoxBarcodeRecord;
import com.fuxi.vo.LatestPrice;
import com.fuxi.vo.Paramer;
import com.fuxi.vo.RequestVo;
import com.fuxi.widget.PopWinShare;

/**
 * Title: PurchaseDetailActivity Description: 采购收货单明细活动界面
 * 
 * @author LYJ
 * 
 */
public class PurchaseDetailActivity extends BaseWapperActivity implements OnItemLongClickListener {

    private static final String TAG = "PurchaseDetailActivity";
    private static final String method = "/purchase.do?purchaseEdit";
    private static final String saveOrderMethod = "/purchase.do?savePurchase";
    private static final String auditMethod = "/purchase.do?auditOrder";
    private static final String deleteMethod = "/purchase.do?deleteItem";
    private static final String updateMemo = "/purchase.do?updateMemo";
    private static final String queryAvailableStock = "/inventoryQuery.do?getAvailableStock";
    private static final String coverSavePath = "/purchase.do?coverSave";
    private static final String checkMethod = "/purchase.do?checkGoodsHasSupplier";

    private ListView lv_detail;
    private LinearLayout ll_title; // 扫码区标题
    private LinearLayout ll_saomiao_div; // 扫码区
    private LinearLayout ll_split2; // 分隔符2
    private LinearLayout ll_scanTitle; // ListView显示标题
    private LinearLayout ll_supplier; // 厂商选项
    private LinearLayout ll_brand; // 品牌
    private EditText et_supplier;
    private EditText et_department;
    private EditText et_brand;
    private EditText et_type;
    private EditText et_barcode;
    private EditText et_qty;
    private EditText et_employee;
    private EditText et_price; // 修改单价(菜单)
    private EditText et_stock; // 可发库存(菜单)
    private EditText et_businessDept; // 业务部门(菜单)
    private TextView tv_qtysum;
    private TextView tv_amount; // 总金额
    private TextView tv_symbol; // 金额符号
    private TextView tv_barcode;
    private TextView tv_qty;
    private TextView tv_unitPrice;
    private TextView bt_addDetail;
    private TextView bt_submit;
    private ImageView iv_pic;

    private ArrayList<HashMap<String, Object>> datas = new ArrayList<HashMap<String, Object>>();// 存储箱条码扫描记录
    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> subList = new ArrayList<Map<String, Object>>();// 提交的数量集合
    private List<Map<String, Object>> delList = new ArrayList<Map<String, Object>>();// 要删除的货品
    private SalesOddAdapter salesOddAdapter;
    private OddAdapter oddAdapter;
    private BarCodeDao barCodeDao = new BarCodeDao(this);
    private ParamerDao paramerDao = new ParamerDao(this);
    private LatestPriceDao latestPriceDao = new LatestPriceDao(this);
    private GoodsBoxBarcodeRecordDao recordDao = new GoodsBoxBarcodeRecordDao(this);
    private TouchListener tl = new TouchListener();
    private Paramer find;// 参数
    private PopWinShare popWinShare;

    private String PurchaseID;
    private String auditFlag; // 盘点单的状态(是否审核)
    private String type; // 结算的类型
    private String supplierid;// 厂商
    private String departmentid;
    private String employeeId;
    private String brandId;
    private String businessDeptId;// 经手人的业务部门
    private String businessDeptName;// 经手人的业务部门
    private String typeName;
    private String memo; // 备注信息
    private String lastMemo = "";// 上一次的备注

    private String goodsCode;
    private String direction = "1";
    private Integer qtysum;
    private int inputType = 1;// 装箱方式，1为散件，2为装箱
    private int boxQtySum;
    private double discountRate; // 厂商折扣
    private boolean sendFlag = false;// 防止手工点击保存跟后台自动保存重复提交
    private boolean useLastPrice = false;// 是否使用最近一次价格
    private boolean isAdd = false;// 是否添加的新的货品
    private boolean isCheckGoodsHasSupplier = false;// 是否验证货品含有默认的厂商
    private boolean addNew = false; // 新增单据
    private boolean modifyRight = false;// 修改单据的权限
    private boolean auditRight = false;// 审核单据的权限

    /**
     * ListView添加新记录
     * 
     * @param barcode
     */
    private void addItem(BarCode barcode) {
        addItemQty(barcode);
    }

    /**
     * ListView添加新记录,同时合并相同的记录
     * 
     * @param barcode
     */
    private void addItemQty(BarCode barcode) {
        goodsCode = barcode.getGoodscode();
        // 从DataList中查找
        Map temp = getList(barcode);
        Integer qty = barcode.getQty();
        if (inputType == 2) {
            qty = qty * barcode.getBoxQty();
        }
        if (temp == null) {
            temp = new HashMap();
            temp.put("Barcode", barcode.getBarcode());
            temp.put("GoodsCode", barcode.getGoodscode());
            temp.put("Color", barcode.getColorname());
            temp.put("Size", barcode.getSizename());
            temp.put("Quantity", qty);
            if (boxQtySum > 0) {
                temp.put("OneBoxQty", barcode.getOneBoxQty());
            } else {
                temp.put("OneBoxQty", 0);
            }
            temp.put("GoodsID", barcode.getGoodsid());
            temp.put("ColorID", barcode.getColorid());
            temp.put("SizeID", barcode.getSizeid());
            temp.put("IndexNo", barcode.getIndexno());
            temp.put("UnitPrice", barcode.getUnitPrice());
            temp.put("DiscountPrice", barcode.getDiscountPrice());
            temp.put("RetailSales", barcode.getRetailSales());
            temp.put("SizeGroupID", barcode.getSizeGroupId());
            temp.put("SizeStr", barcode.getSizeStr());
            temp.put("BoxQty", barcode.getBoxQty());
            temp.put("meno", lastMemo);
            dataList.add(0, temp);
        } else {
            Integer oldQty = Integer.parseInt(String.valueOf(temp.get("Quantity")));
            if (boxQtySum > 0) {
                Integer oldBoxQty = Integer.parseInt(String.valueOf(temp.get("BoxQty")));
                temp.put("BoxQty", oldBoxQty + barcode.getBoxQty());
            }
            temp.put("Quantity", oldQty + qty);
        }
        // 检查货品是否有厂商
        if (LoginParameterUtil.useDefSupplier) {
            if (!isCheckGoodsHasSupplier) {// 装箱时只验证一次
                checkGoodsHasSupplier(barcode.getGoodsid(), barcode.getColorid(), barcode.getSizeid());
                isCheckGoodsHasSupplier = true;
            }
        } else {
            // 检查集合中是否有为0的集合
            checkDataList();
            // 计算并显示总数量
            countTotal();
        }
        resetBarcode();
    }

    /**
     * 检查集合中是否有为0的集合
     */
    private void checkDataList() {
        for (int i = 0; i < dataList.size(); i++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(i).get("Quantity")));
            if (num == 0) {
                dataList.remove(i);
                i--;
            }
        }
    }

    /**
     * 计算并显示总数量和价格
     */
    private void countTotal() {
        int sum = 0;
        double amount = 0.0;
        for (int j = 0; j < dataList.size(); j++) {
            int num = Integer.parseInt(String.valueOf(dataList.get(j).get("Quantity")));
            double peice = Double.parseDouble(String.valueOf(dataList.get(j).get("DiscountPrice") == null ? "0" : dataList.get(j).get("DiscountPrice")));
            amount += peice * num;
            sum += num;
        }
        tv_qtysum.setText(String.valueOf(sum));
        if (LoginParameterUtil.purchaseAmountSumRight) {
            tv_amount.setText(Tools.formatDecimal(String.valueOf(amount)));
        } else {
            tv_symbol.setVisibility(View.GONE);
            tv_amount.setVisibility(View.GONE);
        }
        // 重新计算箱数
        if (boxQtySum > 0) {
            for (int i = 0; i < dataList.size(); i++) {
                int count = 0;
                Map map = dataList.get(i);
                // 获取同货号同颜色的数量
                for (int j = 0; j < dataList.size(); j++) {
                    Map temp = dataList.get(j);
                    if (map.get("GoodsID").equals(temp.get("GoodsID")) && map.get("ColorID").equals(temp.get("ColorID"))) {
                        count++;
                    }
                }
                if (null != PurchaseID && !"".equals(PurchaseID) && map.containsKey("QuantitySum")) {
                    int oneBox = Integer.parseInt(String.valueOf(map.get("OneBoxQty")));
                    if (oneBox >= count) {
                        map.put("OneBoxQty", oneBox / count);
                    }
                }
            }
        }
        // 添加发货单明细时显示保存按钮
        if ((dataList.size() > 0 && (!"true".equals(auditFlag)))) {
            // 判断用户是否有修改单据的操作权限
            if (PurchaseID == null || modifyRight) {
                ll_saomiao_div.setVisibility(View.VISIBLE);
                ll_split2.setVisibility(View.VISIBLE);
            } else {
                ll_saomiao_div.setVisibility(View.GONE);
                ll_split2.setVisibility(View.GONE);
            }
            if (boxQtySum > 0) {
                showBoxTitle();
            }
            // 客户,类型,部门,经手人可读
            et_supplier.setEnabled(false);
            et_department.setEnabled(false);
            et_brand.setEnabled(false);
            et_type.setEnabled(false);
            et_employee.setEnabled(false);
            if (null != PurchaseID && !"".equals(PurchaseID) && !isAdd) {
                bt_submit.setVisibility(View.VISIBLE);
                bt_submit.setText("审核");
            } else {
                // 当添加新的货品时候，把审核按钮变成保存按钮，如果是隐藏的则显示
                bt_submit.setVisibility(View.VISIBLE);
                bt_submit.setText("保存");
            }
        } else if ("true".equals(auditFlag) && boxQtySum > 0) {
            showBoxTitle();
        }
        // 显示列表
        if (LoginParameterUtil.purchaseUnitPriceRight) {
            salesOddAdapter.refresh();
        } else {
            oddAdapter.refresh();
        }
    }

    /**
     * 显示标题为箱条码
     */
    private void showBoxTitle() {
        setTitle("采购收货单(装箱)");
        inputType = 2;
        tv_barcode.setText("箱号");
        tv_qty.setText("箱数");
    }

    /**
     * 箱条码扫描时往ListView中添加数据
     * 
     * @param barcodeList
     */
    private void addItem(List<BarCode> barcodeList) {
        for (BarCode barcode : barcodeList) {
            // 箱条码使用最近价格
            useLastPrice = Boolean.parseBoolean(find.getValue());
            if (useLastPrice) {
                BigDecimal price = latestPriceDao.find(new LatestPrice(barcode.getGoodsid(), supplierid, "-1", String.valueOf(2)));
                if (null != price && price.compareTo(BigDecimal.ZERO) != 0) {
                    barcode.setDiscountPrice(price);
                }
            }
            Map map = getSameByGoodsID(barcode);
            if (null != map) {
                // 其它处理
                BigDecimal unitPrice = barcode.getUnitPrice();
                if (unitPrice.compareTo(BigDecimal.ZERO) == 0) {
                    BigDecimal discountPrice = new BigDecimal(String.valueOf(map.get("DiscountPrice"))).setScale(2, BigDecimal.ROUND_HALF_UP);
                    barcode.setUnitPrice(discountPrice);
                    barcode.setDiscountPrice(discountPrice);
                }
            }
            addItemQty(barcode);
        }
    }

    /**
     * 检查相同货号的货品
     * 
     * @param barcode
     * @return
     */
    private Map getSameByGoodsID(BarCode barcode) {
        for (int i = 0; i < dataList.size(); i++) {
            Map temp = (Map) dataList.get(i);
            if (barcode.getGoodsid().equals(temp.get("GoodsID"))) {
                return temp;
            }
        }
        return null;
    }

    /**
     * 根据条码查找dateList里面对应的货品
     * 
     * @param barcode
     * @return
     */
    private Map getList(BarCode barcode) {
        String sizeStr = barcode.getSizeStr();
        if (null == sizeStr || "".equals(sizeStr) || "null".equalsIgnoreCase(sizeStr)) {
            for (int i = 0; i < dataList.size(); i++) {
                Map temp = (Map) dataList.get(i);
                if (barcode.getGoodsid().equals(temp.get("GoodsID")) && barcode.getColorid().equals(temp.get("ColorID")) && barcode.getSizeid().equals(temp.get("SizeID"))) {
                    return temp;
                }
            }
        } else {
            for (int i = 0; i < dataList.size(); i++) {
                Map temp = (Map) dataList.get(i);
                if (barcode.getGoodsid().equals(temp.get("GoodsID")) && barcode.getColorid().equals(temp.get("ColorID")) && barcode.getSizeStr().equals(temp.get("SizeStr")) && barcode.getSizeid().equals(temp.get("SizeID"))) {
                    return temp;
                }
            }
        }
        return null;
    }

    /**
     * 开启扫描区
     */
    private void closeScanButton() {
        bt_addDetail.setText("开启\n扫描");
        bt_addDetail.setBackground(this.getResources().getDrawable(R.drawable.upload_button));
        et_barcode.setEnabled(false);
        et_barcode.setFocusable(false);
    }

    /**
     * 禁用扫描区
     */
    private void openScanButton() {
        bt_addDetail.setText("添加");
        bt_addDetail.setBackground(this.getResources().getDrawable(R.drawable.custom_button));
        et_barcode.setEnabled(true);
        et_barcode.setFocusable(true);
        et_barcode.setFocusableInTouchMode(true);
        et_barcode.requestFocus();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addDetail:
                if ("添加".equals(bt_addDetail.getText().toString())) {
                    addBarCode();
                } else {
                    openScanButton();
                }
                break;
            case R.id.submit:
                if ("保存".equals(bt_submit.getText())) {
                    submitData();
                } else if ("审核".equals(bt_submit.getText())) {
                    if (auditRight) {
                        audit();
                    } else {
                        Builder dialog = new AlertDialog.Builder(PurchaseDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        dialog.setTitle("提示");
                        dialog.setMessage("当前暂无审核采购收货单的操作权限");
                        // 相当于点击确认按钮
                        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        dialog.setCancelable(false);
                        dialog.create();
                        dialog.show();
                    }
                }
                break;
            case R.id.pic:
                InputMethodManager im = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (im.isActive()) {
                    im.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                if (null != goodsCode && !goodsCode.isEmpty()) {
                    Intent inten = new Intent(PurchaseDetailActivity.this, PictureActivity.class);
                    inten.putExtra("goodsCode", goodsCode);
                    startActivity(inten);
                } else {
                    Toast.makeText(PurchaseDetailActivity.this, "当前图片无对应的货品编码", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHeadRightButton(View v) {
        // 显示菜单
        showpopWinShare();
    }

    /**
     * 根据折扣计算货品折后价格
     * 
     * @param data
     * @return
     */
    protected BigDecimal getPriceByType(double data) {
        double price = 0;
        // 批发
        if (discountRate == 0) {
            discountRate = 10;
        }
        price = data * discountRate / 10;
        BigDecimal decimal = new BigDecimal(price).setScale(2, BigDecimal.ROUND_HALF_UP);
        return decimal;
    }

    /**
     * 扫描条码(新增单据明细记录)
     */
    private void addBarCode() {
        // 添加条码之前先判断是否选择了客户跟部门
        if (supplierid == null && !LoginParameterUtil.useDefSupplier) {
            Toast.makeText(PurchaseDetailActivity.this, "请先选择货品厂商", Toast.LENGTH_SHORT).show();
            et_barcode.setText(null);
            return;
        }
        if (LoginParameterUtil.useDefSupplier) {
            type = "参考进价";
        }
        if (departmentid == null) {
            Toast.makeText(PurchaseDetailActivity.this, R.string.dept_null, Toast.LENGTH_SHORT).show();
            et_barcode.setText(null);
            return;
        }
        typeName = et_type.getText().toString().trim();
        if (typeName == null || "".equals(typeName)) {
            Toast.makeText(PurchaseDetailActivity.this, R.string.type_null, Toast.LENGTH_SHORT).show();
            et_barcode.setText(null);
            return;
        }
        // if (employeeId == null) {
        // Toast.makeText(PurchaseDetailActivity.this, "请先选择经手人",
        // Toast.LENGTH_SHORT).show();
        // et_barcode.setText(null);
        // }
        // if (LoginParameterUtil.useBrandPower
        // && (null == brandId || "".equals(brandId))) {
        // Toast.makeText(PurchaseDetailActivity.this, "请先选择品牌",
        // Toast.LENGTH_SHORT).show();
        // et_barcode.setText(null);
        // }
        String barcodeStr = et_barcode.getText().toString().trim();
        if ("".equals(barcodeStr)) {
            Toast.makeText(PurchaseDetailActivity.this, R.string.barcode_null, Toast.LENGTH_SHORT).show();
            return;
        }
        Integer qty = Integer.valueOf(et_qty.getText().toString());
        // 扫码后允许提交数据
        sendFlag = true;
        // 是否添加了新的货品
        isAdd = true;
        // 验证货品默认厂商
        isCheckGoodsHasSupplier = false;
        bt_submit.setVisibility(View.VISIBLE);
        if (inputType == 1) {
            // 条码
            BarCode barcode = barCodeDao.find(barcodeStr, supplierid, "-1");
            if (barcode == null) {
                ajaxAddItem(barcodeStr, qty);
            } else {
                find = paramerDao.find("useLastPrice");
                useLastPrice = Boolean.parseBoolean(find.getValue());
                if ((supplierid.equals(barcode.getCustomerId()) && "-1".equals(barcode.getType()))) {
                    // 同一个单,同一个货品使用同一个价格
                    for (int j = 0; j < dataList.size(); j++) {
                        if (String.valueOf(dataList.get(j).get("GoodsID")).equals(barcode.getGoodsid())) {
                            barcode.setDiscountPrice(new BigDecimal(String.valueOf(dataList.get(j).get("DiscountPrice"))));
                            break;
                        }
                    }
                    if (!useLastPrice) {// 不使用最近一次价格
                        barcode.setUnitPrice(barcode.getUnitPrice());
                        barcode.setDiscountPrice(getPriceByType(barcode.getUnitPrice().doubleValue()));
                    }
                    barcode.setQty(qty);
                    addItem(barcode);
                    LoadImage(barcode);
                } else {
                    ajaxAddItem(barcodeStr, qty);
                }
            }
        } else {
            // 箱条码识别
            ajaxAddBoxItem(barcodeStr, qty);
        }
    }

    /**
     * 保存箱条码的扫描记录
     * 
     * @param barcodeStr
     * @param qty
     * @param goodsId
     * @param colorId
     * @param sizeStr
     */
    private void saveGoodsBoxBarcode(String barcodeStr, Integer qty, String goodsId, String colorId, String sizeStr) {
        boolean exit = false;
        for (int i = 0; i < datas.size(); i++) {
            HashMap<String, Object> hm = datas.get(i);
            String barcode = String.valueOf(hm.get("Barcode"));
            if (barcodeStr.equals(barcode)) {
                int boxQty = Integer.parseInt(String.valueOf(hm.get("BoxQty")));
                hm.put("BoxQty", (boxQty + qty));
                exit = true;
            }
        }
        // 集合中不存在箱条码
        if (!exit) {
            HashMap<String, Object> hm = new HashMap<String, Object>();
            hm.put("Barcode", barcodeStr);
            hm.put("BoxQty", qty);
            hm.put("GoodsID", goodsId);
            hm.put("ColorID", colorId);
            hm.put("SizeStr", sizeStr);
            datas.add(hm);
        }
    }

    /**
     * 检查货品是否有厂商
     * 
     * @param goodsId
     * @param colorId
     * @param sizeId
     */
    private void checkGoodsHasSupplier(final String goodsId, final String colorId, final String sizeId) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = checkMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsId", goodsId);
        vo.requestDataMap = map;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject paramObject, boolean paramBoolean) {
                try {
                    if (paramObject.getBoolean("success")) {
                        String supplierId = paramObject.getString("obj");
                        for (int i = 0; i < dataList.size(); i++) {
                            Map<String, Object> map = dataList.get(i);
                            String tGoodsId = String.valueOf(map.get("GoodsID"));
                            if (tGoodsId.equals(goodsId)) {
                                map.put("SupplierID", supplierId);
                            }
                        }
                    } else {
                        Toast.makeText(PurchaseDetailActivity.this, "此货品的厂商没有定义,货品添加失败", Toast.LENGTH_SHORT).show();
                        for (int i = 0; i < dataList.size(); i++) {
                            Map<String, Object> map = dataList.get(i);
                            String tGoodsId = String.valueOf(map.get("GoodsID"));
                            String tColorId = String.valueOf(map.get("ColorID"));
                            String tSizeId = String.valueOf(map.get("SizeID"));
                            if (inputType == 1) {
                                if (tGoodsId.equals(goodsId) && tColorId.equals(colorId) && tSizeId.equals(sizeId)) {
                                    dataList.remove(i);
                                    i--;
                                }
                            } else {
                                if (tGoodsId.equals(goodsId) && tColorId.equals(colorId)) {
                                    dataList.remove(i);
                                    i--;
                                }
                            }
                        }
                    }
                    // 检查集合中是否有为0的集合
                    checkDataList();
                    // 计算价格
                    countTotal();
                } catch (JSONException e) {
                    Toast.makeText(PurchaseDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 根据货品编码加载货品图片
     * 
     * @param barcode
     */
    private void LoadImage(BarCode barcode) {
        // 加载图片
        String url = LoginParameterUtil.customer.getIp();
        url = url.concat("/common.do?image&code=").concat(barcode.getGoodscode());
        File imageFile = Tools.getImage(getApplicationContext(), barcode.getGoodscode());
        if (!imageFile.exists()) {
            ImageTask itTask = new ImageTask(imageFile, url, barcode, iv_pic);
            itTask.execute();
        } else {
            // 显示图片
            setImage(imageFile);
        }
    }

    /**
     * 设置显示图片
     * 
     * @param imageFile
     */
    private void setImage(File imageFile) {
        setImage(imageFile.getAbsolutePath());
    }

    /**
     * 设置显示图片
     * 
     * @param imagePath
     */
    private void setImage(String imagePath) {
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        if (null == bm) {
            // 显示默认图片
            iv_pic.setImageResource(R.drawable.unfind);
            return;
        }
        iv_pic.setImageBitmap(bm);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void loadViewLayout() {
        if (LoginParameterUtil.purchaseUnitPriceRight) {
            setContentView(R.layout.activity_purchase_detail);
        } else {
            setContentView(R.layout.activity_purchase_detail_no_price);
        }
        setTitle("采购收货单(散件)");
    }

    /**
     * 重置扫码区
     */
    private void resetBarcode() {
        et_barcode.setText(null);
        et_qty.setText("1");
        et_barcode.setSelection(0);
        et_barcode.requestFocus();
    }

    // //模拟删除键
    // private void reproduceDelete(){
    // int keyCode =KeyEvent.KEYCODE_DEL;
    // KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
    // KeyEvent keyEventUp = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
    // et_barcode.onKeyDown(keyCode, keyEventDown);
    // et_barcode.onKeyUp(keyCode, keyEventUp);
    // }

    /**
     * 解析条码
     * 
     * @param barcodeStr
     * @param qty
     */
    public void ajaxAddItem(String barcodeStr, final int qty) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = "/select.do?analyticalBarcode";
        vo.context = this;
        HashMap map = new HashMap();
        map.put("BarCode", barcodeStr);
        map.put("CustomerId", supplierid);
        map.put("Type", type);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject barCodeInfo = retObj.getJSONObject("obj");
                        BarCode bc = new BarCode();
                        bc.setBarcode(barCodeInfo.getString("BarCode"));
                        bc.setGoodsid(barCodeInfo.getString("GoodsID"));
                        bc.setGoodscode(barCodeInfo.getString("GoodsCode"));
                        bc.setGoodsname(barCodeInfo.getString("GoodsName"));
                        bc.setColorid(barCodeInfo.getString("ColorID"));
                        bc.setColorcode(barCodeInfo.getString("ColorCode"));
                        bc.setColorname(barCodeInfo.getString("ColorName"));
                        bc.setSizeid(barCodeInfo.getString("SizeID"));
                        bc.setSizename(barCodeInfo.getString("SizeName"));
                        bc.setSizecode(barCodeInfo.getString("SizeCode"));
                        bc.setSizeGroupId(barCodeInfo.getString("SizeGroupID"));
                        bc.setSizeStr(null);// 每箱的配码
                        if (barCodeInfo.isNull("IndexNo")) {
                            bc.setIndexno(1);
                        } else {
                            barCodeInfo.getInt("IndexNo");
                        }
                        bc.setType("-1");
                        bc.setQty(qty);
                        bc.setCustomerId(supplierid);
                        if (barCodeInfo.isNull("RetailSales")) {
                            bc.setRetailSales(null);
                        } else {
                            bc.setRetailSales(new BigDecimal(barCodeInfo.getDouble("RetailSales")));
                        }
                        if (barCodeInfo.isNull("UnitPrice")) {
                            bc.setUnitPrice(null);
                            bc.setDiscountPrice(null);
                        } else {
                            bc.setUnitPrice(new BigDecimal(barCodeInfo.getDouble("UnitPrice")));
                            bc.setDiscountPrice(getPriceByType(barCodeInfo.getDouble("UnitPrice")));
                        }
                        barCodeDao.insert(bc);
                        addItem(bc);
                        LoadImage(bc);
                    } else {
                        // 设置条码提示音
                        if (LoginParameterUtil.barcodeWarningTone) {
                            settingVoice("error", getApplicationContext());
                        }
                        Toast.makeText(PurchaseDetailActivity.this, "条码错误或不存在此条码", Toast.LENGTH_SHORT).show();
                        et_barcode.selectAll();
                    }
                } catch (Exception e) {
                    et_barcode.selectAll();
                    Toast.makeText(PurchaseDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 解析箱条码
     * 
     * @param barcodeStr
     * @param qty
     */
    public void ajaxAddBoxItem(final String barcodeStr, final int qty) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = "/select.do?getBoxCode";
        vo.context = this;
        HashMap map = new HashMap();
        map.put("BarCode", barcodeStr);
        map.put("CustomerId", supplierid);
        map.put("Type", type);
        vo.requestDataMap = map;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONArray jsonList = retObj.getJSONArray("obj");
                        List<BarCode> list = new ArrayList<BarCode>();
                        for (int i = 0; i < jsonList.length(); i++) {
                            JSONObject barCodeInfo = jsonList.getJSONObject(i);
                            BarCode bc = new BarCode();
                            bc.setGoodsid(barCodeInfo.getString("GoodsID"));
                            bc.setGoodscode(barCodeInfo.getString("GoodsCode"));
                            bc.setColorid(barCodeInfo.getString("ColorID"));
                            bc.setColorname(barCodeInfo.getString("ColorName"));
                            bc.setSizeid(barCodeInfo.getString("SizeID"));
                            bc.setSizename(barCodeInfo.getString("SizeName"));
                            bc.setIndexno(barCodeInfo.getInt("IndexNo"));
                            bc.setSizeGroupId(barCodeInfo.getString("SizeGroupID"));
                            bc.setQty(barCodeInfo.getInt("Quantity"));
                            bc.setBoxQty(qty);
                            bc.setOneBoxQty(barCodeInfo.getInt("Quantity"));// 每箱中的数量
                            bc.setSizeStr(barCodeInfo.getString("SizeStr"));// 每箱的配码
                            if (barCodeInfo.isNull("RetailSales")) {
                                bc.setRetailSales(null);
                            } else {
                                bc.setRetailSales(new BigDecimal(barCodeInfo.getDouble("RetailSales")));
                            }
                            if (barCodeInfo.isNull("UnitPrice")) {
                                bc.setUnitPrice(null);
                                bc.setDiscountPrice(null);
                            } else {
                                bc.setUnitPrice(new BigDecimal(barCodeInfo.getDouble("UnitPrice")));
                                bc.setDiscountPrice(getPriceByType(barCodeInfo.getDouble("UnitPrice")));
                            }
                            list.add(bc);
                            Integer id = latestPriceDao.findId(new LatestPrice(bc.getGoodsid(), supplierid, "-1", String.valueOf(2)));
                            if (null == id) {
                                // 保存箱条码的货品信息
                                latestPriceDao.insert(new LatestPrice(bc.getGoodsid(), supplierid, "-1", String.valueOf(2), bc.getDiscountPrice()));
                            }
                        }
                        addItem(list);
                        // 保存箱条码扫描记录
                        saveGoodsBoxBarcode(barcodeStr, qty, list.get(0).getGoodsid(), list.get(0).getColorid(), list.get(0).getSizeStr());
                    } else {
                        // 设置条码提示音
                        if (LoginParameterUtil.barcodeWarningTone) {
                            settingVoice("error", getApplicationContext());
                        }
                        Toast.makeText(PurchaseDetailActivity.this, "箱条码错误或不存在此箱条码", Toast.LENGTH_SHORT).show();
                        et_barcode.selectAll();
                    }
                } catch (Exception e) {
                    et_barcode.selectAll();
                    Toast.makeText(PurchaseDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 获取采购收货单明细
     */
    public void submitData() {
        showProgressDialog();
        sendData();
    }

    /**
     * 连接服务端,发送单据信息用于保存或修改
     */
    private void sendData() {
        if (sendFlag) {
            // 集合有改动,允许提交数据
            sendFlag = false;
            RequestVo vo = new RequestVo();
            vo.requestUrl = saveOrderMethod;
            vo.context = this;
            HashMap map = new HashMap();
            // 检查是否存在单据，通过后台判断，前台将客户ID跟部门ID统一传入后台
            if (addNew) {
                PurchaseID = null;
            }
            map.put("PurchaseID", PurchaseID);
            map.put("supplierid", supplierid);
            map.put("departmentid", departmentid);
            map.put("employeeId", employeeId);
            map.put("brandId", brandId);
            map.put("businessDeptId", businessDeptId);
            map.put("type", typeName);
            map.put("typeEName", type);
            map.put("direction", direction);
            map.put("memo", memo);
            // 获取将要提交的数据
            subList.clear();
            subList.addAll(dataList);
            net.sf.json.JSONArray json = net.sf.json.JSONArray.fromObject(subList);
            map.put("data", json.toString());
            vo.requestDataMap = map;
            // 发送数据前，改变按钮文件，禁用按钮，防止重复提交
            bt_submit.setText("保存中");
            bt_submit.setClickable(false);
            super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
                @Override
                public void processData(JSONObject retObj, boolean paramBoolean) {
                    sendFlag = true;
                    bt_submit.setClickable(true);
                    bt_submit.setText("保存");
                    if (retObj == null) {
                        return;
                    }
                    try {
                        if (retObj.getBoolean("success")) {
                            JSONObject rs = retObj.getJSONObject("attributes");
                            if (!rs.isNull("PurchaseID")) {
                                PurchaseID = rs.getString("PurchaseID");
                            }
                            subList.clear();
                            Toast.makeText(PurchaseDetailActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                            sendFlag = false;
                            if (LoginParameterUtil.useDefSupplier) {
                                bt_submit.setVisibility(View.GONE);
                                back();
                            } else {
                                // 保存后按钮变成审核
                                bt_submit.setText("审核");
                                isAdd = false;
                            }
                            // 保存箱条码扫描记录
                            if (inputType == 2) {
                                // 保存前先删除历史扫码记录,防止重复
                                recordDao.deleteAll(PurchaseID);
                                for (int i = 0; i < datas.size(); i++) {
                                    HashMap<String, Object> hm = datas.get(i);
                                    String goodsBoxBarcode = String.valueOf(hm.get("Barcode"));
                                    String sizeStr = String.valueOf(hm.get("SizeStr"));
                                    String goodsId = String.valueOf(hm.get("GoodsID"));
                                    String colorId = String.valueOf(hm.get("ColorID"));
                                    int boxQty = Integer.parseInt(String.valueOf(hm.get("BoxQty")));
                                    recordDao.insert(new GoodsBoxBarcodeRecord(PurchaseID, goodsBoxBarcode, sizeStr, goodsId, colorId, boxQty));
                                }
                            }
                        } else {
                            Toast.makeText(PurchaseDetailActivity.this, "保存失败", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(PurchaseDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                        Logger.e(TAG, e.getMessage());
                    }

                }
            });
        } else {
            // 阻止数据提交
            closeProgressDialog();
            Toast.makeText(PurchaseDetailActivity.this, "保存成功", Toast.LENGTH_LONG).show();
            bt_submit.setText("审核");
        }
    }

    /**
     * 审核采购收货单
     */
    private void audit() {
        if (dataList.size() < 1) {
            Toast.makeText(PurchaseDetailActivity.this, "此单据无法审核", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestVo vo = new RequestVo();
        vo.requestUrl = auditMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("PurchaseID", PurchaseID);
        map.put("direction", direction);
        map.put("departmentid", departmentid);
        vo.requestDataMap = map;
        // 发送数据前，改变按钮文件，禁用按钮，防止重复提交
        bt_submit.setClickable(false);
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                bt_submit.setClickable(true);
                if (retObj == null) {
                    return;
                }
                try {
                    if (retObj.getBoolean("success")) {
                        auditFlag = "true";
                        Toast.makeText(PurchaseDetailActivity.this, "审核成功", Toast.LENGTH_LONG).show();
                        // 已审核的盘点单明细(无法修改) 隐藏扫描区和保存按钮
                        ll_saomiao_div.setVisibility(View.GONE);
                        ll_split2.setVisibility(View.GONE);
                        bt_submit.setVisibility(View.GONE);
                        // setHeadRightVisibility(View.GONE);
                    } else {
                        Toast.makeText(PurchaseDetailActivity.this, "审核失败", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(PurchaseDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }

            }
        });
    }

    /**
     * 获取并显示数据
     */
    public void getData() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = method;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("PurchaseID", PurchaseID);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                try {
                    if (retObj == null) {
                        return;
                    }
                    if (retObj.getBoolean("success")) {
                        JSONObject rs = retObj.getJSONObject("attributes");
                        et_supplier.setText(rs.getString("Supplier"));
                        et_department.setText(rs.getString("Department"));
                        et_brand.setText(rs.getString("Brand"));
                        brandId = rs.getString("BrandID");
                        supplierid = rs.getString("SupplierID");
                        departmentid = rs.getString("DepartmentID");
                        et_employee.setText(rs.getString("Employee"));
                        businessDeptName = rs.getString("BusinessDeptName");
                        employeeId = rs.getString("EmployeeID");
                        qtysum = rs.getInt("QuantitySum");
                        memo = rs.getString("Memo");
                        boxQtySum = rs.getInt("BoxQtySum");
                        typeName = rs.getString("Type");
                        et_type.setText(rs.getString("Type"));
                        tv_qtysum.setText(qtysum.toString());
                        JSONArray array = rs.getJSONArray("detailList");
                        if (boxQtySum == 0) {
                            // 去除array中的重复集合
                            array = mergeData(array);
                        }
                        for (int i = 0; i < array.length(); i++) {
                            Map temp = new HashMap();
                            JSONObject json = array.getJSONObject(i);
                            temp.put("GoodsID", json.getString("GoodsID"));
                            temp.put("ColorID", json.getString("ColorID"));
                            temp.put("SizeID", json.getString("SizeID"));
                            temp.put("Color", json.getString("Color"));
                            temp.put("SizeGroupID", json.getString("SizeGroupID"));
                            temp.put("DiscountPrice", json.getString("DiscountPrice"));
                            temp.put("RetailSales", json.getString("RetailSales"));
                            temp.put("UnitPrice", json.getString("UnitPrice"));
                            temp.put("Size", json.getString("Size"));
                            temp.put("Quantity", json.getInt("Quantity"));
                            temp.put("QuantitySum", json.getInt("QuantitySum"));
                            temp.put("GoodsCode", json.getString("GoodsCode"));
                            temp.put("IndexNo", json.getString("IndexNo"));
                            temp.put("SizeStr", json.getString("SizeStr"));
                            temp.put("meno", json.getString("meno"));
                            temp.put("OneBoxQty", json.getInt("OneBoxQty"));
                            temp.put("BoxQty", json.getInt("BoxQty"));
                            dataList.add(temp);
                        }
                        // 计算价格
                        countTotal();
                        // 客户,类型,部门可读
                        et_supplier.setEnabled(false);
                        et_department.setEnabled(false);
                        et_brand.setEnabled(false);
                        et_type.setEnabled(false);
                        et_employee.setEnabled(false);
                        // 设置按钮
                        if (dataList.size() <= 0) {
                            bt_submit.setVisibility(View.GONE);
                        } else {
                            bt_submit.setText("审核");
                        }
                        // 判断如果单据已经审核，隐藏相关显示
                        if (rs.getBoolean("AuditFlag")) {
                            bt_submit.setVisibility(View.GONE);
                            findViewById(R.id.saomiao_div).setVisibility(View.GONE);
                            findViewById(R.id.split2).setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(PurchaseDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 去除JSONArray中重复的集合
     * 
     * @param array
     * @return
     * @throws JSONException
     */
    private JSONArray mergeData(JSONArray array) throws JSONException {
        for (int i = 0; i < array.length() - 1; i++) {
            JSONObject obj1 = array.getJSONObject(i);
            for (int j = array.length() - 1; j > i; j--) {
                JSONObject obj2 = array.getJSONObject(j);
                if (obj1.getString("GoodsID").equals(obj2.getString("GoodsID")) && obj1.getString("ColorID").equals(obj2.getString("ColorID")) && obj1.getString("SizeID").equals(obj2.getString("SizeID"))) {
                    array.remove(j);
                }
            }
        }
        return array;
    }

    /**
     * 显示货品的库存信息
     * 
     * @param goodsId
     * @param colorId
     * @param sizeId
     */
    private void queryAvailableStock(final String goodsId, final String colorId, final String sizeId) {
        RequestVo vo = new RequestVo();
        vo.requestUrl = queryAvailableStock;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("goodsId", goodsId);
        map.put("colorId", colorId);
        map.put("sizeId", sizeId);
        map.put("tableTag", String.valueOf(22));
        map.put("invoiceId", PurchaseID);
        map.put("departmentId", departmentid);
        vo.requestDataMap = map;
        super.getDataFromServer(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject retObj, boolean paramBoolean) {
                if (retObj == null) {
                    return;
                }
                try {
                    if (retObj.getBoolean("success")) {
                        JSONObject obj = retObj.getJSONObject("obj");
                        int stock = obj.getInt("stock");
                        et_stock.setText(String.valueOf(stock));
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    Toast.makeText(PurchaseDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * 删除发货单明细
     */
    private void delete() {
        RequestVo vo = new RequestVo();
        vo.requestUrl = deleteMethod;
        vo.context = this;
        HashMap map = new HashMap();
        map.put("PurchaseID", PurchaseID);
        net.sf.json.JSONArray json = net.sf.json.JSONArray.fromObject(delList);
        map.put("data", json.toString());
        vo.requestDataMap = map;
        super.getDataFromServerNoProcess(vo, new DataCallback<JSONObject>() {
            @Override
            public void processData(JSONObject paramObject, boolean paramBoolean) {
                try {
                    if (paramObject.getBoolean("success")) {
                        Logger.e(TAG, "删除货品成功");
                        delList.clear();
                        Toast.makeText(PurchaseDetailActivity.this, "删除货品成功", Toast.LENGTH_LONG).show();
                        bt_submit.setText("审核");
                        // 重新计算数量和价格
                        countTotal();
                        if (dataList.size() == 0) {
                            back();
                        }
                        sendFlag = false;
                    }
                } catch (JSONException e) {
                    Toast.makeText(PurchaseDetailActivity.this, "系统错误", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, e.getMessage());
                }
            }
        });
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.supplier:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(PurchaseDetailActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectGoodsSupplier");
                        startActivityForResult(intent, R.id.supplier);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.department:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(PurchaseDetailActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectWarehouse");
                        startActivityForResult(intent, R.id.department);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.type:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(PurchaseDetailActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "purchaseType");
                        startActivityForResult(intent, R.id.type);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.brand:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(PurchaseDetailActivity.this, SelectActivity.class);
                        if (LoginParameterUtil.useBrandPower) {
                            intent.putExtra("selectType", "selectBrandByPower");
                        } else {
                            intent.putExtra("selectType", "selectBrand");
                        }
                        startActivityForResult(intent, R.id.brand);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.employee:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(PurchaseDetailActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectEmployee");
                        startActivityForResult(intent, R.id.employee);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                case R.id.et_business_dept:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Intent intent = new Intent(PurchaseDetailActivity.this, SelectActivity.class);
                        intent.putExtra("selectType", "selectWarehouse");
                        startActivityForResult(intent, R.id.et_business_dept);
                        overridePendingTransition(R.anim.activity_open, 0);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    @Override
    protected void setListener() {
        if (LoginParameterUtil.purchaseUnitPriceRight) {
            salesOddAdapter = new SalesOddAdapter(this, dataList);
            lv_detail.setAdapter(salesOddAdapter);
        } else {
            oddAdapter = new OddAdapter(this, dataList);
            lv_detail.setAdapter(oddAdapter);
        }
        lv_detail.setOnItemLongClickListener(this);
        TouchListener tl = new TouchListener();
        et_supplier.setOnTouchListener(tl);
        et_employee.setOnTouchListener(tl);
        et_department.setOnTouchListener(tl);
        et_type.setOnTouchListener(tl);
        et_brand.setOnTouchListener(tl);
        bt_addDetail.setOnClickListener(this);
        bt_submit.setOnClickListener(this);
        iv_pic.setOnClickListener(this);
        et_barcode.setOnKeyListener(this);
        et_barcode.setOnEditorActionListener(new BarcodeActionListener());
        et_qty.setOnEditorActionListener(new QtyBarcodeActionListener());
    }

    /**
     * Title: BarcodeActionListener Description: 条码扫描完成触发Enter事件
     */
    class BarcodeActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    bt_addDetail.callOnClick();
                } else if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    bt_addDetail.callOnClick();
                    return true;
                } else if (event != null && event.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Title: QtyBarcodeActionListener Description: 货品数量完成触发Enter事件
     */
    class QtyBarcodeActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                // 设置扫码区自动获取焦点
                et_barcode.requestFocus();
                return true;
            }
            return false;
        }

    }

    @Override
    protected void processLogic() {
        // 设置部门为登录用户的默认部门(仓库)
        departmentid = LoginParameterUtil.deptId;
        et_department.setText(LoginParameterUtil.deptName);
        // 设置默认类别为[采购]
        typeName = "采购";
        et_type.setText(typeName);
        type = "";
        // 获取用户操作权限(新增)
        modifyRight = LoginParameterUtil.purchaseRightMap.get("ModifyRight");
        auditRight = LoginParameterUtil.purchaseRightMap.get("AuditRight");
        if (!LoginParameterUtil.purchaseUnitPriceRight) {
            ll_scanTitle.removeView(tv_unitPrice);
            tv_symbol.setVisibility(View.GONE);//2019-08-22 没有权限 隐藏
            tv_amount.setVisibility(View.GONE);
            
            
        }
        find = paramerDao.find("useLastPrice");
        Bundle bundle = this.getIntent().getExtras();
        if (null != bundle) {
            PurchaseID = bundle.getString("PurchaseID");
            auditFlag = bundle.getString("AuditFlag");
            if ("true".equals(auditFlag)) {
                // 已审核的盘点单明细(无法修改) 隐藏扫描区和保存按钮
                ll_saomiao_div.setVisibility(View.GONE);
                ll_split2.setVisibility(View.GONE);
                bt_submit.setVisibility(View.GONE);
            } else if ("false".equals(auditFlag)) {
                // 未审核的盘点单明细(可以修改)
                bt_submit.setText("审核");
            }
            getData();
        } else {
            bt_submit.setVisibility(View.GONE);
        }
        // 设置扫码区自动获取焦点
        et_barcode.requestFocus();
        resetBarcode();
        // 启动定时器，定时保存
        // handler.postDelayed(runnable, saveTime);
        // 显示右上角功能
        setHeadRightVisibility(View.VISIBLE);
        setHeadRightText(R.string.setting);
        // //经手人默认为登录的员工
        // employeeId = LoginParameterUtil.employeeId;
        // et_employee.setText(LoginParameterUtil.supplier.getUserName());
        // 是否使用品牌权限
        // if (!LoginParameterUtil.useBrandPower) {
        // ll_brand.setVisibility(View.GONE);
        // }
        // 使用货品中的默认厂商
        if (null == PurchaseID && LoginParameterUtil.useDefSupplier) {
            addNew = true;
            ll_supplier.setVisibility(View.GONE);
            Builder dialog = new AlertDialog.Builder(PurchaseDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("使用货品默认厂商批量生成采购收货单时,为避免同品牌的单据重复生成,此功能仅限单次保存操作");
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
        }
        // 获取箱条码扫描记录
        if (PurchaseID != null && !"".equals(PurchaseID) && "false".equals(auditFlag)) {
            List<GoodsBoxBarcodeRecord> list = recordDao.getList(PurchaseID);
            // 禁用扫码区
            closeScanButton();
            for (int i = 0; i < list.size(); i++) {
                HashMap<String, Object> hm = new HashMap<String, Object>();
                hm.put("Barcode", list.get(i).getGoodsBoxBarcode());
                hm.put("BoxQty", list.get(i).getBoxQty());
                hm.put("SizeStr", list.get(i).getSizeStr());
                hm.put("GoodsID", list.get(i).getGoodsId());
                hm.put("ColorID", list.get(i).getColorId());
                datas.add(hm);
            }
        }

    }

    @Override
    protected void findViewById() {
        lv_detail = (ListView) findViewById(R.id.sales_detail);
        et_supplier = (EditText) findViewById(R.id.supplier);
        et_department = (EditText) findViewById(R.id.department);
        et_type = (EditText) findViewById(R.id.type);
        et_brand = (EditText) findViewById(R.id.brand);
        tv_qtysum = (TextView) findViewById(R.id.qtysum);
        tv_amount = (TextView) findViewById(R.id.amount);
        et_barcode = (EditText) findViewById(R.id.barcode);
        et_employee = (EditText) findViewById(R.id.employee);
        et_qty = (EditText) findViewById(R.id.qty);
        bt_addDetail = (TextView) findViewById(R.id.addDetail);
        iv_pic = (ImageView) findViewById(R.id.pic);
        bt_submit = (TextView) findViewById(R.id.submit);
        tv_symbol = (TextView) findViewById(R.id.symbol);
        tv_barcode = (TextView) findViewById(R.id.barcode_lable);
        tv_qty = (TextView) findViewById(R.id.qty_lable);
        tv_unitPrice = (TextView) findViewById(R.id.unitPrice);
        ll_saomiao_div = (LinearLayout) findViewById(R.id.saomiao_div);
        ll_split2 = (LinearLayout) findViewById(R.id.split2);
        ll_scanTitle = (LinearLayout) findViewById(R.id.ll_scanTitle);
        ll_supplier = (LinearLayout) findViewById(R.id.ll_supplier);
        ll_brand = (LinearLayout) findViewById(R.id.ll_brand);
        ll_title = (LinearLayout) findViewById(R.id.ll_title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case R.id.supplier:
                if (resultCode == 1) {
                    et_supplier.setText(data.getStringExtra("Name"));
                    supplierid = data.getStringExtra("SupplierID");
                    String discountRateStr = data.getStringExtra("DiscountRate");
                    if (discountRateStr == null || discountRateStr.isEmpty() || "".equals(discountRateStr) || "null".equalsIgnoreCase(discountRateStr)) {
                        discountRateStr = "0";
                    }
                    discountRate = Double.parseDouble(discountRateStr);
                    // 设置扫码区自动获取焦点
                    et_barcode.requestFocus();
                }
                break;
            case R.id.department:
                if (resultCode == 1) {
                    et_department.setText(data.getStringExtra("Name"));
                    departmentid = data.getStringExtra("DepartmentID");
                    // 设置扫码区自动获取焦点
                    et_barcode.requestFocus();
                }
                break;
            case R.id.type:
                if (resultCode == 1) {
                    typeName = data.getStringExtra("Name");
                    et_type.setText(typeName);
                    type = data.getStringExtra("Type");
                    // 设置扫码区自动获取焦点
                    et_barcode.requestFocus();
                }
                break;
            case R.id.brand:
                if (resultCode == 1) {
                    et_brand.setText(data.getStringExtra("Name"));
                    brandId = data.getStringExtra("BrandID");
                    // 设置扫码区自动获取焦点
                    et_barcode.requestFocus();
                }
                break;
            case R.id.employee:
                if (resultCode == 1) {
                    et_employee.setText(data.getStringExtra("Name"));
                    employeeId = data.getStringExtra("EmployeeID");
                    businessDeptId = data.getStringExtra("BusinessDeptID");
                    businessDeptName = data.getStringExtra("BusinessDeptName");
                    // 设置扫码区自动获取焦点
                    et_barcode.requestFocus();
                }
                break;
            case R.id.et_business_dept:
                if (resultCode == 1) {
                    businessDeptName = data.getStringExtra("Name");
                    et_businessDept.setText(businessDeptName);
                    businessDeptId = data.getStringExtra("DepartmentID");
                    // 设置扫码区自动获取焦点
                    et_barcode.requestFocus();
                }
                break;
            case 1:
                if (resultCode == 1) {
                    memo = data.getStringExtra("remark");
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHeadLeftButton(View v) {
        // 点击返回时返回上一页面并刷新
        saveOrNot();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            saveOrNot();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 点击返回时的操作
     */
    private void back() {
        if (null == PurchaseID || "".equals(PurchaseID)) {
            finish();
        } else {
            // 点击返回时返回上一页面并刷新
            Intent intent = new Intent(PurchaseDetailActivity.this, PurchaseActivity.class);
            intent.putExtra("auditFlag", auditFlag);// 把返回数据存入Intent
            startActivity(intent);
            finish();
        }
    }

    /**
     * 点击返回时询问是否保存当前单据信息
     */
    private void saveOrNot() {
        if (sendFlag) {
            // 询问是否保存修改的发货单数据
            Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("保存提示");
            dialog.setMessage("采购收货单尚未保存,确定要返回吗？");
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    back();
                }
            });
            // 相当于点击取消按钮
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            dialog.create();
            dialog.show();
        } else {
            back();
        }
    }

    /**
     * 动态显示菜单选项
     */
    private void showpopWinShare() {
        // 处理单据审核后不显示打印的问题
        if ("true".equals(auditFlag) || PurchaseID != null || dataList.size() > 0) {
            popWinShare = null;
        }
        if (popWinShare == null) {
            // 自定义的单击事件
            OnClickLintener paramOnClickListener = new OnClickLintener();
            popWinShare = new PopWinShare(PurchaseDetailActivity.this, paramOnClickListener);
            // 监听窗口的焦点事件，点击窗口外面则取消显示
            popWinShare.getContentView().setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        popWinShare.dismiss();
                    }
                }
            });
        }
        // 订单未生成及箱条码录入不设置条码校验
        if (PurchaseID == null || PurchaseID.isEmpty() || "".equals(PurchaseID) || "null".equalsIgnoreCase(PurchaseID)) {
            popWinShare.ll_parent.removeView(popWinShare.view_5);
            popWinShare.ll_parent.removeView(popWinShare.layoutBarcodeVerification);
        }
        // 隐藏切换的选项
        if (dataList.size() > 0) {
            popWinShare.ll_parent.removeView(popWinShare.layoutSwitch);
            popWinShare.ll_parent.removeView(popWinShare.view);
        }
        // 不显示单价的时候不能使用最近价格选项
        if (!LoginParameterUtil.purchaseUnitPriceRight) {
            popWinShare.ll_parent.removeView(popWinShare.layout_price);
            popWinShare.ll_parent.removeView(popWinShare.view_3);
            popWinShare.ll_parent.removeView(popWinShare.view_4);
        }
        if (dataList.size() == 0 || (dataList.size() > 0 && inputType == 1)) {
            popWinShare.ll_parent.removeView(popWinShare.layoutGoodsBoxBarcode);
            popWinShare.ll_parent.removeView(popWinShare.view_6);
        }
        // 默认设置
        popWinShare.ll_parent.removeView(popWinShare.layoutSwitchInputMode);
        popWinShare.ll_parent.removeView(popWinShare.view_7);
        // popWinShare.ll_parent.removeView(popWinShare.layout_price);
        popWinShare.ll_parent.removeView(popWinShare.layoutAddCustomer);
        popWinShare.ll_parent.removeView(popWinShare.view_8);
        popWinShare.ll_parent.removeView(popWinShare.view_9);
        popWinShare.ll_parent.removeView(popWinShare.layoutBarcodeVerificationDifferent);
        popWinShare.ll_parent.removeView(popWinShare.layoutExportExcel);
        popWinShare.ll_parent.removeView(popWinShare.view_10);
        popWinShare.ll_parent.removeView(popWinShare.layoutSingleDiscount);
        // popWinShare.ll_parent.removeView(popWinShare.view_11);
        // popWinShare.ll_parent.removeView(popWinShare.layoutOtherOptions);
        // 审核后打印
        if ("true".equals(auditFlag)) {
            popWinShare.ll_parent.removeView(popWinShare.layoutSwitch);
            popWinShare.ll_parent.removeView(popWinShare.view);
            popWinShare.ll_parent.removeView(popWinShare.layoutRemark);
            popWinShare.ll_parent.removeView(popWinShare.layout_price);
            popWinShare.ll_parent.removeView(popWinShare.view_3);
            popWinShare.ll_parent.removeView(popWinShare.layoutGoodsBoxBarcode);
            popWinShare.ll_parent.removeView(popWinShare.view_6);
            popWinShare.ll_parent.removeView(popWinShare.view_4);
            // popWinShare.ll_parent.removeView(popWinShare.view_5);
            // popWinShare.ll_parent.removeView(popWinShare.layoutBarcodeVerification);
        } else {
            if (PurchaseID == null || PurchaseID.isEmpty() || "".equals(PurchaseID) || "null".equalsIgnoreCase(PurchaseID)) {
                popWinShare.ll_parent.removeView(popWinShare.layoutPrint);
                popWinShare.ll_parent.removeView(popWinShare.view_2);
            }
            if (dataList.size() > 0 && LoginParameterUtil.purchaseUnitPriceRight) {
                popWinShare.layout_price.setEnabled(false);
                popWinShare.cb_last_price.setEnabled(false);
                popWinShare.layout_price.setBackgroundColor(Color.GRAY);
                popWinShare.ll_parent.removeView(popWinShare.view_4);
            }
        }
        // 设置默认获取焦点
        popWinShare.setFocusable(true);
        // 以某个控件的x和y的偏移量位置开始显示窗口
        popWinShare.showAsDropDown(super.headRightBtn, 0, 10);
        // 如果窗口存在，则更新
        popWinShare.update();
    }

    class OnClickLintener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            // 切换装箱/散件
                case R.id.layout_switch:
                    // 改变货品成箱号，数量为箱数，条码查找箱条码，其余业务逻辑相同
                    if (inputType == 1) {
                        // 改变标题
                        popWinShare.tvToggle.setText("切换散件");
                        setTitle("采购收货单(装箱)");
                        inputType = 2;
                        tv_barcode.setText("箱号");
                        tv_qty.setText("箱数");
                        boxQtySum = 1;
                    } else {
                        popWinShare.tvToggle.setText("切换装箱");
                        setTitle("采购收货单(散件)");
                        inputType = 1;
                        tv_barcode.setText("货号");
                        tv_qty.setText("数量");
                        boxQtySum = 0;
                    }
                    popWinShare.dismiss();
                    break;
                // 添加备注
                case R.id.layout_remark:
                    popWinShare.dismiss();
                    Intent intent = new Intent(PurchaseDetailActivity.this, RemarkActivity.class);
                    intent.putExtra("id", PurchaseID);
                    intent.putExtra("idName", "PurchaseID");
                    intent.putExtra("updatePath", updateMemo);
                    intent.putExtra("memo", memo);
                    intent.putExtra("auditFlag", Boolean.parseBoolean(auditFlag));
                    startActivityForResult(intent, 1);
                    overridePendingTransition(R.anim.activity_open, 0);
                    break;
                case R.id.layout_print:
                    popWinShare.dismiss();
                    Intent intents = new Intent(PurchaseDetailActivity.this, PrintActivity.class);
                    intents.putExtra("id", PurchaseID);
                    intents.putExtra("tableName", "Purchase");
                    intents.putExtra("docType", "采购收货单");
                    startActivity(intents);
                    break;
                case R.id.layout_price:
                    boolean flag = popWinShare.cb_last_price.isChecked();
                    popWinShare.cb_last_price.setChecked(!flag);
                    paramerDao.update(new Paramer("useLastPrice", String.valueOf(!flag)));
                    break;
                case R.id.layout_goods_box_barcode:
                    popWinShare.dismiss();
                    Intent in = new Intent(PurchaseDetailActivity.this, GoodsBoxBarcodeActivity.class);
                    in.putExtra("billId", PurchaseID);
                    in.putExtra("datas", datas);
                    startActivity(in);
                    break;
                case R.id.layout_barcode_verification:
                    popWinShare.dismiss();
                    Intent inte = new Intent(PurchaseDetailActivity.this, BarcodeVerificationActivity.class);
                    inte.putExtra("idStr", "PurchaseID");
                    inte.putExtra("id", PurchaseID);
                    inte.putExtra("inputType", inputType);
                    inte.putExtra("type", type);
                    inte.putExtra("customerId", supplierid);
                    inte.putExtra("path", method);
                    inte.putExtra("coverSavePath", coverSavePath);
                    startActivity(inte);
                    break;
                case R.id.layout_other_options:
                    popWinShare.dismiss();
                    Builder builder = new AlertDialog.Builder(PurchaseDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                    builder.setTitle("其它选项");
                    View view2 = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_other_options, null);
                    LinearLayout llOthers = (LinearLayout) view2.findViewById(R.id.ll_others);
                    llOthers.setVisibility(View.GONE);
                    et_businessDept = (EditText) view2.findViewById(R.id.et_business_dept);
                    et_businessDept.setOnTouchListener(tl);
                    et_businessDept.setText(businessDeptName);
                    builder.setView(view2);
                    // 相当于点击确认按钮
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    // 相当于点击取消按钮
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    builder.setCancelable(false);
                    builder.create();
                    builder.show();
                    break;
                default:
                    break;
            }

        }

    }

    /**
     * 长按选中ListView的项
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        if ("true".equals(auditFlag)) {
            return false;
        }
        if (modifyRight) {
            Map<String, Object> map = dataList.get(position);
            String goodsCode = String.valueOf(map.get("GoodsCode"));
            final String upGoodsId = String.valueOf(map.get("GoodsID"));
            final String upColorId = String.valueOf(map.get("ColorID"));
            final String upSizeId = String.valueOf(map.get("SizeID"));
            final String oneBoxQty = String.valueOf(map.get("OneBoxQty"));
            final String unitPrice = String.valueOf(map.get("DiscountPrice"));
            final String upSizeStr = String.valueOf(map.get("SizeStr"));
            String upMeno = String.valueOf(map.get("meno"));
            String boxCount = null;
            String count = null;
            if (boxQtySum > 0) {
                // 装箱时显示的是箱数
                boxCount = String.valueOf(map.get("BoxQty"));
            } else {
                count = String.valueOf(map.get("Quantity"));
            }
            View v = null;
            if (LoginParameterUtil.purchaseUnitPriceRight) {
                if (LoginParameterUtil.displayInventory) {
                    // 显示库存
                    v = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_sales, null);
                } else {
                    v = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_sales_ticket, null);
                }
                // 判断用户是否允许修改单价
                if (!LoginParameterUtil.unitPricePermitFlag) {
                    LinearLayout ll_price = (LinearLayout) v.findViewById(R.id.ll_price);
                    View v_price = (View) v.findViewById(R.id.v_price);
                    ll_price.setVisibility(View.GONE);
                    v_price.setVisibility(View.GONE);
                }
            } else {
                if (LoginParameterUtil.displayInventory) {
                    v = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_inventory_has_stock, null);
                } else {
                    v = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_inventory, null);
                }
            }
            if (LoginParameterUtil.purchaseUnitPriceRight) {
                et_price = (EditText) v.findViewById(R.id.et_price);
            }
            if (LoginParameterUtil.displayInventory) {
                et_stock = (EditText) v.findViewById(R.id.et_stock);
                // 查询货品可发库存
                queryAvailableStock(upGoodsId, upColorId, upSizeId);
            }
            final EditText et_count = (EditText) v.findViewById(R.id.et_count);
            final EditText et_meno = (EditText) v.findViewById(R.id.et_meno);
            TextView tv_count = (TextView) v.findViewById(R.id.tv_count);
            // 询问是否删除
            Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            if (LoginParameterUtil.purchaseUnitPriceRight) {
                dialog.setMessage("修改货品" + goodsCode + "的单价和数量 :");
                et_price.setText(unitPrice);
                et_price.setSelection(unitPrice.length());
            } else {
                dialog.setMessage("修改货品" + goodsCode + "的数量 :");
            }
            if (boxQtySum > 0) {
                tv_count.setText("箱数");
                et_count.setText(boxCount);
                et_count.setSelection(boxCount.length());
            } else {
                et_count.setText(count);
                et_count.setSelection(count.length());
            }
            if (null == upMeno || "null".equalsIgnoreCase(upMeno)) {
                upMeno = "";
            }
            et_meno.setText(upMeno);
            et_meno.setSelection(upMeno.length());
            dialog.setView(v);
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 修改单据后允许提交数据
                    sendFlag = true;
                    isAdd = true;
                    String price = null;
                    if (LoginParameterUtil.purchaseUnitPriceRight) {
                        price = et_price.getText().toString();
                    } else {
                        price = unitPrice;
                    }
                    String count = et_count.getText().toString();
                    String meno = et_meno.getText().toString();
                    if ("".equals(price.trim())) {
                        price = "0";
                    }
                    if ("".equals(count.trim())) {
                        count = "1";
                    }
                    BigDecimal tPrice = new BigDecimal(price).setScale(2, BigDecimal.ROUND_HALF_UP);
                    price = String.valueOf(tPrice.doubleValue());
                    if (Integer.parseInt(count) < 1) {
                        // 单据生成后直接删除
                        if (null != PurchaseID && !"".equals(PurchaseID)) {
                            Map upMap = new HashMap();
                            if (boxQtySum > 0) {
                                for (int i = 0; i < dataList.size(); i++) {
                                    Map map = dataList.get(i);
                                    if (map.get("GoodsID").equals(upGoodsId) && map.get("ColorID").equals(upColorId) && map.get("SizeStr").equals(upSizeStr)) {
                                        upMap.put("GoodsID", upGoodsId);
                                        upMap.put("ColorID", upColorId);
                                        upMap.put("SizeID", map.get("SizeID"));
                                        upMap.put("OneBoxQty", map.get("QuantitySum"));
                                        upMap.put("SizeStr", upSizeStr);
                                        delList.add(upMap);
                                        dataList.remove(i);
                                        i--;
                                        // 删除扫描记录
                                        // 箱条码扫描记录
                                        for (int j = 0; j < datas.size(); j++) {
                                            HashMap hm = datas.get(j);
                                            if (hm.get("GoodsID").equals(upGoodsId) && hm.get("ColorID").equals(upColorId) && hm.get("SizeStr").equals(upSizeStr)) {
                                                datas.remove(j);
                                                j--;
                                            }
                                        }
                                    }
                                }
                                delete();
                            } else {
                                upMap.put("GoodsID", upGoodsId);
                                upMap.put("ColorID", upColorId);
                                upMap.put("SizeID", upSizeId);
                                upMap.put("OneBoxQty", oneBoxQty);
                                upMap.put("SizeStr", upSizeStr);
                                delList.add(upMap);
                                dataList.remove(position);
                                delete();
                            }
                        } else {
                            // 未生成单据时的处理
                            if (boxQtySum > 0) {
                                for (int i = 0; i < dataList.size(); i++) {
                                    Map map = dataList.get(i);
                                    if (map.get("GoodsID").equals(upGoodsId) && map.get("ColorID").equals(upColorId) && map.get("SizeStr").equals(upSizeStr)) {
                                        dataList.remove(i);
                                        i--;
                                    }
                                }
                                // 箱条码扫描记录
                                for (int i = 0; i < datas.size(); i++) {
                                    HashMap map = datas.get(i);
                                    if (map.get("GoodsID").equals(upGoodsId) && map.get("ColorID").equals(upColorId) && map.get("SizeStr").equals(upSizeStr)) {
                                        datas.remove(i);
                                        i--;
                                    }
                                }
                            } else {
                                dataList.remove(position);
                            }
                        }
                    } else {
                        for (int i = 0; i < dataList.size(); i++) {
                            Map map = dataList.get(i);
                            if (map.get("GoodsID").equals(upGoodsId)) {
                                map.put("DiscountPrice", price);
                                // 改数量时要求同码,同色
                                if (map.get("ColorID").equals(upColorId)) {
                                    // 添加备注
                                    map.put("meno", meno);
                                    lastMemo = meno;
                                    if (boxQtySum > 0 && map.get("SizeStr").equals(upSizeStr)) {
                                        int boxQty = Integer.parseInt(String.valueOf(map.get("BoxQty")));
                                        int oneBox = Integer.parseInt(String.valueOf(map.get("Quantity"))) / boxQty;
                                        map.put("BoxQty", count);
                                        map.put("Quantity", Integer.parseInt(count) * oneBox);
                                    } else {
                                        if (map.get("SizeID").equals(upSizeId)) {
                                            if (boxQtySum < 1) {
                                                map.put("BoxQty", 0);
                                                map.put("Quantity", count);
                                            }
                                        }
                                    }
                                }
                                if (boxQtySum < 0) {
                                    barCodeDao.update(new BigDecimal(price), String.valueOf(map.get("Barcode")), supplierid, "-1");
                                } else {
                                    latestPriceDao.update(new LatestPrice(upGoodsId, supplierid, "-1", String.valueOf(2), new BigDecimal(price)));
                                }
                            }
                        }
                        for (int i = 0; i < datas.size(); i++) {
                            HashMap map = datas.get(i);
                            if (map.get("GoodsID").equals(upGoodsId) && map.get("ColorID").equals(upColorId) && map.get("SizeStr").equals(upSizeStr)) {
                                map.put("BoxQty", Integer.parseInt(count));
                            }
                        }
                    }
                    // 计算价格
                    countTotal();
                }
            });
            // 相当于点击取消按钮
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            dialog.create();
            dialog.show();
            return true;
        } else {
            Builder dialog = new AlertDialog.Builder(PurchaseDetailActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("提示");
            dialog.setMessage("当前暂无修改采购收货单的操作权限");
            // 相当于点击确认按钮
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            dialog.setCancelable(false);
            dialog.create();
            dialog.show();
            return false;
        }
    }

}
