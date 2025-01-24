package com.github.khalaimovda.data;

import com.github.khalaimovda.model.Comment;
import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import com.github.khalaimovda.repository.PostRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final PostRepository postRepository;

    @PostConstruct
    @Transactional
    public void init() {
        if (postRepository.count() == 0) {
            initializeData();
        }
    }

    private void initializeData() {
        for (int i = 1; i <= 6; i++) {
            Post post = new Post();
            post.setName("Post " + i);
            post.setText("This is the text of post #" + i);
            post.setImagePath(String.format("/path/to/image_%s.jpg", i));

            if (i == 1 || i == 3) {
                post.setTags(Set.of(Tag.POLITICS));
            } else if (i == 2) {
                post.setTags(Set.of(Tag.ART, Tag.RELIGION));
            } else {
                post.setTags(Set.of(Tag.SCIENCE));
            }

            post.setLikes(10 - i);

            for (int j = 1; j <= 3; j++) {
                Comment comment = new Comment();
                comment.setText(String.format("Comment #%s for post #%s", j, i));
                post.addComment(comment);
            }

            postRepository.save(post);
        }
        System.out.println("Database initialized with default data");
    }
}
