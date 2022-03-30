package com.example.modelfashion.History.ViewHistory;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.modelfashion.History.ApdapterHistory.HistoryAdapter;
import com.example.modelfashion.Interface.ApiRetrofit;
import com.example.modelfashion.Model.MHistory.BillModel;
import com.example.modelfashion.Model.MHistory.ModelHistory;
import com.example.modelfashion.Model.MHistory.ProductHistory;
import com.example.modelfashion.Model.MHistory.SubProduct;
import com.example.modelfashion.Model.response.my_product.MyProduct;
import com.example.modelfashion.OrderStatus.AdapterOrderStatus.OrderStatusAdapter;
import com.example.modelfashion.R;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {
    public static List<ModelHistory> listModelHistory ;
    private List<ModelHistory> listModelHistoryNew;
    private List<ProductHistory> listProduct;
    private ListView lv_history;
    private ImageView img_history_back;
    private RelativeLayout rl_filter_history;
    private TextView tv_status_history;
    private int numberStatus = 4;
    private String user_id = "1";
    public static ArrayList<BillModel> billModels = new ArrayList<>();
    private ArrayList<SubProduct> subProducts = new ArrayList<>();
    private ArrayList<MyProduct> myProducts = new ArrayList<>();
    private ArrayList<String> arr_product_name = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        lv_history = findViewById(R.id.lv_history);
        img_history_back = findViewById(R.id.img_history_back);
        rl_filter_history = findViewById(R.id.rl_filter_history);
        tv_status_history = findViewById(R.id.tv_status_history);
        Intent intent = getIntent();
        numberStatus = intent.getIntExtra("numberStatus",4);
        loadTitleStatus(numberStatus);

        img_history_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        rl_filter_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogFilter();
            }
        });
        listModelHistory = new ArrayList<>();
        listModelHistoryNew = new ArrayList<>();
        listProduct = new ArrayList<>();
        //fakeData();
        getListOrder();





    }
    private void getListOrder(){
        ApiRetrofit.apiRetrofit.GetListOrder(user_id).enqueue(new Callback<ArrayList<BillModel>>() {
            @Override
            public void onResponse(Call<ArrayList<BillModel>> call, Response<ArrayList<BillModel>> response) {
                billModels = response.body();
                int count = 0;
                int count2 = 0;
                for (int i = 0;i<billModels.size();i++){
                    count = i;
                    arr_product_name.clear();
                    for (int j = 0;j<billModels.get(i).getBill_detail().size();j++){
                        count2 = j;
                        arr_product_name.add(billModels.get(i).getBill_detail().get(j).getProduct_name());
                    }
                    JSONArray jsonArray = new JSONArray(arr_product_name);
                    try {
                        Log.e("TAG", jsonArray.get(0).toString() );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String id = billModels.get(i).getBill_id();
                    if (count2 == billModels.get(i).getBill_detail().size()-1) {
                        myProducts.clear();
                        getListProduct(jsonArray, id, count);
                    }

                }


            }

            @Override
            public void onFailure(Call<ArrayList<BillModel>> call, Throwable t) {
                billModels.clear();
                Toast.makeText(HistoryActivity.this,"Load faild",Toast.LENGTH_SHORT).show();

            }
        });
    }
    private void getListProduct(JSONArray product_name,String id,int count){
        ApiRetrofit.apiRetrofit.GetProductSubByName(product_name).enqueue(new Callback<ArrayList<MyProduct>>() {
            @Override
            public void onResponse(Call<ArrayList<MyProduct>> call, Response<ArrayList<MyProduct>> response) {
                myProducts = response.body();
//                SubProduct subProduct = new SubProduct(id,myProducts);
             //   Log.e("Produc", myProducts.get(0).getPrice() );
                try {
                    SubProduct subProduct = new SubProduct(id,myProducts);
                    subProducts.add(subProduct);
                    Log.e("id", id );
                    Log.e("Produc", myProducts.get(0).getPrice() );
                }catch (Exception e){

                }
                if(count == billModels.size()-1){

                    loadData(numberStatus);
                }

                //                subProducts.add(subProduct);

            }

            @Override
            public void onFailure(Call<ArrayList<MyProduct>> call, Throwable t) {

            }
        });
    }
    private void loadData(int i){
        try {
            for (int in = 0;in<subProducts.size();in++){
                Log.e("abc",subProducts.get(in).getMyProducts().get(0).getPrice() );
            }
        }catch (Exception e){}

        getData(i);
        if(i == 4){
            HistoryAdapter historyAdapter = new HistoryAdapter(HistoryActivity.this,billModels,subProducts);
            lv_history.setAdapter(historyAdapter);
        }else {
            OrderStatusAdapter historyAdapterOrder = new OrderStatusAdapter(HistoryActivity.this,listModelHistoryNew);
            lv_history.setAdapter(historyAdapterOrder);
        }

    }
    private void getData(int index){
        listModelHistoryNew.clear();
        String newStatus = "";
        if(index == 1){
            newStatus = "Chờ xác nhận";
        }else if(index == 2){
            newStatus = "Chờ lấy hàng";
        }else if(index == 3){
            newStatus = "Đang giao";
        }else if(index == 4){
            newStatus = "Đã giao";
        }
        for (int i = 0;i<listModelHistory.size();i++){
            if(listModelHistory.get(i).getmStatus().matches(newStatus)){
                listModelHistoryNew.add(listModelHistory.get(i));
            }
        }

    }

    private void fakeData(){

        listProduct.add(new ProductHistory("1","a1","10000","L","https://i.pinimg.com/originals/ce/a7/5e/cea75e472d431e283a4a622ed1d7b155.png","10"));
        listProduct.add(new ProductHistory("2","a2","10000","L","https://i.pinimg.com/originals/ce/a7/5e/cea75e472d431e283a4a622ed1d7b155.png","10"));
        listProduct.add(new ProductHistory("3","a3","10000","L","https://i.pinimg.com/originals/ce/a7/5e/cea75e472d431e283a4a622ed1d7b155.png","10"));

        listModelHistory.add(new ModelHistory("HD1","0987563","ha noi","12/2 0:0",
                "13/2 0:0","Chờ xác nhận","100000",listProduct));
        listModelHistory.add(new ModelHistory("HD2","0987563","ha noi","12/2 0:0",
                "13/2 0:0","Chờ lấy hàng","100000",listProduct));
        listModelHistory.add(new ModelHistory("HD3","0987563","ha noi","12/2 0:0",
                "13/2 0:0","Đang giao","100000",listProduct));
        listModelHistory.add(new ModelHistory("HD4","0987563","ha noi","12/2 0:0",
                "13/2 0:0","Đã giao","100000",listProduct));


    }
    private int filter_number = 0;
    private void showDialogFilter(){
        Dialog dialog = new Dialog(HistoryActivity.this);
        dialog.setContentView(R.layout.dialog_filter_history);
        dialog.setCancelable(false);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView status1,status2,status3,status4;
        status1 = dialog.findViewById(R.id.status1);
        status2 = dialog.findViewById(R.id.status2);
        status3 = dialog.findViewById(R.id.status3);
        status4 = dialog.findViewById(R.id.status4);
        TextView filter_history_cancel,filter_history_ok;
        filter_history_cancel = dialog.findViewById(R.id.filter_history_cancel);
        filter_history_ok = dialog.findViewById(R.id.filter_history_ok);
        loadThemeFilterStatus(numberStatus,status1,status2,status3,status4);
        status1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter_number = 1;
                loadThemeFilterStatus(1,status1,status2,status3,status4);
            }
        });
        status2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter_number = 2;
                loadThemeFilterStatus(2,status1,status2,status3,status4);
            }
        });
        status3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter_number = 3;
                loadThemeFilterStatus(3,status1,status2,status3,status4);
            }
        });
        status4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter_number = 4;
                loadThemeFilterStatus(4,status1,status2,status3,status4);
            }
        });
        filter_history_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter_number = 0;
                dialog.dismiss();
            }
        });
        filter_history_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numberStatus = filter_number;
                loadThemeFilterStatus(numberStatus,status1,status2,status3,status4);
                loadTitleStatus(numberStatus);
                loadData(numberStatus);
                dialog.dismiss();

            }
        });

        dialog.show();
    }
    private void loadTitleStatus(int i){
        if (i == 1){
            tv_status_history.setText("Chờ xác nhận");
            tv_status_history.setTextColor(Color.parseColor("#FF0000"));
        }else if (i == 2){
            tv_status_history.setText("Chờ lấy hàng");
            tv_status_history.setTextColor(Color.parseColor("#ff9800"));
        }
        else if (i == 3){
            tv_status_history.setText("Đang giao");
            tv_status_history.setTextColor(Color.parseColor("#4caf50"));
        }
        else if (i == 4){
            tv_status_history.setText("Đã giao");
            tv_status_history.setTextColor(Color.parseColor("#4caf50"));
        }
    }
    private void loadThemeFilterStatus(int i,TextView st1,TextView st2,TextView st3,TextView st4){
        if (i == 1){
            st1.setTextColor(Color.parseColor("#FFFFFF"));
            st1.setBackground(getDrawable(R.drawable.bt_filter_history_select));
            st2.setTextColor(Color.parseColor("#000000"));
            st2.setBackground(getDrawable(R.drawable.bt_item_filter_history));
            st3.setTextColor(Color.parseColor("#000000"));
            st3.setBackground(getDrawable(R.drawable.bt_item_filter_history));
            st4.setTextColor(Color.parseColor("#000000"));
            st4.setBackground(getDrawable(R.drawable.bt_item_filter_history));
        }else if (i == 2){
            st2.setTextColor(Color.parseColor("#FFFFFF"));
            st2.setBackground(getDrawable(R.drawable.bt_filter_history_select));
            st1.setTextColor(Color.parseColor("#000000"));
            st1.setBackground(getDrawable(R.drawable.bt_item_filter_history));
            st3.setTextColor(Color.parseColor("#000000"));
            st3.setBackground(getDrawable(R.drawable.bt_item_filter_history));
            st4.setTextColor(Color.parseColor("#000000"));
            st4.setBackground(getDrawable(R.drawable.bt_item_filter_history));
        }
        else if (i == 3){
            st3.setTextColor(Color.parseColor("#FFFFFF"));
            st3.setBackground(getDrawable(R.drawable.bt_filter_history_select));
            st1.setTextColor(Color.parseColor("#000000"));
            st1.setBackground(getDrawable(R.drawable.bt_item_filter_history));
            st2.setTextColor(Color.parseColor("#000000"));
            st2.setBackground(getDrawable(R.drawable.bt_item_filter_history));
            st4.setTextColor(Color.parseColor("#000000"));
            st4.setBackground(getDrawable(R.drawable.bt_item_filter_history));
        }
        else if (i == 4){
            st4.setTextColor(Color.parseColor("#FFFFFF"));
            st4.setBackground(getDrawable(R.drawable.bt_filter_history_select));
            st1.setTextColor(Color.parseColor("#000000"));
            st1.setBackground(getDrawable(R.drawable.bt_item_filter_history));
            st2.setTextColor(Color.parseColor("#000000"));
            st2.setBackground(getDrawable(R.drawable.bt_item_filter_history));
            st3.setTextColor(Color.parseColor("#000000"));
            st3.setBackground(getDrawable(R.drawable.bt_item_filter_history));
        }


    }
}