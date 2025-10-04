package com.usth.githubclient.activities;

// Các import cần thiết
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.MaterialColors;
import com.usth.githubclient.R;
import com.usth.githubclient.adapters.ViewPagerAdapter;
import com.usth.githubclient.fragments.SearchReposFragment;
import com.usth.githubclient.fragments.SearchUsersFragment;

public class MainActivity extends AppCompatActivity {

    // Khai báo các thành phần giao diện chính.
    private ViewPager2 viewPager;
    private BottomNavigationView navView;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Thiết lập Toolbar làm ActionBar cho Activity này.
        setSupportActionBar(findViewById(R.id.toolbar));

        // Ánh xạ các view từ layout.
        viewPager = findViewById(R.id.view_pager);
        navView = findViewById(R.id.bottom_navigation);

        // Khởi tạo Adapter cho ViewPager2. Adapter này sẽ cung cấp các Fragment
        // cho từng trang (Home, Repositories).
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // Tắt tính năng vuốt ngang của ViewPager2. Điều này có nghĩa là việc chuyển trang
        // sẽ chỉ được điều khiển bởi việc nhấn vào các tab của BottomNavigationView.
        viewPager.setUserInputEnabled(false);

        // Gọi các phương thức để thiết lập việc đồng bộ và thanh tìm kiếm.
        setupNavigationSync();
        setupSearchView();
    }

    // Phương thức này thiết lập sự đồng bộ hóa giữa ViewPager2 và BottomNavigationView.
    private void setupNavigationSync() {
        // Lắng nghe sự kiện khi người dùng nhấn vào một tab trên BottomNavigationView.
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Nếu nhấn tab Home, chuyển ViewPager2 đến trang 0 (SearchUsersFragment).
                // 'false' ở cuối để không có hiệu ứng cuộn mượt.
                viewPager.setCurrentItem(0, false);
                return true; // Trả về true để đánh dấu là sự kiện đã được xử lý.
            } else if (itemId == R.id.nav_repositories) {
                // Nếu nhấn tab Repositories, chuyển ViewPager2 đến trang 1 (SearchReposFragment).
                viewPager.setCurrentItem(1, false);
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Nếu nhấn tab Profile, khởi chạy UserProfileActivity.
                Intent intent = UserProfileActivity.createIntent(this, null);
                startActivity(intent);
                return false; // Trả về false vì chúng ta không muốn chọn tab này,
                // nó chỉ đóng vai trò như một nút bấm.
            }
            return false;
        });

        // Lắng nghe sự kiện khi trang của ViewPager2 thay đổi.
        // (Mặc dù đã tắt vuốt, callback này vẫn hữu ích nếu bạn quyết định bật lại).
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Cập nhật tab được chọn trên BottomNavigationView để khớp với trang hiện tại.
                if (position == 1) {
                    navView.getMenu().findItem(R.id.nav_repositories).setChecked(true);
                } else {
                    navView.getMenu().findItem(R.id.nav_home).setChecked(true);
                }
                // Cập nhật gợi ý (hint) của thanh tìm kiếm cho phù hợp với trang.
                updateSearchHint();
            }
        });
    }

    // Phương thức này thiết lập logic cho SearchView.
    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.search_view);
        updateSearchHint(); // Cập nhật hint ban đầu.

        // Lắng nghe các sự kiện của SearchView.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // Được gọi khi người dùng nhấn nút tìm kiếm trên bàn phím.
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Lấy Fragment hiện tại đang hiển thị trong ViewPager2.
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                if (currentFragment instanceof SearchUsersFragment) {
                    // Nếu là fragment tìm người dùng, gọi phương thức submitQuery của nó.
                    ((SearchUsersFragment) currentFragment).submitQuery(query.trim());
                } else if (currentFragment instanceof SearchReposFragment) {
                    // Nếu là fragment tìm kho lưu trữ, gọi phương thức submitQuery của nó.
                    ((SearchReposFragment) currentFragment).submitQuery(query.trim());
                }
                return true; // Đã xử lý sự kiện.
            }

            // Được gọi mỗi khi văn bản trong thanh tìm kiếm thay đổi.
            @Override
            public boolean onQueryTextChange(String newText) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                // Nếu người dùng xóa hết văn bản...
                if (newText == null || newText.trim().isEmpty()) {
                    // ...thì hiển thị lại danh sách mặc định (followers hoặc my repos).
                    if (currentFragment instanceof SearchUsersFragment) {
                        ((SearchUsersFragment) currentFragment).showFollowers();
                    } else if (currentFragment instanceof SearchReposFragment) {
                        ((SearchReposFragment) currentFragment).showMyRepos();
                    }
                }
                return false; // Trả về false để cho phép SearchView thực hiện hành động mặc định.
            }
        });

        // --- Đoạn mã dưới đây dùng để tùy chỉnh giao diện của SearchView cho đẹp hơn ---
        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        if (searchPlate != null) {
            searchPlate.setBackground(null);
        }
        @SuppressLint("RestrictedApi")
        SearchView.SearchAutoComplete searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchText != null) {
            int onSurface = MaterialColors.getColor(searchView, com.google.android.material.R.attr.colorOnSurface);
            int onSurfaceVariant = MaterialColors.getColor(searchView, com.google.android.material.R.attr.colorOnSurfaceVariant);
            searchText.setTextColor(onSurface);
            searchText.setHintTextColor(onSurfaceVariant);
            searchText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        }
        int iconTint = MaterialColors.getColor(searchView, com.google.android.material.R.attr.colorOnSurfaceVariant);
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            ImageViewCompat.setImageTintList(searchIcon, ColorStateList.valueOf(iconTint));
        }
        ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeIcon != null) {
            ImageViewCompat.setImageTintList(closeIcon, ColorStateList.valueOf(iconTint));
        }
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
    }

    // Cập nhật gợi ý trong thanh tìm kiếm dựa trên trang hiện tại.
    private void updateSearchHint() {
        SearchView searchView = findViewById(R.id.search_view);
        if (viewPager.getCurrentItem() == 0) { // Vị trí 0 là Home (Users)
            searchView.setQueryHint(getString(R.string.search_hint));
        } else { // Vị trí 1 là Repositories
            searchView.setQueryHint(getString(R.string.search_repo_hint));
        }
    }
}