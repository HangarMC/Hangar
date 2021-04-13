package io.papermc.hangar.service.internal.discourse;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class DiscourseService {

    private final DiscourseApi discourseApi;

    public DiscourseService(DiscourseApi discourseApi) {
        this.discourseApi = discourseApi;
    }

    // TODO instead of calling api directly, these need to be jobs

    public void createPost(String poster, int topicId, String content) {
        discourseApi.createPost(poster, topicId, content);
    }

    public void createTopic(String poster, String title, String content, @Nullable Integer categoryId) {
        discourseApi.createTopic(poster, title, content, categoryId);
    }

    public void updateTopic(String poster, int topicId, @Nullable String title, @Nullable Integer categoryId) {
        discourseApi.updateTopic(poster, topicId, title, categoryId);
    }

    public void updatePost(String poster, int postId, String content) {
        discourseApi.updatePost(poster, postId, content);
    }

    public void deleteTopic(String poster, int topicId) {
       discourseApi.deleteTopic(poster, topicId);
    }
}
