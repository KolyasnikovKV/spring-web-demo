package ru.yandex.practicum.model;


import lombok.*;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
@ToString
public class Post {
    private Long id;
    private String title;
    private String text;
    private List<String> tags;
    private int commentsCount;
    private int likesCount;
}
