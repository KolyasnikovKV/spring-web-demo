package ru.yandex.practicum.util;

import ru.yandex.practicum.model.Post;

import java.util.List;

public class DataUtil {

    static public Post getPost1() {
        return new Post()
                .setId(1L)
                .setTitle("Title1")
                .setText("text1")
                .setLikesCount(1)
                .setTags(List.of("tag1", "tag2"));
    }

    static public Post getNewPost1() {
        return new Post()
                .setTitle("NewTitle")
                .setText("NewText")
                .setLikesCount(1)
                .setTags(List.of("tag1", "tag2"));
    }

    static public Post getPost2() {
        return new Post()
                .setId(2L)
                .setTitle("Title2")
                .setText("text2")
                .setLikesCount(2)
                .setTags(List.of("tag2", "tag3"));
    }

}
