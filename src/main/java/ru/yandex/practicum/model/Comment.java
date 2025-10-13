package ru.yandex.practicum.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Comment {
    private Long id;
    private String text;
    private Long postId;
}
