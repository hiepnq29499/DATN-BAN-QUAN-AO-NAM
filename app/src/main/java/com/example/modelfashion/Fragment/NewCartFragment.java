package com.example.modelfashion.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modelfashion.Activity.DeliveryAddressAct;
import com.example.modelfashion.Activity.MainActivity;
import com.example.modelfashion.Adapter.CartItemAdapter;
import com.example.modelfashion.Interface.ApiRetrofit;
import com.example.modelfashion.Model.DeliveryInfo;
import com.example.modelfashion.Model.Product;
import com.example.modelfashion.Model.response.my_product.CartProduct;
import com.example.modelfashion.Model.response.my_product.MyProduct;
import com.example.modelfashion.R;
import com.google.android.gms.common.api.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.momo.momo_partner.AppMoMoLib;

public class NewCartFragment extends Fragment {
    TextView tv_add_address, tv_total_amount;
    Button btn_payment;
    RecyclerView rv_cart;
    String user_id;
    ArrayList<MyProduct> arrProduct = new ArrayList<>();
    ArrayList<CartProduct> arrCartProduct = new ArrayList<>();
    ArrayList<MyProduct> arrProductBuy = new ArrayList<>();
    int total_amount = 0;
    int size_item_buy = 0;
    Boolean check_product_status = true;
    ArrayList<CartProduct> arrCartItemBuy = new ArrayList<>();
    CartItemAdapter cartItemAdapter;
    DecimalFormat formatter = new DecimalFormat("###,###,###");
    private String amount = "10000";
    private String fee = "0";
    int environment = 0;//developer default
    private String merchantName = "FREE STYLE MEN";
    private String merchantCode = "MOMOJDFR20220731";
    private String merchantNameLabel = "BacNguyen";
    private String description = "Mua Quan Ao";
    public NewCartFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart_2, container, false);
        tv_add_address = view.findViewById(R.id.tv_add_delivery_address);
        tv_total_amount = view.findViewById(R.id.tv_total_new_cart);
        btn_payment = view.findViewById(R.id.btn_payment_new_cart);
        rv_cart = view.findViewById(R.id.rv_new_cart);
        Bundle info = getArguments();
        user_id = info.getString("user_id");
        tv_total_amount.setText("0 VND");
        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT); // AppMoMoLib.ENVIRONMENT.PRODUCTION
        if(user_id!="null"){
            SetCartData(user_id);
        }
        tv_add_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddAddress();
            }
        });
        return view;
    }

    private void SetCartData(String user_id){
        ApiRetrofit.apiRetrofit.GetProductInCart(user_id).enqueue(new Callback<ArrayList<MyProduct>>() {
            @Override
            public void onResponse(Call<ArrayList<MyProduct>> call, Response<ArrayList<MyProduct>> response) {
                arrProduct = response.body();
                ApiRetrofit.apiRetrofit.GetCart(user_id).enqueue(new Callback<ArrayList<CartProduct>>() {
                    @Override
                    public void onResponse(Call<ArrayList<CartProduct>> call, Response<ArrayList<CartProduct>> response) {
                        arrCartProduct = response.body();
                        Log.e("remove_check2", arrCartProduct.size()+"");
                        cartItemAdapter = new CartItemAdapter(getContext(), arrProduct, arrCartProduct, user_id);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
                        rv_cart.setLayoutManager(linearLayoutManager);
                        cartItemAdapter.OnItemClickListener(new CartItemAdapter.OnItemClickListener() {
                            @Override
                            public void removeCartItem(int position,String cartId) {
                                ApiRetrofit.apiRetrofit.RemoveCartItem2(cartId).enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        if(response.body().equalsIgnoreCase("ok")){
                                            if(!arrProductBuy.isEmpty()&&!arrCartItemBuy.isEmpty()){
                                                Toast.makeText(getContext(), "Vui l??ng b??? tick t???t c??? s???n ph???m", Toast.LENGTH_SHORT).show();
                                            }else {
                                                arrProduct.remove(position);
                                                arrCartProduct.remove(position);
                                                Log.e("remove_check", arrProduct.size()+" "+arrCartProduct.size());
                                                cartItemAdapter.notifyDataSetChanged();
                                            }
                                        }else {
                                            Log.e("remove_err", response.body());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {

                                    }
                                });
                            }

                            @Override
                            public void itemCheckedTrue(int position, String cartId) {
                                total_amount += Integer.parseInt(arrProduct.get(position).getPrice().split("\\.")[0])*(100-Float.parseFloat(arrProduct.get(position).getDiscount_rate()))/100;
                                tv_total_amount.setText(formatter.format(total_amount)+" VND");
                                arrCartItemBuy.add(arrCartProduct.get(position));
                                arrProductBuy.add(arrProduct.get(position));
                            }

                            @Override
                            public void itemCheckedFalse(int position, String cartId) {
                                total_amount -= Integer.parseInt(arrProduct.get(position).getPrice().split("\\.")[0])*(100-Float.parseFloat(arrProduct.get(position).getDiscount_rate()))/100;
                                tv_total_amount.setText(formatter.format(total_amount)+" VND");
                                arrCartItemBuy.remove(arrCartProduct.get(position));
                                arrProductBuy.remove(arrProduct.get(position));
                            }
                        });
                        btn_payment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                SetPayment();
                            }
                        });
                        rv_cart.setAdapter(cartItemAdapter);
                    }

                    @Override
                    public void onFailure(Call<ArrayList<CartProduct>> call, Throwable t) {

                    }
                });
            }

            @Override
            public void onFailure(Call<ArrayList<MyProduct>> call, Throwable t) {

            }
        });
    }
    private void AddAddress(){
        if(user_id!="null"){
            Intent intent = new Intent(getContext(), DeliveryAddressAct.class);
            intent.putExtra("user_id", user_id);
            getContext().startActivity(intent);
        }else {
            Toast.makeText(getContext(), "B???n ch??a th???c hi???n ????ng nh???p", Toast.LENGTH_SHORT).show();
        }
    }
    private void SetPayment(){
        if(total_amount!=0){
            ApiRetrofit.apiRetrofit.GetDeliveryInfo(user_id).enqueue(new Callback<DeliveryInfo>() {
                @Override
                public void onResponse(Call<DeliveryInfo> call, Response<DeliveryInfo> response) {
                    DeliveryInfo deliveryInfo = response.body();
                    if(!deliveryInfo.getDelivery_id().equalsIgnoreCase("null")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("S??? d???ng ?????a ch??? giao h??ng hi???n t???i ?");
                        builder.setCancelable(true);
                        builder.setNegativeButton("?????a ch??? m???i", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(getContext(), DeliveryAddressAct.class);
                                intent.putExtra("user_id", user_id);
                                getContext().startActivity(intent);
                            }
                        });
                        builder.setPositiveButton("?????ng ??", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                InsertBill(deliveryInfo.getReceiver_name(),deliveryInfo.getStreet_address(),deliveryInfo.getCity(),
                                        deliveryInfo.getContact(),Integer.toString(total_amount),arrCartItemBuy);

                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("B???n ch??a c?? ?????a ch??? nh???n h??ng, h??y ??i???n th??ng tin c???a b???n");
                        builder.setCancelable(true);
                        builder.setPositiveButton("?????ng ??", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(getContext(), DeliveryAddressAct.class);
                                intent.putExtra("user_id", user_id);
                                getContext().startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("H???y", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                builder.create().dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }

                @Override
                public void onFailure(Call<DeliveryInfo> call, Throwable t) {

                }
            });
        }else {
            Toast.makeText(getContext(), "B???n ch??a ch???n s???n ph???m ????? thanh to??n", Toast.LENGTH_SHORT).show();
        }
    }

    private void InsertBill(String receiver_name, String street, String city, String contact, String amount, ArrayList<CartProduct> arrCartProduct){
        ApiRetrofit.apiRetrofit.InsertBill2(user_id, receiver_name, street, city, contact, amount).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.e("Check11", response.body());
                for (int i =0; i<arrProductBuy.size();i++){
                    if(arrProductBuy.get(i).getStatus().equals("H???t h??ng")){
                        check_product_status = false;
                        Toast.makeText(getContext(), "S???n ph???m "+arrProductBuy.get(i).getProduct_name()+" ???? h???t h??ng", Toast.LENGTH_SHORT).show();
                    }
                }
                if(check_product_status == true){
                    for (int i =0; i<arrCartItemBuy.size();i++){
                        InsertBillDetail(arrCartItemBuy.get(i).getSizeId(),response.body(),arrCartItemBuy.get(i).getProductId(),arrCartProduct.get(i).getQuantity(),
                                arrCartItemBuy.get(i).getCartId(), arrProductBuy.get(i).getDiscount_rate(), arrCartItemBuy.size());
                    }
                    requestPayment();
                    Toast.makeText(getContext(), "?????t h??ng th??nh c??ng, ki???m tra l???i trong m???c h??a ????n", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
    private void InsertBillDetail(String size_id, String bill_id, String product_id, String quantity, String cart_id, String discount_rate, int size_check){
        ApiRetrofit.apiRetrofit.InsertBillDetail2(size_id,bill_id,product_id,quantity,cart_id, discount_rate).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                size_item_buy++;
                if(size_item_buy == size_check){
                    size_item_buy = 0;
                    total_amount = 0;
                    arrProduct.removeAll(arrProductBuy);
                    arrCartProduct.removeAll(arrCartItemBuy);
                    arrProductBuy.removeAll(arrProductBuy);
                    arrCartItemBuy.removeAll(arrCartItemBuy);
                    cartItemAdapter = new CartItemAdapter(getContext(), arrProduct, arrCartProduct, user_id);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
                    rv_cart.setLayoutManager(linearLayoutManager);
                    cartItemAdapter.OnItemClickListener(new CartItemAdapter.OnItemClickListener() {
                        @Override
                        public void removeCartItem(int position,String cartId) {
                            ApiRetrofit.apiRetrofit.RemoveCartItem2(cartId).enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if(response.body().equalsIgnoreCase("ok")){
                                        if(!arrProductBuy.isEmpty()&&!arrCartItemBuy.isEmpty()){
                                            Toast.makeText(getContext(), "Vui l??ng b??? tick t???t c??? s???n ph???m", Toast.LENGTH_SHORT).show();
                                        }else {
                                            arrProduct.remove(position);
                                            arrCartProduct.remove(position);
                                            Log.e("remove_check", arrProduct.size()+" "+arrCartProduct.size());
                                            cartItemAdapter.notifyDataSetChanged();
                                        }
                                    }else {
                                        Log.e("remove_err", response.body());
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {

                                }
                            });
                        }

                        @Override
                        public void itemCheckedTrue(int position, String cartId) {
                            total_amount += Integer.parseInt(arrProduct.get(position).getPrice().split("\\.")[0])*(100-Float.parseFloat(arrProduct.get(position).getDiscount_rate()))/100;
                            tv_total_amount.setText(formatter.format(total_amount)+" VND");
                            arrCartItemBuy.add(arrCartProduct.get(position));
                            arrProductBuy.add(arrProduct.get(position));
                        }

                        @Override
                        public void itemCheckedFalse(int position, String cartId) {
                            total_amount -= Integer.parseInt(arrProduct.get(position).getPrice().split("\\.")[0])*(100-Float.parseFloat(arrProduct.get(position).getDiscount_rate()))/100;
                            tv_total_amount.setText(formatter.format(total_amount)+" VND");
                            arrCartItemBuy.remove(arrCartProduct.get(position));
                            arrProductBuy.remove(arrProduct.get(position));
                        }
                    });
                    btn_payment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SetPayment();
                        }
                    });
                    rv_cart.setAdapter(cartItemAdapter);
                    tv_total_amount.setText("0 VND");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    //Get token through MoMo app
    private void requestPayment() {
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);
        Map<String, Object> eventValue = new HashMap<>();
        //client Required
        eventValue.put("merchantname", merchantName); //T??n ?????i t??c. ???????c ????ng k?? t???i https://business.momo.vn. VD: Google, Apple, Tiki , CGV Cinemas
        eventValue.put("merchantcode", merchantCode); //M?? ?????i t??c, ???????c cung c???p b???i MoMo t???i https://business.momo.vn
        eventValue.put("amount", total_amount); //Ki???u integer
        eventValue.put("orderId", "orderId123456789"); //uniqueue id cho Bill order, gi?? tr??? duy nh???t cho m???i ????n h??ng
        eventValue.put("orderLabel", "M?? ????n h??ng"); //g??n nh??n

        //client Optional - bill info
        eventValue.put("merchantnamelabel", "D???ch v???");//g??n nh??n
        eventValue.put("fee", 0); //Ki???u integer
        eventValue.put("description", description); //m?? t??? ????n h??ng - short description

        //client extra data
        eventValue.put("requestId",  merchantCode+"merchant_billId_"+System.currentTimeMillis());
        eventValue.put("partnerCode", merchantCode);
        //Example extra data
        JSONObject objExtraData = new JSONObject();
        try {
            objExtraData.put("site_code", "008");
            objExtraData.put("site_name", "CGV Cresent Mall");
            objExtraData.put("screen_code", 0);
            objExtraData.put("screen_name", "Special");
            objExtraData.put("movie_name", "K??? Tr???m M???t Tr??ng 3");
            objExtraData.put("movie_format", "2D");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventValue.put("extraData", objExtraData.toString());

        eventValue.put("extra", "");
        AppMoMoLib.getInstance().requestMoMoCallBack(getActivity(), eventValue);

    }
    //Get token callback from MoMo app an submit to server side

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1) {
            if(data != null) {
                if(data.getIntExtra("status", -1) == 0) {
                    //TOKEN IS AVAILABLE
//                    tvMessage.setText("message: " + "Get token " + data.getStringExtra("message"));
                    String token = data.getStringExtra("data"); //Token response
                    String phoneNumber = data.getStringExtra("phonenumber");
                    String env = data.getStringExtra("env");
                    if(env == null){
                        env = "app";
                    }
                    Toast.makeText(getContext(), "Ok", Toast.LENGTH_SHORT).show();
                    if(token != null && !token.equals("")) {
                        // TODO: send phoneNumber & token to your server side to process payment with MoMo server
                        // IF Momo topup success, continue to process your order
                    } else {
//                        tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                    }
                } else if(data.getIntExtra("status", -1) == 1) {
                    //TOKEN FAIL
                    String message = data.getStringExtra("message") != null?data.getStringExtra("message"):"Th???t b???i";
//                    tvMessage.setText("message: " + message);
                    Toast.makeText(getContext(), "Fail", Toast.LENGTH_SHORT).show();
                } else if(data.getIntExtra("status", -1) == 2) {
                    //TOKEN FAIL
//                    tvMessage.setText("message: " + this.getString(R.string.not_receive_info));

                } else {
                    //TOKEN FAIL
//                    tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
                }
            } else {
//                tvMessage.setText("message: " + this.getString(R.string.not_receive_info));
            }
        } else {
//            tvMessage.setText("message: " + this.getString(R.string.not_receive_info_err));
        }
    }
}
