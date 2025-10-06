package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.configuration.ContentPaths;
import ru.yandex.practicum.exception.DataNotFoundException;
import ru.yandex.practicum.repository.PostRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Service
public class FilesService {

    private final PostRepository postRepository;
    private final ContentPaths paths;

    public FilesService(PostRepository postRepository, ContentPaths paths) {
        this.postRepository = postRepository;
        this.paths = paths;

    }
    public void saveImageByPostId(@PathVariable("id") long id, @RequestParam("image") MultipartFile image) throws IOException {
        String filePath = upload(image);
        postRepository.updateImageNameById(id, filePath);
    }

    public Resource getImageByPostId(@PathVariable("id") long id) throws IOException {
        Optional<String> imageName = postRepository.findImageNameById(id);
        return imageName.map(this::download).orElseThrow(() ->new DataNotFoundException("Image not found"));

    }
    private String upload(MultipartFile file) {
        try {
            Path uploadDir = Paths.get(paths.getImagePathStr());
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Сохраняем файл
            Path filePath = uploadDir.resolve(file.getOriginalFilename());
            file.transferTo(filePath);

            return file.getOriginalFilename();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Resource download(String filename) {
        try {
            Path filePath = Paths.get(paths.getImagePathStr()).resolve(filename).normalize();
            byte[] content = Files.readAllBytes(filePath);

            return new ByteArrayResource(content);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
