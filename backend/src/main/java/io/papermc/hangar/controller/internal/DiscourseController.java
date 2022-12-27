package io.papermc.hangar.controller.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.internal.job.PostDiscourseReplyJob;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.service.internal.JobService;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static io.papermc.hangar.security.annotations.visibility.VisibilityRequired.Type;

// @el(projectId: long)
@Unlocked
@Controller
@RateLimit(path = "discourse")
@RequestMapping(value = "/api/internal/discourse", produces = MediaType.APPLICATION_JSON_VALUE)
public class DiscourseController extends HangarComponent {

    private final JobService jobService;

    public DiscourseController(final JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/{projectId}/comment")
    @ResponseBody
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 30)
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#projectId}")
    public String createPost(@PathVariable final long projectId, @RequestBody final Map<String, String> content) {
        if (!this.config.discourse.enabled()) {
            throw new HangarApiException("Discourse is NOT enabled!");
        }
        this.jobService.save(new PostDiscourseReplyJob(projectId, this.getHangarPrincipal().getName(), content.get("content")));
        return "dum";
    }
}
