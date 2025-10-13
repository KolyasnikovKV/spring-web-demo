package ru.yandex.practicum.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    List<Post> findAllPaging(int offset, int amount);
    List<Post> findAllPaging(String search, int offset, int amount);

    Optional<Post> getById(Long id);
    Post save(Post post) throws JsonProcessingException;
    void deleteById(long id);

    Optional<String> findImageNameById(long id);
    void updateImageNameById(long id, String fileName);

    Optional<Integer> addLike(long id);

    void addCommentCountById(long id);
    void removeCommentCountById(long id);
}
