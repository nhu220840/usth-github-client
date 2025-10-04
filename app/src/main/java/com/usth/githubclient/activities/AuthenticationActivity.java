package com.usth.githubclient.activities;

// Các import cần thiết cho Activity.
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.text.method.PasswordTransformationMethod;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.usth.githubclient.R;
import com.usth.githubclient.util.ThemeManager;
import com.usth.githubclient.viewmodel.AuthViewModel;

// AuthenticationActivity kế thừa từ AppCompatActivity, là lớp cơ sở cho các activity
// sử dụng các tính năng của AppCompat library.
public class AuthenticationActivity extends AppCompatActivity {
    // Khai báo ViewModel để xử lý logic.
    private AuthViewModel viewModel;
    // Khai báo các thành phần giao diện (UI components).
    private Button btnPatSignIn, btnSignOut;
    private ProgressBar progress;
    private TextView txtStatus;
    private EditText inputPat;
    private EditText inputUserNameOrEmail;
    private CheckBox checkboxShowPassword;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Lấy chế độ theme đã lưu (sáng/tối) và áp dụng nó.
        int savedTheme = ThemeManager.getSavedThemeMode(this);
        ThemeManager.applyTheme(savedTheme);

        // Gọi phương thức onCreate của lớp cha.
        super.onCreate(savedInstanceState);
        // Thiết lập layout cho Activity này từ tệp activity_authentication.xml.
        setContentView(R.layout.activity_authentication);

        // Ánh xạ các biến đã khai báo với các view trong layout bằng ID của chúng.
        btnPatSignIn = findViewById(R.id.btnPatSignIn);
        btnSignOut   = findViewById(R.id.btnSignOut);
        inputPat     = findViewById(R.id.inputPat);
        inputUserNameOrEmail = findViewById(R.id.inputUserNameOrEmail);
        progress     = findViewById(R.id.progress);
        txtStatus    = findViewById(R.id.txtStatus);
        checkboxShowPassword = findViewById(R.id.checkboxShowPassword);

        // Lắng nghe sự kiện thay đổi của checkbox "Show password".
        checkboxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Nếu được check, hiển thị PAT dưới dạng văn bản thường.
                inputPat.setTransformationMethod(null);
            } else {
                // Nếu không được check, ẩn PAT (hiển thị dưới dạng dấu chấm).
                inputPat.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            // Di chuyển con trỏ đến cuối văn bản.
            inputPat.setSelection(inputPat.getText().length());
        });

        // Khởi tạo AuthViewModel.
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Lắng nghe (observe) những thay đổi trạng thái từ ViewModel.
        // Bất cứ khi nào trạng thái trong ViewModel thay đổi, đoạn mã này sẽ được thực thi.
        viewModel.getUiState().observe(this, state -> {
            // Dùng switch-case để xử lý từng trạng thái giao diện.
            switch (state.status) {
                case IDLE: // Trạng thái nghỉ, mặc định
                    progress.setVisibility(View.GONE);
                    btnPatSignIn.setVisibility(View.VISIBLE);
                    btnSignOut.setVisibility(View.GONE);
                    txtStatus.setVisibility(View.GONE);
                    break;

                case LOADING: // Trạng thái đang tải
                    progress.setVisibility(View.VISIBLE);
                    btnPatSignIn.setVisibility(View.GONE);
                    btnSignOut.setVisibility(View.GONE);
                    txtStatus.setVisibility(View.GONE);
                    break;

                case SIGNED_IN: // Trạng thái đăng nhập thành công
                    progress.setVisibility(View.GONE);
                    btnPatSignIn.setVisibility(View.GONE);
                    btnSignOut.setVisibility(View.VISIBLE);
                    txtStatus.setVisibility(View.VISIBLE);
                    txtStatus.setText("Welcome, " + (state.username != null ? state.username : "user") + "!");

                    // Chuyển hướng sang MainActivity và đóng màn hình đăng nhập.
                    startActivity(new Intent(this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish(); // Gọi finish() để người dùng không thể quay lại màn hình này bằng nút Back.
                    break;

                case ERROR: // Trạng thái lỗi
                    progress.setVisibility(View.GONE);
                    btnPatSignIn.setVisibility(View.VISIBLE);
                    btnSignOut.setVisibility(View.GONE);
                    txtStatus.setVisibility(View.VISIBLE);
                    txtStatus.setText("Error: " + (state.error != null ? state.error : "Login fail!!"));
                    break;
            }
        });

        // Thiết lập sự kiện click cho nút đăng nhập.
        btnPatSignIn.setOnClickListener(v -> {
            String pat = inputPat.getText().toString().trim();
            String identifier = inputUserNameOrEmail.getText().toString().trim();

            if (pat.isEmpty()) {
                txtStatus.setVisibility(View.VISIBLE);
                txtStatus.setText("Please enter your PAT");
                return;
            }
            // Gọi phương thức trong ViewModel để xử lý logic đăng nhập.
            viewModel.signInWithPat(pat, identifier);
        });

        // Thiết lập sự kiện click cho nút đăng xuất.
        btnSignOut.setOnClickListener(v -> viewModel.signOut());
    }
}