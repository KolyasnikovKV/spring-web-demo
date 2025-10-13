package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.configuration.ContentPaths;
import ru.yandex.practicum.exception.DataNotFoundException;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.model.PostPage;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.repository.PostRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ContentPaths paths;

    @Transactional(readOnly = true)
    public PostPage getPostPage(String search, int pageNumber, int pageSize) {
        int offset = pageSize * (pageNumber - 1);
        List<Post> posts = StringUtils.hasLength(search)
                ? postRepository.findAllPaging(search, offset, pageSize)
                : postRepository.findAllPaging(offset, pageSize);

        PostPage postPage = new PostPage();
        postPage.setLastPage(pageNumber);
        postPage.setHasPrev(pageNumber > 1);
        postPage.setHasNext(pageSize == posts.size());
        postPage.setPosts(posts);

        return postPage;
    }

    public Post create(Post newPost) {
        String fileName = UUID.randomUUID().toString();
        try {
            return postRepository.save(newPost);
        } catch (IOException e) {
            log.error("Error while saving image: {}", fileName, e);
            throw new RuntimeException(e);
        }
    }

    public int like(long id) {
        return postRepository.addLike(id).orElse(0);
    }

    public Post update(long id, Post newPost) {
        Optional<Post> foundPost = postRepository.getById(id);
        foundPost.ifPresent(value -> {
            value.setTitle(newPost.getTitle());
            value.setText(newPost.getText());
            value.setTags(newPost.getTags());
            try {
                postRepository.save(value);
            } catch (IOException e) {
                log.error("Error while saving post: {}", newPost, e);
                throw new RuntimeException(e);
            }
        });
        return foundPost.orElse(null);
    }

    @Transactional
    public void delete(long id) {
        Optional<String> imageName = postRepository.findImageNameById(id);
        commentRepository.deleteByPostId(id);
        postRepository.deleteById(id);

        imageName.ifPresent(fileName -> {
            try {
                Files.deleteIfExists(Paths.get(paths.getImagePathStr(), fileName));
            } catch (IOException e) {
                log.error("Error while deleting image: {}", fileName, e);
                throw new RuntimeException(e);
            }
            log.info("Image {} deleted", fileName);
        });
        ResponseEntity.ok();
    }

    public Post getById(Long id) {
        return postRepository.getById(id)
                .orElseThrow(() -> new DataNotFoundException("Post with id %d not found".formatted(id)));
    }

}
