package ru.yandex.practicum.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.service.FilesService;

import java.io.IOException;

@RestController
@RequestMapping("/posts")
public class FilesController {

    private final FilesService filesService;

    public FilesController(FilesService filesService) {
        this.filesService = filesService;
    }

    // POST эндпоинт для загрузки файла
    @PostMapping("/{id}/image")
    public ResponseEntity<Void> uploadImageById(@PathVariable("id") long id,@RequestParam("file") MultipartFile image) throws IOException {
        filesService.saveImageByPostId(id, image);
        return ResponseEntity.ok().build();
    }

    // GET эндпоинт для скачивания файла
    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> getImageById(@PathVariable("id") long id) throws IOException {
        Resource file = filesService.getImageByPostId(id);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
    }

}