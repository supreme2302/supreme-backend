package com.supreme.spa.backend.vue.resource;

public class LocalStorage {
    public static String emptyProfile = "{" +
            "\"id\":3,"
            + "\"username\":\"existOneMore\","
            + "\"email\":\"exist2@e.ru\","
            + "\"phone\":\"\","
            + "\"about\":\"\","
            + "\"skills\":null,"
            + "\"genres\":null,"
            + "\"onpage\":false,"
            + "\"rating\":0.0"
            + "}";
    public static String fullProfile = "{"
            + "\"id\":2,"
            + "\"username\":\"userWithProfileAndSkillsAndGenres\","
            + "\"email\":\"exist3@e.ru\","
            + "\"phone\":\"9153456789\","
            + "\"about\":\"bio\","
            + "\"skills\":["
            + "\"drums\",\"bass\""
            + "],"
            + "\"genres\":["
            + "\"rock\",\"jazz\""
            + "],"
            + "\"onpage\":true,"
            + "\"rating\":0.0"
            + "}";
    public static String changedProfile = "{"
            + "\"id\":2,"
            + "\"username\":\"userWithProfileAndSkillsAndGenres\","
            + "\"email\":\"exist3@e.ru\","
            + "\"phone\":\"0000000000\","
            + "\"about\":\"i am user\","
            + "\"skills\":["
            + "\"guitar\",\"keyboards\""
            + "],"
            + "\"genres\":["
            + "\"pop\",\"metal\""
            + "],"
            + "\"onpage\":false,"
            + "\"rating\":0.0"
            + "}";

    public static String commentList = "" +
            "["
            + "{"
            + "\"id\":1,"
            + "\"toUserId\":1,"
            + "\"fromUsername\":\"userForComment0\","
            + "\"fromEmail\":\"userForComment0@e.ru\","
            + "\"commentVal\":\"Not bad - 0\","
            + "\"rating\":4"
            + "},"
            + "{"
            + "\"id\":2,"
            + "\"toUserId\":1,"
            + "\"fromUsername\":\"userForComment1\","
            + "\"fromEmail\":\"userForComment1@e.ru\","
            + "\"commentVal\":\"Not bad - 1\","
            + "\"rating\":5"
            + "}"
            + "]";
}
