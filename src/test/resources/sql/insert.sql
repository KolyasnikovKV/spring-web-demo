insert into posts (title, image, text, likes_count, tags)
values ('Title1', 'images1.jpg', 'text1', 0, '["tag1", "tag2"]'),
       ('Title2', 'images2.jpg', 'text2', 2, '["tag1", "tag2"]'),
       ('Title3', 'images3.jpg', 'text3', 3, '["tag1", "tag3"]');


insert into comments (post_id, text)
values (1, 'Comment11'),
       (1, 'Comment12'),
       (2, 'Comment21'),
       (2, 'Comment22'),
       (2, 'Comment23');
