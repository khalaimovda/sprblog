package com.github.khalaimovda.utils;

import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseUtils {

    public static void cleanData(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DELETE FROM comments;");
        jdbcTemplate.execute("DELETE FROM post_tag;");
        jdbcTemplate.execute("DELETE FROM tags;");
        jdbcTemplate.execute("DELETE FROM posts;");
    }

    public static void fillData(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("INSERT INTO tags (name) VALUES ('SCIENCE');");
        jdbcTemplate.execute("INSERT INTO tags (name) VALUES ('ART');");
        jdbcTemplate.execute("INSERT INTO tags (name) VALUES ('POLITICS');");
        jdbcTemplate.execute("INSERT INTO tags (name) VALUES ('RELIGION');");

        jdbcTemplate.execute(" INSERT INTO posts (title, text, image_path, likes) VALUES('First', 'First text', 'first_image.jpg', 3);");

        // Necessary to split the transaction to set different values in "created_at" column for correct sorting
        jdbcTemplate.execute("COMMIT");

        jdbcTemplate.execute(" INSERT INTO posts (title, text, image_path, likes) VALUES('Second', 'Second text', 'second_image.jpg', 15);");

        jdbcTemplate.execute(
            """
            INSERT INTO comments (text, post_id)
            SELECT 'First comment of first post', id
            FROM posts WHERE title = 'First';
            """
        );

        // Necessary to split the transaction to set different values in "created_at" column for correct sorting
        jdbcTemplate.execute("COMMIT");

        jdbcTemplate.execute(
            """
            INSERT INTO comments (text, post_id)
            SELECT 'Second comment of first post', id
            FROM posts WHERE title = 'First';
            """
        );

        jdbcTemplate.execute(
            """
                INSERT INTO post_tag (post_id, tag_id)
                SELECT
                    (SELECT id FROM posts WHERE title = 'First'),
                    (SELECT id FROM tags WHERE name = 'SCIENCE');
                """
        );
    }
}
