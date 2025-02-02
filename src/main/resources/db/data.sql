MERGE INTO tags (name) KEY(name) VALUES ('SCIENCE');
MERGE INTO tags (name) KEY(name) VALUES ('ART');
MERGE INTO tags (name) KEY(name) VALUES ('POLITICS');
MERGE INTO tags (name) KEY(name) VALUES ('RELIGION');
---

MERGE INTO posts (title, text, image_path) KEY(title) VALUES('First', 'First text', 'x_image.png');
MERGE INTO posts (title, text, image_path) KEY(title) VALUES('Second', 'Second text', 'x_image.png');
MERGE INTO posts (title, text, image_path) KEY(title) VALUES('Third', 'Third text', 'x_image.png');

---

MERGE INTO comments (text, post_id) KEY(text, post_id) VALUES ('First comment of first post', 1);
MERGE INTO comments (text, post_id) KEY(text, post_id) VALUES ('Second comment of first post', 1);
MERGE INTO comments (text, post_id) KEY(text, post_id) VALUES ('Third comment of first post', 1);

MERGE INTO comments (text, post_id) KEY(text, post_id) VALUES ('First comment of second post', 2);

---

MERGE INTO post_tag (post_id, tag_id) KEY (post_id, tag_id) VALUES (1, 1);
MERGE INTO post_tag (post_id, tag_id) KEY (post_id, tag_id) VALUES (1, 2);
MERGE INTO post_tag (post_id, tag_id) KEY (post_id, tag_id) VALUES (1, 4);

MERGE INTO post_tag (post_id, tag_id) KEY (post_id, tag_id) VALUES (3, 3);
