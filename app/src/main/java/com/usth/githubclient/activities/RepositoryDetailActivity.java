package com.usth.githubclient.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.usth.githubclient.R;
import com.usth.githubclient.data.remote.ApiClient;
import com.usth.githubclient.data.remote.GithubApiService;
import com.usth.githubclient.data.remote.dto.RepoDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RepositoryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REPO_OWNER = "repo_owner";
    public static final String EXTRA_REPO_NAME = "repo_name";

    private TextView repoFullName, repoDescription;
    private WebView readmeWebView;
    private MaterialButton btnViewOnGithub, btnStar;
    private GithubApiService apiService;

    private String owner;
    private String name;
    private boolean isStarred = false; // Biến để theo dõi trạng thái star

    public static Intent newIntent(Context context, String owner, String name) {
        Intent intent = new Intent(context, RepositoryDetailActivity.class);
        intent.putExtra(EXTRA_REPO_OWNER, owner);
        intent.putExtra(EXTRA_REPO_NAME, name);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repository_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        repoFullName = findViewById(R.id.repo_full_name);
        repoDescription = findViewById(R.id.repo_description);
        readmeWebView = findViewById(R.id.readme_webview);
        btnViewOnGithub = findViewById(R.id.btn_view_on_github);
        btnStar = findViewById(R.id.btn_star); // Ánh xạ nút Star

        apiService = new ApiClient().createService(GithubApiService.class);

        owner = getIntent().getStringExtra(EXTRA_REPO_OWNER);
        name = getIntent().getStringExtra(EXTRA_REPO_NAME);

        if (owner != null && name != null) {
            getSupportActionBar().setTitle(name);
            loadRepositoryDetails(owner, name);
            checkIfRepoIsStarred(); // Kiểm tra trạng thái star ban đầu
        }

        // Thêm sự kiện click cho nút Star
        btnStar.setOnClickListener(v -> {
            if (isStarred) {
                unstarRepo();
            } else {
                starRepo();
            }
        });
    }

    // Hàm kiểm tra repo đã được star chưa
    private void checkIfRepoIsStarred() {
        apiService.isRepoStarred(owner, name).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // API trả về 204 nghĩa là đã star
                isStarred = response.code() == 204;
                updateStarButtonUI();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Lỗi mạng, không làm gì cả
            }
        });
    }

    // Hàm gọi API để star
    private void starRepo() {
        apiService.starRepo(owner, name).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isStarred = true;
                    updateStarButtonUI();
                    Toast.makeText(RepositoryDetailActivity.this, "Starred!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    // Hàm gọi API để unstar
    private void unstarRepo() {
        apiService.unstarRepo(owner, name).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isStarred = false;
                    updateStarButtonUI();
                    Toast.makeText(RepositoryDetailActivity.this, "Unstarred!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    // Hàm cập nhật giao diện nút Star
    private void updateStarButtonUI() {
        if (isStarred) {
            btnStar.setText(R.string.repo_unstar);
            btnStar.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_star_filled));
        } else {
            btnStar.setText(R.string.repo_star);
            btnStar.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_star_outline));
        }
    }

    private void loadRepositoryDetails(String owner, String name) {
        apiService.getRepository(owner, name).enqueue(new Callback<RepoDto>() {
            @Override
            public void onResponse(Call<RepoDto> call, Response<RepoDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RepoDto repo = response.body();
                    repoFullName.setText(repo.getFullName());
                    repoDescription.setText(repo.getDescription());
                    btnViewOnGithub.setOnClickListener(v -> {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(repo.getHtmlUrl()));
                        startActivity(browserIntent);
                    });
                } else {
                    Toast.makeText(RepositoryDetailActivity.this, "Failed to load repo details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RepoDto> call, Throwable t) {
                Toast.makeText(RepositoryDetailActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}