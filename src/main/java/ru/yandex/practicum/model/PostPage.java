package ru.yandex.practicum.model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class PostPage {
    private List<Post> posts;
    private boolean hasPrev;
    private boolean hasNext;
    private int lastPage;

}
