package me.minidigger.hangar.model.viewhelpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.minidigger.hangar.db.customtypes.JSONB;
import me.minidigger.hangar.db.model.ProjectVersionReviewsTable;
import me.minidigger.hangar.service.ReviewService;

import java.util.ArrayList;
import java.util.List;

public class VersionReview extends ProjectVersionReviewsTable {

    private final String userName;
    private final List<VersionReviewMessage> messages = new ArrayList<>();

    public VersionReview(String userName, ProjectVersionReviewsTable review) throws JsonProcessingException {
        super(review);
        this.userName = userName;
        ObjectMapper mapper = new ObjectMapper();
        if (this.getComment().getJson().hasNonNull("messages")) {
            for (JsonNode node : this.getComment().getJson().get("messages")) {
                this.messages.add(mapper.treeToValue(node, VersionReviewMessage.class));
            }
        }
    }

    public String getUserName() {
        return userName;
    }

    public List<VersionReviewMessage> getMessages() {
        return messages;
    }

    public void addMessage(VersionReviewMessage message, ReviewService reviewService) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode comment = new ObjectNode(JsonNodeFactory.instance);
        messages.add(message);
        comment.set("messages", mapper.valueToTree(messages));
        this.setComment(new JSONB(comment));
        reviewService.update(this);
    }
}
