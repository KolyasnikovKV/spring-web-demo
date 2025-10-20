package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerIntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        // Чистим и наполняем БД перед каждым тестом
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("""
                    INSERT INTO posts (id, title, text, likes_count)
                    VALUES (1,'Title1','Text1',3)
                """);
        jdbcTemplate.execute("""
                    INSERT INTO posts (id, title, text, likes_count)
                    VALUES (2,'Title2','Text2',5)
                """);
    }

    @Test
    void getPosts_returnsJsonArray() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$['posts']", hasSize(2)))
                .andExpect(jsonPath("$['posts'][0].title").value("Title1"))
                .andExpect(jsonPath("$['posts'][0].text").value("Text1"));
    }

    @Test
    void createPost_acceptsJson_andPersists() throws Exception {
        String json = """
                  {"id":null,"title":"title3","text":"text3","likes_count":0}
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("title3"));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['posts']", hasSize(3)));
    }

    @Test
    void deletePost_noContent() throws Exception {
        mockMvc.perform(delete("/api/posts/1/delete"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['posts']", hasSize(1)));
    }

    @Test
    void uploadAndDownloadImage_error() throws Exception {
        byte[] jpgStub = new byte[]{(byte) 137, 80, 78, 71};
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "application/octet-stream", jpgStub);

        mockMvc.perform(multipart("/posts/{id}/image", 1L).file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAndDownloadImage_success() throws Exception {
        byte[] jpgStub = new byte[]{(byte) 137, 80, 78, 71};
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", jpgStub);

        mockMvc.perform(multipart("/posts/{id}/image", 1L).file(file))
                .andExpect(status().isOk());

        mockMvc.perform(get("/posts/{id}/image", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(jpgStub));
    }
}
