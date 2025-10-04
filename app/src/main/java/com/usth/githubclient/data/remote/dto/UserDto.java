package com.usth.githubclient.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Lớp biểu diễn thô (raw representation) của một người dùng GitHub như được trả về bởi public REST API.
 * Các thuộc tính trong lớp này phải khớp chính xác với các khóa (keys) trong đối tượng JSON
 * mà API trả về. Lớp này không chứa bất kỳ logic nghiệp vụ nào.
 */
public final class UserDto {

    // @SerializedName: Annotation này của thư viện Gson nói rằng:
    // "Hãy tìm khóa 'id' trong JSON và gán giá trị của nó cho thuộc tính 'id' này".
    // Điều này rất hữu ích khi bạn muốn đặt tên thuộc tính trong Java khác với
    // tên khóa trong JSON (ví dụ: "avatar_url" trong JSON thành "avatarUrl" trong Java).
    @SerializedName("id")
    private long id;

    @SerializedName("login")
    private String login;

    @SerializedName("name")
    private String name;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("bio")
    private String bio;

    @SerializedName("company")
    private String company;

    @SerializedName("blog")
    private String blog;

    @SerializedName("email")
    private String email;

    @SerializedName("location")
    private String location;

    @SerializedName("public_repos")
    private int publicRepos;

    @SerializedName("followers")
    private int followers;

    @SerializedName("following")
    private int following;

    @SerializedName("html_url")
    private String htmlUrl;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    /**
     * Constructor rỗng (không có tham số).
     * Đây là yêu cầu bắt buộc để thư viện Gson có thể tự tạo ra các thể hiện (instances)
     * của lớp này khi phân tích (parse) JSON.
     */
    public UserDto() {
    }

    // --- CÁC PHƯƠNG THỨC GETTER ---
    // Cung cấp cách để các lớp khác (như UserMapper) có thể truy cập vào các giá trị
    // private của đối tượng DTO này.

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public String getCompany() {
        return company;
    }

    public String getBlog() {
        return blog;
    }

    public String getEmail() {
        return email;
    }

    public String getLocation() {
        return location;
    }

    public int getPublicRepos() {
        return publicRepos;
    }

    public int getFollowers() {
        return followers;
    }

    public int getFollowing() {
        return following;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}