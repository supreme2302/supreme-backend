package com.supreme.spa.backend.vue.services;

import com.supreme.spa.backend.vue.util.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.rmi.CORBA.Util;
import java.io.File;
import java.io.IOException;

import static com.supreme.spa.backend.vue.services.UserService.PATH_AVATARS_FOLDER;

@Service
@Transactional
//@CrossOrigin(origins = {"http://localhost:8080", "https://supreme-spa.firebaseapp.com"}, allowCredentials = "true")
public class MediaService {

    private final JdbcTemplate jdbc;

    @Autowired
    public MediaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String getLink(int id) {
        return jdbc.queryForObject("SELECT link FROM picture WHERE client_id = ?", String.class, id);
    }

    public String store(MultipartFile file, int id) throws IOException {
        String link = String.valueOf(id) + "." + RandomString.getRandName() + ".jpg";
        File tosave = new File(PATH_AVATARS_FOLDER + link);
        file.transferTo(tosave);
        String sql = "UPDATE picture SET link = ? WHERE client_id = ?";
        jdbc.update(sql, link, id);
        return link;
    }
}
