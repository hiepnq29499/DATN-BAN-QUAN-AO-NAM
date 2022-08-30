package com.example.modelfashion.Activity.SignIn;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.modelfashion.Common.ProgressLoadingCommon;
import com.example.modelfashion.Interface.ApiRetrofit;
import com.example.modelfashion.R;
import com.example.modelfashion.network.ApiClient;
import com.example.modelfashion.network.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

//    ImageView imgBack;
//    EditText edtAccountSu, edtPassword, edtConfirmPassword;
//    Button btnSignUp;
//    CheckBox cbRules;
//    TextView tvSignIn, tvRules;
    ApiInterface apiInterface;
//    ProgressLoadingCommon progressLoadingCommon;
    TextInputEditText edtName;
    TextInputEditText edtEmail;
    TextInputEditText edtPw;
    TextInputEditText edtRePw;
    TextView tvSignUp;
    TextView tvForgotPw;
    TextView tvSignIn;
    ImageView imgBack;
    CheckBox cbRules;
    TextView tvRules;
    String account_type = "";
    Boolean check_register = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_2);
        account_type = "normal";
        viewHolder();
        setListener();
    }

    //tham chiếu
    private void viewHolder() {
//        imgBack = findViewById(R.id.imgBack);
//        edtAccountSu = findViewById(R.id.edtAccountSu);
//        edtPassword = findViewById(R.id.edtPasswordSu);
//        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
//        btnSignUp = findViewById(R.id.btnSignUp);
//        cbRules = findViewById(R.id.cbRules);
//        tvSignIn = findViewById(R.id.tvSignIn);
        apiInterface = ApiClient.provideApiInterface(SignUpActivity.this);
//        progressLoadingCommon = new ProgressLoadingCommon();
//        tvRules = findViewById(R.id.tvRules);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPw = findViewById(R.id.edtPw);
        edtRePw = findViewById(R.id.edtRePw);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPw = findViewById(R.id.tvForgotPw);
        tvSignIn = findViewById(R.id.tvSignIn);
        imgBack = findViewById(R.id.imgBack);
        cbRules = findViewById(R.id.cbRules);
        tvRules = findViewById(R.id.tvRules);
    }

    // bắt sự kiện
    private void setListener() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
