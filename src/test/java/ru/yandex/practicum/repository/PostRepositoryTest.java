package ru.yandex.practicum.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.configuration.ContentPaths;
import ru.yandex.practicum.controller.DatabaseTestConfig;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.util.DataUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@Import({DatabaseTestConfig.class, JdbcNativePostRepository.class})
@DataJdbcTest
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @MockitoBean
    private ContentPaths paths;

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    void findAllShouldReturnAllPosts() throws Exception {
        List<Post> posts = postRepository.findAllPaging(0,3);
        assertNotNull(posts);
        assertEquals(3, posts.size());
        Post first = posts.getFirst();
        assertEquals(1L, first.getId());
    }

   @Test
   @Sql(scripts = {"/sql/clear.sql"})
    void saveShouldAddPostToDatabase() throws JsonProcessingException {
        Post post = DataUtil.getNewPost1();
        postRepository.save(post);
        List<Post> all = postRepository.findAllPaging(0,3);;
        Post saved = all.stream().filter(u -> u.getId().equals(1L)).findFirst().orElse(null);
        assertNotNull(saved);
        assertEquals(post.getTitle(), saved.getTitle());
        assertEquals(post.getText(), saved.getText());
        assertEquals(post.getId(), saved.getId());
    }


    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    void deleteByIdshouldRemovePostFromDatabase() {
        postRepository.deleteById(3L);
        List<Post> posts = postRepository.findAllPaging(0,3);
        assertEquals(2, posts.size());
        assertTrue(posts.stream().noneMatch(u -> u.getId().equals(3L)));
    }


    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    void findImageById() {
        assertTrue(postRepository.findImageNameById(1L).isPresent());
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    void increaseLikeTest() {
        postRepository.addLike(1L);
        Optional<Post> post = postRepository.getById(1L);

        assertTrue(post.isPresent());
        assertEquals(1, post.orElse(new Post()).getLikesCount());

    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    void deletePostTest() {
        postRepository.deleteById(1L);
        Optional<Post> post = postRepository.getById(1L);
        assertFalse(post.isPresent());
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    void getPostByIdForEditOrDeleteTest() {
        Optional<Post> post = postRepository.getById(1L);
        assertTrue(post.isPresent());
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    void saveEditPostTest() throws JsonProcessingException {
        Post post = DataUtil.getPost1();
        post.setId(1L);
        post.setTitle("editTitle");
        postRepository.save(post);
        Optional<Post> postAfterSave = postRepository.getById(1L);
        assertNotNull(postAfterSave);
        assertEquals("editTitle", postAfterSave.orElseGet(() -> new Post()).getTitle());
    }
}
