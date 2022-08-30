package com.example.modelfashion.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.modelfashion.Activity.BrandListAct;
import com.example.modelfashion.Activity.BrandsProductAct;
import com.example.modelfashion.Activity.GeneralProductAct;
import com.example.modelfashion.Activity.MyFavoriteAct;
import com.example.modelfashion.Activity.NewProductDetailAct;
import com.example.modelfashion.Activity.ProductDetailActivity;
import com.example.modelfashion.Activity.SignIn.SignUpActivity;
import com.example.modelfashion.Adapter.ProductListAdapter;
import com.example.modelfashion.Adapter.VpSaleMainFmAdapter;
import com.example.modelfashion.Interface.ApiRetrofit;
import com.example.modelfashion.Model.Brand;
import com.example.modelfashion.Model.ItemSaleMain;
import com.example.modelfashion.Model.response.my_product.CartProduct;
import com.example.modelfashion.Model.response.my_product.MyProduct;
import com.example.modelfashion.R;
import com.example.modelfashion.Utility.Constants;

import java.util.ArrayList;
import java.util.Timer;

import me.relex.circleindicator.CircleIndicator3;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewProductFragment extends Fragment {
    private ViewPager2 vpSaleMain;
    private CircleIndicator3 ciSale;
    private RecyclerView rvProductFrag;
    private ProductListAdapter productListAdapter;
    private ArrayList<MyProduct> arrMyProduct = new ArrayList<>();
    private ArrayList<String> arrType = new ArrayList<>();
    private ArrayList<Brand> arrBrand = new ArrayList<>();
    private TextView tvSeeBrand;
    private ImageView imgFav;
    private EditText edtSearch;
    private SwipeRefreshLayout refreshLayout;
    ArrayList<MyProduct> arrSearchProduct = new ArrayList<>();
    Timer timer;
    private ImageView imgBrand1, imgBrand2, imgBrand3, imgBrand4, imgBrand5, imgBrand6;
    ArrayList<ItemSaleMain> arrItem = new ArrayList<>();
    private String user_id;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRunable = new Runnable() {
        @Override
        public void run() {
            int currentPosition = vpSaleMain.getCurrentItem();
            if (currentPosition == arrItem.size() - 1) {
                vpSaleMain.setCurrentItem(0);
            } else
                vpSaleMain.setCurrentItem(currentPosition + 1);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_new_product, container, false);
        vpSaleMain = view.findViewById(R.id.vp_sale_main_fm);
        ciSale = view.findViewById(R.id.ci_sale_main_fm);
        rvProductFrag = view.findViewById(R.id.rv_men_page_fm);
        imgBrand1 = view.findViewById(R.id.brand_logo1);
        imgBrand2 = view.findViewById(R.id.brand_logo2);
        imgBrand3 = view.findViewById(R.id.brand_logo3);
        imgBrand4 = view.findViewById(R.id.brand_logo4);
        imgBrand5 = view.findViewById(R.id.brand_logo5);
        imgBrand6 = view.findViewById(R.id.brand_logo6);
        tvSeeBrand = view.findViewById(R.id.tv_see_all_brand);
        edtSearch = view.findViewById(R.id.edt_search);
        imgFav = view.findViewById(R.id.img_fav_mainfm);
        refreshLayout = view.findViewById(R.id.refresh_layout);
        Bundle info = getArguments();
        user_id = info.getString("user_id");
        arrItem.add(new ItemSaleMain(R.drawable.sale_1));
        arrItem.add(new ItemSaleMain(R.drawable.sale_2));
        arrItem.add(new ItemSaleMain(R.drawable.sale_3));
        VpSaleMainFmAdapter vpSaleMainFmAdapter = new VpSaleMainFmAdapter(arrItem);
        vpSaleMain.setAdapter(vpSaleMainFmAdapter);

        PopupMenu popupMenu = new PopupMenu(getActivity(),edtSearch);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//               Log.e("search","start "+i+" before "+i1+" count "+i2);
                popupMenu.getMenu().clear();
                arrSearchProduct.removeAll(arrSearchProduct);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!edtSearch.getText().toString().isEmpty()){
                    ApiRetrofit.apiRetrofit.GetSearchProduct(edtSearch.getText().toString()).enqueue(new Callback<ArrayList<MyProduct>>() {
                        @Override
                        public void onResponse(Call<ArrayList<MyProduct>> call, Response<ArrayList<MyProduct>> response) {
                            if(response.body().size() == 0){
                                popupMenu.getMenu().add("No Result..");
                            }
                            arrSearchProduct = response.body();
                            for (int i = 0; i < response.body().size(); i++){
                                popupMenu.getMenu().add(response.body().get(i).getProduct_name());
                            }
                            popupMenu.getMenuInflater().inflate(R.menu.menu_search,popupMenu.getMenu());
                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                      String product_id = "";
                                      for (int i = 0; i<arrSearchProduct.size(); i++){
                                          if(menuItem.getTitle().equals(arrSearchProduct.get(i).getProduct_name())){
                                              product_id = arrSearchProduct.get(i).getId();
                                          }
                                      }
                                    Intent intent = new Intent(getContext(),NewProductDetailAct.class);
                                    intent.putExtra("user_id", user_id);
                                    intent.putExtra(Constants.KEY_PRODUCT_ID,product_id);
                                    startActivity(intent);
                                    return false;
                                }
                            });
                            popupMenu.show();
                        }

                        @Override
                        public void onFailure(Call<ArrayList<MyProduct>> call, Throwable t) {

                        }
                    });
                }
            }
        });

        tvSeeBrand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), BrandListAct.class);
                intent.putExtra("user_id",user_id);
                startActivity(intent);
            }
        });
        ciSale.setViewPager(vpSaleMain);
        vpSaleMain.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mHandler.removeCallbacks(mRunable);
                mHandler.postDelayed(mRunable, 2000);
            }
        });
        imgFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user_id.equalsIgnoreCase("null")){
                    Toast.makeText(getContext(), "Bạn phải đăng nhập để sử dụng chức năng này", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(getContext(), MyFavoriteAct.class);
                    intent.putExtra("user_id",user_id);
                    startActivity(intent);
                }
            }
        });
        return view;
    }
    private void GetProductData(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang tải sản phẩm");
        progressDialog.show();
//        progressDialog.setCancelable(false);
        ApiRetrofit.apiRetrofit.GetAllProduct().enqueue(new Callback<ArrayList<MyProduct>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<ArrayList<MyProduct>> call, Response<ArrayList<MyProduct>> response) {
                GetBrandData();
                arrMyProduct = response.body();
                ApiRetrofit.apiRetrofit.GetRecentType().enqueue(new Callback<ArrayList<String>>() {
                    @Override
                    public void onResponse(Call<ArrayList<String>> call, Response<ArrayList<String>> response) {
                        progressDialog.hide();
                        arrType = response.body();
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                        rvProductFrag.setLayoutManager(linearLayoutManager);
                        productListAdapter = new ProductListAdapter(getContext(),arrType,arrMyProduct);
                        rvProductFrag.setAdapter(productListAdapter);
                        productListAdapter.onItemClickListener(new ProductListAdapter.OnItemClickListener() {
                            @Override
                            public void imgClick(int position, MyProduct product) {
                                Log.e("test_size",product.getSizes()+"");
                                Intent intent = new Intent(getActivity(), NewProductDetailAct.class);
                                intent.putExtra(Constants.KEY_PRODUCT_ID,product.getId());
                                intent.putExtra("user_id",user_id);
                                startActivity(intent);
                            }

                            @Override
                            public void imgAddToCartClick(int position, MyProduct product) {
                                if(user_id.equalsIgnoreCase("null")){
                                    Toast.makeText(getContext(), "Thực hiện đăng nhập để sử dụng chức năng này", Toast.LENGTH_SHORT).show();
                                }else {
                                    AddToFavorite(product.getId());
                                }
                            }

                            @Override
                            public void seeAll(int position, String type) {
                                Intent intent = new Intent(getActivity(), GeneralProductAct.class);
                                intent.putExtra("user_id",user_id);
                                intent.putExtra("type", type);
                                startActivity(intent);
                            }
                        });
                        Log.e("loading_prod",arrMyProduct.size()+" "+arrType);
                    }

                    @Override
                    public void onFailure(Call<ArrayList<String>> call, Throwable t) {

                    }
                });
            }

            @Override
            public void onFailure(Call<ArrayList<MyProduct>> call, Throwable t) {
                progressDialog.hide();
            }
        });
    }
    private void GetBrandData(){
            ApiRetrofit.apiRetrofit.Get6RdBrand().enqueue(new Callback<ArrayList<Brand>>() {
                @Override
                public void onResponse(Call<ArrayList<Brand>> call, Response<ArrayList<Brand>> response) {
                    arrBrand = response.body();
                    imgBrand1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), BrandsProductAct.class);
                            intent.putExtra("user_id",user_id);
                            intent.putExtra("brand_id",arrBrand.get(0).getBrand_id());
                            startActivity(intent);
                        }
                    });
                    imgBrand2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), BrandsProductAct.class);
                            intent.putExtra("user_id",user_id);
                            intent.putExtra("brand_id",arrBrand.get(1).getBrand_id());
                            startActivity(intent);
                        }
                    });
                    imgBrand3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), BrandsProductAct.class);
                            intent.putExtra("user_id",user_id);
                            intent.putExtra("brand_id",arrBrand.get(2).getBrand_id());
                            startActivity(intent);
                        }
                    });
                    imgBrand4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), BrandsProductAct.class);
                            intent.putExtra("user_id",user_id);
                            intent.putExtra("brand_id",arrBrand.get(3).getBrand_id());
                            startActivity(intent);
                        }
                    });
                    imgBrand5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), BrandsProductAct.class);
                            intent.putExtra("user_id",user_id);
                            intent.putExtra("brand_id",arrBrand.get(4).getBrand_id());
                            startActivity(intent);
                        }
                    });
                    imgBrand6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), BrandsProductAct.class);
                            intent.putExtra("user_id",user_id);
                            intent.putExtra("brand_id",arrBrand.get(5).getBrand_id());
                            startActivity(intent);
                        }
                    });
                    Glide.with(getContext()).load(arrBrand.get(0).getLogo().replace("localhost", Constants.KEY_IP)).into(imgBrand1);
                    Glide.with(getContext()).load(arrBrand.get(1).getLogo().replace("localhost", Constants.KEY_IP)).into(imgBrand2);
                    Glide.with(getContext()).load(arrBrand.get(2).getLogo().replace("localhost", Constants.KEY_IP)).into(imgBrand3);
                    Glide.with(getContext()).load(arrBrand.get(3).getLogo().replace("localhost", Constants.KEY_IP)).into(imgBrand4);
                    Glide.with(getContext()).load(arrBrand.get(4).getLogo().replace("localhost", Constants.KEY_IP)).into(imgBrand5);
                    Glide.with(getContext()).load(arrBrand.get(5).getLogo().replace("localhost", Constants.KEY_IP)).into(imgBrand6);
                }

                @Override
                public void onFailure(Call<ArrayList<Brand>> call, Throwable t) {

                }
            });
    }
    private void AddToFavorite(String product_id){
        ApiRetrofit.apiRetrofit.InsertFavorite(user_id, product_id).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body().equalsIgnoreCase("true")){
                    Toast.makeText(getContext(), "Thêm vào 'Theo dõi' thành công", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getContext(), "Sản phẩm đang theo dõi", Toast.LENGTH_SHORT).show();
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
        GetProductData();
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetBrandData();
                refreshLayout.setRefreshing(false);
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunable);
        mRunable = null;
        mHandler = null;
    }
}
