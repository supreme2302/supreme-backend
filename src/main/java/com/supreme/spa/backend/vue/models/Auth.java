package com.supreme.spa.backend.vue.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.beans.Transient;


//@Getter
//@Setter
//@NoArgsConstructor
public class Auth {
    private int id;
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @JsonCreator
    public Auth(@JsonProperty("id") int id,
                @JsonProperty("username") String username,
                @JsonProperty("email") String email,
                @JsonProperty("password") String password,
                @JsonProperty("confirmPassword") String confirmPassword) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = password;
    }

    public Auth() {}

    public boolean checkConfirm(String password, String confirm) {
        return password.equals(confirm);
    }

    public boolean checkPassword(String pass) {
        return passwordEncoder.matches(this.password, pass);
    }

    public void saltHash() {
        this.password = passwordEncoder.encode(password);
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

    @Transient
    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @Transient
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
