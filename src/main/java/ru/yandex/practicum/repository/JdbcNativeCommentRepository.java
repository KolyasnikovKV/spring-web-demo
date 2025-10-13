package ru.yandex.practicum.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Comment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcNativeCommentRepository implements CommentRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Comment> findAllByPostId(long postId) {
        return jdbcTemplate.query("SELECT id, text FROM comments WHERE post_id = ?", rs -> {
            List<Comment> comments = new ArrayList<>();
            while (rs.next()) {
                comments.add(new Comment(
                       rs.getLong("id"),
                       rs.getString("text"),
                       postId
                ));
            }
            return comments;
        }, postId);
    }

    @Override
    public Comment addByPostId(long postId, Comment newComment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO comments (text, post_id) VALUES (?, ?)",
                    new String[] { "id" }
            );
            ps.setString(1, newComment.getText());
            ps.setLong(2, postId);
            return ps;
        }, keyHolder);
        newComment.setPostId(keyHolder.getKey().longValue());
        return newComment;
    }

    @Override
    public void save(long postId, Comment ediComment) {
        jdbcTemplate.update("UPDATE comments SET text = ? WHERE id = ?", ediComment.getText(), ediComment.getId());
    }

    @Override
    public Optional<Comment> getById(long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT id, text, post_id FROM comments WHERE id = ?",
                    (rs, rowNum) -> mapToComment(rs), id));
        } catch (EmptyResultDataAccessException e) {
            log.warn("Comment with id {} not found", id);
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(long commentId) {
        jdbcTemplate.update("DELETE FROM comments WHERE id = ?", commentId);
    }

    @Override
    public void deleteByPostId(long postId) {
        jdbcTemplate.update("DELETE FROM comments WHERE post_id = ?", postId);
    }

    private Comment mapToComment(ResultSet resultSet) throws SQLException {
        return new Comment(
                resultSet.getLong("id"),
                resultSet.getString("text"),
                resultSet.getLong("post_id")
        );
    }
}
