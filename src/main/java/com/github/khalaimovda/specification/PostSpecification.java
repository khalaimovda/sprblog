package com.github.khalaimovda.specification;

import com.github.khalaimovda.model.Post;
import com.github.khalaimovda.model.Tag;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecification {

    public static Specification<Post> hasTag(Tag tag) {
        return (root, query, builder) -> builder.isMember(tag, root.get("tags"));
    }
}
