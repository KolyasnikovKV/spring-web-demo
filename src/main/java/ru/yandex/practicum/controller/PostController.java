package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.model.PostPage;
import ru.yandex.practicum.service.CommentService;
import ru.yandex.practicum.service.PostService;

import java.util.List;


@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PostPage getPosts(Model model,
                           @RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
                           @RequestParam(value = "pageSize", defaultValue = "5") int pageSize) {
        PostPage postPage = postService.getPostPage(search, pageNumber, pageSize);
        return postPage;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Post getPostById(@PathVariable("id") long id) {
        return postService.getById(id);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Post addPost(@RequestBody Post newPost) {
        return postService.create(newPost);
    }

    @PostMapping("/{id}/likes")
    public Integer likePost(@PathVariable("id") long id) {
        return postService.like(id);
    }

    @PutMapping(value ="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Post editPost(@PathVariable("id") long id,
                         @RequestBody Post newPost) {
        return postService.update(id, newPost);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deletePost(@PathVariable("id") long id) {
        postService.delete(id);
        return ResponseEntity.ok().build();
    }

     @GetMapping(value = "/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Comment> getComments(@PathVariable("id") long postId) {
        return commentService.get(postId);
    }

    @PostMapping(value = "/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public Comment addComment(@PathVariable("id") long postId, @RequestBody Comment newComment) {
        return commentService.add(postId, newComment);
    }

    @PutMapping(value = "/{id}/comments/{commentId}")
    public Comment editComment(@PathVariable("id") long postId, @RequestBody Comment editComment) {
        return commentService.edit(postId, editComment);
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("id") long postId,
                              @PathVariable("commentId") long commentId) {
        commentService.delete(postId, commentId);
        return ResponseEntity.ok().build();
    }
}
