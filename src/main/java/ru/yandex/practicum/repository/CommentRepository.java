package ru.yandex.practicum.repository;


import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository {
    List<Comment> findAllByPostId(long postId);
    void deleteById(long commentId);
    void deleteByPostId(long postId);
    void save(long postId, Comment comment);
    Comment addByPostId(long postId, Comment newComment);
    Optional<Comment> getById(long id);
}
