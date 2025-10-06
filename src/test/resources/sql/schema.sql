CREATE TABLE IF NOT EXISTS POSTS
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR NOT NULL,
    text        VARCHAR NOT NULL,
    image       VARCHAR,
    likes_count INT     NOT NULL DEFAULT 0,
    comments_count INT  NOT NULL DEFAULT 0,
    tags        JSON    NOT NULL DEFAULT '[]'
);

CREATE TABLE IF NOT EXISTS comments
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    text    VARCHAR,
    post_id BIGINT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES POSTS (id)
);
