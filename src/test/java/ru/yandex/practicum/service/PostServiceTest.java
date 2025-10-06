package ru.yandex.practicum.service;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.yandex.practicum.TestWebConfiguration;
import ru.yandex.practicum.configuration.ContentPaths;
import ru.yandex.practicum.configuration.DataSourceConfiguration;
import ru.yandex.practicum.exception.DataNotFoundException;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.model.PostPage;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.repository.JdbcNativeCommentRepository;
import ru.yandex.practicum.repository.JdbcNativePostRepository;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.util.DataUtil;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, PostService.class, CommentService.class,
        JdbcNativePostRepository.class, JdbcNativeCommentRepository.class, FilesService.class})
@TestPropertySource(locations = "classpath:application.properties")
@Sql(scripts = "/sql/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class PostServiceTest {
    @Autowired
    private FilesService filesService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private PostService postService;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @MockitoBean
    private ContentPaths paths;

    @BeforeEach
    public void beforeEach() throws URISyntaxException {
        Mockito.doReturn(Paths.get(getClass().getClassLoader().getResource("uploads").toURI()).toString())
                .when(paths)
                .getImagePathStr();
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    public void testGetAllPostsPage() {
        PostPage postPage = postService.getPostPage(null, 1, 3);

        Assertions.assertThat(postPage.getPosts().size()).isEqualTo(3);
        Assertions.assertThat(postPage.getLastPage()).isEqualTo(1);
        Assertions.assertThat(postPage.isHasPrev()).isFalse();
        Assertions.assertThat(postPage.isHasNext()).isTrue();
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    public void testGetPostById() {
        Post post = postService.getById(1L);

        Assertions.assertThat(post)
                .usingRecursiveComparison()
                .isEqualTo(DataUtil.getPost1());
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    public void testGetPostByIdNotFound() {
        Assertions.assertThatThrownBy(() -> postService.getById(999L))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql"})
    public void testCreateNewPost() {
        postService.create(DataUtil.getNewPost1());
        Optional<Post> foundPost = postRepository.getById(1L);

        Assertions.assertThat(foundPost)
                .isPresent()
                .get()
                .extracting(Post::getTitle)
                .isEqualTo("NewTitle");
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    public void testUpdatePost() {
        postService.update(1L, DataUtil.getNewPost1());
        Assertions.assertThat(postRepository.getById(1L))
                .isPresent()
                .get()
                .extracting(Post::getTitle, Post::getText, Post::getTags)
                .containsExactly("NewTitle", "NewText", List.of("tag1", "tag2"));
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    public void testDeletePost() {
        postService.delete(2L);
        Assertions.assertThat(postRepository.getById(2L))
                .isNotPresent();
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    public void addCommentPost() {
        final long postId = 1L;
        final String commentText = "New test comment";
        commentService.add(postId, new Comment(null ,commentText, postId));

        Assertions.assertThat(commentRepository.findAllByPostId(postId))
                .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration.builder()
                        .withIgnoredFields("id")
                        .build())
                .contains(new Comment(null, commentText, postId));
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    public void testEditCommentPost() {
        final long commentId = 1L;
        final String newCommentText = "Edited test comment";
        commentService.edit(1L, new Comment(commentId, newCommentText, 1L));

        Optional<Comment> actualComment = commentRepository.getById(commentId);
        Assertions.assertThat(actualComment).isPresent();
        Assertions.assertThat(actualComment.get())
                .usingRecursiveComparison(RecursiveComparisonConfiguration.builder()
                        .withIgnoredFields("postId")
                        .build())
                .isEqualTo(new Comment(commentId, newCommentText, null));
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    public void deleteCommentToPost() {
        final long commentId = 1L;
        postService.delete(commentId);

        Optional<Comment> actualComment = commentRepository.getById(commentId);
        Assertions.assertThat(actualComment).isNotPresent();;
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    public void testGetPostImageByPostId() throws IOException {
        Resource image = filesService.getImageByPostId(3);
        Assertions.assertThat(image)
                .isNotNull();
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    public void testGetPostImageByPostIdNotFound() {
        Assertions.assertThatThrownBy(() -> filesService.getImageByPostId(999L))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @Sql(scripts = {"/sql/clear.sql", "/sql/insert.sql"})
    public void testAddLikePost() {
        postService.like(1L);

        Assertions.assertThat(postRepository.getById(1L))
                .isPresent()
                .get()
                .extracting(Post::getLikesCount)
                .isEqualTo(2);
    }
}