//                    progressLoadingCommon.showProgressLoading(SignUpActivity.this);
                    openConfirmEmailDialog();

                }
            }
        });

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            }
        });

        tvRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
    }
    private void openConfirmEmailDialog(){
        ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setMessage("Pending");
        progressDialog.show();
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
        ApiRetrofit.apiRetrofit.SendConfirmEmail(edtEmail.getText().toString()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                progressDialog.hide();
                Log.e("confirm_mail",response.body());
                if(response.body().equals("in use")){
                    Toast.makeText(SignUpActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                    builder.setCancelable(true);
                    builder.setTitle("Email đã được sử dung, vui lòng đăng kí bằng email khác");
                    builder.setNegativeButton("Đóng", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            builder.create().cancel();
                        }
                    });
                    builder.create().show();
                }else {
                    View confirm_layout = getLayoutInflater().inflate(R.layout.dialog_confirm,null,false);
                    builder.setView(confirm_layout);
                    EditText edt_confirm_code = confirm_layout.findViewById(R.id.edt_confirm_code);

                    builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            builder.create().dismiss();
                        }
                    });
                    builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            progressDialog.setMessage("Pending");
                            progressDialog.show();
                            ApiRetrofit.apiRetrofit.GetConfirmMail(edtEmail.getText().toString(),edt_confirm_code.getText().toString()).enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    progressDialog.hide();
                                    Log.e("test1",response.body());
                                    if(response.body().trim().equalsIgnoreCase("true")){
                                        insertUser();
                                        alertDialog.dismiss();
                                    }else {
//                                        builder.create().show();
                                        Toast.makeText(SignUpActivity.this, "Mã xác nhận không chính xác", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    progressDialog.hide();
                                }
                            });
                        }
                    });

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, ""+t.toString(), Toast.LENGTH_LONG).show();
                progressDialog.hide();
            }
        });
    }
    private boolean insertUser() {
        ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setMessage("Pending");
        progressDialog.show();
        ApiRetrofit.apiRetrofit.InsertUser(edtName.getText().toString(),edtPw.getText().toString(),edtEmail.getText().toString(),account_type).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                progressDialog.hide();
                Log.e("insert_user",response.body());
                if(response.body().trim().equalsIgnoreCase("Success")){
                    Toast.makeText(SignUpActivity.this, "Đăng kí thành công", Toast.LENGTH_SHORT).show();
                    Log.e("insert_user",response.body());
                    check_register = true;
                }else {
                    Toast.makeText(SignUpActivity.this, ""+response.body(), Toast.LENGTH_SHORT).show();
                    check_register = false;
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
        return check_register;
    }

    // validate
    private Boolean validate() {
        Pattern special = Pattern.compile("[!#$%&*^()_+=|<>?{}\\[\\]~-]");
        if(edtName.getText().toString().contains(" ") || edtPw.getText().toString().contains(" ")){
            Toast.makeText(SignUpActivity.this, "Tên đăng nhập, mật khẩu không có dấu cách", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtName.getText().toString().isEmpty() || edtEmail.getText().toString().isEmpty() || edtPw.getText().toString().isEmpty() || edtRePw.getText().toString().isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Không để trống", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(edtName.getText().toString().length() < 8 || edtName.getText().toString().length()>16){
            Toast.makeText(SignUpActivity.this, "Tên đăng nhập chỉ gồm 8-16 kí tự", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (special.matcher(edtName.getText().toString()).find() || special.matcher(edtEmail.getText().toString()).find()) {
            Toast.makeText(SignUpActivity.this, "Không được viết kí tự đặc biệt", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (edtPw.getText().toString().length() < 8 || edtPw.getText().toString().length()>16) {
            Toast.makeText(SignUpActivity.this, "Mật khẩu chỉ gồm 8-16 kí tự", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!edtPw.getText().toString().equalsIgnoreCase(edtRePw.getText().toString())) {
            Toast.makeText(SignUpActivity.this, "Mật khẩu không giống nhau", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!cbRules.isChecked()) {
            Toast.makeText(SignUpActivity.this, "Vui lòng đọc và đồng ý với chính sách bảo mật", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);

        // Set Title and Message:
        builder.setTitle("Chính sách bảo mật.").setMessage("Chúng tôi thu thập, lưu trữ và xử lý thông tin của bạn cho quá trình mua hàng, cho những thông báo sau này và để cung cấp dịch vụ. Chúng tôi không giới hạn thông tin cá nhân: danh hiệu, tên, giới tính, ngày sinh, email, địa chỉ, địa chỉ giao hàng, số điện thoại, fax, chi tiết thanh toán, chi tiết thanh toán bằng thẻ hoặc chi tiết tài khoản ngân hàng.\n" +
                "\n" +
                "Chúng tôi sẽ dùng thông tin bạn đã cung cấp để xử lý đơn đặt hàng, cung cấp các dịch vụ và thông tin yêu cầu thông qua trang web và theo yêu cầu của bạn. Chúng tôi có thể chuyển tên và địa chỉ cho bên thứ ba để họ giao hàng cho bạn (ví dụ cho bên chuyển phát nhanh hoặc nhà cung cấp).");

        //
        builder.setCancelable(true);

        // Create "Positive" button with OnClickListener.
        builder.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                builder.create().dismiss();
            }
        });
        // Create AlertDialog:
        AlertDialog alert = builder.create();
        alert.show();
    }

    //chặn back
    @Override
    public void onBackPressed() {
        super.onBackPressed();  // optional depending on your needs
    }

}