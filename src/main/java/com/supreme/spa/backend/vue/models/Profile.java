package com.supreme.spa.backend.vue.models;

import lombok.Data;

@Data
public class Profile {
    private int id;
    private String phone;
    private boolean onpage;
    private String about;
    private String skills;
    private int userId;
    private float rating;
}
