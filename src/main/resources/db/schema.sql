-- H2 db schema and data

CREATE TABLE IF NOT EXISTS posts(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  title VARCHAR(255) NOT NULL,
  text CLOB NOT NULL,
  image_path VARCHAR(255) NOT NULL,
  likes INTEGER DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS comments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  text CLOB NOT NULL,
  post_id BIGINT NOT NULL REFERENCES posts(id)
);

CREATE TABLE IF NOT EXISTS tags (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS post_tag (
    post_id BIGINT NOT NULL REFERENCES posts(id),
    tag_id BIGINT NOT NULL REFERENCES tags(id),
    PRIMARY KEY (post_id, tag_id)
);

-----------------------------------------------------------------

INSERT INTO tags (name) VALUES ('SCIENCE');
INSERT INTO tags (name) VALUES ('ART');
INSERT INTO tags (name) VALUES ('POLITICS');
INSERT INTO tags (name) VALUES ('RELIGION');
---

INSERT INTO posts (title, text, image_path) VALUES('First', 'First text', 'x_image.png');
INSERT INTO posts (title, text, image_path) VALUES('Second', 'Second text', 'x_image.png');
INSERT INTO posts (title, text, image_path) VALUES('Third', 'Third text', 'x_image.png');

---

INSERT INTO comments (text, post_id)
SELECT 'First comment of first post', id
FROM posts WHERE title = 'First';

INSERT INTO comments (text, post_id)
SELECT 'Second comment of first post', id
FROM posts WHERE title = 'First';

INSERT INTO comments (text, post_id)
SELECT 'Third comment of first post', id
FROM posts WHERE title = 'First';

INSERT INTO comments (text, post_id)
SELECT 'First comment of second post', id
FROM posts WHERE title = 'Second';

---

INSERT INTO post_tag (post_id, tag_id)
SELECT
    (SELECT id FROM posts WHERE title = 'First'),
    (SELECT id FROM tags WHERE name = 'SCIENCE');

INSERT INTO post_tag (post_id, tag_id)
SELECT
    (SELECT id FROM posts WHERE title = 'First'),
    (SELECT id FROM tags WHERE name = 'ART');

INSERT INTO post_tag (post_id, tag_id)
SELECT
    (SELECT id FROM posts WHERE title = 'First'),
    (SELECT id FROM tags WHERE name = 'RELIGION');

INSERT INTO post_tag (post_id, tag_id)
SELECT
    (SELECT id FROM posts WHERE title = 'Third'),
    (SELECT id FROM tags WHERE name = 'POLITICS');
