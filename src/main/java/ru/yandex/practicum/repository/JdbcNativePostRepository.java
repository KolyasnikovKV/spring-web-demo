package ru.yandex.practicum.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.validation.Validator;
import ru.yandex.practicum.model.Post;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcNativePostRepository implements PostRepository {
    public static final String SPACE = " ";
    public static final String TAGS_CONDITION = "%TAGS_CONDITION%";
    public static final String TITLE_CONDITION = "%TITLE_CONDITION%";
    public static final char HASH_TAG = '#';
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public List<Post> findAllPaging(int offset, int amount) {
        return jdbcTemplate.query("SELECT p.id, p.title, p.image, p.text, p.likes_count, p.comments_count, p.tags FROM posts p ORDER BY id LIMIT ? OFFSET ?", rs -> {
            List<Post> posts = new ArrayList<>();
            while (rs.next()) {
                posts.add(mapToPost(rs));
            }
            return posts;
        }, amount, offset);
    }

    @Override
    public List<Post> findAllPaging(String search, int offset, int amount) {
        StringBuilder tagsCondition = new StringBuilder("true");
        StringBuilder titleCondition = new StringBuilder("true");

        String sqlSummary = "SELECT p.id, p.title, p.image, p.text, p.likes_count, p.comments_count, p.tags FROM posts p " +
                "WHERE %TAGS_CONDITION% AND %TITLE_CONDITION% ORDER BY id LIMIT ? OFFSET ?";

        Map<String, List<String>> mapCondition = Arrays.stream(search.strip().split(SPACE))
                .map(String::strip)
                .collect(Collectors.groupingBy((String key) -> key.charAt(0) == HASH_TAG ? TAGS_CONDITION : TITLE_CONDITION));

        if (mapCondition.containsKey(TAGS_CONDITION)){
            tagsCondition.setLength(0);
            tagsCondition.append(mapCondition.get(TAGS_CONDITION).stream()
                    .map(value -> value.substring(1))
                    .map(value -> " p.tags like '%" + value + "%' ")
                    .collect(Collectors.joining(" AND ")));
        }

        if (mapCondition.containsKey(TITLE_CONDITION)){
            titleCondition.setLength(0);
            titleCondition.append(" p.title like '%");
            titleCondition.append(String.join(SPACE, mapCondition.get(TITLE_CONDITION)));
            titleCondition.append("%' ");
        }

        sqlSummary = sqlSummary.replace(TAGS_CONDITION, tagsCondition.toString());
        sqlSummary = sqlSummary.replace(TITLE_CONDITION, titleCondition.toString());

        return jdbcTemplate.query(sqlSummary, rs -> {
            List<Post> posts = new ArrayList<>();
            while (rs.next()) {
                posts.add(mapToPost(rs));
            }
            return posts;
        }, amount, offset);
    }

    @Override
    public Optional<Post> getById(Long id) {
        final String sql = "SELECT p.id, p.title, p.image, p.text, p.comments_count, p.likes_count, p.tags FROM posts p WHERE p.id = ?";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> mapToPost(rs), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> findImageNameById(long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT image FROM posts WHERE id = ?",
                    (rs, rowNum) -> rs.getString("image"), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void updateImageNameById(long id, String imageName) {
        jdbcTemplate.update("UPDATE posts SET image = ? WHERE id = ?",
                imageName, id);
    }

   @Override
    public Post save(Post post) throws JsonProcessingException {
        return Objects.isNull(post.getId())
                ? saveNewPost(post)
                : updatePost(post);
    }

    private Post saveNewPost(Post newPost) throws JsonProcessingException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String tagsString = jsonMapper.writeValueAsString(newPost.getTags());

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO posts (title, text, likes_count, comments_count, tags) VALUES (?, ?, ?, ?, ?)",
                    new String[] { "id" }
            );
            ps.setString(1, newPost.getTitle());
            ps.setString(2, newPost.getText());
            ps.setInt(3, newPost.getLikesCount());
            ps.setInt(4, 0);
            ps.setString(5, tagsString);
            return ps;
        }, keyHolder);
        newPost.setId(keyHolder.getKey().longValue());
        return newPost;
    }

    public Post updatePost(Post post) throws JsonProcessingException {
        String tagsString = jsonMapper.writeValueAsString(post.getTags());
        jdbcTemplate.update("UPDATE posts SET title = ?, text = ?, tags = ? WHERE id = ?",
                post.getTitle(), post.getText(), tagsString, post.getId());

        return post;
    }

    @Override
    public Optional<Integer> addLike(long id) {
        jdbcTemplate.update("UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?", id);
        return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT likes_count FROM posts WHERE id = ?",
                    (rs, rowNum) -> rs.getInt("likes_count"), id));
    }

        @Override
    public void deleteById(long id) {
        jdbcTemplate.update("DELETE FROM posts WHERE id = ?", id);
    }

    private Post mapToPost(ResultSet resultSet) {

        try {
            String tagsDeserialized = jsonMapper.readValue(resultSet.getString("tags"), String.class);
            List<String> tagList = jsonMapper.readValue(tagsDeserialized, new TypeReference<List<String>>() {});

            return new Post(
                    resultSet.getLong("id"),
                    resultSet.getString("title"),
                    resultSet.getString("text"),
                    tagList,
                    resultSet.getInt("comments_count"),
                    resultSet.getInt("likes_count")
                    );
        } catch (SQLException e) {
            throw new RuntimeException("Unexpected SQL Exception", e);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("Unexpected Jackson mapping Exception", e);
        }
    }

    @Override
    public void addCommentCountById(long postId) {
        jdbcTemplate.update("UPDATE posts SET comments_count = comments_count + 1 WHERE id = ?",
                postId);
    }

    @Override
    public void removeCommentCountById(long postId) {
        jdbcTemplate.update("UPDATE posts SET comments_count = comments_count - 1 WHERE id = ?",
                postId);
    }
}
