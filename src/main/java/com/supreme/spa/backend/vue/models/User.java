package com.supreme.spa.backend.vue.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
public class User {
    private int id;
    private String username;
    private String email;
    private String phone;
    private String about;
    private String[] skills;
    private String[] genres;
    private Boolean onpage;
    private float rating;
    private String image;

//    @JsonCreator
//    public User(
//            @JsonProperty("id") int id,
//            @JsonProperty("fromUsername") String fromUsername,
//            @JsonProperty("email") String email,
//            @JsonProperty("password") String password,
//            @JsonProperty("confirmPassword") String confirmPassword,
//            @JsonProperty("phone") String phone,
//            @JsonProperty("about") String about,
//            @JsonProperty("skills") String skills,
//            @JsonProperty("onpage") Boolean onpage) {
//        this.id = id;
//        this.fromUsername = fromUsername;
//        this.email = email;
//        this.password = password;
//        this.confirmPassword = confirmPassword;
//        this.phone = phone;
//        this.about = about;
//        this.skills = skills;
//        this.onpage = onpage;
//    }
//
//    public User() {
//    }


//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public String getFromUsername() {
//        return fromUsername;
//    }
//
//    public void setFromUsername(String fromUsername) {
//        this.fromUsername = fromUsername;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public Boolean checkConfirm(String password, String confirm) {
//        return password.equals(confirm);
//    }
//
//    public String getPhone() {
//        return phone;
//    }
//
//    public void setPhone(String phone) {
//        this.phone = phone;
//    }
//
//    public String getAbout() {
//        return about;
//    }
//
//    public void setAbout(String about) {
//        this.about = about;
//    }
//
//    public Boolean getOnpage() {
//        return onpage;
//    }
//
//    public void setOnpage(Boolean onpage) {
//        this.onpage = onpage;
//    }
//
//    public String[] getSkills() {
//        return skills;
//    }
//
//    public void setSkills(String[] skills) {
//        this.skills = skills;
//    }
}
