package com.supreme.spa.backend.vue.controller;

import com.supreme.spa.backend.vue.models.Message;
import com.supreme.spa.backend.vue.models.UserStatus;
import com.supreme.spa.backend.vue.services.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static com.supreme.spa.backend.vue.services.UserService.PATH_AVATARS_FOLDER;


@RestController
@RequestMapping("/media")
public class MediaController {

    private final MediaService mediaService;

    @Autowired
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/chava")
    public ResponseEntity changeAva(@RequestParam("image") MultipartFile file,
                                    @RequestParam("id") int id,
                                    HttpSession session) {
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not Found");
        }
        String link;
        try {
            link = mediaService.store(file, id);
        } catch (IOException except) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    "Unexpected Error");
        }
        return ResponseEntity.status(HttpStatus.OK).body(link);
    }

    @GetMapping(path = "/{imageName}")
    public ResponseEntity getImageByEmail(@PathVariable("imageName") String imageName) throws IOException {
        BufferedImage file;
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        try {
//            file = mediaService.getLink(id);
            file = ImageIO.read(new File(PATH_AVATARS_FOLDER + imageName));
        } catch (IIOException e) {
            file = ImageIO.read(new File(PATH_AVATARS_FOLDER + "default.jpg"));
        }

        ImageIO.write(file, "png", bao);
        return ResponseEntity.ok(bao.toByteArray());
    }
}
