package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.repository.PostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public List<Comment> get(long postId) {
        return commentRepository.findAllByPostId(postId);
    }

    @Transactional
    public Comment add(long postId, Comment newComment) {
        postRepository.addCommentCountById(postId);
        return commentRepository.addByPostId(postId, newComment);
    }

    public Comment edit(long postId, Comment editComment) {
        commentRepository.save(postId, editComment);
        return editComment;
    }

    @Transactional
    public void delete(long postId, long commentId) {
        postRepository.removeCommentCountById(postId);
        commentRepository.deleteById(commentId);
    }



}
