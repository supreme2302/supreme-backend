package com.supreme.spa.backend.vue.models;

import lombok.Data;

@Data
public class Comment {
    private int id;
    private int toUserId;
    private String fromUsername;
    private String fromEmail;
    private String commentVal;
    private int rating;
}
