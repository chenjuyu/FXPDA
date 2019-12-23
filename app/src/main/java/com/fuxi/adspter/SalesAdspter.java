package com.fuxi.adspter;

import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fuxi.activity.InventorySheetActivity;
import com.fuxi.activity.PurchaseActivity;
import com.fuxi.activity.PurchaseOrderActivity;
import com.fuxi.activity.PurchaseReturnActivity;
import com.fuxi.activity.SalesActivity;
import com.fuxi.activity.SalesOrderActivity;
import com.fuxi.activity.SalesReturnsActivity;
import com.fuxi.activity.StockMoveActivity;
import com.fuxi.activity.WarehouseStockInActivity;
import com.fuxi.activity.WarehouseStockOutActivity;
import com.fuxi.main.R;
import com.fuxi.util.LoginParameterUtil;
import com.fuxi.util.Tools;

/**
 * Title: SalesAdspter Description: 单据主表显示的ListView适配器
 * 
 * @author LYJ
 * 
 */
public class SalesAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public SalesAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 组件集合，对应list_sales_invoice.xml中的控件
     */
    public final class Zujian {
        public TextView number; // 单号
        public TextView department;
        public TextView customer;
        public TextView count;
        public TextView amount;
        public TextView invoicedate;
        public TextView employee;
        public TextView memo;
        public TextView brand;
        public TextView other;
        public LinearLayout ll_second;
        public LinearLayout ll_third;
        public LinearLayout ll_fourth;
    }

    public void refresh(List<Map<String, Object>> list) {
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    /**
     * 获得某一位置的数据
     */
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    /**
     * 获得唯一标识
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Zujian zujian = null;
        if (convertView == null) {
            zujian = new Zujian();
            Activity activity = (Activity) context;
            // 获得组件，实例化组件
            convertView = layoutInflater.inflate(R.layout.list_sales_invoice, null);
            zujian.number = (TextView) convertView.findViewById(R.id.tv_number);
            zujian.customer = (TextView) convertView.findViewById(R.id.tv_customer);
            zujian.amount = (TextView) convertView.findViewById(R.id.tv_amount);
            zujian.count = (TextView) convertView.findViewById(R.id.tv_count);
            zujian.invoicedate = (TextView) convertView.findViewById(R.id.tv_invoicedate);
            zujian.employee = (TextView) convertView.findViewById(R.id.tv_employee);
            zujian.memo = (TextView) convertView.findViewById(R.id.tv_memo);
            zujian.other = (TextView) convertView.findViewById(R.id.tv_other);
            zujian.department = (TextView) convertView.findViewById(R.id.tv_department);
            zujian.brand = (TextView) convertView.findViewById(R.id.tv_brand);
            zujian.ll_second = (LinearLayout) convertView.findViewById(R.id.ll_second);
            zujian.ll_third = (LinearLayout) convertView.findViewById(R.id.ll_third);
            zujian.ll_fourth = (LinearLayout) convertView.findViewById(R.id.ll_fourth);
            // 根据系统设置的字段权限显示
            dynamicDisplay(activity, zujian);
            convertView.setTag(zujian);
        } else {
            zujian = (Zujian) convertView.getTag();
        }
        // 绑定数据
        Activity activity = (Activity) context;
        zujian.number.setText(String.valueOf(data.get(position).get("No")));
        zujian.department.setText(String.valueOf(data.get(position).get("Department")));
        if (activity instanceof PurchaseActivity || activity instanceof PurchaseReturnActivity || activity instanceof PurchaseOrderActivity) {
            if(data.get(position).get("Supplier")==null || data.get(position).get("Supplier").equals("")){
                zujian.customer.setText("");
            }else {
                zujian.customer.setText(String.valueOf(data.get(position).get("Supplier")));
            }
        } else {
            if(data.get(position).get("Customer")==null || data.get(position).get("Customer").equals("")){
                zujian.customer.setText("");
            }else {
                zujian.customer.setText(String.valueOf(data.get(position).get("Customer")));
            }
        }
        if (activity instanceof WarehouseStockInActivity || activity instanceof WarehouseStockOutActivity || activity instanceof StockMoveActivity) {
            zujian.amount.setText("￥" + Tools.formatDecimal(String.valueOf(data.get(position).get("RelationAmountSum"))));
        } else if (activity instanceof InventorySheetActivity) {
            zujian.amount.setText("￥" + Tools.formatDecimal(String.valueOf(data.get(position).get("RetailAmountSum"))));
        } else {
            zujian.amount.setText("￥" + Tools.formatDecimal(String.valueOf(data.get(position).get("AmountSum"))));
        }
        zujian.count.setText(String.valueOf(data.get(position).get("QuantitySum")));
        zujian.invoicedate.setText(String.valueOf(data.get(position).get("Date")));
        if(data.get(position).get("Employee")==null || "".equals(data.get(position).get("Employee"))){
            zujian.employee.setText("");
        }else{
            zujian.employee.setText(String.valueOf(data.get(position).get("Employee")));
        }
        if(data.get(position).get("Brand")==null || "".equals(data.get(position).get("Brand"))){
            zujian.brand.setText("");
        }else{
            zujian.brand.setText(String.valueOf(data.get(position).get("Brand")));
        }



        if (activity instanceof SalesOrderActivity) {
            zujian.other.setText(String.valueOf(data.get(position).get("Warehouse")));
        } else if (activity instanceof SalesActivity) {
            zujian.other.setText(String.valueOf(data.get(position).get("OrderNo")));
        } else if (activity instanceof SalesReturnsActivity) {
            zujian.other.setText(String.valueOf(data.get(position).get("Warehouse")));
        } else if (activity instanceof StockMoveActivity) {
            zujian.other.setText(String.valueOf(data.get(position).get("WarehouseIn")));
        } else if (activity instanceof WarehouseStockInActivity || activity instanceof WarehouseStockOutActivity) {
            zujian.other.setText(String.valueOf(data.get(position).get("RelationWarehouse")));
        }
        String memo = String.valueOf(data.get(position).get("Memo")).trim();
        if (memo != null && !memo.isEmpty() && !"".equals(memo) && memo.length() > 11) {
            memo = memo.substring(0, 11) + "...";
        }
        zujian.memo.setText(memo);
        return convertView;
    }

    private void dynamicDisplay(Activity activity, Zujian zujian) {
        // 销售订单
        if (activity instanceof SalesOrderActivity) {
            if (!LoginParameterUtil.salesOrderEmployee) {
                zujian.employee.setVisibility(View.GONE);
            } else {
                zujian.employee.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesOrderCustomer) {
                zujian.customer.setVisibility(View.GONE);
            } else {
                zujian.customer.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesOrderBrand) {
                zujian.brand.setVisibility(View.GONE);
            } else {
                zujian.brand.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesOrderMemo) {
                zujian.memo.setVisibility(View.GONE);
            } else {
                zujian.memo.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesOrderWarehouse) {
                zujian.other.setVisibility(View.GONE);
            } else {
                zujian.other.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesOrderDepartment) {
                zujian.department.setVisibility(View.GONE);
            } else {
                zujian.department.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesOrderAmountSum) {
                zujian.amount.setVisibility(View.GONE);
            } else {
                zujian.amount.setVisibility(View.VISIBLE);
            }
        }
        // 销售发货单
        if (activity instanceof SalesActivity) {
            if (!LoginParameterUtil.salesEmployee) {
                zujian.employee.setVisibility(View.GONE);
            } else {
                zujian.employee.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesCustomer) {
                zujian.customer.setVisibility(View.GONE);
            } else {
                zujian.customer.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesBrand) {
                zujian.brand.setVisibility(View.GONE);
            } else {
                zujian.brand.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesMemo) {
                zujian.memo.setVisibility(View.GONE);
            } else {
                zujian.memo.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesOrderNo) {
                zujian.other.setVisibility(View.GONE);
            } else {
                zujian.other.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesWarehouse) {
                zujian.department.setVisibility(View.GONE);
            } else {
                zujian.department.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesAmountSum) {
                zujian.amount.setVisibility(View.GONE);
            } else {
                zujian.amount.setVisibility(View.VISIBLE);
            }
        }
        // 销售退货单
        if (activity instanceof SalesReturnsActivity) {
            if (!LoginParameterUtil.salesReturnEmployee) {
                zujian.employee.setVisibility(View.GONE);
            } else {
                zujian.employee.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesReturnCustomer) {
                zujian.customer.setVisibility(View.GONE);
            } else {
                zujian.customer.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesReturnBrand) {
                zujian.brand.setVisibility(View.GONE);
            } else {
                zujian.brand.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesReturnMemo) {
                zujian.memo.setVisibility(View.GONE);
            } else {
                zujian.memo.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesReturnWarehouse) {
                zujian.other.setVisibility(View.GONE);
            } else {
                zujian.other.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesReturnDepartment) {
                zujian.department.setVisibility(View.GONE);
            } else {
                zujian.department.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.salesReturnAmountSum) {
                zujian.amount.setVisibility(View.GONE);
            } else {
                zujian.amount.setVisibility(View.VISIBLE);
            }
        }

         //采购订单
          if(activity instanceof PurchaseOrderActivity){
              zujian.other.setVisibility(View.GONE);
              if (!LoginParameterUtil.purchaseOrderEmployee) {
                  zujian.employee.setVisibility(View.GONE);
              } else {
                  zujian.employee.setVisibility(View.VISIBLE);
              }
              if (!LoginParameterUtil.purchaseOrderSupplier) {
                  zujian.customer.setVisibility(View.GONE);
              } else {
                  zujian.customer.setVisibility(View.VISIBLE);
              }
              if (!LoginParameterUtil.purchaseOrderBrand) {
                  zujian.brand.setVisibility(View.GONE);
              } else {
                  zujian.brand.setVisibility(View.VISIBLE);
              }
              if (!LoginParameterUtil.purchaseOrderMemo) {
                  zujian.memo.setVisibility(View.GONE);
              } else {
                  zujian.memo.setVisibility(View.VISIBLE);
              }
              if (!LoginParameterUtil.purchaseOrderDepartment) {
                  zujian.department.setVisibility(View.GONE);
              } else {
                  zujian.department.setVisibility(View.VISIBLE);
              }
              if (!LoginParameterUtil.purchaseOrderAmountSum) {
                  zujian.amount.setVisibility(View.GONE);
              } else {
                  zujian.amount.setVisibility(View.VISIBLE);
              }
          }

        // 采购收货单
        if (activity instanceof PurchaseActivity) {
            zujian.other.setVisibility(View.GONE);
            if (!LoginParameterUtil.purchaseEmployee) {
                zujian.employee.setVisibility(View.GONE);
            } else {
                zujian.employee.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.purchaseSupplier) {
                zujian.customer.setVisibility(View.GONE);
            } else {
                zujian.customer.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.purchaseBrand) {
                zujian.brand.setVisibility(View.GONE);
            } else {
                zujian.brand.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.purchaseMemo) {
                zujian.memo.setVisibility(View.GONE);
            } else {
                zujian.memo.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.purchaseDepartment) {
                zujian.department.setVisibility(View.GONE);
            } else {
                zujian.department.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.purchaseAmountSum) {
                zujian.amount.setVisibility(View.GONE);
            } else {
                zujian.amount.setVisibility(View.VISIBLE);
            }
        }
        // 采购退货单
        if (activity instanceof PurchaseReturnActivity) {
            zujian.other.setVisibility(View.GONE);
            if (!LoginParameterUtil.purchaseReturnEmployee) {
                zujian.employee.setVisibility(View.GONE);
            } else {
                zujian.employee.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.purchaseReturnSupplier) {
                zujian.customer.setVisibility(View.GONE);
            } else {
                zujian.customer.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.purchaseReturnBrand) {
                zujian.brand.setVisibility(View.GONE);
            } else {
                zujian.brand.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.purchaseReturnMemo) {
                zujian.memo.setVisibility(View.GONE);
            } else {
                zujian.memo.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.purchaseReturnDepartment) {
                zujian.department.setVisibility(View.GONE);
            } else {
                zujian.department.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.purchaseReturnAmountSum) {
                zujian.amount.setVisibility(View.GONE);
            } else {
                zujian.amount.setVisibility(View.VISIBLE);
            }
        }
        // 转仓单
        if (activity instanceof StockMoveActivity) {
            zujian.customer.setVisibility(View.GONE);
            if (!LoginParameterUtil.stockMoveEmployee) {
                zujian.employee.setVisibility(View.GONE);
            } else {
                zujian.employee.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockMoveWarehouseOut) {
                zujian.department.setVisibility(View.GONE);
            } else {
                zujian.department.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockMoveBrand) {
                zujian.brand.setVisibility(View.GONE);
            } else {
                zujian.brand.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockMoveMemo) {
                zujian.memo.setVisibility(View.GONE);
            } else {
                zujian.memo.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockMoveWarehouseIn) {
                zujian.other.setVisibility(View.GONE);
            } else {
                zujian.other.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockMoveAmountSum) {
                zujian.amount.setVisibility(View.GONE);
            } else {
                zujian.amount.setVisibility(View.VISIBLE);
            }
        }
        // 进仓单
        if (activity instanceof WarehouseStockInActivity) {
            if (!LoginParameterUtil.stockInEmployee) {
                zujian.employee.setVisibility(View.GONE);
            } else {
                zujian.employee.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockInWarehouse) {
                zujian.department.setVisibility(View.GONE);
            } else {
                zujian.department.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockInWarehouseOut) {
                zujian.customer.setVisibility(View.GONE);
            } else {
                zujian.customer.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockInBrand) {
                zujian.brand.setVisibility(View.GONE);
            } else {
                zujian.brand.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockInMemo) {
                zujian.memo.setVisibility(View.GONE);
            } else {
                zujian.memo.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockInRelationNo) {
                zujian.other.setVisibility(View.GONE);
            } else {
                zujian.other.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockInAmountSum) {
                zujian.amount.setVisibility(View.GONE);
            } else {
                zujian.amount.setVisibility(View.VISIBLE);
            }
        }
        // 出仓单
        if (activity instanceof WarehouseStockOutActivity) {
            if (!LoginParameterUtil.stockOutEmployee) {
                zujian.employee.setVisibility(View.GONE);
            } else {
                zujian.employee.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockOutWarehouse) {
                zujian.department.setVisibility(View.GONE);
            } else {
                zujian.department.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockOutWarehouseIn) {
                zujian.customer.setVisibility(View.GONE);
            } else {
                zujian.customer.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockOutBrand) {
                zujian.brand.setVisibility(View.GONE);
            } else {
                zujian.brand.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockOutMemo) {
                zujian.memo.setVisibility(View.GONE);
            } else {
                zujian.memo.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockOutRelationNo) {
                zujian.other.setVisibility(View.GONE);
            } else {
                zujian.other.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stockOutAmountSum) {
                zujian.amount.setVisibility(View.GONE);
            } else {
                zujian.amount.setVisibility(View.VISIBLE);
            }
        }
        // 盘点单
        if (activity instanceof InventorySheetActivity) {
            zujian.employee.setVisibility(View.GONE);
            zujian.other.setVisibility(View.GONE);
            zujian.customer.setVisibility(View.GONE);
            if (!LoginParameterUtil.stocktakingEmployee) {
                zujian.employee.setVisibility(View.GONE);
            } else {
                zujian.employee.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stocktakingWarehouse) {
                zujian.department.setVisibility(View.GONE);
            } else {
                zujian.department.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stocktakingBrand) {
                zujian.brand.setVisibility(View.GONE);
            } else {
                zujian.brand.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stocktakingMemo) {
                zujian.memo.setVisibility(View.GONE);
            } else {
                zujian.memo.setVisibility(View.VISIBLE);
            }
            if (!LoginParameterUtil.stocktakingAmountSum) {
                zujian.amount.setVisibility(View.GONE);
            } else {
                zujian.amount.setVisibility(View.VISIBLE);
            }
        }
        // 判断第二行是否全部为空
        if (zujian.department.getVisibility() == View.GONE && zujian.customer.getVisibility() == View.GONE && zujian.amount.getVisibility() == View.GONE) {
            zujian.ll_second.setVisibility(View.GONE);
        } else {
            zujian.ll_second.setVisibility(View.VISIBLE);
        }
        // 判断第三行是否全部为空
        if (zujian.employee.getVisibility() == View.GONE && zujian.brand.getVisibility() == View.GONE) {
            zujian.ll_third.setVisibility(View.GONE);
        } else {
            zujian.ll_third.setVisibility(View.VISIBLE);
        }
        // 判断第四行是否全部为空
        if (zujian.other.getVisibility() == View.GONE && zujian.memo.getVisibility() == View.GONE) {
            zujian.ll_fourth.setVisibility(View.GONE);
        } else {
            zujian.ll_fourth.setVisibility(View.VISIBLE);
        }
    }





}
