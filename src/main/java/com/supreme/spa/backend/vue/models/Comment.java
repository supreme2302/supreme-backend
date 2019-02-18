package com.supreme.spa.backend.vue.models;

import lombok.Data;

@Data
public class Comment {
    private int id;
    private int userId;
    private String username;
    private String commentVal;
    private int rating;
}
