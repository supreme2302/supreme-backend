package com.supreme.spa.backend.vue.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    private String phone;
    private String about;
    private String skills;
    private Boolean onpage;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @JsonCreator
    public User(
            @JsonProperty("id") int id,
            @JsonProperty("username") String username,
            @JsonProperty("email") String email,
            @JsonProperty("password") String password,
            @JsonProperty("confirmPassword") String confirmPassword,
            @JsonProperty("phone") String phone,
            @JsonProperty("about") String about,
            @JsonProperty("skills") String skills,
            @JsonProperty("onpage") Boolean onpage) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.phone = phone;
        this.about = about;
        this.skills = skills;
        this.onpage = onpage;
    }
    public User () {}
    public User (String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean checkPassword(String pass) {
        return passwordEncoder.matches(pass, this.password);
    }

    public void saltHash() {
        this.password = passwordEncoder.encode(password);
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Boolean checkConfirm(String password, String confirm) {
        return password.equals(confirm);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public Boolean getOnpage() {
        return onpage;
    }

    public void setOnpage(Boolean onpage) {
        this.onpage = onpage;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }
}
