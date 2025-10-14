package com.usth.githubclient.viewmodel;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usth.githubclient.data.remote.dto.EventDto;
import com.usth.githubclient.data.remote.dto.UserDto;
import com.usth.githubclient.data.repository.UserRepository;
import com.usth.githubclient.di.ServiceLocator;
import com.usth.githubclient.domain.mapper.UserMapper;
import com.usth.githubclient.domain.model.ContributionDataEntry;
import com.usth.githubclient.domain.model.GitHubUserProfileDataEntry;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import retrofit2.Response;

public class UserViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final ExecutorService executorService;
    private final UserMapper userMapper;
    private final MutableLiveData<UserUiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<List<ContributionDataEntry>> contributions = new MutableLiveData<>();

    public UserViewModel() {
        this.userRepository = ServiceLocator.getInstance().userRepository();
        this.executorService = ServiceLocator.getInstance().getExecutorService();
        this.userMapper = ServiceLocator.getInstance().userMapper();
    }

    public LiveData<UserUiState> getUiState() {
        return uiState;
    }

    public LiveData<List<ContributionDataEntry>> getContributions() {
        return contributions;
    }

    public void loadUserProfile(@Nullable String username) {
        uiState.postValue(new UserUiState(true, null, null));
        executorService.execute(() -> {
            try {
                // Correctly fetch UserDto first
                Response<UserDto> response = (username == null)
                        ? userRepository.authenticate().execute()
                        : userRepository.getUser(username).execute();

                if (response.isSuccessful() && response.body() != null) {
                    // Map UserDto to GitHubUserProfileDataEntry
                    GitHubUserProfileDataEntry profile = userMapper.map(response.body());
                    uiState.postValue(new UserUiState(false, null, profile));
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    uiState.postValue(new UserUiState(false, error, null));
                }
            } catch (IOException e) {
                uiState.postValue(new UserUiState(false, "Network error", null));
            }
        });
    }

    public void loadContributions(String username) {
        executorService.execute(() -> {
            try {
                List<EventDto> allEventsInMonth = new ArrayList<>();
                int currentPage = 1;
                boolean keepFetching = true;
                int currentMonth = Calendar.getInstance().get(Calendar.MONTH);

                while (keepFetching && currentPage <= 10) { // Giới hạn 10 trang để tránh vòng lặp vô tận
                    Response<List<EventDto>> response = userRepository.getUserEvents(username, currentPage, 100).execute();

                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        for (EventDto event : response.body()) {
                            if (event.getCreatedAt() == null) continue;

                            Instant instant = Instant.parse(event.getCreatedAt());
                            Calendar eventCal = Calendar.getInstance();
                            eventCal.setTime(Date.from(instant));

                            if (eventCal.get(Calendar.MONTH) == currentMonth) {
                                allEventsInMonth.add(event);
                            } else {
                                // Đã gặp sự kiện của tháng trước, dừng lại
                                keepFetching = false;
                                break;
                            }
                        }
                        currentPage++;
                    } else {
                        // Không còn dữ liệu hoặc có lỗi, dừng lại
                        keepFetching = false;
                    }
                }

                if (!allEventsInMonth.isEmpty()) {
                    List<ContributionDataEntry> processedContributions = processEvents(allEventsInMonth);
                    contributions.postValue(processedContributions);
                } else {
                    // Post một danh sách rỗng nếu không có contribution nào trong tháng
                    contributions.postValue(new ArrayList<>());
                }
            } catch (IOException e) {
                contributions.postValue(new ArrayList<>()); // Post danh sách rỗng khi có lỗi mạng
            }
        });
    }
    @SuppressLint("NewApi")
    private List<ContributionDataEntry> processEvents(List<EventDto> events) {
        Map<Integer, ContributionDataEntry> contributionsMap = new HashMap<>();
        Calendar cal = Calendar.getInstance();

        // Lấy tháng và năm hiện tại để làm mốc so sánh
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        for (EventDto event : events) {
            if (event.getCreatedAt() == null) continue; // Bỏ qua nếu không có ngày tháng

            Instant instant = Instant.parse(event.getCreatedAt());
            Date date = Date.from(instant);

            Calendar eventCal = Calendar.getInstance();
            eventCal.setTime(date);

            int eventMonth = eventCal.get(Calendar.MONTH);
            int eventYear = eventCal.get(Calendar.YEAR);

            // Chỉ xử lý các sự kiện xảy ra trong tháng và năm hiện tại
            if (eventMonth == currentMonth && eventYear == currentYear) {
                int dayOfMonth = eventCal.get(Calendar.DAY_OF_MONTH);

                ContributionDataEntry entry = contributionsMap.get(dayOfMonth);
                if (entry == null) {
                    Calendar dayCal = Calendar.getInstance();
                    dayCal.setTime(eventCal.getTime());
                    entry = new ContributionDataEntry(dayCal, 1);
                    contributionsMap.put(dayOfMonth, entry);
                } else {
                    entry.incrementCount();
                }
            }
        }
        return new ArrayList<>(contributionsMap.values());
    }
    public static class UserUiState {
        private final boolean isLoading;
        private final String errorMessage;
        private final GitHubUserProfileDataEntry profile;

        public UserUiState(boolean isLoading, @Nullable String errorMessage, @Nullable GitHubUserProfileDataEntry profile) {
            this.isLoading = isLoading;
            this.errorMessage = errorMessage;
            this.profile = profile;
        }

        public boolean isLoading() {
            return isLoading;
        }

        @Nullable
        public String getErrorMessage() {
            return errorMessage;
        }

        @Nullable
        public GitHubUserProfileDataEntry getProfile() {
            return profile;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserUiState that = (UserUiState) o;
            return isLoading == that.isLoading &&
                    Objects.equals(errorMessage, that.errorMessage) &&
                    Objects.equals(profile, that.profile);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isLoading, errorMessage, profile);
        }

        @NonNull
        @Override
        public String toString() {
            return "UserUiState{" +
                    "isLoading=" + isLoading +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", profile=" + profile +
                    '}';
        }
    }
}