insert into posts (title, text, image, tags, likes_count, comments_count)
values ('title1',
        'text1',
        'images1.jpg',
        '["tag1","tag2"]',
        1,
        2),
       ('title2',
        'text2',
        'images2.jpg',
        '["tag3","tag2"]',
        2,
        3),
       ('title3',
        'text3',
        'images3.jpg',
        '["tag1","tag3"]',
        0,
        0);


insert into comments (post_id, text)
values
    (1, 'comments11'),
    (1, 'comments12'),
    (2, 'comments21'),
    (2, 'comments22'),
    (2, 'comments23');
